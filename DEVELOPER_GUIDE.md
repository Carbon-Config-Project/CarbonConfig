# Developer Guide

This is a basic Develoer guide that provides you with the essentials you need to implement this mod if wanted.

## Basic Config

How to implement the basic config:

```java
public class ExampleMod
{
	public static ConfigHandler CONFIG;
	public static BoolValue EXAMPLE_FLAG;
	public static BoolValue EXAMPLE_SUGGESTION;
	
	public ExampleMod() {
		Config config = new Config("example");
		ConfigSection section = config.add("general");
		EXAMPLE_FLAG = section.addBool("example_key", false);
		EXAMPLE_SUGGESTION = section.addBool("suggestion example", false).addSuggestion("True", true).addSuggestion("False", false).addSuggestion("Random", new Random().nextBoolean());
		CONFIG = CarbonConfig.CONFIGS.createConfig(config);
		CONFIG.register();
	}
	
	public static void setFlag(boolean value) {
		EXAMPLE_FLAG.set(value);
		CONFIG.save();
	}
}
```
Relevant notes:    
- The Config Handler has to be loaded during the Mod Loading Phase. (You don't have to register it just create the instance) to assosiate the Config to the ModId.    
- Register function    
- Config Sections/ConfigEntries can be added after the register function.   
- Saving is done via the ConfigHandler since the Config.class itself doesn't handle serialization.    
- Suggestions are only visible inside the gui, and if it is not an array element are forced values to pick inside the gui. With arrays they are basically templates people can pick

## Config Ranges / Filters

If a Number Range / String validator is required this is how it is done.

```java
	public ExampleMod() {
		Config config = new Config("example");
		ConfigSection section = config.add("general");
		section.addInt("Number Example", 0, "Comment").setMin(0).setMax(24).setRange(0, 24);
		section.addString("String example", "Testing.", "Comment").withFilter(T -> T.contains("."));
	}
```
Relevant Notes:    
- setMin/Max are single options if only the upper/lower bound should be set. SetRange is for both.
- Double.MIN_VALUE != Integer.MIN_VALUE. Double.MIN_VALUE is 0.0000000000000000000000001 while Integer.MIN_VALUE is -2147483648. Use -Double.MAX_VALUE instead
- withFilter should work not just once per reload of the config because for the gui the entries are copied and tested against. (Also multiple threads will call this function)


## World Reload / Game Restart

How to identify that a config has a specific limitation to take effect.    

```java
	public ExampleMod() {
		Config config = new Config("example");
		ConfigSection section = config.add("general");
		EXAMPLE_FLAG = section.addBool("example_key", false).setRequiredReload(ReloadMode.GAME);
	}
```
Relevant notes:    
- ReloadMode is a enum and only detected by the gui.    
- Since the API uses a Interface custom implementations can be provided but will not be detected by the gui on its own so you have to provide the implementation to that too.

## Per World Configs?

How to implement PerWorld Configs?

```java
	public ExampleMod() {
		Config config = new Config("example");
		ConfigSection section = config.add("general");
		EXAMPLE_FLAG = section.addBool("example_key", false);
		CONFIG = CarbonConfig.CONFIGS.createConfig(config, CarbonConfig.getPerWorldProxy());
		CONFIG.register();
	}
```
Proxies provide the relevant folder information when/where configs load.    
If a proxy is not a PerWorldProxy but a dynamic proxy note that the "CONFIG.load()/CONFIG.unload()" function has to be implemented by yourself. Where it should be called specifically.    
(Also proxies work like a stack)    

## Synced Configs

How do i make synced Configs?

```java
public class ExampleMod
{
	public static BoolValue SERVER_TO_CLIENT_EXAMPLE;
	public static SyncedConfig<BoolValue> CLIENT_TO_SERVER_EXAMPLE;
	
	public ExampleMod() {
		Config config = new Config("example");
		ConfigSection section = config.add("general");
		SERVER_TO_CLIENT_EXAMPLE = section.addBool("Server Variable", false).setServerSynced();
		CLIENT_TO_SERVER_EXAMPLE = section.addBool("Client Variable", false).setClientSynced();
		CONFIG = CarbonConfig.CONFIGS.createConfig(config);
		CONFIG.register();
	}
	
	public static boolean getClientVariable(Player player) {
		return CLIENT_TO_SERVER_EXAMPLE.get(player.getUUID()).get();
	}
}
```
Relevant notes:    
- Server to Client configs sync automatically as long the "AutomationType.AUTO_SYNC" is set, which it is by default.
- Client to Server configs have to be SyncedConfigs because a Map<UUID, ConfigEntry> has to be established somehow. 
- If a Player for whatever reason doesn't have a client value then the it defaults to the server value. So keep that in mind.


## Parsed / Compound Values

How to do custom Data types?   
Please Check out this [Example](src/test/java/carbonconfiglib/examples/ParsedValueConfigExample.java)    

## Set Configs

What if i have an array i just want to test against?    

```java
public class ExampleMod
{
	public static ConfigHandler CONFIG;
	public static HashSetCache<String> SET_EXAMPLE;
	
	public ExampleMod() {
		Config config = new Config("example");
		ConfigSection section = config.add("general");
		CONFIG = CarbonConfig.CONFIGS.createConfig(config);
		SET_EXAMPLE = HashSetCache.create(CONFIG, section.addArray("example_key", new String[]{"minecraft:diamond", "minecraft:dirt"}));
		CONFIG.register();
	}
}
```
Relevant Notes:    
- The HashSetCache needs the ConfigHandler because it registers a reload Listener so it automatically updates.
- Automatically updates.
- Is readOnly. No setting of values inside unless you manually set it and update it manually?
- There is also a MappedConfig as an option that works very similar.


## How to set SubFolder / Config Types

To Set the SubFolder / Config Type you simply need ConfigSettings.

```java
	public ExampleMod() {
		Config config = new Config("example");
		ConfigSection section = config.add("general");
		EXAMPLE_FLAG = section.addBool("example_key", false);
		CONFIG = CarbonConfig.CONFIGS.createConfig(config, ConfigSettings.of().withType(ConfigType.SHARED).withSubFolder("mySubFolder"));
		CONFIG.register();
	}
```

## Reload Listeners.

If you want to detect config changes after a reload happened this can be easily done via the config handler.

```java
	public ExampleMod() {
		Config config = new Config("example");
		ConfigSection section = config.add("general");
		EXAMPLE_FLAG = section.addBool("example_key", false);
		CONFIG = CarbonConfig.CONFIGS.createConfig(config);
		CONFIG.addLoadedListener(() -> {/** My Callback **/});
		CONFIG.register();
	}
```
Relevant notes:    
- Callback's don't have to be added before the "CONFIG.register()" but you can not remove them.    
- Callback's will also be triggered during a "ConfigLoad" so the initial load is handled by them too. 

## Registry Entries    

No example provided but some notes    
If modded Entries should be supported then make sure the "Config" is loaded AFTER "FMLCommonSetupEvent" event because otherwise modded entries get seen as invalid.    
This is a thing with any Config Library.   
The PerWorldProxy automatically makes sure it loads during ServerStart and defaults are created during the "FMLCommonSetupEvent".   
(Defaults being that it saves the config once during startup if not present to ensure a config was created in the "default configs" folder without creating a world)     
