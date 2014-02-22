package shikamaru.intern

class Role {

    String entityId   /* On ne peut pas utiliser 'id' car il gerer en backend par Grails.
                        Impossible de lui setter une valeur, et n'est en plas reconnu par 'DomainInstance.properties'.
                        */
    String name
    String authority
    String type
    String dateCreated = new Date()?.format("yyyy-MM-dd HH:mm:ss")
    String labelsList

    static mapping = {
        cache true
        id generator: 'assigned'
    }

    static constraints = {
        entityId(blank: false)
        authority(blank: false, unique: true, validator: { value, roleInstance, errors ->
            if (!value || !(value?.startsWith("ROLE_"))) {
                errors.rejectValue("authority", "role.authority.wrong.syntax", "Le role doit commencer par 'ROLE_' ")
                return false
            }
            return true
        })
        name(nullable: true)
        type(nullable: true)
        labelsList(nullable: true)
        dateCreated(nullable: true)
    }

    String toString() {
        return "${authority}"
    }



    def beforeValidate() {
        // On set une valeur à entityId
        def service = ApplicationContextHolder.getBean("toolsService")
        entityId = service?.setEntityId()
        labelsList = ":" + this?.class?.simpleName + ":" + this?.type
    }

    def getList() {
        def listLabels = ['Role']
        def service = ApplicationContextHolder.getBean("cypherRequestCreatorService")
        return service.list(listLabels)
    }
}
