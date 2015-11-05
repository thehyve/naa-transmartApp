package com.recomdata.transmart.data.export

import grails.orm.HibernateCriteriaBuilder

import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.dataconstraints.DataConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.assayconstraints.PlatformConstraint
import org.transmartproject.db.dataquery.highdim.dataconstraints.CriteriaDataConstraint
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

    /**
     * Create data constraints based on a list of filter definitions,
     * e.g., the following filters can be used for filtering data of type <code>snp_lz</code>:
     * <code>
     * [type: 'snps', names: ['rs12890222']],
     * [type: 'chromosome_segment', chromosome: 'X', start: 4000000, end: 5000000],
     * [type: 'genes', names: ['TPTEP1', 'LOC63930']]
     * </code>
     * Look at the dataquery.highdim data resource modules for available data constraints.
     * @param dataTypeResource the data type resource for which data constraints are created.
     * @param filters a list of filter definitions. Each of the definitions should have a field
     *        <code>type</code>, that should match one of the constraint types supported by the
     *        data type resource (e.g., <code>chromosome_segment</code> is the constraint type
     *        name of {@link ChromosomeSegmentConstraintFactory}, available in {@link SnpLzModule},
     *        the data type resource for the <code>snp_lz</code> data type.
     *        The other data fields of the filter are the fields that are required by the constraint
     *        factory, e.g., <code>names</code> for the <code>genes</code> filter type. 
     */
    def List<DataConstraint> createDataConstraints(HighDimensionDataTypeResource dataTypeResource, List<Map> filters) {
        def dataConstraints = []
        filters.each { Map filter ->
            log.info "creating filter of type ${filter.type}..."
            def type = filter.type
            def data = filter.findAll { it.key != 'type' && it.key != 'id' }
            log.info "  data: ${data}"
            def constraint = dataTypeResource.createDataConstraint(data, type)
            log.info "  constraint: ${constraint}"
            dataConstraints << constraint
        }
        dataConstraints
    }

    def exportHighDimData(Map args) {
        String jobName =                    args.jobName
        String dataType =                   args.dataType
        def resultInstanceId =              args.resultInstanceId
        Collection<String> gplIds =         args.gplIds
        Collection<String> conceptPaths =   args.conceptPaths
        String studyDir =                   args.studyDir
        String format =                     args.format
        List<Map> filters =                 args.filters // See {@link #createDataConstraints(HighDimensionDataTypeResource, List<Map>)} for a description.
        
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
            
            List<DataConstraint> dataConstraints = createDataConstraints(filters, dataTypeResource)
    
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
