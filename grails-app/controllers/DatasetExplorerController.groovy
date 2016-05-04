import com.recomdata.transmart.domain.searchapp.Subset
import grails.converters.JSON
import org.transmart.searchapp.AuthUser

class DatasetExplorerController {
    def springSecurityService
    def i2b2HelperService

    def index = {
        def subsetId = params.sId
        def qid1 = null
        def qid2 = null
        boolean restorecomparison = false

        if(subsetId != null && subsetId != "") {
            Subset subset = Subset.get(subsetId)
            if(subset != null) {
                restorecomparison = true
                qid1 = subset.queryID1
                qid2 = subset.queryID2
            }

        }

        def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
        def admin = i2b2HelperService.isAdmin(user);
        def tokens = i2b2HelperService.getSecureTokensCommaSeparated(user)
        def initialaccess = new JSON(i2b2HelperService.getAccess(i2b2HelperService.getRootPathsWithTokens(), user)).toString();
        log.trace("admin =" + admin)
        render(view: "datasetExplorer", model: [admin             : admin,
                                                tokens            : tokens,
                                                initialaccess     : initialaccess,
                                                restorecomparison : restorecomparison,
                                                qid1              : qid1,
                                                qid2              : qid2,
                                                debug             : params.debug,])
    }
}
