plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "SimpleCrates"

include(":SimpleLib")
project(":SimpleLib").projectDir = file("C:/Projetos/Evo/SimpleLib")