plugins {
    application
    id("me.champeau.jmh") version "0.7.2"
}

application {
    mainClass.set("me.seyfu_t.App")
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation(files("/usr/share/java/gson.jar"))
    // implementation(files("/usr/share/java/guava.jar"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.3") // JUnit Jupiter API
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.3") // JUnit Jupiter Engine

}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter("5.10.3")
        }
    }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

jmh {
    benchmarkMode.set(listOf("thrpt", "ss"))
    fork.set(1)
    iterations.set(3)
    warmupIterations.set(2)
}

tasks.register<Jar>("fatJar") {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
    from(sourceSets.main.get().output)

    // Include dependencies
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // Handle duplicates
}

// Task to run tests
tasks.test {
    useJUnitPlatform() // Enable JUnit Platform for the test task
}