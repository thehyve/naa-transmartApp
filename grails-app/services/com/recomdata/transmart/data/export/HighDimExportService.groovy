package com.recomdata.transmart.data.export

import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.dataconstraints.DataConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.assayconstraints.PlatformConstraint
import org.transmartproject.db.dataquery.highdim.dataconstraints.PropertyDataConstraint
import org.transmartproject.db.dataquery.highdim.snp_lz.SnpSubjectSortedDef
import org.transmartproject.export.HighDimColumnExporter
import org.transmartproject.export.HighDimExporter
import org.transmartproject.export.HighDimTabularResultExporter

class HighDimExportService {

    def highDimensionResourceService
    def highDimExporterRegistry
    
    // FIXME: jobResultsService lives in Rmodules, so this is probably not a dependency we should have here
    def jobResultsService

    /**
     * Limit to the number of elements in a query parameter, used in
     * {@link #fetchSnpSubjectDataForAssays(Collection<Assay>)}.
     * Prevents an error like "ORA-01795: maximum number of expressions in a list is 1000"
     * The value should correspond to the database configuration and should probably
     * move to a configuration file.
     */
    static final int max_query_param_elements = 500

    /**
     * Fetches SNP subject data (from {@link SnpSubjectSortedDef}) for the subjects
     * in <code>assays</code>, using <code>patient.id</code> in the assay.
     * The task is performed in chunks of {@link #max_query_param_elements} subjects at a time,
     * to prevent overly long queries.
     * @param assays A collection of assays, containing patient data.
     * @return a map from patient id to subject data of type {@link SnpSubjectSortedDef}.
     */
    Map<Long, SnpSubjectSortedDef> fetchSnpSubjectDataForAssays(Collection<Assay> assays) {
        List<Long> patientIds = assays*.patient.id
        Map<Long, SnpSubjectSortedDef> subjectData = [:]
        if (!patientIds.empty) {
            def startTime = System.currentTimeMillis()
            log.debug "Fetching subject data..."
            int start = 0
            while (start < patientIds.size()) {
                int end = Math.min(start + max_query_param_elements, patientIds.size())
                List<Long> cell = patientIds.subList(start, end);
                log.debug "(size = ${patientIds.size()}, start = $start, end = $end)"
                def subjects = SnpSubjectSortedDef.where { patient.id in cell }.list()
                int count = subjects.size()
                log.debug "(count = $count)"
                subjects.each { SnpSubjectSortedDef subject -> subjectData[subject.patient.id] = subject }
                start += count
            }
            log.debug "Fetching subject data took ${System.currentTimeMillis() - startTime} ms."
        }
        assert subjectData.size() == patientIds.size()
        subjectData
    }

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

        // Setup class to export the data
        HighDimExporter exporter = highDimExporterRegistry.getExporterForFormat( format )

        File outputFile = new File(studyDir, dataType + '.' + format.toLowerCase() )
        String fileName = outputFile.getAbsolutePath()

        if (exporter instanceof HighDimColumnExporter) {
            exporter = exporter as HighDimColumnExporter
            def startTime = System.currentTimeMillis()
            log.debug "Fetching assays..."
            Map<HighDimensionDataTypeResource, Collection<Assay>> assayMap = highDimensionResourceService.getSubResourcesAssayMultiMap(assayconstraints)
            log.debug "Fetching assays took ${System.currentTimeMillis() - startTime} ms."
            // for some reason, this dataTypeResourceKey is not the same as dataTypeResource:
            def dataTypeResourceKey = assayMap.keySet().find { it.dataTypeName == dataTypeResource.dataTypeName }
            Collection<Assay> assays = assayMap[dataTypeResourceKey]
            Map subjectData = [:]
            // Fetch SNP subject data for SNP exporters
            if (exporter.isDataTypeSupported('snp_lz')) {
                subjectData = fetchSnpSubjectDataForAssays(assays)
            }
            // Start exporting column data
            outputFile.withOutputStream { outputStream ->
                exporter.export assays, subjectData, outputStream, { jobIsCancelled(jobName) }
            }
        } else {
            exporter = exporter as HighDimTabularResultExporter
            
            Projection projection = dataTypeResource.createProjection( exporter.projection )
            
            // Add dataconstraints
            def dataConstraints = []
            filters.each { Map filter ->
                if (filter.type == DataConstraint.CHROMOSOME_SEGMENT_CONSTRAINT) {
                    dataConstraints << dataTypeResource.createDataConstraint(filter.data, DataConstraint.CHROMOSOME_SEGMENT_CONSTRAINT)
                } else {
                    Map data = filter.data
                    data.each { k, v ->
                        dataConstraints << new PropertyDataConstraint(property: k, values: v)
                    }
                }
            }
    
            // Retrieve the tabular data
            TabularResult<AssayColumn, DataRow<Map<String, String>>> tabularResult =
                    dataTypeResource.retrieveData(assayconstraints, dataConstraints, projection)

            // Start exporting tabular data
            try {
                outputFile.withOutputStream { outputStream ->
                    exporter.export tabularResult, projection, outputStream, { jobIsCancelled(jobName) }
                }
            } finally {
                tabularResult.close()
            }
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
