package org.transmartproject

import grails.gorm.DetachedCriteria
import groovy.util.logging.Slf4j

import javax.annotation.PostConstruct

import org.springframework.cache.annotation.Cacheable
import org.transmartproject.db.dataquery.highdim.snp_lz.DeRcSnpInfo
import org.transmartproject.db.dataquery.highdim.snp_lz.DeSnpInfo

@Slf4j
class FilterAutocompleteService {

	private static final int max_results = 50

    private Map<String, Closure<List<String>>> registry = [:]

	/**
	 * Returns a sorted list of maximum {@link #max_results} 
	 * gene names, starting with <code>search</code>.
	 * Gene names are fetched from the {@link DeRcSnpInfo} data type.
	 * 
	 * @param search The start segment used in the query.
	 * @return a list of gene names, starting with <code>search</code>.
	 */
    @Cacheable('gene_autocomplete')
    private List<String> autoCompleteGene(String search) {
        DetachedCriteria query = DeRcSnpInfo
            .where { geneName =~ "${search}%" }
		query
			.max(max_results)
			.order('geneName')
            .list {
                projections {
                  distinct('geneName')  
                }
            }
    }

	/**
	 * Returns a sorted list of maximum {@link #max_results}
	 * Single Nucleotide Polymorphism (SNP) names, starting with <code>search</code>.
	 * SNP names are fetched from the {@link DeSnpInfo} data type.
	 *  
	 * @param search The start segment used in the query.
	 * @return a list of gene names, starting with <code>search</code>.
	 */
    @Cacheable('snp_autocomplete')
    private List<String> autoCompleteSnp(String search) {
        DetachedCriteria query = DeSnpInfo
            .where { name =~ "${search}%" }
		query
			.max(max_results)
			.order('name')
            .list {
                projections {
                  distinct('name')
                }
            }
    }

    void register(String type, Closure<List<String>> f) {
        log.info "Registring autocompleter for type ${type}..."
        registry[type] = f
    }

    @PostConstruct
    void init() {
        register("genes", this.&autoCompleteGene)
        register("snps", this.&autoCompleteSnp)
    }

	/**
	 * Returns the list of supported filter types as a list of strings.
	 */
    public List<String> getTypes() {
        registry.keySet().toList()
    }

	/**
	 * Returns a sorted list of maximum {@link #max_results} suggestions, 
	 * starting with <code>search</code> if the type is one of the supported
	 * filter types; an empty list otherwise.
	 * 
	 * @param type The filter type for which suggestions are requested.
	 * @param search The start segment used in the query.
	 * @return a list with strings, starting with <code>search</code>.
	 */
    public List<String> autocomplete(String type, String search) {
        log.info "Autocomplete for type $type: '$search'"
        registry[type] ? registry[type](search) : []
    }

}
