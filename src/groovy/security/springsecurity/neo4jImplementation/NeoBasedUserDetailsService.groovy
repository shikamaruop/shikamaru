package security.springsecurity.neo4jImplementation

import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.plugin.springsecurity.userdetails.GrailsUserDetailsService
import grails.plugin.springsecurity.userdetails.NoStackUsernameNotFoundException
import groovy.sql.Sql
import groovy.util.logging.Log4j
import org.springframework.dao.DataAccessException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException

import javax.naming.Context
import java.sql.PreparedStatement

/**
 * @author: hyoga
 *
 * Classe de load des Users.
 Utilisée par SpringSecurity. Mais non basée sur Neo4j, mais plutôt sur GORM originalement.
 Implementation reprenant la méthode "loadUserByUsername", mais en la readaptant à Neo4j.
 I.e, utilisant Cypher.

 Divisée en 3 parties:
 1. Retrouve un User par son username.
 2. Charge les Roles de ce User.
 3. Crée un UserDetails = new User(String username, String password, Boolean isEnabled, Boolean isNotExpiredAccount,
 Boolean isNotPasswordExpired, Boolean isNotAccountLocked, List RoleList).
 Grails utilise une classe 'GrailsUser' heritant de UserDetails. Il lui a juste ajouté l'id du user
 */

/*
La mêthode principale "loadUserByUsername" renvoie un UserDetails: classe principale des Users de SpringSecurity.
Constructeur: UserDetails(username, password, enabled, !accountExpired, !passwordExpired,
				!accountLocked, authorities, user.id)
 */

@Log4j
class NeoBasedUserDetailsService implements GrailsUserDetailsService {

    Context ctx = new javax.naming.InitialContext()
    def conn = ctx.lookup("java:comp/env/jdbc/shikamaru")
    def sql = new Sql(conn)

    UserDetails loadUserByUsername(String username, boolean loadRoles)
    throws UsernameNotFoundException {
        return loadUserByUsername(username)
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        //on recherche un User par son username
        def userLoaded = findUserByUsername(username, ['entityId'])
        if (!userLoaded || userLoaded?.isEmpty()) {
            log.warn "User not found: $username"
            throw new NoStackUsernameNotFoundException()
        }

        // On recupere ses roles
        Collection<GrantedAuthority> roleList = loadAuthorities(username)
        log.debug("roleList= " + roleList)
        //On crée une instance de UserDetails
        def user = createUserDetails(username, roleList)
        return user
    }

    /*Methode de recuperation des Roles pour un username (identifié par son username)
    Renvoie une NO_ROLE si aucun role trouvé
    */

    protected Collection<GrantedAuthority> loadAuthorities(String username) {
        try {
            def roleLookupRequest = "MATCH (u:User) - [:HAS_ROLE] -> (r:Role) WHERE u.username = {0} " +
                    "RETURN r.authority"
            def roleResults = new ArrayList<GrantedAuthority>()
            sql.cacheConnection { conn ->
                PreparedStatement stmt = conn?.prepareStatement(roleLookupRequest);
                stmt.setObject(0, username)
                def exec = stmt.executeQuery()
                while (exec.next()) {
//                    log.info("exec= " + exec?.toRowResult())  //Affiche la ligne entiere
                    roleResults?.add(new SimpleGrantedAuthority(exec?.getObject("r.authority")))
                }
            }
            if (roleResults?.isEmpty()) {
                roleResults?.add(new SimpleGrantedAuthority('ROLE_NO_ROLES'))
            }
            return roleResults
        } catch (Exception e) {
            log.error("loadAuthorities error= " + e.getMessage())
            e.printStackTrace()
            return ['ROLE_NO_ROLES']
        }
    }

    protected def findUserByUsername(String username, List results) {
        try {
            // Creation de la requete
            def cypherRequest = "MATCH (a:User) WHERE a.username = {0} "

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

            def rsltId = []
            sql.cacheConnection { conn ->
                PreparedStatement stmt = conn?.prepareStatement(cypherRequest);
                stmt.setObject(0, username)
                def exec = stmt.executeQuery()
                while (exec.next()) {
//                    log.info("findUserByUsername exec= " + exec?.toRowResult())  //Affiche la ligne entiere
                    rsltId = exec?.toRowResult()
                }
            }
            return rsltId

        } catch (Exception e) {
            e.printStackTrace()
            return null
        }
    }

    /*
    Méthode de creation de createUserDetails
     */

    protected UserDetails createUserDetails(String username, Collection<GrantedAuthority> authorities) {
        try {
            def userLoaded = findUserByUsername(username, ['password',
                    'enabled', 'accountExpired', 'passwordExpired', 'accountLocked', 'entityId'])

            //Creation d'un UserDetails
            //new GrailsUser(username, password, enabled, !accountExpired, !passwordExpired, !accountLocked, authorities, user.id)
            boolean enabled = userLoaded[1]
            boolean accountExpired = userLoaded[2]
            boolean passwordExpired = userLoaded[3]
            boolean accountLocked = userLoaded[4]

            GrailsUser user = new GrailsUser(username, userLoaded[0], enabled, !accountExpired, !passwordExpired,
                    !accountLocked, authorities, userLoaded[5])

            return user
        } catch (Exception e) {
            e.printStackTrace()
        }
    }
}
