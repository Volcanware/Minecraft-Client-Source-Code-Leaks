package net.optifine;

import java.util.Properties;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.IRandomEntity;
import net.optifine.RandomEntities;
import net.optifine.RandomEntity;
import net.optifine.config.ConnectedParser;
import net.optifine.config.Matches;
import net.optifine.config.NbtTagValue;
import net.optifine.config.RangeInt;
import net.optifine.config.RangeListInt;
import net.optifine.config.VillagerProfession;
import net.optifine.config.Weather;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorField;
import net.optifine.util.ArrayUtils;
import net.optifine.util.MathUtils;

public class RandomEntityRule {
    private String pathProps = null;
    private ResourceLocation baseResLoc = null;
    private int index;
    private int[] textures = null;
    private ResourceLocation[] resourceLocations = null;
    private int[] weights = null;
    private BiomeGenBase[] biomes = null;
    private RangeListInt heights = null;
    private RangeListInt healthRange = null;
    private boolean healthPercent = false;
    private NbtTagValue nbtName = null;
    public int[] sumWeights = null;
    public int sumAllWeights = 1;
    private VillagerProfession[] professions = null;
    private EnumDyeColor[] collarColors = null;
    private Boolean baby = null;
    private RangeListInt moonPhases = null;
    private RangeListInt dayTimes = null;
    private Weather[] weatherList = null;

    public RandomEntityRule(Properties props, String pathProps, ResourceLocation baseResLoc, int index, String valTextures, ConnectedParser cp) {
        String s;
        this.pathProps = pathProps;
        this.baseResLoc = baseResLoc;
        this.index = index;
        this.textures = cp.parseIntList(valTextures);
        this.weights = cp.parseIntList(props.getProperty("weights." + index));
        this.biomes = cp.parseBiomes(props.getProperty("biomes." + index));
        this.heights = cp.parseRangeListInt(props.getProperty("heights." + index));
        if (this.heights == null) {
            this.heights = this.parseMinMaxHeight(props, index);
        }
        if ((s = props.getProperty("health." + index)) != null) {
            this.healthPercent = s.contains((CharSequence)"%");
            s = s.replace((CharSequence)"%", (CharSequence)"");
            this.healthRange = cp.parseRangeListInt(s);
        }
        this.nbtName = cp.parseNbtTagValue("name", props.getProperty("name." + index));
        this.professions = cp.parseProfessions(props.getProperty("professions." + index));
        this.collarColors = cp.parseDyeColors(props.getProperty("collarColors." + index), "collar color", ConnectedParser.DYE_COLORS_INVALID);
        this.baby = cp.parseBooleanObject(props.getProperty("baby." + index));
        this.moonPhases = cp.parseRangeListInt(props.getProperty("moonPhase." + index));
        this.dayTimes = cp.parseRangeListInt(props.getProperty("dayTime." + index));
        this.weatherList = cp.parseWeather(props.getProperty("weather." + index), "weather." + index, (Weather[])null);
    }

    private RangeListInt parseMinMaxHeight(Properties props, int index) {
        String s = props.getProperty("minHeight." + index);
        String s1 = props.getProperty("maxHeight." + index);
        if (s == null && s1 == null) {
            return null;
        }
        int i = 0;
        if (s != null && (i = Config.parseInt((String)s, (int)-1)) < 0) {
            Config.warn((String)("Invalid minHeight: " + s));
            return null;
        }
        int j = 256;
        if (s1 != null && (j = Config.parseInt((String)s1, (int)-1)) < 0) {
            Config.warn((String)("Invalid maxHeight: " + s1));
            return null;
        }
        if (j < 0) {
            Config.warn((String)("Invalid minHeight, maxHeight: " + s + ", " + s1));
            return null;
        }
        RangeListInt rangelistint = new RangeListInt();
        rangelistint.addRange(new RangeInt(i, j));
        return rangelistint;
    }

    public boolean isValid(String path) {
        if (this.textures != null && this.textures.length != 0) {
            if (this.resourceLocations != null) {
                return true;
            }
            this.resourceLocations = new ResourceLocation[this.textures.length];
            boolean flag = this.pathProps.startsWith("mcpatcher/mob/");
            ResourceLocation resourcelocation = RandomEntities.getLocationRandom((ResourceLocation)this.baseResLoc, (boolean)flag);
            if (resourcelocation == null) {
                Config.warn((String)("Invalid path: " + this.baseResLoc.getResourcePath()));
                return false;
            }
            for (int i = 0; i < this.resourceLocations.length; ++i) {
                int j = this.textures[i];
                if (j <= 1) {
                    this.resourceLocations[i] = this.baseResLoc;
                    continue;
                }
                ResourceLocation resourcelocation1 = RandomEntities.getLocationIndexed((ResourceLocation)resourcelocation, (int)j);
                if (resourcelocation1 == null) {
                    Config.warn((String)("Invalid path: " + this.baseResLoc.getResourcePath()));
                    return false;
                }
                if (!Config.hasResource((ResourceLocation)resourcelocation1)) {
                    Config.warn((String)("Texture not found: " + resourcelocation1.getResourcePath()));
                    return false;
                }
                this.resourceLocations[i] = resourcelocation1;
            }
            if (this.weights != null) {
                if (this.weights.length > this.resourceLocations.length) {
                    Config.warn((String)("More weights defined than skins, trimming weights: " + path));
                    int[] aint = new int[this.resourceLocations.length];
                    System.arraycopy((Object)this.weights, (int)0, (Object)aint, (int)0, (int)aint.length);
                    this.weights = aint;
                }
                if (this.weights.length < this.resourceLocations.length) {
                    Config.warn((String)("Less weights defined than skins, expanding weights: " + path));
                    int[] aint1 = new int[this.resourceLocations.length];
                    System.arraycopy((Object)this.weights, (int)0, (Object)aint1, (int)0, (int)this.weights.length);
                    int l = MathUtils.getAverage((int[])this.weights);
                    for (int j1 = this.weights.length; j1 < aint1.length; ++j1) {
                        aint1[j1] = l;
                    }
                    this.weights = aint1;
                }
                this.sumWeights = new int[this.weights.length];
                int k = 0;
                for (int i1 = 0; i1 < this.weights.length; ++i1) {
                    if (this.weights[i1] < 0) {
                        Config.warn((String)("Invalid weight: " + this.weights[i1]));
                        return false;
                    }
                    this.sumWeights[i1] = k += this.weights[i1];
                }
                this.sumAllWeights = k;
                if (this.sumAllWeights <= 0) {
                    Config.warn((String)("Invalid sum of all weights: " + k));
                    this.sumAllWeights = 1;
                }
            }
            if (this.professions == ConnectedParser.PROFESSIONS_INVALID) {
                Config.warn((String)("Invalid professions or careers: " + path));
                return false;
            }
            if (this.collarColors == ConnectedParser.DYE_COLORS_INVALID) {
                Config.warn((String)("Invalid collar colors: " + path));
                return false;
            }
            return true;
        }
        Config.warn((String)("Invalid skins for rule: " + this.index));
        return false;
    }

    public boolean matches(IRandomEntity randomEntity) {
        Weather weather;
        WorldClient world2;
        int k1;
        WorldClient world1;
        int j1;
        WorldClient world;
        EntityLiving entityliving;
        RandomEntity randomentity2;
        Entity entity2;
        RandomEntity randomentity1;
        Entity entity1;
        RandomEntity randomentity;
        Entity entity;
        String s;
        BlockPos blockpos;
        if (this.biomes != null && !Matches.biome((BiomeGenBase)randomEntity.getSpawnBiome(), (BiomeGenBase[])this.biomes)) {
            return false;
        }
        if (this.heights != null && (blockpos = randomEntity.getSpawnPosition()) != null && !this.heights.isInRange(blockpos.getY())) {
            return false;
        }
        if (this.healthRange != null) {
            int i;
            int i1 = randomEntity.getHealth();
            if (this.healthPercent && (i = randomEntity.getMaxHealth()) > 0) {
                i1 = (int)((double)(i1 * 100) / (double)i);
            }
            if (!this.healthRange.isInRange(i1)) {
                return false;
            }
        }
        if (this.nbtName != null && !this.nbtName.matchesValue(s = randomEntity.getName())) {
            return false;
        }
        if (this.professions != null && randomEntity instanceof RandomEntity && (entity = (randomentity = (RandomEntity)randomEntity).getEntity()) instanceof EntityVillager) {
            EntityVillager entityvillager = (EntityVillager)entity;
            int j = entityvillager.getProfession();
            int k = Reflector.getFieldValueInt((Object)entityvillager, (ReflectorField)Reflector.EntityVillager_careerId, (int)-1);
            if (j < 0 || k < 0) {
                return false;
            }
            boolean flag = false;
            for (int l = 0; l < this.professions.length; ++l) {
                VillagerProfession villagerprofession = this.professions[l];
                if (!villagerprofession.matches(j, k)) continue;
                flag = true;
                break;
            }
            if (!flag) {
                return false;
            }
        }
        if (this.collarColors != null && randomEntity instanceof RandomEntity && (entity1 = (randomentity1 = (RandomEntity)randomEntity).getEntity()) instanceof EntityWolf) {
            EntityWolf entitywolf = (EntityWolf)entity1;
            if (!entitywolf.isTamed()) {
                return false;
            }
            EnumDyeColor enumdyecolor = entitywolf.getCollarColor();
            if (!Config.equalsOne((Object)enumdyecolor, (Object[])this.collarColors)) {
                return false;
            }
        }
        if (this.baby != null && randomEntity instanceof RandomEntity && (entity2 = (randomentity2 = (RandomEntity)randomEntity).getEntity()) instanceof EntityLiving && (entityliving = (EntityLiving)entity2).isChild() != this.baby.booleanValue()) {
            return false;
        }
        if (this.moonPhases != null && (world = Config.getMinecraft().theWorld) != null && !this.moonPhases.isInRange(j1 = world.getMoonPhase())) {
            return false;
        }
        if (this.dayTimes != null && (world1 = Config.getMinecraft().theWorld) != null && !this.dayTimes.isInRange(k1 = (int)world1.getWorldInfo().getWorldTime())) {
            return false;
        }
        return this.weatherList == null || (world2 = Config.getMinecraft().theWorld) == null || ArrayUtils.contains((Object[])this.weatherList, (Object)(weather = Weather.getWeather((World)world2, (float)0.0f)));
    }

    public ResourceLocation getTextureLocation(ResourceLocation loc, int randomId) {
        if (this.resourceLocations != null && this.resourceLocations.length != 0) {
            int i = 0;
            if (this.weights == null) {
                i = randomId % this.resourceLocations.length;
            } else {
                int j = randomId % this.sumAllWeights;
                for (int k = 0; k < this.sumWeights.length; ++k) {
                    if (this.sumWeights[k] <= j) continue;
                    i = k;
                    break;
                }
            }
            return this.resourceLocations[i];
        }
        return loc;
    }
}
