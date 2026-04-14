plugins {
    application
}

group = "nl.kiesraad.osv2020_apportionment_wrapper"
version = "1.0-SNAPSHOT"

application {
    mainClass = "nl.kiesraad.osv2020_apportionment_wrapper.Main"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":nl-sitzberechnung-osv-legacy"))
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("org.slf4j:slf4j-api:2.0.17")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
