package org.transmartproject

import grails.transaction.Transactional

import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.dataconstraints.DataConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.exceptions.EmptySetException
import org.transmartproject.core.exceptions.UnexpectedResultException
import org.transmartproject.db.dataquery.highdim.snp_lz.SnpLzRow

import com.recomdata.transmart.data.export.HighDimExportService

@Transactional
class HighDimChartDataService {

    HighDimensionResource highDimensionResourceService

    Set<String> getSupportedDataConstraints(String dataType) {
        HighDimensionDataTypeResource typeResource =
            highDimensionResourceService.getSubResourceForType(dataType)
        typeResource.supportedDataConstraints
    }

    Closure<Map> getBarChartTransformerForDatatype(HighDimensionDataTypeResource typeResource) {
        switch(typeResource.dataTypeName) {
            case "snp_lz":
                return { Object rowData ->
                    // Count number of cells with a certain value:
                    // the result will be a map from values to the count.
                    SnpLzRow row = rowData as SnpLzRow
                    Map result = [:]
                    for (String cell: row) {
                        if (!(cell in result)) {
                            result[cell] = 1
                        } else {
                            result[cell]++
                        }
                    }
                    return result
                }
            default:
                log.warn "No transformer found for datatype ${typeResource.dataTypeName}!"
                return null // FIXME
        }
    }

    Map getBarChartMetadataForDatatype(HighDimensionDataTypeResource typeResource, Object row) {
        switch(typeResource.dataTypeName) {
            case "snp_lz":
                return [
                    title: row.label,
                    labels: ["${row.a1}_${row.a1}", "${row.a1}_${row.a2}", "${row.a2}_${row.a2}"]
                ]
            default:
                log.warn "No metadata found for datatype ${typeResource.dataTypeName}!"
                return null // FIXME
        }
    }

    Projection getProjectionForDatatype(HighDimensionDataTypeResource typeResource) {
        switch(typeResource.dataTypeName) {
            case "snp_lz":
                return typeResource.createProjection("alleles")
            default:
                log.warn "No default projection found for datatype ${typeResource.dataTypeName}!"
                return null // FIXME
        }
    }

    String getTrialNameFromAssays(Collection<Assay> assays) {
        def trials = [] as Set
        assays.each { trials << it.trialName }
        log.debug "Set of trials for assays: $trials"

        if (trials.size() == 0) {
            throw new EmptySetException(
                    "Assay set $trials has no trial information")
        } else if (trials.size() > 1) {
            // Do not permit more than one trial, otherwise we wouldn't be able
            // to returns meaningful row properties (or we'd have to make them
            // maps)
            throw new UnexpectedResultException("Found more than one trial in " +
                    "the assay set; this is not allowed for this data type")
        }
        return trials[0]
    }

    /**
     * Retrieves the highdim data for the given dataType, assay constraints and filters
     * and returns data for generating a bar chart.
     *
     * @param dataType highdim data type
     */
    Map getBarChartData(
                String dataType,
                Map assayConstraintsSpec,
                List filters) {

        log.debug "getBarChartData: dataType = ${dataType}."

        HighDimensionDataTypeResource typeResource =
                highDimensionResourceService.getSubResourceForType(dataType)

        Projection projection = getProjectionForDatatype(typeResource)
        Map metadata = [:]
        Closure<Map> transformRow = getBarChartTransformerForDatatype(typeResource)

        List<AssayConstraint> assayConstraints =
                assayConstraintsSpec.collect { String type, List instances ->
                    instances.collect { Map params ->
                        typeResource.createAssayConstraint(params, type)
                    }
                }.flatten()

        List<DataConstraint> dataConstraints = [HighDimExportService.createFilterConstraints(typeResource, filters)]

        log.debug "Fetching assays..."
        Map<HighDimensionDataTypeResource, Collection<Assay>> assayMap = highDimensionResourceService.getSubResourcesAssayMultiMap(assayConstraints)
        // for some reason, this dataTypeResourceKey is not the same as dataTypeResource:
        def dataTypeResourceKey = assayMap.keySet().find { it.dataTypeName == typeResource.dataTypeName }
        Collection<Assay> assays = assayMap[dataTypeResourceKey]

        // Add trialName constraint for SNP data
        if (typeResource.dataTypeName == 'snp_lz') {
            def trialName = getTrialNameFromAssays(assays)
            log.debug "Add constraint for trailName '${trialName}'"
            dataConstraints << typeResource.createDataConstraint(
                [trialName: trialName], 'trialName')
        }

        TabularResult tabularResult = typeResource.retrieveData(
                assayConstraints, dataConstraints, projection)

        List<Map> barChartData = []
        Set<String> labels = []
        try {
            for (Object row: tabularResult) {
                if (!metadata) {
                    metadata = getBarChartMetadataForDatatype(typeResource, row)
                }
                Map rowData = transformRow(row)
                labels.addAll(rowData.keySet())
                barChartData.add(rowData)
            }
        } finally {
            tabularResult.close() //closing the tabular result, no matter what
        }

        [
            title: metadata.title,
            labels: labels,
            data: barChartData
        ]
    }

    /**
     * Retrieves the highdim data for the given dataType, assay constraints and filters
     * and returns data for adding a highdim data column to the grid view.
     *
     * @param dataType highdim data type
     */
    Map getTableDataByPatient(
                String dataType,
                Map assayConstraintsSpec,
                List filters) {

        log.debug "getTableDataByPatient: dataType = ${dataType}."

        HighDimensionDataTypeResource typeResource =
                highDimensionResourceService.getSubResourceForType(dataType)

        Projection projection = getProjectionForDatatype(typeResource)

        List<AssayConstraint> assayConstraints =
                assayConstraintsSpec.collect { String type, List instances ->
                    instances.collect { Map params ->
                        typeResource.createAssayConstraint(params, type)
                    }
                }.flatten()

        List<DataConstraint> dataConstraints = [HighDimExportService.createFilterConstraints(typeResource, filters)]

        log.debug "Fetching assays..."
        Map<HighDimensionDataTypeResource, Collection<Assay>> assayMap = highDimensionResourceService.getSubResourcesAssayMultiMap(assayConstraints)
        // for some reason, this dataTypeResourceKey is not the same as dataTypeResource:
        def dataTypeResourceKey = assayMap.keySet().find { it.dataTypeName == typeResource.dataTypeName }
        Collection<Assay> assays = assayMap[dataTypeResourceKey]

        // Add trialName constraint for SNP data
        if (typeResource.dataTypeName == 'snp_lz') {
            def trialName = getTrialNameFromAssays(assays)
            log.debug "Add constraint for trailName '${trialName}'"
            dataConstraints << typeResource.createDataConstraint(
                [trialName: trialName], 'trialName')
        }

        TabularResult tabularResult = typeResource.retrieveData(
                assayConstraints, dataConstraints, projection)

        List colnames = []
        List subjects = assays*.patient.id // patientInTrialId
        Map tableData = subjects.collectEntries { [(it): []] }
        try {
            for (Object row: tabularResult) {
                colnames.add(row.label)
                int i = 0
                for (Object cell: row) {
                    tableData[subjects[i]] << cell
                    i++
                }
            }
        } finally {
            tabularResult.close() //closing the tabular result, no matter what
        }

        [
            colnames: colnames,
            subjects: subjects,
            tableData: tableData
        ]
    }

}
