var projectDir = "../osv2020-u-versie-1.10.5.1/nl-sitzberechnung-osv-legacy-1.1.11-sources"

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
