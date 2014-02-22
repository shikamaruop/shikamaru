package shikamaru.intern.domainConstraints

class CommonConstraintsValidationService {

    def transactional = false

    def cypherRequestCreatorService

    /*Methode utilisée pour verifier qu'une contrainte d'unicité est respectée par des valeurs passées en params.
    La methode recherche dans la bdd d'autres entrées avec les mêmes valeurs que celles proposées au niveau des
    propriétés listées.
     */

    def unique(List labels, Map constaints) {
        //Recherche dans la bdd d'enregistrements avec les valeurs soumises
        def results = cypherRequestCreatorService.findNodeBy(labels, constaints, ['entityId'])
        if (results?.size() > 0) {
            return false
        }
        return true
    }
}
