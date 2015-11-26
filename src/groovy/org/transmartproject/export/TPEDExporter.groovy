package org.transmartproject.export

import javax.annotation.PostConstruct

import org.apache.commons.lang.NotImplementedException
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.AssayColumn;
import org.transmartproject.core.dataquery.highdim.projections.Projection
//import org.transmartproject.db.dataquery.highdim.snp_lz.SnpLzCell
//import org.transmartproject.db.dataquery.highdim.snp_lz.SnpLzRow

/**
 * Export for Single Nucleotide Polymorphism (SNP) data.
 * Exports transposed pedigree (TPED) file.
 * @see {@link http://pngu.mgh.harvard.edu/~purcell/plink/data.shtml}
 * for a rather brief overview.
 * 
 * The exported genotypes are the most likely alleles. I.e., 
 * for a SNP with A1 = 'C' and A2 = 'T', the probabilities of A1A1,
 * A1A2 and A2A2 are used to export 'C C', 'C T', or 'T T'.
 * If the probability triples only contains one 1 and two 0's, 
 * the export is precise.
 * 
 * @author gijs@thehyve.nl
 */
class TPEDExporter implements HighDimTabularResultExporter {

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

    /**
     * Uses the {@link org.transmartproject.db.dataquery.highdim.snp_lz.SnpLzAllelesProjection} projection.
     */
    @Override
    public String getProjection() {
        "alleles"
    }

    @Override
    public String getFormat() {
        "TPED"
    }

    @Override
    public String getDescription() {
        "Transposed pedigree file (TPED)"
    }

    @Override
    public Map<String, Object> getDisplayAttributes() {
        [selectOnFilterPriority: 200, group: 'TFAM_TPED']
    }

    @Override
    public void export(TabularResult /*<AssayColumn, SnpLzRow>*/ data, Projection projection,
            OutputStream outputStream) {
        export( data, projection, outputStream, { false } )
    }

    static final int default_distance = 0 // Genetic distance (morgans)
            
    @Override
    public void export(TabularResult /*<AssayColumn, SnpLzRow>*/ data, Projection projection,
            OutputStream outputStream, Closure isCancelled) {
        log.info "Started exporting to ${format}..."
        def startTime = System.currentTimeMillis()
        
        if (isCancelled() ) {
            return
        }
      
        def i = 1
        outputStream.withWriter( "UTF-8" ) { out ->
            for (/*SnpLzRow*/ Object row: data) {
                if (isCancelled() ) {
                    return
                }
                
                String chromosome = row.chromosome  // (1-22, X, Y or 0 if unplaced)
                String snpId = row.snpName          // rs# or snp identifier
                Integer position = row.position     // Base-pair position (bp units)
                
                out << chromosome
                out << ' '
                out << snpId        
                out << ' '
                out << default_distance
                out << ' '
                out << position
                
                for (String likelyGenotype: row) {
                    // likelyGenotype is of the format "${likelyAllele1}_${likelyAllele2}"
                    List<String> likelyAlleles = likelyGenotype?.split('_')
                    assert(likelyAlleles.size() == 2)
                    out << ' '
                    out << likelyAlleles[0]   // most likely value for allele1
                    out << ' '
                    out << likelyAlleles[1]   // most likely value for allele2
                }
                out << '\n'
                i++
            }
        }
        
        log.info("Exporting took ${System.currentTimeMillis() - startTime} ms.")
    }

}
