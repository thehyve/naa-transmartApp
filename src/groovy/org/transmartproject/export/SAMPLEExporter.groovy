package org.transmartproject.export

import java.util.Map;

import javax.annotation.PostConstruct

import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.projections.Projection

/**
 * Export for Single Nucleotide Polymorphism (SNP) data.
 * Exports sample file.
 * @see {@link http://www.stats.ox.ac.uk/~marchini/software/gwas/file_format.html}
 * for a description of the file format.
 * 
 * @author gijs@thehyve.nl
 */
class SAMPLEExporter implements HighDimColumnExporter {

    class SampleRow {
        String id1
        String id2
    }
    
    @Autowired
    HighDimExporterRegistry highDimExporterRegistry
    
    @PostConstruct
    void init() {
        this.highDimExporterRegistry.registerHighDimensionExporter(
                format, this )
    }
    
    @Override
    public boolean isDataTypeSupported(String dataType) {
        log.debug "Checking support for datatype ${dataType}"
        dataType == "snp_lz"
    }

    @Override
    public String getFormat() {
        "SAMPLE"
    }

    @Override
    public String getDescription() {
        "Sample file (SAMPLE)"
    }

    @Override
    public Map<String, Object> getDisplayAttributes() {
        [selectOnFilterPriority: 100]
    }

    @Override
    public void export(Collection<Assay> assays,
            OutputStream outputStream) {
        export(assays, outputStream, { false })
    }

    @Override
    public void export(Collection<Assay> assays,
            OutputStream outputStream, Closure isCancelled) {
        log.info "Started exporting to ${format}..."
        def startTime = System.currentTimeMillis()

        if (isCancelled() ) {
            return
        }

        outputStream.withWriter( "UTF-8" ) { out ->
            exportHeader(out)
            
            for (Assay assay: assays) {
                if (isCancelled() ) {
                    return
                }

                def row = new SampleRow(
                    id1: assay.patientInTrialId, // should be equal to column subject_id in deapp.de_snp_subject_sorted_def
                    id2: assay.patient.id // should be equal to column patient_num in i2b2demodata.patient_dimension
                )
                exportSampleRow(row, out)
            }
        }
        
        log.info("Exporting took ${System.currentTimeMillis() - startTime} ms.")
    }

    protected void exportHeader(Writer out) {
        // First header line, specifying column names. 
        // The first three elements are mandatory.
        out << 'ID_1 ID_2 missing\n'

        // Second header line, specifying the column types.
        // 0 is used for the first three columns. The following codes can be used 
        // for additional columns:
        //   D = Discrete covariate (coded using positive integers) 
        //   C = Continuous covariates
        //   P = Continuous Phenotype
        //   B = Binary Phenotype (0 = Controls, 1 = Cases)
        out << '0 0 0\n' 
    }
            
    protected void exportSampleRow(SampleRow row, Writer out) {
        out << row.id1
        out << ' '
        out << row.id2
        out << ' '
        out << 0
        out << '\n'
    }

}
