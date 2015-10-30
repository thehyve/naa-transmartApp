package org.transmartproject.export

import grails.test.mixin.*

import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.db.dataquery.highdim.snp_lz.SnpLzCell
import org.transmartproject.db.dataquery.highdim.snp_lz.SnpLzRow
import org.transmartproject.db.dataquery.highdim.snp_lz.SnpSubjectSortedDef

class SNPMockDataHelper extends MockTabularResultHelper {

    List<AssayColumn> sampleAssays
    List<Map> subjectData
    Map<String, Map> snpProperties
    Map<String, List> cellData
    
    final int subjectCount = 2
    
    void generateSubjectData() {
        this.sampleAssays = createSampleAssays(subjectCount)
        def sampleCodes = sampleAssays*.sampleCode
        
        this.subjectData = [
            [ "subjectId": sampleCodes[0], "subjectPosition": 1 ],
            [ "subjectId": sampleCodes[1], "subjectPosition": 2 ]
        ]
    }
    
    Map createMockSubjectData() {
        if (!subjectData) {
            generateSubjectData()
        }
        Map subjects = [:]
        for (Map data: subjectData) {
            SnpSubjectSortedDef subject = mock(SnpSubjectSortedDef)
            subject.patientPosition.returns(data.subjectPosition).stub()
            subject.subjectId.returns(data.subjectId).stub()
            def patient = patients[data.subjectPosition - 1]
            subject.patient.returns(patient).stub()
            subjects[new Long(data.subjectPosition)] = subject // assuming patient id == subjectPosition
        }
        return subjects
    }
    
    TabularResult createMockSnpLzTabularResult() {
        generateSubjectData()
        
        this.snpProperties = [
            "row1": [ chromosome: 1, position: 100, snpName: "rs0010", a1: 'A', a2: 'T'],
            "row2": [ chromosome: 1, position: 200, snpName: "rs1234", a1: 'C', a2: 'G'],
            "row3": [ chromosome: 'X', position: 30, snpName: "rs9999", a1: 'G', a2: 'T'],
        ]

        this.cellData = [
            "row1": [
                [   probabilityA1A1: (double)1.0,
                    probabilityA1A2: (double)0.0,
                    probabilityA2A2: (double)0.0,
                    likelyAllele1: 'A',
                    likelyAllele2: 'A',
                    minorAlleleDose: (double)0.0
                ],
                [   probabilityA1A1: (double)0.0,
                    probabilityA1A2: (double)1.0,
                    probabilityA2A2: (double)0.0,
                    likelyAllele1: 'A',
                    likelyAllele2: 'T',
                    minorAlleleDose: (double)0.0
                ],
            ],
            "row2": [
                [   probabilityA1A1: (double)0.0,
                    probabilityA1A2: (double)0.0,
                    probabilityA2A2: (double)1.0,
                    likelyAllele1: 'G',
                    likelyAllele2: 'G',
                    minorAlleleDose: (double)0.0
                ],
                [   probabilityA1A1: (double)0.0,
                    probabilityA1A2: (double)1.0,
                    probabilityA2A2: (double)0.0,
                    likelyAllele1: 'C',
                    likelyAllele2: 'G',
                    minorAlleleDose: (double)0.0
                ],
            ],
            "row3": [
                [   probabilityA1A1: (double)1.0,
                    probabilityA1A2: (double)0.0,
                    probabilityA2A2: (double)0.0,
                    likelyAllele1: 'G',
                    likelyAllele2: 'T',
                    minorAlleleDose: (double)0.0
                ],
                [   probabilityA1A1: (double)1.0,
                    probabilityA1A2: (double)0.0,
                    probabilityA2A2: (double)0.0,
                    likelyAllele1: 'G',
                    likelyAllele2: 'G',
                    minorAlleleDose: (double)0.0
                ],
            ],
        ]
        
        def iterator = cellData.collect { String label, List data ->
            createSnpLzRowForAssays(sampleAssays, data, snpProperties[label])
        }.iterator()
                
        TabularResult highDimResult = mock TabularResult
        highDimResult.indicesList.returns(sampleAssays).stub()
        highDimResult.getRows().returns(iterator).stub()
        highDimResult.iterator().returns(iterator).stub()
        
        highDimResult
    }
    
    DataRow createSnpLzRowForAssays(List<AssayColumn> assays,
            List data,
            Map<String,Object> snpProperties) {
        createMockSnpLzRow(
                dot(assays, data, {a, b -> [ a, b ]})
                         .collectEntries(Closure.IDENTITY),
                snpProperties)
    }
            
    private DataRow<AssayColumn, Object> createMockSnpLzRow(Map<AssayColumn, Object> data,
            Map<String,Object> snpProperties) {
            
        def cells = data.collect { assay, celldata ->
            new SnpLzCell(
                (double)celldata.probabilityA1A1,
                (double)celldata.probabilityA1A2,
                (double)celldata.probabilityA2A2,
                (char)celldata.likelyAllele1,
                (char)celldata.likelyAllele2,
                (double)celldata.minorAlleleDose)
        }
        
        SnpLzRow row = mock(SnpLzRow)
        row.snpName.returns(snpProperties.snpName).stub()
        row.chromosome.returns(snpProperties.chromosome).stub()
        row.position.returns(snpProperties.position).stub()
        row.a1.returns(snpProperties.a1).stub()
        row.a2.returns(snpProperties.a2).stub()
        
        cells.eachWithIndex { cell, i ->
            row.getAtPatientIndex(i).returns(cell).stub()
        }
        row.iterator().returns(cells.iterator()).stub()
        
        row
    }

}
