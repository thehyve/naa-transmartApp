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
 * Tests for the TFAM exporter in {@link TFAMExporter}
 * 
 * Generates mock data for the data types provided by {@link SnpSubjectModule}
 * and tests if valid TFAM output is written, i.e., one line per subject. 
 * The format has six columns for subject data. Only the first two are used:
 * <code>familyId</code> and <code>individualId</code>.
 * 
 * @author gijs@thehyve.nl
 */
@WithGMock
class TFAMExporterTests {

    private static final log = LogFactory.getLog(this)
    
    @Delegate
    SNPMockDataHelper snpMockDataHelper
    
    TFAMExporter exporter
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
        exporter = new TFAMExporter()
    }
    
    @Test
    void "test whether supported datatypes are recognized"() {
        // Only SNP subject datatype is supported
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
            
            lines.eachWithIndex { String line, i -> 
                log.debug "Line: " + line
                def values = line.tokenize()
                assert values.size() == 6
                def familyId = values[0]
                def individualId = values[1]
                def assay = sampleAssays[i]
                assert assay.patientInTrialId == familyId        // subjectId is used for the familyId field
                assert assay.patientInTrialId == individualId    // subjectId is used for the individualId field
            }
        }
    }
    
}
