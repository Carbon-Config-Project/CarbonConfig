# Resource Pack Features
Carbon Config comes with resource pack overrides in play.   
Some configs may not be interpreted properly or you have extra data accessible in forge configs that you can't add via code.   
This featureset allows you to do some modifications to all configs.   


## Quick life example
Here is a quick example on what this can look like

```json

{
	"theoneprobe-client": {
		"tooltipScale": {
			"id": "carbonconfig:slider",
			"stepSize": 0.01
		},
		"showHarvestLevel": {
			"id": "carbonconfig:force_selection",
			"selection": [
				{ "name": "Not", "value": "NOT" },
				{ "name": "Always", "value": "EXTENDED" },
				{ "name": "Sneak", "value": "NORMAL" }
			]
		}
	}
}

```java

## Format explained
Carbon Config looks for json files in the following directory:    
- assets/modId/carbonoverrides/anything.json    

The modId decides which mod the override is attached to.    
This makes it simpler for everyone.   
The Json Structure is also like this:    

```json
{
	"configFileNameWithoutExtension": {
		"configFolder": {
			"configEntry": {
				"id": "OverideId"
				//Settings
			}
		},
		"configEntry": [
			{
				"id": "OverideId"
				//Settings
			}
		]
	}
}

```
The structure is effectively like the config file itself.     
We simply define the file, the path and then simple objects or array.    
You can apply multiple overrides on to one entry as long the overrides are an array.    

## Override Types

Here is a list of Overrides you can use:    
- "carbonconfig:translation_key": (Carbon Config only)    
	Allows you to define translation keys into a config.   
	Json: { "id": "carbonconfig:translation_key", "key": "translationString" }   
- "carbonconfig:translation_comment": (Carbon Config only)    
	Allows you to define translation keys for comments into a config.   
	Json: { "id": "carbonconfig:translation_comment", "comment": "translationString" }   
- "carbonconfig:color_type":    
	Allows you to define if a color element has alpha or not   
	Json: { "id": "carbonconfig:color_type", "hasAlpha": true }   
- "carbonconfig:slider":    
	Allows you to turn a double/float text field into a Slider   
	Json: { "id": "carbonconfig:slider", "stepSize": 0.01 }   
- "carbonconfig:force_mode":    
	Allows you to force a text or non text mode for config elements (not all are accessible)   
	Json: { "id": "carbonconfig:force_mode", "forceText": false }   
- "carbonconfig:force_selection":    
	Allows you to convert config entries into a Selection List.   
	The one probe for example didn't fix its enums being strings after forge introduced enums in 1.19.2   
	This can be fixed.   
	Json: {   
		"id": "carbonconfig:force_selection",    
		"selection": [   
			{   
				"name": "displayName",   
				"value": "configvalue"   
			}   
		]   
	}