plugins {
    kotlin("jvm") version "2.3.10"

    id("net.minecrell.plugin-yml.bukkit") version ("0.6.0")
    id("com.gradleup.shadow") version ("9.0.0")
}

group = "com.simplesurvival.crates"
version = "1.0.4"

bukkit {
    name = "SimpleCrates"
    author = "Cássio Martim"
    website = "cassiomartim.com"
    version = project.version.toString()
    main = "com.simplesurvival.crates.SimpleCrates"
    apiVersion = "1.21"
    softDepend = listOf("DecentHolograms", "CMI", "FancyHolograms", "PlaceholderAPI")
}

repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        name = "placeholderapi"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("dev.jorel:commandapi-paper-core:11.2.0")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.9.9")

    implementation(project(":SimpleLib"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks.shadowJar {
    archiveBaseName.set("SimpleCrates")
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("")

    relocate("com.simplesurvival.lib", "com.simple.crates.libs.simple")
}

tasks.register<Copy>("deploy") {
    dependsOn(tasks.shadowJar)

    val pluginFolder = "$rootDir/server/plugins/"

    from(tasks.shadowJar.get().archiveFile)
    into(pluginFolder)

    doFirst {
        logger.lifecycle("Sending the JAR file to '$pluginFolder'...")
    }
}

kotlin {
    jvmToolchain(21)
}
