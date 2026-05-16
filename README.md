# AutoLan

**Do you play with your friends often from your world, but don't want to open the world to LAN manually? This is the mod for you**

With AutoLan, your singleplayer world will automatically open to LAN whenever you open it, without any extra steps!

# A little video demonstration

[Watch the video](https://www.youtube.com/watch?v=dVND587-MtI)

# How to use
Just install the mod, and whenever you open a singleplayer world, AutoLan will start working automatically

## Configure the LAN port
By default, AutoLan keeps Minecraft's random available LAN port behavior.

To force a port for one launch, add this JVM system property:

```bash
-Dauto-lan.port=25565
```

To set a persistent port, create `config/auto-lan.properties` in your Minecraft game directory:

```properties
port=25565
```

The JVM system property takes precedence over the config file. If the configured value is invalid or the configured port cannot be opened, AutoLan falls back to a random available port.

## Suggested mod
I have another mod, [Force Port](https://modrinth.com/mod/forceport), which allows you to configure which port you want your LAN server to run on. This is useful if you want to run multiple servers at once, or for added security.

# Supported versions
AutoTorcher is available for Minecraft versions 1.21.10 and above!

Due to the frequency of Minecraft updates now, each Minecraft version has its own .jar file. This is mainly to prevent crashes from each minor update.
