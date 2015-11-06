package org.transmartproject

import grails.converters.JSON

/**
 * Controller for the <code>/filterAutoComplete</code> endpoint.
 * Provides autocompletion functionality for specific data filters.
 * The url <code>/filterAutoComplete</code> will return a list of supported
 * filter types.
 * The url <code>/filterAutoComplete/autocomplete/$type?search=$search</code>
 * will return a list of suggestions for filter type <code>$type</code>, starting
 * with <code>$search</code>.
 * 
 * @see {@link FilterAutocompleteService} for supported filter types.
 * 
 * @author gijs@thehyve.nl
 */
class FilterAutocompleteController {

    static responseFormats = ['json']

    def filterAutocompleteService

	/**
	 * Returns a JSON-formatted list of supported filter types, 
	 * available at the endpoint:
	 * <code>/filterAutoComplete</code>.
	 * 
	 * Will fetch a list of data types from the {@link FilterAutocompleteService}.
	 * E.g., <code>['snps', 'genes']</code>.
	 * 
	 * @return a list of filter types for which suggestions can be requested.
	 */
    def index() {
        log.info "Autocomplete index"
        def types = filterAutocompleteService.getTypes()
        render types as JSON
    }

	/**
	 * Returns a JSON-formatted list of suggestions for autocompletion, 
	 * available at the endpoint:
	 * <code>/filterAutoComplete/autocomplete/$type?search=$search</code>.
	 * E.g., <code>/filterAutoComplete/autocomplete/genes?search=T</code>
	 * will fetch a list of gene names starting with 'T' from the 
	 * {@link FilterAutocompleteService}.
	 * @param id The filter type for which suggestions are requested,
	 * 			 passed in the url as id.
	 * @param search The string that is used in the query.
	 * @return a list of suggestions in JSON format.
	 */
    def autocomplete() {
		String type = params.id
		String search = params.search
        log.info "Autocomplete for type '$type': $search"
        render filterAutocompleteService.autocomplete(type, search) as JSON
    }
}
