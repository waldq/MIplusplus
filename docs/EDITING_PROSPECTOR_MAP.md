# EDITING PROSPECTOR MAP
In order to correctly display found ores in its map, Electric Prospector requires you to define a color and/or a replacement in the ore_color data map:
- ore tag or block that you want to display. Every variation of the ore (e.g. gold, deepslate gold, nether gold) will use the color for its corresponding tag, if provided.
  - color that you want your ore to be displayed with
  - replacement that you want to replace found ore's name with. It's necessary for the tag and optional for the block (block's name will be used directly if replacement is not defined, see example).

In the example below, both the lead ore and its deepslate variant will be displayed as the Lead Ore on the prospector map and colored the same, but if "#c:ores/lead" ever gets another variant (for example, nether lead ore), you will have to define its color separately, if you don't want it to be gray on the map.  
For your own convenience I suggest you to define colors for ore tags rather than blocks.   
Also worth mentioning that Electric Prospector only checks for blocks with "#c:ores" tag, so coloring other blocks is meaningless.
```json
{
  "values": {
    "#c:ores/antimony": {
      "color": "DCDCF0",
      "mainBlock": "modern_industrialization:antimony_ore"
    },
    "modern_industrialization:deepslate_lead_ore": {
      "color": "6A76BC",
      "mainBlock": "modern_industrialization:lead_ore"
    },
    "minecraft:lead_ore": {
      "color": "6A76BC"
    }
  }
}
```
After defining colors and replacements, you can put your `ore_color.json` file in the `data/mipp/data_maps/block` in KubeJS folder for the data map to start working.
