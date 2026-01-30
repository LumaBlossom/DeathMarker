package luma.blossom;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "deathmarker")
public class DeathMarkerConfig implements ConfigData {
    
    @ConfigEntry.Category("colors")
    @ConfigEntry.ColorPicker
    public int skullColor = 0xFF5555;
    
    @ConfigEntry.Category("colors")
    @ConfigEntry.ColorPicker
    public int timerColor = 0xFFFFFF;
    
    @ConfigEntry.Category("colors")
    @ConfigEntry.ColorPicker
    public int distanceColor = 0xCCCCCC;
    
    @ConfigEntry.Category("behavior")
    public int shrinkDistance = 25;
    
    @ConfigEntry.Category("behavior")
    public double clearDistance = 2.0;
}
