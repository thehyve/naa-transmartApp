package org.transmartproject

import grails.converters.JSON

/**
 * Controller for the <code>/highDimDataType</code> endpoint.
 * Fetches data about a data type.
 * The url <code>/highDimDataType/supportedDataConstraints?dataType$dataType</code> 
 * will return a list of supported data constraints for data type <code>$dataType</code>.
 *
 * @author gijs@thehyve.nl
 */
class HighDimDataTypeController {

    static responseFormats = ['json']

    def highDimChartDataService

    /**
     * Returns a JSON-formatted list of supported data constraints for a data type <code>$dataType</code>,
     * available at the endpoint:
     * <code>/highDimDataType/supportedDataConstraints?dataType=$dataType</code>.
     *
     * Will fetch a list of data types from the {@link HighDimChartDataService}.
     * E.g., <code>['snps', 'genes', 'disjunction']</code>.
     *
     * @param dataType the data type for which supported data constraints are requested.
     *
     * @return a list of supported data constraints in JSON format.
     */
    def supportedDataConstraints() {
        def dataType = params.dataType ?: null
        def constraints = []
        if (dataType) {
            constraints = highDimChartDataService.getSupportedDataConstraints(dataType) as List
        }
        render constraints as JSON
    }
}
