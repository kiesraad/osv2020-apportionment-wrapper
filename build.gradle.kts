plugins {
    id("java")
}

group = "nl.kiesraad"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    dependencies {
        implementation("org.slf4j:slf4j-api:2.0.17")
    }
}

tasks.register<Copy>("collectDependencies") {
    description = "Collects all dependencies into a single directory"
    group = "build"

    // Make this task depend on the build task
    dependsOn(tasks.named("build"))
    dependsOn(subprojects.map { it.tasks.named("build") })

    val outputDir = "${rootProject.layout.buildDirectory}/classpath"

    // Delete any existing output directory first
    doFirst {
        delete(outputDir)
    }

    // For each subproject
    subprojects.forEach { subproject ->
        // Include the project's JAR file
        from(subproject.tasks.named("jar"))

        // Include all dependencies of the project
        from(subproject.configurations.named("runtimeClasspath"))
    }

    // Copy all files to the output directory
    into(outputDir)

    doLast {
        val classPathFiles = fileTree(outputDir).files
        val classPathString = classPathFiles.joinToString(File.pathSeparator)
        logger.lifecycle("Classpath for all subprojects: ${outputDir}")
        logger.lifecycle("Use with: java -Djava.class.path=\"${classPathString}\" ...")

        // Create a script to set the classpath
        file("${outputDir}/classpath.sh").writeText("#!/bin/sh\nexport CLASSPATH=\"${classPathString}\"\n")
        file("${outputDir}/classpath.bat").writeText("@echo off\nset CLASSPATH=${classPathString}\n")
    }
}
