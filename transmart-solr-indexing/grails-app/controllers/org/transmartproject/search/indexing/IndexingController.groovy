package org.transmartproject.search.indexing

class IndexingController {
    FacetsIndexingService facetsIndexingService

    def triggerFullIndex() {
        facetsIndexingService.clearIndex()
        facetsIndexingService.fullIndex()
    }
}
