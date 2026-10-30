package lesbisoft.cellularsand.mixin;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.apache.commons.compress.compressors.lz77support.LZ77Compressor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

import static lesbisoft.cellularsand.Cellularsand.*;

@Mixin(FallingBlock.class)
public class SandBlockMixin {

    /**
     * Inject into tick() to alter the conditions under which sand decides to fall.
     */
    @Inject(method = "scheduledTick", at = @At("HEAD"), cancellable = true)
    private void customFallingLogic(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if(!physicsEnabled) return;
        if(world.getBlockState(pos.add(0,-1,0)).getBlock()==Blocks.AIR&&smoothFallingPhysics) return;
        BlockPos lastValid=null;
        for (int index = 0; index < cellularSandPositions.size(); index++) {

            Optional<BlockPos> correctPos =checkRing(index, world, pos, random);
            if (correctPos.isPresent()) {
                lastValid=correctPos.get();
                break;
            }
        }



        if(lastValid!=null){
            world.setBlockState(lastValid.add(0,smoothFallingPhysics?1:0,0),state);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
            ci.cancel();
        }








        /*for (BlockPos offset : posArray) {
            BlockPos offsetPos = pos.add(offset);
            Block offsetBlock = world.getBlockState(offsetPos).getBlock();
            if (canFallInto(offsetBlock)){
                world.setBlockState(offsetPos,state);
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                ci.cancel();
                return;
            }
        }*/
    }
    private static boolean canFallInto(Block block) {
        return block == Blocks.AIR ||
                block == Blocks.WATER;
    }
    private static Optional<BlockPos> checkRing(int index, ServerWorld world, BlockPos pos, Random random) {
        int randomOffset = random.nextInt(cellularSandPositions.get(index).size());
        int size = cellularSandPositions.get(index).size();
        for(int i = 0; i < size; i++){
            BlockPos offsetPos = cellularSandPositions.get(index).get((i+randomOffset) % size);
            BlockPos blockPos = pos.add(offsetPos);
            Block offsetBlock = world.getBlockState(blockPos).getBlock();
            if(canFallInto(offsetBlock)){
                return Optional.of(blockPos);
            }
        }
        return Optional.empty();
    }
}