package org.transmartproject.export

import java.util.Map;

import javax.annotation.PostConstruct

import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.dataquery.Sex
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.projections.Projection

/**
 * Export for Single Nucleotide Polymorphism (SNP) data.
 * Exports individuals (TFAM) file.
 * @see {@link http://pngu.mgh.harvard.edu/~purcell/plink/data.shtml}
 * for a rather brief overview.
 * 
 * @author gijs@thehyve.nl
 */
class TFAMExporter implements HighDimColumnExporter {

    enum TFAM_Sex {
        Male(1),
        Female(2),
        Unknown(0)

        int value

        TFAM_Sex(int value) {
            this.value = value
        }
    }

    static final TFAM_Sex toTFAM_Sex(Sex value) {
        switch(value) {
        case Sex.MALE:
            return TFAM_Sex.Male
        case Sex.FEMALE:
            return TFAM_Sex.Female
            break
        case Sex.UNKNOWN:
        default:
            return TFAM_Sex.Unknown
        }
    }

    enum TFAM_Phenotype {
        Missing(-9),
        Unaffected(0),
        Affected(1)
        
        int value
        
        TFAM_Phenotype(value) {
            this.value = value
        }
    }

    class TFAM_Row {
        String familyId
        String individualId
        String paternalId   
        String maternalId
        TFAM_Sex sex
        TFAM_Phenotype phenotype
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
        "TFAM"
    }

    @Override
    public String getDescription() {
        "Transposed individuals file (TFAM)"
    }

    @Override
    public Map<String, Object> getDisplayAttributes() {
        [selectOnFilterPriority: 200, group: 'TFAM_TPED']
    }

    @Override
    public Map<String, Object> export(Collection<Assay> assays,
            OutputStream outputStream) {
        export(assays, outputStream, { false })
    }

    @Override
    public Map<String, Object> export(Collection<Assay> assays,
            OutputStream outputStream, Closure isCancelled) {
        log.info "Started exporting to ${format}..."
        def startTime = System.currentTimeMillis()
        
        if (isCancelled() ) {
            return
        }

        long i = 0

        outputStream.withWriter( "UTF-8" ) { out ->
            for (Assay assay: assays) {
                if (isCancelled() ) {
                    return
                }
                
                def row = new TFAM_Row(
                    familyId: assay.patientInTrialId, // should be equal to subjectId in SnpSubjectSortedDef
                    individualId: assay.patientInTrialId,
                    maternalId: 0,
                    paternalId: 0,
                    sex: toTFAM_Sex(assay.patient.sex),
                    phenotype: TFAM_Phenotype.Missing
                )
                exportTFAMRow(row, out)
                i++
            }
        }
        
        log.info("Exporting took ${System.currentTimeMillis() - startTime} ms.")
        [rowsWritten: i]
    }

    protected void exportTFAMRow(TFAM_Row row, Writer out) {
        out << row.familyId
        out << ' '
        out << row.individualId
        out << ' '
        out << row.paternalId
        out << ' '
        out << row.maternalId
        out << ' '
        out << row.sex.value
        out << ' '
        out << row.phenotype.value
        out << '\n'
    }

}
