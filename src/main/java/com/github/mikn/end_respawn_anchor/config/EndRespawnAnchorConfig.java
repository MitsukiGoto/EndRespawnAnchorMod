package com.github.mikn.end_respawn_anchor.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class EndRespawnAnchorConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<Boolean> isExplode;

    static {
        BUILDER.push("Config for LavaWalker Enchantment Mod");
        isExplode = BUILDER.comment("This defines whether it explodes or not in dimensions other than the End.").define("isExplode", false);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
