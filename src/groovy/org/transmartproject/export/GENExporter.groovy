package org.transmartproject.export

import javax.annotation.PostConstruct

import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.AssayColumn;
import org.transmartproject.core.dataquery.highdim.projections.Projection
//import org.transmartproject.db.dataquery.highdim.snp_lz.SnpLzCell
//import org.transmartproject.db.dataquery.highdim.snp_lz.SnpLzRow

/**
 * Export for Single Nucleotide Polymorphism (SNP) data.
 * Exports a genotype (GEN) file.
 * @see {@link http://www.stats.ox.ac.uk/~marchini/software/gwas/file_format.html}
 * for a description of the file format.
 *  
 * @author gijs@thehyve.nl
 */
class GENExporter implements HighDimTabularResultExporter {

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
    public String getProjection() {
        Projection.ALL_DATA_PROJECTION
    }

    @Override
    public String getFormat() {
        "GEN"
    }

    @Override
    public String getDescription() {
        "Genotype file (GEN)"
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
                
                String chromosome = row.chromosome // the chromosome number is used as id
                String rsId = row.snpName          // rs# or snp identifier
                Integer position = row.position    // Base-pair position (bp units)
                
                out << chromosome
                out << ' '
                out << rsId        
                out << ' '
                out << position
                out << ' '
                out << row.a1
                out << ' '
                out << row.a2
                
                for (/*SnpLzCell*/ Object cell: row) { 
                    out << ' '
                    out << cell.probabilityA1A1
                    out << ' '
                    out << cell.probabilityA1A2
                    out << ' '
                    out << cell.probabilityA2A2
                }
                out << '\n'
                i++
            }
        }
        
        log.info("Exporting took ${System.currentTimeMillis() - startTime} ms.")
    }

}
