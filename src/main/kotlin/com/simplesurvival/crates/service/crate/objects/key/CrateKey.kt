package com.simplesurvival.crates.service.crate.objects.key

class CrateKey(
    var required: Boolean = true,
    var ids: MutableList<String> = mutableListOf()
)