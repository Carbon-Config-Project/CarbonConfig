# How to expand the Functionality    

## Expanding Config    
Please check out the Carbon Library [Link](https://github.com/Carbon-Config-Project/CarbonConfigLib/blob/master/CUSTOM.md).   
If you want to add custom Config Entries    

## Adding Custom Gui Components     
Gui Components are added through [DataType](src/main/java/carbonconfiglib/gui/api/types/DataType.java).    
Which is simply a Factory Pattern registry.    
DataTypes require a Function that turns a IValueNode to a BaseElement.   
Which is in charge of rendering an element.    

But it is highly suggested you use ValueElement as a base, or if you need subnodes NodeElement.    
As critical functionality is implemented there that you would have to implemented as well.    

Note: If your BaseElement implementation spawns subnodes you need to implement IFolderNode    
	which just gives you access to a expand function for control you need and asks for the NodeName which is used for traversal.     
	(Mods can request to open sub nodes IFolderNode basically provides traversal data with NodeName)


### ValueElement implementation Details    
Anything that is extending BaseElement is split into 2 Sections.
- The left section:   
	Which is in charge of traversal and config names.
- The right side:    
	Which is the charge of visualizing the config entry.
	However you want.
- The Right border:     
	Technically the third section, but that you have 0 control over.
	Its in charge of standardized options. That will never change.    
	

Function List:    
- readValue:    
	Function that asks the implementation to read the "node" value and push it into the UI.     
- setEditable:    
	Components are by default inactive. So that you can't accidentally modify values.    
	This function simply tells you if your config components are enabled or not.   
- setRightComponentsVisible:    
	As the gui is split into 2 parts right side might be not visible as other layers are focused right now.    
	This function is basically telling your components if they are visible or not.    
	As the input system doesn't care if your components received draw calls or not.    
- renderLeftPart:    
	Only needed if you use NodeElement as a base.   
	This renders the name and traversal buttons if needed.    
- renderRightPart:    
	Renders the config elements that allow you to edit configs.
	The width parameter is the entire space that is accessible to you.
	The desiredWidth is the ensured size you will get no matter what.    
	Also it is a unified size to make components look decent.    
	Carbon Config itself have a few Optional Configurations inside a config that is rendered if width-desiredWidt is big enough to contain them.
- getChildNodes:    
	If your entry has child nodes this is the function you use.    
	Which returns a List of BaseElements.    
	Assuming you want to follow the implementation you call BaseElement#**createNode(INode)** which automatically finds the right type based on the child INodes you have.   

### Space Constraints
If you need more than 20 pixels of Height, technically 24 but padding is enforced in your component,     
just override the getItemHeight function.     
Carbon Configs ListView implementation is designed to handle Dynamic Heights, thanks to Chunk Pregen.    
Just remember to account for the 4 pixels used in the padding as these are auto subtracted and its deep enough where you won't be able to change it.   
(There are ways but you break other things trying it)


### How to make the Gui Component show up.    
Once you implemented your Gui Component and created a DataType with it we can go to the next step.   
The first thing you have to do is register your DataType using the registerType function in said class.   
Which requires a linking class that is used as identifier.    

Now its accessible through the gui but you still need a link.    
Assuming you either create a compound or have your own ConfigEntry implementation its not difficult to use.   

Links are created through IStructureData class.    
Which has all the metadata you need.    
Here are the calls you need to do for each IStructureData implementation:    
- SimpleData.variant(CUSTOM, MY_LINKING_CLASS): Custom field is required otherwise it will not use the link itself but the type provided by the EntryDataType
- ListBuilder.variant(CUSTOM, MY_LINKING_CLASS, ParseFunction, SerializeFunction): Same rule as SimpleData, but you need to provide a parsing and serializing function, but you should have them already.
- CompoundBuilder.variants("ID", CUSTOM, MY_LINKING_CLASS, ParseFunction, SerializeFunction): same as Simple and List, you just require key as its a Map Object.

Once your config entry has your variant registered in your configentry or compound object it should automatically show up.    


## Adding Custom Compound Screens
Compound Objects are nice. They allow you to simplify the parsing and combine objects that are connected to each other.   
But sadly Carbon Config by default doesn't give you a direct access to edit them as one.    
This is due to the base library not being directly attached to the gui.   

But there is a simple fix to this:    
The [CompoundType](src/main/java/carbonconfiglib/gui/api/types/CompoundType.java) class.    
Using the registerStandardType(LINKING_CLASS, CreatorFunction) function you can create a dedicated screen for your compound object.

Accessing this is also trivial.    
When creating your compound simply call CompoundBuilder#**addSettings(new CompoundOverride(LINKING_CLASS));   
That will automatically find your screen provider and will open it.    
You need to provide your own way to deal with the unserialized information but its not that bad. Lookup: what Compound#**registerWidgetAligner** does.    
