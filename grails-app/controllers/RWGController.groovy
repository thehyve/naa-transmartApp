import com.google.common.collect.ImmutableMap
import grails.validation.Validateable
import groovy.time.TimeCategory
import org.grails.databinding.BindUsing
import org.json.JSONArray
import org.json.JSONObject
import org.transmart.biomart.Experiment
import org.transmart.searchapp.AccessLog
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.SearchTaxonomy
import org.transmartproject.browse.fm.FmFile
import org.transmartproject.browse.fm.FmFolder
import org.transmartproject.core.exceptions.InvalidRequestException

//import bio.BioAnalysisAttribute
//import RWGVisualizationDAO
// so we can render as JSON
class RWGController {
    def trialQueryService
//    def searchKeywordService
    def springSecurityService
    def formLayoutService
    def fmFolderService
//    def ontologyService
//    def solrFacetService
//    def geneSignatureService

    private static final Map<String, Integer> FOLDER_TYPE_COUNTS_EMPTY_TEMPLATE = ImmutableMap.of(
            'PROGRAM',  0,
            'STUDY',    0,
            'ASSAY',    0,
            'ANALYSIS', 0,
            'FOLDER',   0,)


    def index = {

        def exportList = session['export'];

        def rwgSearchFilter = session['rwgSearchFilter'];
        if (rwgSearchFilter) {
            rwgSearchFilter = rwgSearchFilter.join(",,,")
        } else {
            rwgSearchFilter = "";
        }

        def rwgSearchOperators = session['rwgSearchOperators'];
        if (rwgSearchOperators) {
            rwgSearchOperators = rwgSearchOperators.join(";")
        } else {
            rwgSearchOperators = "";
        }

        def globalOperator = session['globalOperator'];
        def searchCategory = session['searchCategory'];

        return [rwgSearchFilter: rwgSearchFilter, rwgSearchOperators: rwgSearchOperators, globalOperator: globalOperator, rwgSearchCategory: searchCategory, exportCount: exportList?.size(), debug: params.debug];
    }

    def ajaxWelcome = {
        //add a unused model to be able to use the template
        render(template: 'welcome', model: [page: "RWG"]);
    }


    /**
     * Add a new node to the taxonomy Dynatree (and recursively add children if any exist).
     * parentNode: Node to add to tree
     * json: JSON array containing the "children" of the jQuery dynaTree
     * isCategory: boolean indicating whether the node being added is a category
     * categoryName: name of the category (i.e. as stored in database and displayed in tree)
     * uniqueTreeId: unique identifier for the node being added. This ill be a concatenation of the parent's unique id + the index of this child's index in children list
     *     e.g. category nodes will be 1,2,3; their children will be 1:1, 1:2, 1:3, 2:1, ...; their children 1:1:1, 1:1:2, ...
     * initialFacetCounts: JSONObject containing the initial facet counts for the nodes in the tree
     */
    // note: use a function instead of a closure - this is being called hundreds of times, and being a fn makes a big difference
    //def addDynaNode = {SearchTaxonomy parentNode, JSONArray json, boolean isCategory, String categoryName  ->
    public void addDynaNode(SearchTaxonomy parentNode, JSONArray json, boolean isCategory,
                            String categoryName, String uniqueTreeId, JSONObject initialFacetCounts) {
        JSONArray children = new JSONArray()

        // create map for attributes of node
        def parent = [:]

        // create a custom attribute for term name
        parent["termName"] = parentNode.termName

        // generate the id for use in tree and for link to active terms
        // if there is a link to an active term, use that as id (i.e. search_keyword_id)
        // if not, use the id from the search_taxonomy table prepended with a letter (to ensure that there are no id collisions)
        def id
        if (parentNode.searchKeywordId) {
            id = parentNode.searchKeywordId
        } else {
            id = 'X' + parentNode.id
        }
        parent["id"] = id

        // create the key that matches what we use in javascript to identify search terms
        // assuming for now that the category and the category display are the same (with category being all caps); may
        // need to break this out into separate fields
        parent["key"] = categoryName + "|" + categoryName.toUpperCase() + ";" + parentNode.termName + ";" + id

        // if category, then display as folder and don't show checkbox; other levels, not a folder and show checkbox
        parent["isFolder"] = isCategory
        parent["hideCheckbox"] = isCategory

        // add custom attributes for each node
        parent["isCategory"] = isCategory
        parent["categoryName"] = categoryName + "|" + categoryName.toUpperCase()

        // create a uniqueTreeId for each node so we can identify it from it's copies
        //  (id and key are not unique amongst copies)
        parent["uniqueTreeId"] = uniqueTreeId

        // Create custom attributes for the facet count for this node, and one for the initial facet
        //   count which will be used to save the value when the tree gets cleared so we don't have to requery
        // Set to -1 for category nodes
        if (isCategory) {
            parent["facetCount"] = -1
            parent["initialFacetCount"] = -1

            //title is same as term name for categories
            parent["title"] = parentNode.termName
        } else {
            // get the json object for the category
            JSONObject jo = (JSONObject) initialFacetCounts.get(getSOLRCategoryName(categoryName))

            // convert the term id to a string
            String idString = id.toString()

            // retrieve the count for the term id if it exists in the json object, otherwise
            //  none found so it's zero
            int count
            if (jo.has(idString)) {
                count = jo.getInt(idString)
            } else {
                count = 0
            }

            parent["facetCount"] = count
            parent["initialFacetCount"] = count

            // if the initial count is zero, don't add to tree
            if (count == 0) {
                return
            }

            // include facet count in title for non-category nodes
            parent["title"] = /${parentNode.termName} (${count})/
        }

        def childIndex = 1
        if (parentNode.children) {
            // recursively add each child
            for (childNode in parentNode.children) {
                addDynaNode(childNode, children, false, categoryName, uniqueTreeId + ";" + childIndex, initialFacetCounts)
                childIndex++
            }
        }

        // don't add categories without children to tree
        if (isCategory && (children.length() == 0)) {
            //Removing this for now, we won't have any children in our tree. We are doing browse popups.
            //return
        }

        // add children to parent map
        parent["children"] = children

        // add parent map to json array
        json.put(parent)
    }

    def renderRoot(RenderRootCommand command) {
        if (!command.validate()) {
            throw new InvalidRequestException('bad parameters: ' + command.errors)
        }

        def user = AuthUser.findByUsername(springSecurityService.principal.username)
        def folderContentsAccessLevelMap = fmFolderService.getFolderContentsWithAccessLevelInfo(user, null)

        if (command.search && !command.folderIds) {
            render template: '/fmFolder/noResults',
                    plugin: 'folderManagement',
                    model: [resultNumber: new JSONObject(FOLDER_TYPE_COUNTS_EMPTY_TEMPLATE)]
            return
        }

        String folderSearchString
        String uniqueLeavesString
        def counts = [*: FOLDER_TYPE_COUNTS_EMPTY_TEMPLATE,]

        if (command.folderIds) {
            List<FmFolder> folders = FmFolder.findAllByIdInList(command.folderIds)
            folderSearchString = folders*.folderFullName.join(',') + ','
            // inefficient quadratic algorithm, but it shouldn't matter
            uniqueLeavesString = folders.findAll { folderUnderConsideration ->
                // false that it has a parent in the list
                !folders.any {
                    !it.is(folderUnderConsideration) &&
                            folderUnderConsideration.folderFullName.startsWith(it.folderFullName)
                }
            }*.folderFullName.join(',') + ','

            counts = [
                    *: counts,
                    *: folders.inject([:], { cur, el ->
                        cur[el.folderType] = (cur[el.folderType] ?: 0) + 1
                        cur
                    })]
        }

        render(
                template: '/fmFolder/folders',
                plugin: 'folderManagement',
                model: [
                        folderContentsAccessLevelMap: folderContentsAccessLevelMap,
                        folderSearchString: folderSearchString,
                        uniqueLeavesString: uniqueLeavesString,
                        auto: true,
                        resultNumber: new JSONObject(counts),
                        // not used anymore, but I'd rather not touch the gsp too much
//                        nodesToExpand: nodesToExpand,
//                        nodesToClose: nodesToClose,
                ])
    }

    // Load the search results for the given search terms using the new annotation tables
    // return the html string to be rendered for the results panel
    def loadSearchResults = { studyCounts, startTime ->
        def exprimentAnalysis = [:]                        // Map of the trial objects and the number of analysis per trial
        def total = 0                                // Running total of analysis to show in the top banner

        def studyWithResultsFound = false

        for (studyId in studyCounts.keys().sort()) {
            def c = studyCounts[studyId].toInteger()

            if (c > 0) {
                studyWithResultsFound = true

                Long expNumber = Long.parseLong(studyId)

                def exp = Experiment.createCriteria()
                def experiment = exp.get {
                    eq("id", expNumber)
                }
                if (experiment == null) {
                    log.warn "Unable to find an experiment for ${expNumber}"
                } else {
                    exprimentAnalysis.put((experiment), c)
                    total += c
                }
            }
        }
        // capture html as a string that will be passed back in JSON object
        def html
        if (!studyWithResultsFound) {
            html = g.render(template: '/search/noResult').toString()
        } else {
            html = g.render(template: '/RWG/experiments', model: [experiments: exprimentAnalysis, analysisCount: total, duration: TimeCategory.minus(new Date(), startTime)]).toString()
        }

        return html
    }

    // Load the trial analysis for the given trial
    def getTrialAnalysis = {
        new AccessLog(username: springSecurityService.getPrincipal().username,
                event: "Loading trial analysis", eventmessage: params.trialNumber, accesstime: new Date()).save()

        def analysisList = trialQueryService.querySOLRTrialAnalysis(params, session.solrSearchFilter)
        render(template: '/RWG/analysis', model: [aList: analysisList])
    }

    def getFileDetails = {
        def layout = formLayoutService.getLayout('file')
        render(template: '/fmFolder/fileMetadata', model: [layout: layout, file: FmFile.get(params.id)])
    }
}

@Validateable
class RenderRootCommand {
    boolean search
    @BindUsing({ obj, source ->
        def folderIds = source['folderIds']
        if (!folderIds) {
            return []
        }
        if (!(folderIds instanceof List ) && !folderIds.getClass().array) {
            folderIds = [folderIds]
        }
        folderIds.collect {
            if (it.isLong()) {
                it as Long
            }
        }
    })
    List<Long> folderIds

    static constraints = {
        folderIds validator: { val, obj ->
            if (!obj.search && val) {
                return false
            }
            val.every { it instanceof Long }
        }
    }
}
