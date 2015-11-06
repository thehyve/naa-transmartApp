package org.transmartproject.export

import java.util.Map;

import org.apache.commons.logging.LogFactory
import org.gmock.WithGMock
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.projections.Projection

/**
 * Tests for the SAMPLE exporter in {@link SAMPLEExporter}
 * 
 * Generates mock data for the data types provided by {@link SnpSubjectModule}
 * and tests if valid SAMPLE output is written, i.e., one line per subject.

 * The format has three mandatory columns for subject information:
 * <code>ID_1</code>, <code>ID_2</code> and <code>missing</code>.
 * The first two lines are header lines.
 * 
 * @author gijs@thehyve.nl
 */
@WithGMock
class SAMPLEExporterTests {

    private static final log = LogFactory.getLog(this)
    
    @Delegate
    SNPMockDataHelper snpMockDataHelper
    
    SAMPLEExporter exporter
    TabularResult tabularResult
    Map subjects
    Projection projection
    
    Map<String, Map> snpProperties
    Map<String, List> cellData
    
    @Before
    void before() {
        snpMockDataHelper = new SNPMockDataHelper()
        snpMockDataHelper.gMockController = $gmockController
        
        // Setup exporter
        exporter = new SAMPLEExporter()
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
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        
        play {
            List<AssayColumn> assays = tabularResult.getIndicesList()
            exporter.export( assays, outputStream )
            
            // Assert we have at least some text, in UTF-8 encoding
            String output = outputStream.toString("UTF-8")
            assert output
            
            List lines = output.readLines()
            
            assert lines.size() == 2 + subjectCount
            
            lines.eachWithIndex { String line, i ->
                if (i < 2) {
                    return // skip the two header lines
                } 
                log.debug "Line: " + line
                def values = line.tokenize()
                assert values.size() >= 3
                def id1 = values[0]
                def id2 = values[1]
                def assay = sampleAssays[i-2]
                // subjectId is used for the ID_1 field
                assert assay.patientInTrialId == id1
                // patientNum is used for the ID_2 field
                assert assay.patient.id == Integer.parseInt(id2)
            }
        }
    }
    
}
