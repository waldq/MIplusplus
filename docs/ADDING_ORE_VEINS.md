# ADDING ORE VEINS
To add an ore vein, you need to define the following fields in its JSON:
- 1: id - unique identificator of your vein, used in lang files to display your vein's name.
- 2: general vein data: 
  - type: the type you want you vein to be. You can check [here](../src/main/java/dev/waldq/mipp/worldgen/veins/veintypes/VeinTypes.java) for the currently existing types.
  - weight: value that controls how often your vein will spawn in comparison to other veins.
  - density: float value that controls how many blocks in your vein will actually be ores and/or rare blocks.
- 3: dimension data:
  - dimensions: a list of dimensions where you vein will spawn. Generally it's used in case biomes for you vein aren't defined.  
  - biomes: a list of [biome tags](https://minecraft.wiki/w/Biome_tag_(Java_Edition)) or [locations](https://minecraft.wiki/w/Biome).
- 4: ore weights:
  - a map with [block tags](https://minecraft.wiki/w/Block_tag_(Java_Edition)) or IDs as keys and their corresponing maps, consisting of ore blocks as keys and their weights as values. Generally, you can define any block or block tag to be replaced with any block that exists in registries. You can view ore/block's weight as a chance of it being spawned, just not limited by their maximum sum (well, unless you surpass integer's max value, which I hope you won't).
- 5: rare block weights: they work exactly like ore weights, except that they can replace any block defined in ore weights.
- 6: rare block chance: the chance of rare block being spawned instead of normal ore/block from ore weights.
- 7: bounds:
  - radiusX, radiusY, radiusZ: parameters that control direct size and some other parameters of your ore vein. You can look at your vein's corresponding code [here](../src/main/java/dev/waldq/mipp/worldgen/veins/veintypes/VeinTypes.java) for in-depth information. I also provide you with math functions that describe each vein type to understand their shape better. My suggestions are:
    - for stock vein type: radiusX and radiusZ bigger than radiusY;
    - for spiky vein type: relatively same numbers for each of the three parameters;
    - for tube vein type: radiusY (depth of the tube) bigger than radiusX and radiusZ;
    - for dike vein type: big radiusX (length of the dike), small radiusZ (thickness of the dike).
  - maxY and minY: maximum and minimum Y coordinate for your vein, it will never spawn highr than maxY or lower than minY. 
  - size: float scale factor, generally every radius is multiplied by it.
- 8: samples:
  - number: number of samples that will spawn in the center chunk of your vein on the surface.
  - sample weights: they work exaclty like ore weights and rare block weights, except that samples will spawn on the surface. I don't provide you with all the possible samples for the existing ores so you either have to create you own or use standard sample block.
- 9: enabled: true if you want to enable you vein.
After defining your vein's JSON, you can put it in KubeJS data folder in ore_veins folder for it to start working.
For your own convenience I suggest putting your custom veins under your modpack's namespace and editing MI++ veins under its namespace.
```json
{
  "id": "coal",
  "data": {
    "type": "stock",
    "weight": 8,
    "density": 0.1
  },
  "dimData": {
    "dimensions": [
      "minecraft:overworld"
    ],
    "biomes": [
      "#c:is_ocean"
    ]
  },
  "oreWeights": {
    "#mipp:stones": {
      "minecraft:coal_ore": 50,
      "modern_industrialization:lignite_coal_ore": 50
    },
    "minecraft:deepslate": {
      "minecraft:deepslate_coal_ore": 50,
      "modern_industrialization:deepslate_lignite_coal_ore": 50
    }
  },
  "rareBlocks": {
    "minecraft:coal_block": 1
  },
  "rareBlockChance": 0.01,
  "bounds": {
    "radiusX": 20,
    "radiusY": 7,
    "radiusZ": 21,
    "maxY": 70,
    "minY": 16,
    "size": 1.0
  },
  "samples": {
    "number": 6,
    "sampleWeights" : {
      "mipp:sample": 10
    }
  },
  "enabled": true
}
```
# EDITING EXISTING VEIN
Editing is really straightforward, you just follow same steps as with adding a vein, but the id of the edited vein has to exist somewhere in ore_veins folders.
