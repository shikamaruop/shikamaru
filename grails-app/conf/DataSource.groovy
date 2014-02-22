environments {

    development {
        dataSource {
            jndiName = "java:comp/env/jdbc/shikamaru"
            dbCreate = "update" // one of 'create', 'create-drop','update'
        }
    }

    test {
        dataSource {
            jndiName = "java:comp/env/jdbc/shikamaru"
            dbCreate = "update" // one of 'create', 'create-drop','update'
        }
    }

    production {
        dataSource {
            jndiName = "java:comp/env/jdbc/shikamaru"
            dbCreate = "update" // one of 'create', 'create-drop','update'
        }
    }
}

hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}

/*
grails {
    neo4j {
        type = "rest"
        location = "http://localhost:7474/db/data/"
    }
}
*/