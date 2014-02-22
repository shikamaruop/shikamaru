package shikamaru.intern

class User {

    Long entityId
    Long version

    String userType
    String username
    String password
    String nom
    String prenom
    String email
    String telMobile
    String sexe

    Boolean enabled = true
    Boolean accountExpired = false
    Boolean accountLocked = false
    Boolean passwordExpired = false
    Boolean reinitialise = false
    String userReinitialise
    String motifVerouillage
    Date dateVerouillage

    String labelsList

    //Informations Système
    Boolean deleted = false
    Date dateCreated
    Date lastUpdated
    String userCreated
    String userUpdated

    def userConstraintsValidationService

    static constraints = {
        entityId(blank: false)
        userType(blank: false, inList: ['ADM', 'ABO'])
        username(blank: false, validator: { value, userInstance, errors ->
            def serviceResponse = userInstance.userConstraintsValidationService.usernameValidation(['User'], ['username': userInstance?.username])
            if (serviceResponse?.serviceOk == false) {
                errors.rejectValue("username", serviceResponse?.codeMessage, serviceResponse?.message)
                return false
            }
            return true
        })
        password(blank: false)
        prenom(blank: false)
        nom(blank: false)
        email(blank: false, email: true, validator: { value, userInstance, errors ->
            def serviceResponse = userInstance.userConstraintsValidationService.emailValidation(['User'], ['email': userInstance?.email])
            if (serviceResponse?.serviceOk == false) {
                errors.rejectValue("email", serviceResponse?.codeMessage, serviceResponse?.message)
                return false
            }
            return true
        })
        telMobile(nullable: true)
        sexe(nullable: true)
        enabled(nullable: true)
        accountExpired(nullable: true)
        accountLocked(nullable: true)
        passwordExpired(nullable: true)
        passwordExpired(nullable: true)
        reinitialise(nullable: true)
        userReinitialise(nullable: true)
        motifVerouillage(nullable: true)
        dateVerouillage(nullable: true)
        labelsList(nullable: true)

        dateCreated(nullable: true)
        lastUpdated(nullable: true)
        userCreated(nullable: true)
        userUpdated(nullable: true)

    }

    String toString() {
        return "${username}"
    }


    def beforeValidate() {
        // On set une valeur à entityId
        def service = ApplicationContextHolder.getBean("toolsService")
        entityId = service?.setEntityId()
        labelsList = ":" + this?.class?.simpleName + ":" + this?.userType
    }


    def getList() {
        def listLabels = ['User']
        def service = ApplicationContextHolder.getBean("cypherRequestCreatorService")
        return service.list(listLabels)
    }

    /*
    Cela ne marchera pas: pas d'hibernate, pas de GORM
    //TODO ecrire une méthode à appeler lors de chaque insertion: dans le beforeValidate
     */
    def springSecurityService
    // GORM Events
    def beforeInsert = {
        // userCreated = principalInfo?.username
        def userPrincipal = springSecurityService.currentUser
        if ((userPrincipal != null) && (userPrincipal != 'anonymousUser')) {
            userCreated = userPrincipal.username
        } else {
            userCreated = ""
        }

    }

    def beforeUpdate = {
        // userUpdated = principalInfo?.username
        def userPrincipal = springSecurityService.currentUser
        if ((userPrincipal != null) && (userPrincipal != 'anonymousUser')) {
            userUpdated = userPrincipal.username
        } else {
            userUpdated = ""
        }
    }

}
