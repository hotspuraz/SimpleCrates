package com.simplesurvival.crates.service.profile.meta

class CrateProfileMeta
{

    val keys: MutableMap<String, Int> = mutableMapOf()

    val createdAt: Long = System.currentTimeMillis()
}