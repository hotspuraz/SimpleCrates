package com.simplesurvival.crates.service.profile

import com.simplesurvival.crates.service.profile.meta.CrateProfileMeta
import com.simplesurvival.lib.SimpleLib
import com.simplesurvival.lib.database.registry.Column
import com.simplesurvival.lib.database.registry.Data
import java.sql.ResultSet
import java.util.UUID
import java.util.logging.Level

class CrateProfile : Data<CrateProfile>
{

    var meta: CrateProfileMeta = CrateProfileMeta()

    constructor() : super()

    constructor(uniqueId: UUID) : super(uniqueId)

    override fun table(): String = "crate_profiles"

    override fun defaultColumns(): Set<Column> = setOf(
        Column("meta", Column.Type.LONGTEXT, SimpleLib.getGson().toJson(meta))
    )

    override fun loadResultFromSet(rs: ResultSet): CrateProfile
    {

        try
        {
            this.meta = SimpleLib.getGson().fromJson(rs.getString("meta"), CrateProfileMeta::class.java)
        } catch (ex: Exception)
        {
            SimpleLib.getLogger().log(Level.SEVERE, "Could not load crate profile: '$uniqueId'", ex)
        }

        return this
    }

    fun save() = CrateProfileService.instance.updateAsync(this) {}

    // Meta

    fun createdAt() = meta.createdAt

    // Keys

    fun keys() = meta.keys

    fun getKey(key: String): Int = keys()[key.lowercase()] ?: 0

    fun addKey(key: String)
    {
        val k = key.lowercase()

        keys()[k] = (keys()[k] ?: 0) + 1
        save()
    }

    fun removeKey(key: String, amount: Int? = null)
    {
        val k = key.lowercase()

        if (amount == null)
        {
            keys().remove(k)
            save()
            return
        }

        if (amount <= 0) return

        val current = keys()[k] ?: return

        if (amount >= current)
        {
            keys().remove(k)
        } else
        {
            keys()[k] = current - amount
        }

        save()
    }

}