var projectDir = "../osv2020-u-versie-1.12.7.2/nl-sitzberechnung-osv-legacy-1.6.0"

sourceSets {
    main {
        java {
            setSrcDirs(listOf(projectDir))
        }
        resources {
            setSrcDirs(listOf(projectDir))
        }
    }
}
