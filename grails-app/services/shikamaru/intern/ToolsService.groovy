package shikamaru.intern

import org.apache.commons.lang.RandomStringUtils

class ToolsService {

    def transactional = false
    // Comme on n'utilise pas Hibernate, ni de datasource, il n'ya pas de TransactionManager
    // Donc les transactions doivent être false. Toujours

    /*
    Utilisée pour les Id de la bdd. Since neo4j n'a pas d'id autoincrementé,
    on utilise la date du jour à la milliseconde prés, puis on lui rajoute 2 caracteres
    alphanumerics aleatoires.
     */

    def setEntityId() {
        def now = Calendar.getInstance()?.format("yyyyMMddHHmmssSSS") + randomChar(2)
        return now
    }

    /*
     Methode recevant en param un chiffre x, et renvoie un string d'elements aleatoires, de
      longueur x
      */

    def randomChar(Integer length) {
        String charset = (('A'..'Z') + ('0'..'9')).join()
        String randomString = RandomStringUtils.random(length, charset.toCharArray())
        return randomString
    }


}

