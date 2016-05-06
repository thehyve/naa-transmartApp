package org.transmartproject.search.indexing

import groovy.util.logging.Log4j
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.PoolingClientConnectionManager
import org.apache.http.params.CoreConnectionPNames
import org.apache.http.params.HttpParams
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.impl.HttpSolrServer
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
@Log4j
class SolrFacetsCore implements DisposableBean {

    public static final int MAX_CONNECTIONS = 25
    public static final int TIMEOUT_IN_SECONDS = 5

    @Autowired
    private GrailsApplication grailsApplication

    @Delegate(interfaces=false)
    private HttpSolrServer delegate

    private getBaseUrl() {
        def c = grailsApplication.config.com.rwg.solr
        def path = c.facets.path ?: c.browse.path.split('/').findAll()
                .reverse().drop(2).reverse() // drop two last elements (rwg, select)
                .plus('facets').join('/')
        "${c.scheme}://${c.host}/$path"
    }

    @PostConstruct
    private void init() {
        delegate = new HttpSolrServer(baseUrl).with {
            maxTotalConnections = MAX_CONNECTIONS
            defaultMaxConnectionsPerHost = MAX_CONNECTIONS
            connectionTimeout = TIMEOUT_IN_SECONDS * 1000
            soTimeout = TIMEOUT_IN_SECONDS * 1000
            it
        }
    }

    SolrServer getSolrServer() {
        delegate
    }

    @Override
    void destroy() {
        delegate.shutdown()
    }
}
