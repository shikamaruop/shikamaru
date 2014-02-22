package shikamaru.intern.domainConstraints

import shikamaru.intern.ServiceResponse

/*
Classe utilisée pour valider les contraintes mises dans le modele User.
Ces contraintes sont mises via les validators
 */

class UserConstraintsValidationService {

    def transactional = false

    def commonConstraintsValidationService

    /*Methode utilisée pour verifier qu'une contrainte d'unicité est respectée par des valeurs passées en params.
    La methode recherche dans la bdd d'autres entrées avec les mêmes valeurs que celles proposées au niveau des
    propriétés listées.
     */

    //Validation du champ username du modele User
    def usernameValidation(List labels, Map constaints) {
        def serviceResponse = new ServiceResponse()
        if (!commonConstraintsValidationService.unique(labels, constaints)) {    // Contrainte d'unicité
            serviceResponse.serviceOk = false
            serviceResponse.message = "Ce nom d\'utilisateur a déjà été utilisé"
            serviceResponse.codeMessage = "user.username.already.used"
            return serviceResponse
        }
        serviceResponse.serviceOk = true
        return serviceResponse
    }

    //Validation du champ username du modele User
    def emailValidation(List labels, Map constaints) {
        def serviceResponse = new ServiceResponse()
        if (!commonConstraintsValidationService.unique(labels, constaints)) {   // Contrainte d'unicité
            serviceResponse.serviceOk = false
            serviceResponse.message = "Cette adresse mail a déjà été utilisée avec un autre compte"
            serviceResponse.codeMessage = "user.email.already.used"
            return serviceResponse
        }
        serviceResponse.serviceOk = true
        return serviceResponse
    }
}
