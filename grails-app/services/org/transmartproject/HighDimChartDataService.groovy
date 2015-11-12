package org.transmartproject

import grails.transaction.Transactional

import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.snp_lz.SnpLzRow

import com.recomdata.transmart.data.export.HighDimExportService

@Transactional
class HighDimChartDataService {

    HighDimensionResource highDimensionResourceService

    Closure<Map> getBarChartTransformerForDatatype(HighDimensionDataTypeResource typeResource) {
        switch(typeResource.dataTypeName) {
            case "snp_lz":
                return { Object rowData ->
                    SnpLzRow row = rowData as SnpLzRow
                    def result = 
                        [("${row.a1}_${row.a1}"): row.a1a1Count,
                         ("${row.a1}_${row.a2}"): row.a1a2Count,
                         ("${row.a2}_${row.a2}"): row.a2a2Count]
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

    /**
     * Retrieves the highdim data for the given conceptKey/dataType/projectionName
     * and returns row data.
     *
     * @param conceptKey key of the concept to retrieve highdim for
     * @param dataType highdim data type
     */
    Map getBarChartData(String conceptKey,
                String dataType,
                Map assayConstraintsSpec,
                List filters) {

        log.debug "getBarChartData: conceptKey = ${conceptKey}, dataType = ${dataType}."

        HighDimensionDataTypeResource typeResource =
                highDimensionResourceService.getSubResourceForType(dataType)

        Projection projection = typeResource.createProjection(Projection.ALL_DATA_PROJECTION)
        Map metadata = [:]
        Closure<Map> transformRow = getBarChartTransformerForDatatype(typeResource)

        List<AssayConstraint> assayConstraints = /* [
                typeResource.createAssayConstraint(
                        AssayConstraint.ONTOLOGY_TERM_CONSTRAINT,
                        concept_key: conceptKey)] + */
                assayConstraintsSpec.collect { String type, List instances ->
                    instances.collect { Map params ->
                        typeResource.createAssayConstraint(params, type)
                    }
                }.flatten()

        def dataConstraints = HighDimExportService.createFilterConstraints(typeResource, filters)

        TabularResult tabularResult = typeResource.retrieveData(
                assayConstraints, [dataConstraints], projection)

        List<Map> barChartData = []
        try {
            for (Object row: tabularResult) {
                if (!metadata) {
                    metadata = getBarChartMetadataForDatatype(typeResource, row)
                }
                barChartData.add(transformRow(row))
            }
        } finally {
            tabularResult.close() //closing the tabular result, no matter what
        }

        [
            title: metadata.title,
            labels: metadata.labels,
            data: barChartData
        ]
    }

    Projection getDefaultTableDataProjectionForDatatype(HighDimensionDataTypeResource typeResource) {
        switch(typeResource.dataTypeName) {
            case "snp_lz":
                return typeResource.createProjection("alleles")
            default:
                log.warn "No default projection found for datatype ${typeResource.dataTypeName}!"
                return null // FIXME
        }
    }

    /**
     * Retrieves the highdim data for the given conceptKey/dataType/projectionName
     * and returns row data.
     *
     * @param conceptKey key of the concept to retrieve highdim for
     * @param dataType highdim data type
     */
    Map getTableDataByPatient(String conceptKey,
                String dataType,
                Map assayConstraintsSpec,
                List filters) {

        log.debug "getTableDataByPatient: conceptKey = ${conceptKey}, dataType = ${dataType}."

        HighDimensionDataTypeResource typeResource =
                highDimensionResourceService.getSubResourceForType(dataType)

        Projection projection = getDefaultTableDataProjectionForDatatype(typeResource)

        List<AssayConstraint> assayConstraints = /* [
                typeResource.createAssayConstraint(
                        AssayConstraint.ONTOLOGY_TERM_CONSTRAINT,
                        concept_key: conceptKey)] + */
                assayConstraintsSpec.collect { String type, List instances ->
                    instances.collect { Map params ->
                        typeResource.createAssayConstraint(params, type)
                    }
                }.flatten()

        def dataConstraints = HighDimExportService.createFilterConstraints(typeResource, filters)

        log.debug "Fetching assays..."
        Map<HighDimensionDataTypeResource, Collection<Assay>> assayMap = highDimensionResourceService.getSubResourcesAssayMultiMap(assayConstraints)
        // for some reason, this dataTypeResourceKey is not the same as dataTypeResource:
        def dataTypeResourceKey = assayMap.keySet().find { it.dataTypeName == typeResource.dataTypeName }
        Collection<Assay> assays = assayMap[dataTypeResourceKey]

        TabularResult tabularResult = typeResource.retrieveData(
                assayConstraints, [dataConstraints], projection)

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
