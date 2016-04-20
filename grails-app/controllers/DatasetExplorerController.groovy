import grails.converters.JSON
import org.transmart.searchapp.AuthUser

class DatasetExplorerController {
    def springSecurityService
    def i2b2HelperService

    def index = {
        def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
        def admin = i2b2HelperService.isAdmin(user);
        def tokens = i2b2HelperService.getSecureTokensCommaSeparated(user)
        def initialaccess = new JSON(i2b2HelperService.getAccess(i2b2HelperService.getRootPathsWithTokens(), user)).toString();
        log.trace("admin =" + admin)
        render(view: "datasetExplorer", model: [admin             : admin,
                                                tokens            : tokens,
                                                initialaccess     : initialaccess,
                                                debug             : params.debug,])
    }
}
