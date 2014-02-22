package shikamaru.intern

/*
Convention: toujours garder ces syntaxes:
    - le nom d'une classe representant une relation doit toujours finir par 'Relationship',
    - 'startNode' represente le noeud de depart de la relation. Toujours present. Et garde toujours ce nom là. Toujours
    - 'endNode' represente le noeud d'arrivée de la relation. Toujours present. Et garde toujours ce nom là. Toujours
    - 'relationShipName' est le nom utilisé dans les requetes Cypher afin de representer cette relation. Obligatoire. Et toujours en majuscule
        Ex: BaggioUser - [:HAS_ROLE] -> AdminRole
        'BaggioUser' est startNode
        'AdminRole' est l'endNode

 */
class UserRoleRelationship {

    User startNode
    Role endNode

    String entityId

    String relationshipName = "HAS_ROLE"

    String dateCreated = new Date()?.format("yyyy-MM-dd HH:mm:ss")   //Toujours present
    String userCreated


    static mapping = {
        id column: 'entityId'
    }


    static constraints = {
        relationshipName(blank: false)
        entityId(blank: false)
        startNode(blank: false)
        endNode(blank: false)
        dateCreated(nullable: true)
        userCreated(nullable: true)
    }

    // Toujours present
    def beforeValidate() {
        // On set une valeur à entityId
        def service = ApplicationContextHolder.getBean("toolsService")
        entityId = service?.setEntityId()
    }
}
