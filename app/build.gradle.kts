plugins {
    application
    `java-library`
    id("org.beryx.jlink") version "3.1.1"
    id("org.javamodularity.moduleplugin") version "1.8.15"
}

application {
    mainClass.set("me.seyfu_t.App")
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // implementation(files("/usr/share/java/gson.jar"))
    // implementation(files("libs/guava.jar"))
    implementation("com.google.code.gson:gson:2.11.0")
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
        languageVersion = JavaLanguageVersion.of(17)
    }
    modularity.inferModulePath.set(true)
}

jlink {
    imageName.set("custom-runtime")
    customImage {
        appModules = listOf("com.google.gson")
    }
    launcher {
        name = "kauma"
        // jvmArgs.addAll("-Xmx512m") // Optional: Specify JVM arguments
    }
    // addExtraModulePath("/usr/share/java")
    addExtraDependencies("com.google")
    // options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    // mergedModule {
    //     requires("java.base")
    //     requires("gson")
    // }
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

tasks.compileJava {
    options.javaModuleVersion = provider { "17" }
    // options.compilerArgs.addAll(listOf(
    //     // "--module-path", "/usr/share/java",
    //     "--add-modules", "gson",
    // ))
}
