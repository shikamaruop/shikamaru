package shikamaru.intern


import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * @Ahad
 *
 * http://burtbeckwith.com/blog/?p=1017
 *
 * ####### Debut d'une tentative d'explication de ce truc pas compris #########
 *
 * Voulais injecter un service dans un Domain Class. La maniere basique [def xxxService au debut de la classe] ne
 * m'arrange pas, car 'xxxService' serait considéré comme une des propriétés du Domain [via 'DomainInstance.properties'].
 * L'autre maniere est d'utiliser ApplicationHolder, fournie par Grails.
 *
 * Ex: ApplicationHolder.application.getMainContext.getBean("toolsService")
 *
 * Mais ApplicationHolder a été depreciée, car elle cause des blemes avec les methodes statics.
 * Donc le but de cette classe-ci est d'avoir le 'contexte' de l'application dans un environnement static.
 * Grace au 'contexte', on a accés à un service
 * Cette classe, grace à ApplicationContextAware, a acces au contexte.
 * Ensuite les methodes (getApplicationContext, getBean, getGrailsApplication ou getConfig) permettent de loader
 * ces elements de Grails et d'avoir accés à leurs methodes (i.e ApplicationContext, n'importe quel service,
 * GrailsApplication ou le fichier Config).
 * Il faut aussi la declarer dans les Ressources de Grails (fichier 'Resources' dans Configuration/Spring).
 *
 * Pour l'utiliser, faire juste:
 * ApplicationContextHolder.getBean("toolsService") par exemple
 */

@Singleton
class ApplicationContextHolder implements ApplicationContextAware {
    private ApplicationContext ctx

    void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext
    }

    static ApplicationContext getApplicationContext() {
        getInstance().ctx
    }


    static Object getBean(String name) {
        getApplicationContext().getBean(name)
    }

    static GrailsApplication getGrailsApplication() {
        getBean('grailsApplication')
    }

    static ConfigObject getConfig() {
        getGrailsApplication().config
    }
}
