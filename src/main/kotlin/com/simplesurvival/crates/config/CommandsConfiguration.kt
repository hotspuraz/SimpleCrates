package com.simplesurvival.crates.config

import com.simplesurvival.crates.SimpleCrates
import com.simplesurvival.lib.configuration.key.SimpleKey
import com.simplesurvival.lib.configuration.serializer.types.message.MessageWrapper
import com.simplesurvival.lib.configuration.yaml.YamlBuilder

object CommandsConfiguration : YamlBuilder<SimpleCrates>(SimpleCrates.plugin, "commands.yml")
{

    @field:SimpleKey(node = "commands.givekey.usage")
    var giveKeyUsage = MessageWrapper("&cUsage: /crates givekey <player> <keyId> [amount]")

    @field:SimpleKey(node = "commands.givekey.player-not-online")
    var giveKeyPlayerNotOnline = MessageWrapper("&cPlayer '&e%player%&c' is not online.")

    @field:SimpleKey(node = "commands.givekey.key-not-found")
    var giveKeyNotFound = MessageWrapper("&cKey '&e%key%&c' was not found.")

    @field:SimpleKey(node = "commands.givekey.success")
    var giveKeySuccess = MessageWrapper("&aSent &e%amount%&a key(s) '&e%key%&a' to &e%player%&a.")

    @field:SimpleKey(node = "commands.givekey.received")
    var receivedKey = MessageWrapper("&aYou received &e%amount% keys&a from &6%key%&a sent by &e%sender%&a.")

    @field:SimpleKey(node = "commands.givecrate.usage")
    var giveCrateUsage = MessageWrapper("&cUsage: /crates givecrate <player> <crateId> [amount]")

    @field:SimpleKey(node = "commands.givecrate.player-not-online")
    var giveCratePlayerNotOnline = MessageWrapper("&cPlayer '&e%player%&c' is not online.")

    @field:SimpleKey(node = "commands.givecrate.crate-not-found")
    var giveCrateNotFound = MessageWrapper("&cCrate '&e%crate%&c' was not found.")

    @field:SimpleKey(node = "commands.givecrate.success")
    var giveCrateSuccess = MessageWrapper("&aSent &e%amount%&a crate(s) '&e%crate%&a' to &e%player%&a.")

    @field:SimpleKey(node = "commands.takekey.usage")
    var takeKeyUsage = MessageWrapper("&cUsage: /crates takekey <player> <keyId> [amount]")

    @field:SimpleKey(node = "commands.takekey.player-not-online")
    var takeKeyPlayerNotOnline = MessageWrapper("&cPlayer '&e%player%&c' is not online.")

    @field:SimpleKey(node = "commands.takekey.player-has-no-key")
    var takeKeyPlayerHasNoKey = MessageWrapper("&e%player% does not have any '&f%key%&e' keys.")

    @field:SimpleKey(node = "commands.takekey.success")
    var takeKeySuccess = MessageWrapper(
        "&aRemoved &e%removed%&a '&e%key%&a' key(s) from &e%player%&a. Remaining: &e%remaining%&a."
    )

    @field:SimpleKey(node = "commands.keys.usage")
    var keysUsage = MessageWrapper("&cUsage: /crates keys <player> [keyId]")

    @field:SimpleKey(node = "commands.keys.player-not-online")
    var keysPlayerNotOnline = MessageWrapper("&cPlayer '&e%player%&c' is not online.")

    @field:SimpleKey(node = "commands.keys.total")
    var keysTotal = MessageWrapper("&a%player% has &e%amount%&a key(s) in total.")

    @field:SimpleKey(node = "commands.keys.specific")
    var keysSpecific = MessageWrapper("&a%player% has &e%amount%&a key(s) of '&e%key%&a'.")
}
