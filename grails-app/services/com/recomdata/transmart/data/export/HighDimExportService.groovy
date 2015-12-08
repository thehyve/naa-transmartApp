package com.recomdata.transmart.data.export

import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.dataconstraints.DataConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.assayconstraints.PlatformConstraint
import org.transmartproject.db.dataquery.highdim.dataconstraints.PropertyDataConstraint
import org.transmartproject.export.HighDimColumnExporter
import org.transmartproject.export.HighDimExporter
import org.transmartproject.export.HighDimTabularResultExporter

class HighDimExportService {

    def highDimensionResourceService
    def highDimExporterRegistry
    
    // FIXME: jobResultsService lives in Rmodules, so this is probably not a dependency we should have here
    def jobResultsService

    /**
     * Create data constraints based on a list of filter definitions,
     * e.g., the following filters can be used for filtering data of type <code>snp_lz</code>:
     * <code>
     * [type: 'snps', names: ['rs12890222']],
     * [type: 'chromosome_segment', chromosome: 'X', start: 4000000, end: 5000000],
     * [type: 'genes', names: ['TPTEP1', 'LOC63930']]
     * </code>
     * Look at the dataquery.highdim data resource modules for available data constraints.
     * The filters are combined disjunctively, i.e., the resulting constraint matches if
     * any of the provided filters matches.
     * @param dataTypeResource the data type resource for which data constraints are created.
     * @param filters a list of filter definitions. Each of the definitions should have a field
     *        <code>type</code>, that should match one of the constraint types supported by the
     *        data type resource (e.g., <code>chromosome_segment</code> is the constraint type
     *        name of {@link ChromosomeSegmentConstraintFactory}, available in {@link SnpLzModule},
     *        the data type resource for the <code>snp_lz</code> data type.
     *        The other data fields of the filter are the fields that are required by the constraint
     *        factory, e.g., <code>names</code> for the <code>genes</code> filter type. 
     */
    static DataConstraint createFilterConstraints(HighDimensionDataTypeResource dataTypeResource, List<Map> filters) {
        def dataConstraints = [:].withDefault { _ -> []}
        filters.each { Map filter ->
            log.debug "creating filter of type ${filter.type}..."
            def type = filter.type
            def data = filter.findAll { it.key != 'type' && it.key != 'id' }
            log.debug "  data: ${data}"
            dataConstraints[type] << data
        }
        DataConstraint disjunction = dataTypeResource.createDataConstraint(
                [subconstraints: dataConstraints],
                DataConstraint.DISJUNCTION_CONSTRAINT)
        disjunction
    }

    static List knownKeys = ['names', 'chromosome', 'start', 'end']
    /**
     * Sorts a collection of keys such that:
     * - keys in {@link #knownKeys} appear before other keys;
     * - keys in {@link #knownKeys} are ordered according to their order in {@link #knownKeys};
     * - other keys are ordered according to their natural ordering.
     * @param k a collection of keys.
     * @return the list containing all keys in <var>k</var>, ordered according to 
     * the ordering described above.
     */
    static List sortKeys(Collection k) {
        List keys = new ArrayList(k);
        keys.sort { a, b ->
            if (a in knownKeys && b in knownKeys) {
                return knownKeys.indexOf(a) <=> knownKeys.indexOf(b)
            } else if (a in knownKeys) {
                return -1
            } else if (b in knownKeys) {
                return 1
            } else {
                return a <=> b
            }
        }
        keys
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
        String study =                      args.study

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

        // serialise filter definition to be used in the generated filename
        String serialisedFilterDescription = filters.collect({ filter ->
            List keys = sortKeys(filter.keySet().findAll { it != 'type' && it != 'id' })
            return keys.collect({ filter[it] }).join('_')
        }).join('_')
        String name = "${study}_${dataType}_${serialisedFilterDescription}_${jobName}.${format.toLowerCase()}"
        File outputFile = new File(studyDir, name)
        String fileName = outputFile.getAbsolutePath()
        Map result

        if (exporter instanceof HighDimColumnExporter) {
            exporter = exporter as HighDimColumnExporter
            def startTime = System.currentTimeMillis()
            log.debug "Fetching assays..."
            Map<HighDimensionDataTypeResource, Collection<Assay>> assayMap = highDimensionResourceService.getSubResourcesAssayMultiMap(assayconstraints)
            log.debug "Fetching assays took ${System.currentTimeMillis() - startTime} ms."
            // for some reason, this dataTypeResourceKey is not the same as dataTypeResource:
            def dataTypeResourceKey = assayMap.keySet().find { it.dataTypeName == dataTypeResource.dataTypeName }
            Collection<Assay> assays = assayMap[dataTypeResourceKey]
            // Start exporting column data
            outputFile.withOutputStream { outputStream ->
                result = exporter.export assays, outputStream, { jobIsCancelled(jobName) }
            }
        } else {
            exporter = exporter as HighDimTabularResultExporter
            
            Projection projection = dataTypeResource.createProjection( exporter.projection )
            
            //DataConstraint filterConstraints = createFilterConstraints(filters, dataTypeResource)
            List<DataConstraint> dataConstraints = [createFilterConstraints(dataTypeResource, filters)]

            // Add study constraint for SNP data
            if (dataTypeResource.dataTypeName == 'snp_lz') {
                log.debug "Add constraint for trailName '${study}'"
                dataConstraints << dataTypeResource.createDataConstraint(
                    [trialName: study], 'trialName')
            }

            // Retrieve the tabular data
            TabularResult<AssayColumn, DataRow<Map<String, String>>> tabularResult =
                    dataTypeResource.retrieveData(assayconstraints, dataConstraints, projection)

            // Start exporting tabular data
            try {
                outputFile.withOutputStream { outputStream ->
                    result = exporter.export tabularResult, projection, outputStream, { jobIsCancelled(jobName) }
                }
            } finally {
                tabularResult.close()
            }
        }

        long rowsWritten = result?.rowsWritten ?: 0
        log.info "HighDimExportService: ${rowsWritten} rows written."
        if (!("RowsWritten" in jobResultsService[jobName])) {
            jobResultsService[jobName]["RowsWritten"] = [:]
        }
        jobResultsService[jobName]["RowsWritten"][name] = rowsWritten
        return [outFile: fileName, rowsWritten: rowsWritten]
    }

    def boolean jobIsCancelled(jobName) {
        if (jobResultsService[jobName]["Status"] == "Cancelled") {
            log.warn("${jobName} has been cancelled")
            return true
        }
        return false
    }
}
