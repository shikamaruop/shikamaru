package shikamaru.intern

/**
 * Created with IntelliJ IDEA.
 * User: macbookpro
 * Date: 22/02/2014
 * Time: 17:22
 * To change this template use File | Settings | File Templates.
 */
class ServiceResponse {

    boolean serviceOk = false
    String message
    String codeMessage
    def ObjetInstance

    def mapError = [:]
    def listObject = []
}
