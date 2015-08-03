import java.awt.Color
import java.awt.Rectangle

import org.apache.commons.math.stat.inference.TestUtils
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartRenderingInfo
import org.jfree.chart.JFreeChart
import org.jfree.chart.entity.StandardEntityCollection
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.renderer.category.StandardBarPainter
import org.jfree.chart.renderer.xy.StandardXYBarPainter
import org.jfree.chart.renderer.xy.XYBarRenderer
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.Dataset
import org.jfree.data.general.DefaultPieDataset
import org.jfree.data.statistics.BoxAndWhiskerCalculator
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset
import org.jfree.data.statistics.HistogramDataset
import org.jfree.graphics2d.svg.SVGGraphics2D
import org.jfree.ui.RectangleInsets
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint

import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew

/**
 * Created by Florian Guitton <f.guitton@imperial.ac.uk> on 17/12/2014.
 */
class ChartService {

    def i2b2HelperService
    def exportMetadataService
    def highDimChartDataService

    def public keyCache = []

    def getSubsetsFromRequest(params) {

        // We retrieve the result instance ids from the client
        def result_instance_id1 = params.result_instance_id1 ?: null;
        def result_instance_id2 = params.result_instance_id2 ?: null;

        // We create our subset reference Map
        [
            1: [ exists: !(result_instance_id1 == null || result_instance_id1 == ""), instance: result_instance_id1],
            2: [ exists: !(result_instance_id2 == null || result_instance_id1 == ""), instance: result_instance_id2],
            commons: [:]
        ]
    }

    def computeChartsForSubsets(subsets) {

        // We intend to use some legacy functions that are used elsewhere
        // We need to use a printer for this
        StringWriter output = new StringWriter()
        PrintWriter writer = new PrintWriter(output)

        // We want to automatically clear the output buffer as we go
        output.metaClass.toStringAndFlush = {
            def tmp = buf.toString()
            buf.setLength(0)
            tmp
        }

        // We need to run some common statistics first
        // This must be changed for multiple (>2) cohort selection
        // We grab the intersection count for our two cohort
        if (subsets[2].exists)
            subsets.commons.patientIntersectionCount = i2b2HelperService.getPatientSetIntersectionSize(subsets[1].instance, subsets[2].instance)

        // Lets prepare our subset shared diagrams, we will fill them later
        def ageHistogramHandle = [:]
        def agePlotHandle = [:]

        subsets.findAll { n, p ->
            p.exists
        }.each { n, p ->

            // First we get the Query Definition
            i2b2HelperService.renderQueryDefinition(p.instance, "Query Summary for Subset ${n}", writer)
            p.query = output.toStringAndFlush()

            // Lets fetch the patient count
            p.patientCount = i2b2HelperService.getPatientSetSize(p.instance)

            // Getting the age data
            p.ageData = i2b2HelperService.getPatientDemographicValueDataForSubset("AGE_IN_YEARS_NUM", p.instance).toList()
            p.ageStats = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(p.ageData)
            ageHistogramHandle["Subset $n"] = p.ageData
            agePlotHandle["Subset $n"] = p.ageStats

            // Sex chart has to be generated for each subset
            p.sexData = i2b2HelperService.getPatientDemographicDataForSubset("sex_cd", p.instance)
            p.sexPie = getSVGChart(type: 'pie', data: p.sexData, title: "Sex")

            // Same thing for Race chart
            p.raceData = i2b2HelperService.getPatientDemographicDataForSubset("race_cd", p.instance)
            p.racePie = getSVGChart(type: 'pie', data: p.raceData, title: "Race")

        }

        // Lets build our age diagrams now that we have all the points in
        subsets.commons.ageHisto = getSVGChart(type: 'histogram', data: ageHistogramHandle, title: "Age")
        subsets.commons.agePlot = getSVGChart(type: 'boxplot', data: agePlotHandle, title: "Age")

        subsets
    }

    def getConceptsForSubsets(subsets) {

        // We also retrieve all concepts involved in the query
        def concepts = [:]

        i2b2HelperService.getDistinctConceptSet(subsets[1].instance, subsets[2].instance).collect {
            i2b2HelperService.getConceptKeyForAnalysis(it)
        }.findAll() {
            it.indexOf("SECURITY") <= -1
        }.each {
            concepts[it] = getConceptAnalysis(concept: it, subsets: subsets)
        }

        concepts
    }

    def addHighDimDataToTable(ExportTableNew tablein, String concept, subsets, List<Map> filters) {
        log.debug "addHighDimDataToTable"

        def metadata = exportMetadataService.getHighDimMetaData(subsets[1]?.instance as Long, subsets[2]?.instance as Long)
        metadata.each { data ->
            log.debug "addHighDimDataToTable: dataType = ${data.datatype}"
            def rowdata = [:]
            subsets.each { k, v ->
                if (v.instance) {
                    def assayConstraints = [
                            (AssayConstraint.PATIENT_SET_CONSTRAINT):
                                [[result_instance_id: v.instance]],
                            (AssayConstraint.ONTOLOGY_TERM_CONSTRAINT):
                                [[concept_key: concept]],
                        ]
                    Map subjectData = highDimChartDataService.getTableDataByPatient(data.datatype.dataTypeName, assayConstraints, filters)
                    List colnames = subjectData.colnames
                    List subjects = subjectData.subjects
                    Map tableData = subjectData.tableData
                    colnames.each { colname ->
                        if (tablein.getColumn(colname) != null) {
                            log.warn "Column '${colname}' already exists."
                        } else {
                            log.debug "Adding column '${colname}'"
                            tablein.putColumn(colname, new ExportColumn(colname, colname, "", "string"))
                        }
                    }
                    tableData.each { id, values ->
                        def subjectId = id as String
                        if (!tablein.containsRow(subjectId)) {
                            log.warn "Row with subject id '${subjectId}' does not exists."
                        } else {
                            log.debug "Adding values for subject '${subjectId}'"
                            ExportRowNew row = tablein.getRow(subjectId)
                            values.eachWithIndex { value, i ->
                                row.put(colnames[i], value)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 
     * @param subsets
     * @param concept
     * @param chartSize
     * @param filters
     * @return
     */
    List<Map> getHighDimAnalysis(subsets, concept, chartSize, List<Map> filters) {
        List<Map> result = []
        def metadata = exportMetadataService.getHighDimMetaData(subsets[1]?.instance as Long, subsets[2]?.instance as Long)
        metadata.each { data ->
            log.debug "getHighDimAnalysis: concept = ${concept}, dataType = ${data.datatype}"
            def barChartData = [:]
            subsets.each { k, v ->
                if (v.instance) {
                    def assayConstraints = [
                            (AssayConstraint.PATIENT_SET_CONSTRAINT):
                                [[result_instance_id: v.instance]],
                            (AssayConstraint.ONTOLOGY_TERM_CONSTRAINT):
                                [[concept_key: concept]]
                        ]
                    barChartData[k] = highDimChartDataService.getBarChartData(data.datatype.dataTypeName, assayConstraints, filters)
                }
            }
            def rowCount = barChartData.max { it.value.data.size() }.value.data.size()
            log.debug "Row count: ${rowCount}"
            // Rows are the different selected high dimension data rows
            (0..(rowCount > 0 ? rowCount-1 : 0)).each { i ->
                def title = ''
                Set<String> labels = []
                def series = []
                def bardata = [:]
                barChartData.each { k, v ->
                    if (i < v.data.size()) {
                        title = v.title
                        labels.addAll(v.labels)
                        series.add("Subset ${k}")
                        bardata[k] = v.data[i]
                    }
                }
                List labelList = new ArrayList(labels)
                labelList.sort()
                Map sortedData = bardata.collectEntries { k, v ->
                    SortedMap d = new TreeMap()
                    labelList.each { label ->
                        d[label] = v[label] ?: 0
                    }
                    [(k): d]
                }
                log.debug "Title: ${title}, plot data: ${sortedData}"
                result << [
                    plots: sortedData.collectEntries { k, v ->
                        def plottitle = title //"Genotype distribution in subset $k for SNP $title"
                        [(k): getSVGChart(type: 'bar', title: "$plottitle", data: v, size: chartSize)]
                    },
                    title: title,
                    labels: labelList,
                    series: series,
                    data: sortedData,
                    concept: concept,
                    dataType: data.datatype.dataTypeName,
                    dataTypeDescription: data.datatype.dataTypeDescription,
                    filters: filters
                ]
            }
        }
        result
    }

    def getConceptAnalysis (Map args) {

        // Retrieving function parameters
        def subsets = args.subsets ?: null
        def concept = args.concept ?: null
        def conceptKey = args.conceptKey ?: null
        def chartSize = args.chartSize ?: null
        def filters = args.filters ?: null

        // We create our result holder and initiate it from subsets
        def result = [:]
        subsets.each { k, v ->
            result[k] = [:]
            v.exists == null ?: (result[k].exists = v.exists)
            v.instance == null ?: (result[k].instance = v.instance)
        }

        // We retrieve the basics
        result.commons.conceptCode = i2b2HelperService.getConceptCodeFromKey(concept);
        result.commons.conceptName = i2b2HelperService.getShortNameFromKey(concept);

        if (filters) {
            result.commons.type = 'highdim'
            result.commons.conceptName = i2b2HelperService.getShortNameFromKey(conceptKey);
            result.commons.highdim = getHighDimAnalysis(subsets, conceptKey, chartSize, filters)
        } else if (i2b2HelperService.isValueConceptCode(result.commons.conceptCode)) {

            result.commons.type = 'value'

            // Lets prepare our subset shared diagrams, we will fill them later
            def conceptHistogramHandle = [:]
            def conceptPlotHandle = [:];

            result.findAll { n, p ->
                p.exists
            }.each { n, p ->

                // Getting the concept data
                p.conceptData = i2b2HelperService.getConceptDistributionDataForValueConceptFromCode(result.commons.conceptCode, p.instance).toList()
                p.conceptStats = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(p.conceptData)
                conceptHistogramHandle["Subset $n"] = p.conceptData
                conceptPlotHandle["Series $n"] = p.conceptStats
            }

            // Lets build our concept diagrams now that we have all the points in
            result.commons.conceptHisto = getSVGChart(type: 'histogram', data: conceptHistogramHandle, size: chartSize)
            result.commons.conceptPlot = getSVGChart(type: 'boxplot', data: conceptPlotHandle, size: chartSize)

            // Lets calculate the T test if possible
            if (result[2].exists) {

                if (result[1].conceptData.toArray() == result[2].conceptData.toArray())
                    result.commons.testmessage = 'No T-test calculated: these are the same subsets'
                else if (result[1].conceptData.size() < 2 || result[2].conceptData.size() < 2)
                    result.commons.testmessage = 'No T-test calculated: not enough data'
                else {

                    def double [] o = (double[])result[1].conceptData.toArray()
                    def double [] t = (double[])result[2].conceptData.toArray()

                    result.commons.tstat = TestUtils.t(o, t).round(5)
                    result.commons.pvalue = TestUtils.tTest(o, t).round(5)
                    result.commons.significance = TestUtils.tTest(o, t, 0.05)

                    if (result.commons.significance)
                        result.commons.testmessage = 'T-test demonstrated results are significant at a 95% confidence level'
                    else
                        result.commons.testmessage = 'T-test demonstrated results are <b>not</b> significant at a 95% confidence level'

                }
            }

        } else {

            result.commons.type = 'traditional'

            result.findAll { n, p ->
                p.exists
            }.each { n, p ->

                // Getting the concept data
                p.conceptData = i2b2HelperService.getConceptDistributionDataForConcept(concept, p.instance)
                p.conceptBar = getSVGChart(type: 'bar', data: p.conceptData, size: [width: 400, height: p.conceptData.size() * 15 + 80])

            }

            // Lets calculate the χ² test if possible
            if (result[2].exists) {

                def junction = false

                result[1].conceptData.each { k, v ->
                    junction = junction ?: (v > 0 && result[2].conceptData[k] > 0)
                }

                if (!junction)
                    result.commons.testmessage = 'No χ² test calculated: subsets are disjointed'
                else if (result[1].conceptData == result[2].conceptData)
                    result.commons.testmessage = 'No χ² test calculated: these are the same subsets'
                else if (result[1].conceptData.size() != result[2].conceptData.size())
                    result.commons.testmessage = 'No χ² test calculated: subsets have different sizes'
                else if (result[1].conceptData.size() < 2)
                    result.commons.testmessage = 'No χ² test calculated: insufficient dimension'
                else {

                    def long [][] counts = [result[1].conceptData.values(), result[2].conceptData.values()]

                    result.commons.chisquare = TestUtils.chiSquare(counts).round(5)
                    result.commons.pvalue = TestUtils.chiSquareTest(counts).round(5)
                    result.commons.significance = TestUtils.chiSquareTest(counts, 0.05)

                    if (result.commons.significance)
                        result.commons.testmessage = 'χ² test demonstrated results are significant at a 95% confidence level'
                    else
                        result.commons.testmessage = 'χ² test demonstrated results are <b>not</b> significant at a 95% confidence level'

                }
            }
        }

        return result
    }

    private def getSVGChart(Map args) {

        // Retrieving function parameters
        def type = args.type ?: null
        def data = args.data ?: [:]
        def size = args.size ?: [:]
        def title = args.title ?: ""

        // We retrieve the dimension if provided
        def width = size?.width ?: 300
        def height = size?.height ?: 300

        // If no data is being sent we return an empty string
        if (data.isEmpty()) return ''

        // We initialize a couple of objects that we are going to need
        Dataset set = null
        JFreeChart chart = null
        Color transparent = new Color(255, 255, 255, 0)
        SVGGraphics2D renderer = new SVGGraphics2D(width, height)

        // If not already defined, we add a method for defaulting parameters
        if (!JFreeChart.metaClass.getMetaMethod("setChartParameters", []))
            JFreeChart.metaClass.setChartParameters = {

                padding = RectangleInsets.ZERO_INSETS
                backgroundPaint = transparent
                plot.outlineVisible = false
                plot?.backgroundPaint = transparent

                if (plot instanceof CategoryPlot || plot instanceof XYPlot) {

                    plot?.domainGridlinePaint = Color.LIGHT_GRAY
                    plot?.rangeGridlinePaint = Color.LIGHT_GRAY
                    plot?.renderer?.setSeriesPaint(0, new Color(254, 220, 119, 150))
                    plot?.renderer?.setSeriesPaint(1, new Color(110, 158, 200, 150))
                    plot?.renderer?.setSeriesOutlinePaint(0, new Color(214, 152, 13))
                    plot?.renderer?.setSeriesOutlinePaint(1, new Color(17, 86, 146))

                    if (plot?.renderer instanceof BarRenderer) {

                        plot?.renderer?.drawBarOutline = true
                        plot?.renderer?.shadowsVisible = false
                        plot?.renderer?.barPainter = new StandardBarPainter()
                    }

                    if (plot?.renderer instanceof XYBarRenderer) {

                        plot?.renderer?.drawBarOutline = true
                        plot?.renderer?.shadowsVisible = false
                        plot?.renderer?.barPainter = new StandardXYBarPainter()
                    }
                }
            }

        // Depending on the type of chart we proceed
        switch (type) {
            case 'histogram':

                def min = null
                def max = null
                set = new HistogramDataset()
                data.each { k, v ->
                    min = min != null ? (v.min() != null && min > v.min() ? v.min() : min) : v.min()
                    max = max != null ? (v.max() != null && max < v.max() ? v.max() : max) : v.max()
                }.each { k, v ->
                    log.debug "histogram: k = ${k}, v = ${v}"
                    if (k) set.addSeries(k, (double [])v.toArray(), 10, min, max)
                }

                chart = ChartFactory.createHistogram(title, null, "", set, PlotOrientation.VERTICAL, true, true, false)
                chart.setChartParameters()
                chart.legend.visible = false
                break;

            case 'boxplot':

                set = new DefaultBoxAndWhiskerCategoryDataset();
                data.each { k, v ->
                    if (k) set.add(v, k, k)
                }

                chart = ChartFactory.createBoxAndWhiskerChart(title, "", "", set, false)
                chart.setChartParameters()
                chart.plot.renderer.maximumBarWidth = 0.09

                break;

            case 'pie':

                set = new DefaultPieDataset();
                data.each { k, v ->
                    if (k) set.setValue(k, v)
                }

                chart = ChartFactory.createPieChart(title, set, false, false, false)
                chart.setChartParameters()

                chart.title.font.size = 13
                chart.title.padding = new RectangleInsets(30, 0, 0, 0)
                chart.plot.labelBackgroundPaint = new Color(230, 230, 230)
                chart.plot.labelOutlinePaint = new Color(130, 130, 130)
                chart.plot.labelShadowPaint = transparent
                chart.plot.labelPadding = new RectangleInsets(5, 5, 5, 5)
                chart.plot.maximumLabelWidth = 0.2
                chart.plot.shadowPaint = transparent
                chart.plot.interiorGap = 0
                chart.plot.baseSectionOutlinePaint = new Color(213, 18, 42)

                data.eachWithIndex { o, i ->
                    chart.plot.setSectionPaint(o.key, new Color(213, 18, 42, (255 / (data.size() + 1) * (data.size() - i)).toInteger()))
                }

                break;

            case 'bar':

                boolean renderLegend = false
                set = new DefaultCategoryDataset();
                if (data.find { it.value instanceof Map}) {
                    data.each { i, row ->
                        row.each { k, v ->
                            if (k) set.setValue(v, "Subset ${i}", k)
                        }
                    }
                    renderLegend = true
                } else {
                data.each { k, v ->
                    if (k) set.setValue(v, '', k)
                }
                }

                chart = ChartFactory.createBarChart(title, "", "", set, PlotOrientation.HORIZONTAL, renderLegend, true, false)
                chart.setChartParameters()

                chart.plot.renderer.setSeriesPaint(0, new Color(128, 193, 119))
                chart.plot.renderer.setSeriesOutlinePaint(0, new Color(84, 151, 12))

                break;
        }

        chart.draw(renderer, new Rectangle(0, 0, width, height), new ChartRenderingInfo(new StandardEntityCollection()));
        renderer.getSVGElement()
    }
}
