package furgl.mobSpawningLightLevel;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.Category;

public class MobSpawningLightLevel implements ModInitializer {

	public static final String MODNAME = "Mob Spawning Light Level";
	public static final String MODID = "mobspawninglightlevel";

	public static GameRules.Key<GameRules.IntRule> gamerule;

	@Override
	public void onInitialize() {
		gamerule = GameRuleRegistry.register("mobSpawningLightLevel", Category.MOBS, GameRuleFactory.createIntRule(7, 0, 15));
	}

}