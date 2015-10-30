package org.transmartproject.export

import org.apache.commons.logging.LogFactory
import org.gmock.WithGMock
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.projections.Projection

/**
 * Tests for the TPED exporter in {@link TPEDExporter}
 * 
 * Generates mock data for the data types provided by {@link SnpLzModule}
 * and tests if valid TPED output is written, i.e., one line per 
 * Single Nucleotide Polymorphism (SNP). 
 * The format has four columns for information about the SNP, and 
 * per subject two columns for alleles, stored as <code>likelyAllele1</code> 
 * and <code>likelyAllele2</code> respectively in {@link SnpLzCell}.
 * 
 * @author gijs@thehyve.nl
 */
@WithGMock
class TPEDExporterTests {

    private static final log = LogFactory.getLog(this)
    
    @Delegate
    SNPMockDataHelper snpMockDataHelper
    
    TPEDExporter exporter
    TabularResult tabularResult
    Projection projection
    
    Map<String, Map> snpProperties
    Map<String, List> cellData
    
    @Before
    void before() {
        snpMockDataHelper = new SNPMockDataHelper()
        snpMockDataHelper.gMockController = $gmockController
        
        // Setup exporter
        exporter = new TPEDExporter()
    }
    
    @Test
    void "test whether supported datatypes are recognized"() {
        // Only SNP datatype is supported
        assert exporter.isDataTypeSupported( "snp_lz" )
        
        assert !exporter.isDataTypeSupported( "other" )
        assert !exporter.isDataTypeSupported( null )
    }
    
    @Test
    void "test whether a basic tabular result is exported properly"() {
        tabularResult = createMockSnpLzTabularResult()
        
        // Create cohort projection, as that is used for exporting.
        def projection = mock(Projection)
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        
        play {
            exporter.export( tabularResult, projection, outputStream )
            
            // Assert we have at least some text, in UTF-8 encoding
            String output = outputStream.toString("UTF-8")
            assert output
            
            List lines = output.readLines()
            
            lines.each { String line -> 
                log.debug "Line: " + line
                def values = line.tokenize()
                assert values.size() == 4 + 2 * subjectCount // four columns for SNP info + two alleles per subject
                def chromosome = values[0]
                def snpId = values[1]
                def position = values[3]
                def snpKey = snpProperties.find { it.value.snpName == snpId }.key
                cellData[snpKey].eachWithIndex { cell, i ->
                    assert cell.likelyAllele1 == values[4 + i*2]
                    assert cell.likelyAllele2 == values[4 + i*2 + 1]
                }
            }
        }
    }
    
}
