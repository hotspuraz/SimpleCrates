package com.simplesurvival.crates.service.profile

import com.simplesurvival.lib.database.registry.DataRegistry
import java.util.UUID

class CrateProfileService : DataRegistry<CrateProfile>(CrateProfile::class.java)
{

    companion object
    {
        @JvmStatic
        val instance: CrateProfileService by lazy { CrateProfileService().also { it.register() } }
    }

    override fun onRegister()
    {
    }

    fun load(uuid: UUID): CrateProfile
    {

        var profile = read(uuid)

        if (profile != null)
            return profile

        profile = CrateProfile(uuid)

        insertAsync(profile) {}

        return profile
    }
}