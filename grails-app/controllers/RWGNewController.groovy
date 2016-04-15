import grails.converters.JSON
import grails.validation.Validateable
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.SolrException
import org.grails.databinding.BindUsing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.transmart.biomart.BioMarker
import org.transmartproject.core.concept.ConceptFullName
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.core.exceptions.UnexpectedResultException
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.Study
import org.transmartproject.search.indexing.FacetsIndexingService
import org.transmartproject.search.indexing.FacetsQueryingService
import org.transmartproject.search.indexing.TermCount

import java.util.regex.Matcher
import java.util.regex.Pattern

class RWGNewController {

    static scope = 'singleton'

    private static final Pattern KEY_CODE_PATTERN = Pattern.compile('(?<=\\A\\\\\\\\)[^\\\\]+')

    FacetsQueryingService facetsQueryingService
    FacetsIndexingService facetsIndexingService

    @Autowired
    ConceptsResource conceptsResource

    private static final String GENE_LIST_PSEUDO_FIELD = '__gene_list'
    private static final String GENE_SIGNATURE_PSEUDO_FIELD = '__gene_signature'
    private static final String PATHWAY_PSEUDO_FIELD = '__pathway'
    private static final PSEUDO_FIELDS = [GENE_LIST_PSEUDO_FIELD,
                                          GENE_SIGNATURE_PSEUDO_FIELD,
                                          PATHWAY_PSEUDO_FIELD]

    private static final Pattern LUCENE_SPECIAL_CHARACTER = ~/[\Q+-&|!(){}[]^"~*?:\\E]/
    private static final int MAX_RESULTS = 100
    public static final String GET_FACETS_RESULT_COMMAND_SESSION_KEY = 'getFacetsResultCommand'

    //Just clear the search filter and render non-null back
    def clearSearchFilter() {
        //session['solrSearchFilter'] = []
        session[GET_FACETS_RESULT_COMMAND_SESSION_KEY] = null
        render(text: "OK")
    }

    // for faceting
    def getFilterCategories() {
        render facetCountsToOutputMap(facetsQueryingService.topTerms) as JSON
    }

    // Return search categories for the drop down
    def getSearchCategories() {
        render facetsQueryingService.allDisplaySettings.findAll { e ->
            !e.value.hideFromListings
        }.collectEntries { e ->
            [e.key, e.value.displayName]
        } as JSON
    }

    def autocomplete() {
        def field = params.category
        def value = params.term

        if (field == 'ALL') {
            // not supported :(
            render([] as JSON)
            return
        }

        // NOT IMPLEMENTED
        def result = [
                [
                    category: field,
                    value: 'not-implemented'
                ]
        ]

        render result as JSON
    }

    def getFacetResults(GetFacetsCommand command) {
        if (command.hasErrors()) {
            throw new InvalidRequestException("bad parameters: $command.errors")
        }

        // expand __gene_list, __gene_signature and __pathway
        command.fieldTerms.findAll { e ->
            e.key == GENE_LIST_PSEUDO_FIELD || e.key == GENE_SIGNATURE_PSEUDO_FIELD
        }.each { e->
            e.value.searchTerms = e.value.searchTerms.collect { SearchTerm term ->
                term.literalTerm.collect {
                    val -> expandGeneSignature(val)
                }
            }.flatten()
        }
        command.fieldTerms.findAll { e ->
            e.key == PATHWAY_PSEUDO_FIELD
        }.each { e->
            e.value.searchTerms = e.value.searchTerms.collect { SearchTerm term ->
                term.literalTerm.collect {
                    val -> expandPathway(val)
                }
            }.flatten()
        }

        // build query
        def q = new SolrQuery()
        q.addFacetField(*facetsQueryingService.allFacetFields)
        q.rows = MAX_RESULTS
        q.query = commandToQueryString command

        // execute query
        QueryResponse resp
        try {
            resp = facetsQueryingService.query(q)
        } catch (SolrException soe) {
            throw new UnexpectedResultException(soe);
        }



        // format output
        render([
                numFound: resp.results.numFound,
                docs: resp.results,
                conceptKeys: extractConceptKeys(resp.results),
                folderIds: extractFolderIds(resp.results),
                facets: facetCountsToOutputMap(facetsQueryingService.parseFacetCounts(resp))
        ] as JSON)
    }

    def fullReindex() {
        facetsIndexingService.clearIndex()
        facetsIndexingService.fullIndex()
        facetsQueryingService.clearCaches()

        render 'OK'
    }

    def clearQueryingCaches() {
        facetsQueryingService.clearCaches()

        render 'OK'
    }

    @Cacheable('misc_cache')
    protected Map<ConceptFullName, Study> getCategoriesFullNameMap() {
        conceptsResource.allCategories.collectEntries {
            [new ConceptFullName(it.fullName), it]
        }
    }

    private List extractFolderIds(SolrDocumentList documentList) {
        documentList.collect {
            it.getFieldValue(FacetsIndexingService.FIELD_NAME_FOLDER_ID)
        }.findAll().unique()
    }


    private List extractConceptKeys(SolrDocumentList documentList) {
        def catsFullNameMap = categoriesFullNameMap
        documentList
                .collect {
                    it.getFieldValue(FacetsIndexingService.FIELD_NAME_CONCEPT_PATH)
                }
                .findAll()
                .unique()
                .collect {
                    def conceptKey = guessConceptKey(it, catsFullNameMap)
                    if (!conceptKey) {
                        log.info("Could not determine concept key for $it")
                    }
                    conceptKey
                }.findAll()
    }

    private String guessConceptKey(String conceptPath, Map<ConceptFullName, Study> catsFullNameMap) {
        // take the study with whose concept full name the the overlap is larger
        def conceptFullName = new ConceptFullName(conceptPath)
        def r = catsFullNameMap.collectEntries { ConceptFullName categoryFullName, OntologyTerm t ->
            int i = 0
            for (; i < Math.min(conceptFullName.length, categoryFullName.length); i++) {
                if (conceptFullName[i] != categoryFullName[i]) {
                    break
                }
            }
            [t, i]
        }
        OntologyTerm candidateCategory = r.max { it.value }?.key
        if (candidateCategory == null) {
            return null
        }

        Matcher m = KEY_CODE_PATTERN.matcher(candidateCategory.key)
        if (m.find()) {
            "\\\\${m.group(0)}$conceptPath"
        }
    }

    private static escapeSolrLiteral(String s) {
        s.replaceAll LUCENE_SPECIAL_CHARACTER, { "\\$it" }
    }

    private List<String> expandGeneSignature(String signatureUniqueId) {
        BioMarker.executeQuery('''
            select items.bioMarker.name from GeneSignature s inner join s.geneSigItems items
            where s.uniqueId = :signatureUniqueId
        ''', [signatureUniqueId: signatureUniqueId])
    }

    private List<String> expandPathway(String pathway) {
        BioMarker.executeQuery('''
            select bio.bio_marker_name
            from org.transmart.searchapp.SearchKeyword sk,
            org.transmart.biomart.BioMarkerCorrelationMV mv,
            org.transmart.biomart.BioMarker bio
            where sk.dataCategory = 'PATHWAY' and sk.bioDataId = mv.bioMarkerId
            and bio.bioMarkerType = 'GENE' and mv.assoBioMarkerId = bio.id
            and mv.correlType = 'PATHWAY GENE'
            and sk.uniqueId = :uniqueId
        ''', [uniqueId: pathway])
    }

    private String commandToQueryString(GetFacetsCommand command) {
        command.fieldTerms.collect { String fieldName, FieldTerms fieldTerms ->
            def s = fieldTerms.searchTerms.collect { SearchTerm searchTerm ->
                searchTerm.luceneTerm ?: "\"${escapeSolrLiteral(searchTerm.literalTerm)}\""
            }.join(" ${fieldTerms.operator} ")
            "$fieldName:(" + s + ')'
        }.join(" ${command.operator} ")
    }

    private List facetCountsToOutputMap(LinkedHashMap<String, SortedSet<TermCount>> originalMap) {
        def displaySettings = facetsQueryingService.allDisplaySettings
        originalMap.collect { e ->
            [
                    category: [
                            field      : e.key,
                            displayName: displaySettings[e.key].displayName,
                    ],
                    choices : e.value.collect {
                        [
                                value: it.term,
                                count: it.count
                        ]
                    },
            ]
        }
    }
}

@Validateable
class GetFacetsCommand {
    String operator
    @BindUsing({ obj, source ->
        source['fieldTerms'].collectEntries { k, v ->
            [k, new FieldTerms(v).with { ft ->
                ft.searchTerms = ft.searchTerms.collect {
                    new SearchTerm(it)
                }
                ft
            }]
        }
    })
    Map<String, FieldTerms> fieldTerms

    static constraints = {
        operator inList: ['OR', 'AND']
        fieldTerms validator: { val, obj ->
            val.values().every { it.validate() }
        }
    }
}

@Validateable
class FieldTerms {
    String operator
    List<SearchTerm> searchTerms = []

    static constraints = {
        operator inList: ['OR', 'AND']
    }
}

@Validateable
class SearchTerm {
    String literalTerm
    String luceneTerm
}
