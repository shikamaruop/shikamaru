package shikamaru.intern.cypher

class CypherRequestHelperService {

    def transactional = false

    /*
    Methode de generation des labels de la requete.
    Si un seul label, la methode transforme le parametre en :label.
    Si une liste de labels, elle le transforme en :liste[1]:liste[2]:liste[3]
     */

    def createLabels(def labels) {
        try {
            def label = ""
            if (labels instanceof String) {
                label += ":" + labels
            } else if (labels instanceof List) {
                labels = labels?.join(":")
//                log.info("labels = " + labels)
                label += ":" + labels
            }
//            log.info("label final= " + label)
            return label
        } catch (Exception e) {
            e.printStackTrace()
            log.error("createLabels exception= " + e.getMessage())
            return ""
        }
    }

    /*
Methode de generation des valeurs de la requete.
Elle transforme les valeurs reçues en
[proprieteName:{1}, proprieteName:{2}, proprieteName:{3}], [1:proprieteValue, 2:proprieteValue, 3:proprieteValue].
 */

    def createRequestValues(def params) {
        def values = [:]
        def mappingValues = [:]
        params.eachWithIndex { val, i ->
            values?.put(val?.key, "{" + i + "}")
            mappingValues?.put(i, val?.value)
        }
        values = values?.toString().substring(1, values?.toString().length() - 1)
//        log.info("values= " + values?.toString())
//        log.info("mappingValues= " + mappingValues?.toString())
        return [values, mappingValues]
    }

    /*
    Recoit en params une map contenant une liste de propriétés avec leurs valeurs à persister.
    Elimine les propriétés ayant null ou "" comme valeur
     */

    def filterValuesToSave(def params) {
        try {
            def properties = [:]
            params?.each {
                if (it.value != null && it.value != "null" && it.value != "") {
                    properties.put(it?.key, it?.value)
                }
            }
//            log.info("properties definitly= " + properties)
            return properties
        } catch (Exception e) {
            e.printStackTrace()
            return null
        }
    }

    /*
    Reçoit en params une instance de model avec des valeurs à saver.
    La methode compare les champs à persister du model (ses contraintes) avec les valeurs reçues.
    Et ne recupere que les champs en commun.
    Permet, entre autre, de supprimer les transients des valeurs à persister,
    ainsi que les noms de methodes, et les noms de services appelés
     */

    def filterPersistableValuesFromDomainClass(def objectInstance) {
        try {
            def properties = [:]
            objectInstance.properties?.each {
                if (it.key in objectInstance.constraints.keySet()) {
                    properties.put(it?.key, it?.value)
                }
            }
            return properties
        } catch (Exception e) {
            e.printStackTrace()
            return null
        }
    }

    /*
    ################### Utilisée lorsque le modele a des foreign keys ##########################
    Reçoit en params une instance de model avec des valeurs à saver, plus les params envoyés [depuis une vue]
    La methode parcours les params et transforme les champs foreign keys.
    Ex: params.startNode.entityId = 20140127231803036AL
        devient
        params.startNode = 20140127231803036AL
    Elle compare ensuite les champs à persister du model (ses contraintes) avec les valeurs reçues.
    Et ne recupere que les champs en commun.
    Cela permet, entre autre, de supprimer les transients des valeurs à persister,
    ainsi que les noms de methodes, et les noms de services appelés
     */

    def filterPersistableValuesFromDomainClass(def objectInstance, Map params) {
        try {
            def properties = [:]
            params?.each {
                // On verifie d'abord qu'il ya une valeur à persister pour la propriété
                // Les null ne sont pas persistés.
                if (it?.value != null && it?.value != "null" && it?.value != "") {
                    // Les "jointures" se presentent de la forme params.startNode.entityId
                    // On enleve le '.entityId', puis on recupere la valeur qui lui est associée
                    if (it?.key?.endsWith(".entityId")) {
                        def foreignKey = it?.key?.minus(".entityId")
                        properties?.remove(foreignKey)
                        properties.put(foreignKey, params.get(it?.key))
                    } else {
                        // Quand c une "jointure", "la classe originale = org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
                        // Ici on recupere les valeurs associées aux proriétés 'simples' de la classe
                        if (!(it?.value instanceof java.util.Map)) {
                            properties.put(it?.key, it?.value)
                        }
                    }
                }
            }

            // On ajoute tous les champs (ayant une valeur) qui sont dans le modele (entityId par exemple) mais
            // qui ne sont pas dans le map.
            objectInstance?.properties?.each { prop ->
                if (!(prop?.key in properties) && !(prop.key?.contains("startNode")) && !(prop.key?.contains("endNode"))
                        && prop?.value != null && prop?.value != "null" && prop?.value != "") {
                    properties.put(prop?.key, prop?.value)
                }
            }

            // On finit par verifier que les champs qui ne sont pas dans le modele alors qu'ils sont dans le map
            // ne sont pas pris en compte
            // On fait un '.clone()' car manipuler directement le map 'properties' cause un 'concurrentmodificationexception'
            def finalProperties = properties?.clone()
            finalProperties?.each {
                if (!(it.key in objectInstance.constraints.keySet())) {
                    properties?.remove(it?.key)
                }
            }

            return properties
        } catch (Exception e) {
            e.printStackTrace()
            return null
        }
    }


}

