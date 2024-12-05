plugins {
    application
    java
}

application {
    mainClass.set("me.seyfu_t.App")
}

dependencies {
    implementation(files("/usr/share/java/gson.jar"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.3")
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.register<Jar>("fatJar") {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
    from(sourceSets.main.get().output)

    // Include dependencies
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.test {
    useJUnitPlatform()
}

// tasks.register<Copy>("copyNativeLib") {
//     dependsOn(":native:assemble")
//     val nativeLib = when {
//         org.gradle.internal.os.OperatingSystem.current().isWindows -> "native/build/libs/ubigint_native.dll"
//         org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "native/build/libs/libubigint_native.dylib"
//         else -> "native/build/libs/libubigint_native.so"
//     }
//     from(nativeLib)
//     into("$buildDir/libs")
// }

// tasks.named<Jar>("fatJar") {
//     dependsOn("copyNativeLib")
//     from("$buildDir/libs") {
//         include("**/*")
//         into("libs") // Place native libs inside a 'libs' folder within the jar
//     }
// }


// Task to compile the C code
tasks.register<Exec>("compileC") {
    group = "build"
    description = "Compile the C code for JNI"

    val srcDir = file("src/main/jni") // Directory containing C source files
    val buildDir = file("$buildDir/native") // Output directory for compiled files

    inputs.dir(srcDir)
    outputs.dir(buildDir)

    // Ensure the output directory exists
    doFirst {
        if (!srcDir.exists()) {
            throw GradleException("Source directory $srcDir does not exist!")
        }
        buildDir.mkdirs()
        println("Compiling C code from $srcDir to $buildDir")
    }

    // Define the command line for gcc
    commandLine = listOf(
        "gcc", "-shared", "-fPIC", "-o", "${buildDir}/libnative.so",
        "-I", "${System.getProperty("java.home")}/include",
        "-I", "${System.getProperty("java.home")}/include/linux",
        "${srcDir}/native.c" // Replace with your C source file name
    )
}


// Clean up the compiled native code
tasks.register<Delete>("cleanC") {
    group = "build"
    description = "Clean compiled native code"

    delete("$buildDir/native")
}

// Ensure C code is compiled before building the JAR
tasks.named("build") {
    dependsOn("compileC")
}