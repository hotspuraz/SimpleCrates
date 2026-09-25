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
    apiVersion = "26.3"
    softDepend = listOf("DecentHolograms", "CMI", "FancyHolograms", "PlaceholderAPI")

    commands {
        register("crates") {
            description = "Manage SimpleCrates crates and keys."
            usage = "/crates <editor|givecrate|givekey|takekey|keys|reload>"
        }
    }
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
    compileOnly("io.papermc.paper:paper-api:26.3.build.41-alpha")
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
    jvmToolchain(25)
}
