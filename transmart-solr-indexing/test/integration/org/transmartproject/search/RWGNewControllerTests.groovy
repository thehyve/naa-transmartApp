package org.transmartproject.search

import grails.test.mixin.TestMixin
import grails.util.Holders
import org.junit.Before
import org.junit.BeforeClass
import org.transmartproject.db.test.RuleBasedIntegrationTestMixin
import org.transmartproject.search.indexing.FacetsIndexingService

@TestMixin(RuleBasedIntegrationTestMixin)
class RWGNewControllerTests {

    RWGNewController rwgNewController

    @BeforeClass
    static void beforeClass() {
        Holders.applicationContext.getBean(FacetsIndexingService).clearIndex()
        Holders.applicationContext.getBean(FacetsIndexingService).fullIndex()
    }

    @Before
    void before() {
        rwgNewController = new RWGNewController()
    }

    void testAutocompleteOneField() {
        def command = new AutoCompleteCommand(category: 'therapeutic_domain_s', term: 'b')
        rwgNewController.autocomplete(command)

        def resp = rwgNewController.response.json
        assert resp[0].category == 'therapeutic_domain_s'
        assert resp[0].value    == 'Behaviors and Mental Disorders'
        assert resp[0].count    == 1
    }

    void testAutoCompleteAll() {
        def command = new AutoCompleteCommand(category: '*', term: 'e', requiredField: 'CONCEPT_PATH')
        rwgNewController.autocomplete(command)

        def resp = rwgNewController.response.json

        assert resp.size() == 3
        assert resp[0].category == 'biomarker_type_s'
        assert resp[0].value    == 'Efficacy biomarker'
        assert resp[0].count    == 4
    }

    void testSearchAllFields() {
        def command = new GetFacetsCommand(operator: 'OR',
                fieldTerms: ['*': new FieldTerms(operator: 'OR',
                        searchTerms: [
                                new SearchTerm(literalTerm: 'age'),
                                new SearchTerm(literalTerm: 'FOLDER:1992455'),
                        ])])

        rwgNewController.getFacetResults(command)

        assert rwgNewController.response.json.numFound == 4
    }

    void testSearchAllFieldsNumber() {
        def command = new GetFacetsCommand(operator: 'AND',
                fieldTerms: [
                        '*': new FieldTerms(operator: 'OR',
                                searchTerms: [
                                        new SearchTerm(literalTerm: '58'),
                                        new SearchTerm(luceneTerm: '[18 TO 20]'),
                                ]),
                        id: new FieldTerms(operator: 'OR',
                                searchTerms: [
                                        new SearchTerm(luceneTerm: 'FOLDER\\:*'),
                                ]),
                ])

        rwgNewController.getFacetResults(command)

        assert rwgNewController.response.json.numFound == 2
    }
}
