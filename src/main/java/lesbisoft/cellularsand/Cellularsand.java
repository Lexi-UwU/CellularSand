package lesbisoft.cellularsand;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class Cellularsand implements ModInitializer {
    public enum Gravity{
        down,
        east,
        west,
        north,
        south,
        up
    }
    public static final GameRules.Key<GameRules.BooleanRule> ENABLE_FALLING_PHYSICS =
            GameRuleRegistry.register("enableFallingPhysics", GameRules.Category.MISC,
                    GameRuleFactory.createBooleanRule(true,(server, rule) -> {
                        UpdateArray(server);
    }));
    public static final GameRules.Key<GameRules.BooleanRule> FALL_ONLY_STRAIGHT_LINES =
            GameRuleRegistry.register("fallOnlyOnStraightLines", GameRules.Category.MISC,
                    GameRuleFactory.createBooleanRule(false,(server, rule) -> {
                        UpdateArray(server);
                    }));
    public static final GameRules.Key<GameRules.IntRule> MAX_SAND_DISTANCE_FALLOFF =
            GameRuleRegistry.register("maxSandDistanceFallOff", GameRules.Category.MISC,
                    GameRuleFactory.createIntRule(1, 0, 30, (server, rule) -> {
                        UpdateArray(server);
                    }));
    public static final GameRules.Key<GameRules.BooleanRule> SMOOTH_FALLING_PHYSICS =
            GameRuleRegistry.register("smoothFallingPhysics", GameRules.Category.MISC,
                    GameRuleFactory.createBooleanRule(false,(server, rule) -> {
                        UpdateArray(server);
                    }));
    public static final GameRules.Key<EnumRule<Gravity>> GRAVITY =
            GameRuleRegistry.register("fallDirection", GameRules.Category.MISC,
                    GameRuleFactory.createEnumRule(Gravity.down,new Gravity[]{Gravity.down, Gravity.up, Gravity.south, Gravity.north, Gravity.east, Gravity.west},(server, rule) -> {
                        UpdateArray(server);
                    }));
    @Override
    public void onInitialize() {
        ServerWorldEvents.LOAD.register((server, world) -> {UpdateArray(server);});
    }
    public static boolean physicsEnabled=true;
    public static boolean smoothFallingPhysics=true;
    public static Gravity gravity=Gravity.down;
    public static List<List<BlockPos>> cellularSandPositions = new ArrayList<>();
    public static void UpdateArray(MinecraftServer server) {
        cellularSandPositions.clear();
        int fallOffDistance = server.getGameRules().getInt(MAX_SAND_DISTANCE_FALLOFF);
        boolean onlyStraightLines = server.getGameRules().getBoolean(FALL_ONLY_STRAIGHT_LINES);
        physicsEnabled = server.getGameRules().getBoolean(ENABLE_FALLING_PHYSICS);
        smoothFallingPhysics = server.getGameRules().getBoolean(SMOOTH_FALLING_PHYSICS);
        gravity=server.getGameRules().get(GRAVITY).get();
        int count=0;
        if(gravity!=Gravity.down){
            smoothFallingPhysics=false;
            onlyStraightLines=false;
        }
        for (int i = 0; i <= fallOffDistance; i++) {
            List<BlockPos> sandPositionsRing = new ArrayList<>();
            if (!onlyStraightLines) {
                for (int x = -i; x <= i; x++) {
                    for (int z = -i; z <= i; z++) {
                        if (x == i || z == i || x == -i || z == -i) {
                            sandPositionsRing.add(offsetFor(gravity, x, z));
                            count++;
                        }

                    }
                }
            } else {
                sandPositionsRing.add(new BlockPos(i, -1, 0));
                sandPositionsRing.add(new BlockPos(-i, -1, 0));
                sandPositionsRing.add(new BlockPos(0, -1, i));
                sandPositionsRing.add(new BlockPos(i, -1, i));
                count++;
            }
            cellularSandPositions.add(sandPositionsRing);


        }
        server.getPlayerManager().broadcast(
                Text.literal("Updated Sand Falling Array with "+count+" total positions"), false);
    }
    private static BlockPos offsetFor(Gravity gravity, int a, int b) {
        return switch (gravity) {
            case down  -> new BlockPos(a, -1, b);
            case up    -> new BlockPos(a,  1, b);
            case east  -> new BlockPos( 1, a, b);
            case west  -> new BlockPos(-1, a, b);
            case south -> new BlockPos(a, b,  1);
            case north -> new BlockPos(a, b, -1);
        };
    }

}
