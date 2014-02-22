package shikamaru.intern.cypher

import groovy.sql.Sql

import javax.naming.Context
import java.sql.PreparedStatement

class CypherRequestCreatorService {

    def transactional = false

    def cypherRequestHelperService

    Context ctx = new javax.naming.InitialContext()
    def conn = ctx.lookup("java:comp/env/jdbc/shikamaru")
    def sql = new Sql(conn)


    // ##################  Utilisée ##########################
    /*
    Utilisée pour persister dans la bdd.
    Reçoit une map: en clé: la propriété (d'un model), en valeur: la valeur de la propriété.
    Genere une requete Cypher de creation qui retourne le champ 'entityId'.
    entityId est l'id de toute modele.
     */

    def cypherSaveNode(Map params) {
        try {
            log.info("cypherSaveNode params= " + params)
            //On elimine les propriétés ayant "" ou null comme valeur
            def properties = cypherRequestHelperService.filterValuesToSave(params)
           //On met les parametres de la requete
            def requestElements = cypherRequestHelperService.createRequestValues(properties)
            def cypherRequest = "CREATE (a" + params.labelsList + " {" + requestElements[0] + "}) " +
                    "RETURN a.entityId"
            log.info("cypherReq= " + cypherRequest)
            def rsltId
            sql.cacheConnection { conn ->
                PreparedStatement stmt = conn?.prepareStatement(cypherRequest);

                requestElements[1]?.each {
                    stmt.setObject(it?.key?.toInteger(), it?.value)
                }
                def exec = stmt.executeQuery()
                while (exec.next()) {
                    log.info("exec= " + exec?.getObject("a.entityId"))
                    log.info("exec= " + exec?.toRowResult())  //Affiche la ligne entiere
                    rsltId = exec?.getObject("a.entityId")
                }
            }

            return rsltId
        } catch (Exception e) {
            e.printStackTrace()
            return null
        }
    }

    /*
        Methode de recuperation d'un enregistrement de la bdd. A partir de son Id (unique)
     */

    def get(String id) {
        try {
            def cypherRequest = "MATCH a WHERE a.entityId={0} " +
                    "RETURN a"
            def objectInstance
            sql.cacheConnection { conn ->
                PreparedStatement stmt = conn?.prepareStatement(cypherRequest);
                stmt.setObject(0, id)
                def exec = stmt.executeQuery()
                while (exec.next()) {
                    log.info("exec= " + exec?.toRowResult())  //Affiche la ligne entiere
                    objectInstance = exec?.getObject("a")
                    log.info("objectInstance= " + objectInstance)
                }
            }
            return objectInstance

        } catch (Exception e) {
            e.printStackTrace()
            return null
        }
    }

    /*
    Methode de recherche dans la bdd. Mappe la methode List
     Cree une requete MATCH de Cypher, composée de labels, et renvoie les resultats.
     */

    def list(List labelList) {
        try {
            def label = cypherRequestHelperService.createLabels(labelList)
            def cypherRequest = "MATCH (a" + label + ")" +
                    "RETURN a"
            def listInstance = []
            def exec = sql.executeQuery(cypherRequest)
            while (exec.next()) {
//                log.info("exec= " + exec?.toRowResult())  //Affiche la ligne entiere
                listInstance?.add(exec?.getObject("a"))
            }
            return listInstance

        } catch (Exception e) {
            e.printStackTrace()
            return null
        }
    }

    //TODO
    /*
   Methode de recherche dans la bdd. Mappe la methode List
    Cree une requete MATCH de Cypher, composée de labels (contenus dans le map), et renvoie comme resultats
    les champs demandés (contenus dans le map).
    */

    def list(Map params) {

    }

    /*
    Methode de creation de relationship entre 2 nodes.
    Reçoit en parametre l'instance, elimine les champs 'non-proprieté' (i.e le startNode, l'endNode et le relationshipName)
    enregistre le-a relation et lui ajoute les propriétés définies
    Ex de requete:
    MATCH (u:User {username:'admin'}), (r:Role {name:'ROLE_WEB_USER'})
    CREATE (u)-[rel:HAS_ROLE]->(r)
    SET rel.entityId = 20140127231803036AL
     */

    def cypherSaveRelationship(Map params, Object objectInstance) {
        try {

            def requestElements = cypherRequestHelperService.createRequestValues(params)
            log.info("requestElements= " + requestElements)

            def listRequestElements = [:]
            requestElements[0]?.split(",")?.each { req ->
                def minimap = req?.split(":")
                listRequestElements.put(minimap[0]?.trim(), minimap[1]?.trim())
            }
            //Construction de la requete de creation de relation
            def cypherRequest = "MATCH (a" + objectInstance?.startNode?.labelsList + " {entityId:" + listRequestElements?.get("startNode") +
                    "}), (b" + objectInstance?.endNode?.labelsList + "{entityId:" + listRequestElements?.get("endNode") + "}) " +
                    "CREATE (a) - [rel:" + objectInstance?.relationshipName + "] -> (b) "

            // Assignation des propriétés
            listRequestElements.each {
                cypherRequest += "SET rel." + it?.key + " = " + it?.value + " "
            }
            cypherRequest += " RETURN rel.entityId"

            log.info("cypherRequest= " + cypherRequest)
            def rsltId
            sql.cacheConnection { conn ->
                PreparedStatement stmt = conn?.prepareStatement(cypherRequest);

                requestElements[1]?.each {
                    stmt.setObject(it?.key?.toInteger(), it?.value)
                }
                def exec = stmt.executeQuery()
                while (exec.next()) {
                    log.info("exec= " + exec?.getObject("rel.entityId"))
                    log.info("exec= " + exec?.toRowResult())  //Affiche la ligne entiere
                    rsltId = exec?.getObject("rel.entityId")
                }
            }

            return rsltId
        } catch (Exception e) {
            e.printStackTrace()
            return null
        }
    }



    /*
    Methode de recherche.
    Reçoit en params
    - une liste de Labels,
    - une map contenant des propriétés et leurs valeurs supposées,
    - une liste de prorpiétés à retourner.

    NB: Cette methode ne s'applique lorsque la requete de recherche ne concerne qu'un noeud, et ne prend pas
    en compte les relations
     */

    def findNodeBy(List labelList, Map values, List results) {
        log.info("labelList= "+labelList)
        try {
            //Pour eviter que le serveur ne soit surchargé, aucune requete sans Label n'est autorisée
            if (!labelList || labelList?.isEmpty()) {
                throw new Exception("cypher.findBy.no.label")
                return null
            }
            //Mise en forme des labels: ils se presenteront ainsi: ':label1:label2:label3'
            def labels = ""
            if (labelList) {
                labels = cypherRequestHelperService.createLabels(labelList)
            }
//            log.info("labels= " + labels)
            if (labels?.trim() == ":") {
                throw new Exception("cypher.findBy.no.label")
                return null
            }
            // Mise en forme des elements de la requete (ceux qui seront dans le WHERE). Ils seront de la forme
            // [proprieteName:{1}, proprieteName:{2}, proprieteName:{3}], [1:proprieteValue, 2:proprieteValue, 3:proprieteValue].
            // Utile pour utiliser des parametres dans la requete Cypher
            def requestElements
            if (values) {
                requestElements = cypherRequestHelperService.createRequestValues(values)
            }
//            log.info("requestElements= " + requestElements)

            // Mise en forme des requestElements. La 1ère partie ([proprieteName:{1}, proprieteName:{2}, proprieteName:{3}])
            // devient une Map<proprieteName><1>
            def listRequestElements = [:]
            if (requestElements) {
                requestElements[0]?.split(",")?.each { req ->
                    def minimap = req?.split(":")
                    listRequestElements.put(minimap[0]?.trim(), minimap[1]?.trim())
                }
            }

            // Creation de la requete
            def cypherRequest = "MATCH (a" + labels + ") "
            // Définition de la partie Where de la requete
            listRequestElements.eachWithIndex { key, value, index ->
                if (index == 0) {
                    cypherRequest += "WHERE "
                }
                if (index + 1 != listRequestElements?.size()) {
                    cypherRequest += "a." + key + " = " + value + " AND "
                } else {
                    cypherRequest += "a." + key + " = " + value + " "
                }
            }
//            log.info("cypherRequest before queue= " + cypherRequest)

            def queueRequest = "RETURN "
            if (!results || results?.isEmpty()) {
                queueRequest += "a"
            } else {
                results?.each {
                    if (it != results?.last()) {
                        queueRequest += "a." + it + ", "
                    } else {
                        queueRequest += "a." + it + " "
                    }
                }
            }
            cypherRequest += queueRequest
//            log.info("cypherRequest= " + cypherRequest)

            def rsltId = []
            sql.cacheConnection { conn ->
                PreparedStatement stmt = conn?.prepareStatement(cypherRequest);
                if (requestElements) {
                    requestElements[1]?.each {
                        stmt.setObject(it?.key?.toInteger(), it?.value)
                    }
                }
                def exec = stmt.executeQuery()
                while (exec.next()) {
//                    log.info("exec= " + exec?.getObject("a"))
//                    log.info("exec= " + exec?.toRowResult())  //Affiche la ligne entiere
                    rsltId?.add(exec?.toRowResult())
                }
            }
//            log.info("rsltId= " + rsltId)
            return rsltId

        } catch (Exception e) {
            e.printStackTrace()
            return null
        }
    }
}
