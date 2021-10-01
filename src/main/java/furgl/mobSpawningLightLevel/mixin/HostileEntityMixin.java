package furgl.mobSpawningLightLevel.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import furgl.mobSpawningLightLevel.MobSpawningLightLevel;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

@Mixin(HostileEntity.class)
public class HostileEntityMixin {

	/**Adjust ambient darkness (since block light would be 4 in open sky and wouldn't spawn any mobs when gamerule = 0)*/
	@Unique
	private static int getAmbientDarknessOffset(World world) {
		int requiredLightLevel = world.getGameRules().getInt(MobSpawningLightLevel.gamerule);
		return requiredLightLevel < 7 ? (7-requiredLightLevel) : 0;
	}

	/**Controls how likely mobs can spawn in this darkness*/
	@Inject(method = "isSpawnDark(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)Z", 
			at = @At(value = "CONSTANT", target = "Ljava/util/Random;nextInt(I)I", remap = false, args = "intValue=8"), 
			locals = LocalCapture.CAPTURE_FAILSOFT, 
			cancellable = true)
	private static void isSpawnDarkOverride(ServerWorldAccess world, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> ci, int i) {
		int requiredLightLevel = world.toServerWorld().getGameRules().getInt(MobSpawningLightLevel.gamerule);
		// fix i to adjust ambient light so mobs can still spawn in open sky with gamerule = 0
		if (requiredLightLevel < 7)
			i = world.toServerWorld().isThundering() ? world.getLightLevel(pos, 10+getAmbientDarknessOffset(world.toServerWorld())) : world.getLightLevel(pos, world.toServerWorld().getAmbientDarkness()+getAmbientDarknessOffset(world.toServerWorld()));
		if (requiredLightLevel == 0 || i <= random.nextInt(requiredLightLevel)) // random chance based on light level
			ci.setReturnValue(true);
		else
			ci.setReturnValue(false);
	}

	/**Modify getPathfindingFavor to use requiredLightLevel/15f instead of 7/15f (~0.5F)
	 * which controls whether or not mobs can spawn in this darkness*/
	@ModifyConstant(method = "getPathfindingFavor(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/WorldView;)F",
			constant = @Constant(floatValue = 0.5F))
	public float getPathfindingFavor(float initialFavor) {
		int requiredLightLevel = ((HostileEntity)(Object)this).world.getGameRules().getInt(MobSpawningLightLevel.gamerule);
		return requiredLightLevel / 15f;
	}

	/**Modify getBrightness to adjust ambient darkness (since block light would be 4 in open sky and wouldn't spawn any mobs when gamerule = 0)
	 * which controls whether or not mobs can spawn in this darkness*/
	@Redirect(method = "getPathfindingFavor(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/WorldView;)F",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldView;getBrightness(Lnet/minecraft/util/math/BlockPos;)F"))
	public float getBrightness(WorldView world, BlockPos pos) {
		return world.getDimension().method_28516(world.getLightLevel(pos, world.getAmbientDarkness()+
				(world instanceof World ? getAmbientDarknessOffset((World) world) : 0)));
	}

}