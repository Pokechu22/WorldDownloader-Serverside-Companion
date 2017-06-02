# WDLCompanion / World Downloader API

Companion bukkit plugin for [the World Downloader mod](https://github.com/Pokechu22/WorldDownloader) that gives control over what can be downloaded.

If this plugin is not installed, World Downloader assumes that it can download everything; you can use this plugin if you want to limit WDL's capabilities.

[SpigotMC entry](https://www.spigotmc.org/resources/world-downloader-api-wdlcompanion.19950/) | [BukkitDev entry](http://dev.bukkit.org/bukkit-plugins/wdl-companion/)

[Protocol documentation](http://wiki.vg/Plugin_channels/World_downloader)

# Building

First, compile the log4j stubs:

```bash
$ cd log4jstub
$ mvn clean install
$ cd ..
```

Then, compile the plugin itself:

```
$ mvn clean install
```