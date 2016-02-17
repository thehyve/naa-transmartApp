package org.transmartproject

import grails.gorm.DetachedCriteria
import grails.plugin.cache.Cacheable
import groovy.util.logging.Slf4j

import javax.annotation.PostConstruct

import org.transmartproject.db.dataquery.highdim.snp_lz.GenotypeProbeAnnotation;

@Slf4j
@Cacheable('org.transmartproject.FilterAutocompleteService')
class FilterAutocompleteService {

	private static final int max_results = 50

    private Map<String, Closure<List<String>>> registry = [:]

	/**
	 * Returns a sorted list of maximum {@link #max_results} 
	 * gene names, starting with <code>search</code>.
	 * Gene names are fetched from the {@link GenotypeProbeAnnotation} data type.
	 * 
	 * @param search The start segment used in the query.
	 * @return a list of gene names, starting with <code>search</code>.
	 */
    private List<String> autoCompleteGene(String search) {
        DetachedCriteria query = GenotypeProbeAnnotation
            .where { geneInfo ==~ "%${search}%" }
		List<String> geneInfos = query
			.max(max_results)
			.order('geneInfo')
            .list {
                projections {
                  distinct('geneInfo')
                }
            }
        /*
         * This piece of code just because the geneInfo column contains a '|'-
         * separated list of tuples "<geneName>:<geneId>", instead of having
         * a separate table where geneName and geneId are columns.
         */
        List<String> results = []
        for (String geneInfo: geneInfos) {
            List<String> genes = geneInfo.tokenize('|')
            for (String gene: genes) {
                List<String> parts = gene.tokenize(':')
                if (parts.size() > 0) {
                    String geneName = parts[0]
                    if (geneName.startsWith(search)) {
                        results += geneInfo
                    }
                }
            }
        }
        results.unique()
    }

	/**
	 * Returns a sorted list of maximum {@link #max_results}
	 * Single Nucleotide Polymorphism (SNP) names, starting with <code>search</code>.
	 * SNP names are fetched from the {@link GenotypeProbeAnnotation} data type.
	 *  
	 * @param search The start segment used in the query.
	 * @return a list of gene names, starting with <code>search</code>.
	 */
    private List<String> autoCompleteSnp(String search) {
        DetachedCriteria query = GenotypeProbeAnnotation
            .where { snpName ==~ "${search}%" }
		query
			.max(max_results)
			.order('snpName')
            .list {
                projections {
                  distinct('snpName')
                }
            }
    }

    void register(String type, Closure<List<String>> f) {
        log.info "Registering autocompleter for type ${type}..."
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
