import shikamaru.intern.ApplicationContextHolder


// Place your Spring DSL code here
beans = {
    applicationContextHolder(ApplicationContextHolder) { bean ->
        bean.factoryMethod = 'getInstance'
    }

    userDetailsService(security.springsecurity.neo4jImplementation.NeoBasedUserDetailsService)
}
