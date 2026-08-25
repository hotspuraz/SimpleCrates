package com.simplesurvival.crates.service.crate.objects.engine

enum class CrateEngineType(val identifier: String)
{
    VANILLA_BLOCK("Vanilla Block"),
    VANILLA_MODEL("Vanilla Model");

    fun next(): CrateEngineType = entries[(ordinal + 1) % entries.size]
}