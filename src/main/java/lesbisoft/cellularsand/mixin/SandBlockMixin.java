package lesbisoft.cellularsand.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.SandBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlock.class)
public class SandBlockMixin {

    /**
     * Inject into tick() to alter the conditions under which sand decides to fall.
     */
    @Inject(method = "scheduledTick", at = @At("HEAD"), cancellable = true)
    private void customFallingLogic(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {

        BlockPos downPos = pos.add(0, -1, 0);
        if (world.isAir(downPos)) {
            world.setBlockState(downPos, state);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
            ci.cancel();
            return;
        }


        BlockPos[] posArray = new BlockPos[4];

        posArray[0] = new BlockPos(1, -1, 0);
        posArray[1] = new BlockPos(0, -1, 1);
        posArray[2] = new BlockPos(-1, -1, 0);
        posArray[3] = new BlockPos(0, -1, -1);

        for (int i = posArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            BlockPos temp = posArray[i];
            posArray[i] = posArray[j];
            posArray[j] = temp;
        }



        //posArray[0] = new BlockPos(0, -1, 0);
        //posArray[1] = new BlockPos(1, 0, 0);
        //posArray[2] = new BlockPos(0, 0, 1);
        //posArray[3] = new BlockPos(-1, 0, 0);
        //posArray[4] = new BlockPos(0, 0, -1);


        for (BlockPos offset : posArray) {
            BlockPos offsetPos = pos.add(offset);

            if (world.isAir(offsetPos)){
                world.setBlockState(offsetPos,state);
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                ci.cancel();
                return;
            }
        }
    }
}