package com.simplesurvival.crates.service.key

import com.simplesurvival.crates.service.crate.Crate
import com.simplesurvival.crates.service.crate.CrateService
import com.simplesurvival.lib.configuration.serializer.types.item.SimpleItemBuilder
import java.util.UUID

class Key(
    val uuid: UUID,
    val identifier: String,
    var item: SimpleItemBuilder,
    var enabled: Boolean = true,
    var virtual: Boolean = true,
    var glowing: Boolean = false,
)
{

    fun update()
    {
        KeyService.updateKey(this)
    }

    fun delete() = KeyService.deleteKey(identifier)

    fun clone() = KeyService.cloneKey(identifier)

    fun linkedCrates(): List<Crate>
    {
        return CrateService.crates.values.filter { crate -> crate.key.ids.contains(identifier.lowercase()) }
    }

    fun unlinkedCrates(): List<Crate>
    {
        return CrateService.crates.values.filter { crate -> !crate.key.ids.contains(identifier.lowercase()) }
    }

    fun totalLinkedCrates() = linkedCrates().size
}
