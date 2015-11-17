package org.transmartproject

import grails.transaction.Transactional

import javax.annotation.PostConstruct

import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection

import com.recomdata.transmart.data.export.HighDimExportService

@Transactional
class HighDimRowDataService {

    HighDimensionResource highDimensionResourceService

    /**
     * Retrieves the highdim data for the given conceptKey/dataType/projectionName
     * and returns row data.
     *
     * @param conceptKey key of the concept to retrieve highdim for
     * @param dataType highdim data type
     */
    List getRowData(String conceptKey,
                String dataType,
                Map assayConstraintsSpec,
                List filters) {

        log.debug "getRowData: conceptKey = ${conceptKey}, dataType = ${dataType}."

        HighDimensionDataTypeResource typeResource =
                highDimensionResourceService.getSubResourceForType(dataType)

        Projection projection = typeResource.createProjection(Projection.ALL_DATA_PROJECTION)

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

        List rowData = []
        try {
            for (Object row: tabularResult) {
                rowData << row
            }
        } finally {
            tabularResult.close() //closing the tabular result, no matter what
        }

        rowData
    }

    /**
     * Retrieves the highdim data for the given conceptKey/dataType/projectionName
     * and returns row data.
     *
     * @param conceptKey key of the concept to retrieve highdim for
     * @param dataType highdim data type
     */
    Map getDataByPatient(String conceptKey,
                String dataType,
                Map assayConstraintsSpec,
                List filters) {

        log.debug "getRowData: conceptKey = ${conceptKey}, dataType = ${dataType}."

        HighDimensionDataTypeResource typeResource =
                highDimensionResourceService.getSubResourceForType(dataType)

        Projection projection = typeResource.createProjection("alleles")

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
        Map rowData = subjects.collectEntries { [(it): []] }
        try {
            for (Object row: tabularResult) {
                colnames.add(row.label)
                int i = 0
                for (Object cell: row) {
                    rowData[subjects[i]] << cell
                    i++
                }
            }
        } finally {
            tabularResult.close() //closing the tabular result, no matter what
        }

        [
            colnames: colnames,
            subjects: subjects,
            rowData: rowData
        ]
    }

}
