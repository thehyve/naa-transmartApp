package com.recomdata.transmart.data.export

import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.dataconstraints.DataConstraint;
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.assayconstraints.PlatformConstraint
import org.transmartproject.db.dataquery.highdim.dataconstraints.PropertyDataConstraint
import org.transmartproject.export.HighDimExporter

class HighDimExportService {

    def highDimensionResourceService
    def highDimExporterRegistry
    
    // FIXME: jobResultsService lives in Rmodules, so this is probably not a dependency we should have here
    def jobResultsService

    def exportHighDimData(Map args) {
        String jobName =                    args.jobName
        String dataType =                   args.dataType
        def resultInstanceId =              args.resultInstanceId
        Collection<String> gplIds =         args.gplIds
        Collection<String> conceptPaths =   args.conceptPaths
        String studyDir =                   args.studyDir
        String format =                     args.format
        List<Map> filters =                 args.filters // See {@link DataExportController#getgetExportFilters()} for a description.
        
        log.debug 'ExportHighDimData args = ' + args

        if (jobIsCancelled(jobName)) {
            return null
        }

        HighDimensionDataTypeResource dataTypeResource = highDimensionResourceService.getSubResourceForType(dataType)

        // Add constraints to filter the output
        def assayconstraints = []

        assayconstraints << dataTypeResource.createAssayConstraint(
                AssayConstraint.PATIENT_SET_CONSTRAINT,
                result_instance_id: resultInstanceId)

        assayconstraints << new PlatformConstraint(gplIds: gplIds)

        assayconstraints << dataTypeResource.createAssayConstraint(
                AssayConstraint.DISJUNCTION_CONSTRAINT,
                subconstraints:
                    [(AssayConstraint.ONTOLOGY_TERM_CONSTRAINT): conceptPaths.collect {[concept_key: it]}])

        // Add dataconstraints
        def dataConstraints = []
        filters.eachWithIndex { Map filter, i ->
            dataConstraints << dataTypeResource.createDataConstraint(filter, "filter_${i}")
        }

        // Setup class to export the data
        HighDimExporter exporter = highDimExporterRegistry.getExporterForFormat( format )
        Projection projection = dataTypeResource.createProjection( exporter.projection )

        File outputFile = new File(studyDir, dataType + '.' + format.toLowerCase() )
        String fileName = outputFile.getAbsolutePath()

        // Retrieve the data itself
        TabularResult<AssayColumn, DataRow<Map<String, String>>> tabularResult =
                dataTypeResource.retrieveData(assayconstraints, dataConstraints, projection)

        // Start exporting
        try {
            outputFile.withOutputStream { outputStream ->
                exporter.export tabularResult, projection, outputStream, { jobIsCancelled(jobName) }
            }
        } finally {
            tabularResult.close()
        }

        return [outFile: fileName]
    }

    def boolean jobIsCancelled(jobName) {
        if (jobResultsService[jobName]["Status"] == "Cancelled") {
            log.warn("${jobName} has been cancelled")
            return true
        }
        return false
    }
}
