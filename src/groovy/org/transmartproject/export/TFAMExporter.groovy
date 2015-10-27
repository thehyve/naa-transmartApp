package org.transmartproject.export

import java.util.Map;

import javax.annotation.PostConstruct

import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.snp_lz.SnpSubjectSortedDef

/**
 * Export for Single Nucleotide Polymorphism (SNP) data.
 * Exports individuals (TFAM) file.
 * @see {@link http://pngu.mgh.harvard.edu/~purcell/plink/data.shtml}
 * for a rather brief overview.
 * 
 * @author gijs@thehyve.nl
 */
class TFAMExporter implements HighDimColumnExporter {

    enum Sex {
        Male(1),
        Female(2),
        Unknown(0)
        
        int value
        
        Sex(value) {
            this.value = value
        }
    }
    
    enum Phenotype {
        Missing(-9),
        Unaffected(0),
        Affected(1)
        
        int value
        
        Phenotype(value) {
            this.value = value
        }
    }

    class TFAMRow {
        String familyId
        String individualId
        String paternalId   
        String maternalId
        Sex sex
        Phenotype phenotype
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
    public void export(Collection<Assay> assays, Map<Long, SnpSubjectSortedDef> subjectData,
            OutputStream outputStream) {
        export(assays, subjectData, outputStream, { false })
    }
            
    @Override
    public void export(Collection<Assay> assays, Map<Long, SnpSubjectSortedDef> subjectData,
            OutputStream outputStream, Closure isCancelled) {
        log.info "Started exporting to ${format}..."
        def startTime = System.currentTimeMillis()
        
        if (isCancelled() ) {
            return
        }
      
        outputStream.withWriter( "UTF-8" ) { out ->
            for (Assay assay: assays) {
                if (isCancelled() ) {
                    return
                }
                
                Patient patient = assay.getPatient()
                if (patient) {
                    SnpSubjectSortedDef subject = subjectData[patient.getId()]
                    def row = new TFAMRow(
                        familyId: subject.subjectId,
                        individualId: subject.subjectId,
                        maternalId: 0,
                        paternalId: 0,
                        sex: Sex.Unknown,
                        phenotype: Phenotype.Missing
                    )
                    exportTFAMRow(row, out)
                }
            }
        }
        
        log.info("Exporting took ${System.currentTimeMillis() - startTime} ms.")
    }

    protected void exportTFAMRow(TFAMRow row, Writer out) {
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
