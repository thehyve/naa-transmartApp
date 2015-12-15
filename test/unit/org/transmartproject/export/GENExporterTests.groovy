package org.transmartproject.export

import groovy.lang.Delegate;

import org.apache.commons.logging.LogFactory
import org.gmock.WithGMock
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.projections.Projection

/**
 * Tests for the GEN exporter in {@link GENExporter}
 * 
 * Generates mock data for the data types provided by {@link SnpLzModule}
 * and tests if valid GEN output is written, i.e., one line per 
 * Single Nucleotide Polymorphism (SNP). 
 * The format has five columns for information about the SNP, and 
 * per subject three probabilities: <code>probabilityA1A1</code>, 
 * <code>probabilityA1A2</code> and <code>probabilityA1A1</code> respectively in {@link SnpLzCell}.
 * 
 * @author gijs@thehyve.nl
 */
@WithGMock
class GENExporterTests {

    private static final log = LogFactory.getLog(this)
    
    @Delegate
    SNPMockDataHelper snpMockDataHelper
    
    GENExporter exporter
    TabularResult tabularResult
    Projection projection
    
    @Before
    void before() {
        snpMockDataHelper = new SNPMockDataHelper()
        snpMockDataHelper.gMockController = $gmockController
        
        // Setup exporter
        exporter = new GENExporter()
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
        tabularResult = createMockSnpLzTabularResult(exporter.getProjection())
        
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
                assert values.size() == 5 + 3 * subjectCount // five columns for SNP info + three probabilities per subject
                def chromosome = values[0]
                def snpId = values[1]
                def position = values[2]
                def a1 = values[3]
                def a2 = values[4]
                def snpKey = snpMockDataHelper.snpProperties.find { it.value.snpName == snpId }.key
                snpMockDataHelper.cellData[snpKey].eachWithIndex { cell, i ->
                    assert cell.probabilityA1A1 == Double.parseDouble(values[5 + i*3])
                    assert cell.probabilityA1A2 == Double.parseDouble(values[5 + i*3 + 1])
                    assert cell.probabilityA2A2 == Double.parseDouble(values[5 + i*3 + 2])
                }
            }
        }
    }

}
