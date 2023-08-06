package me.danterus.seedfilter.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.MoreOptionsDialog;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    private int initCount = 0;

    private AtomicBoolean reset = new AtomicBoolean();
    
    private static List<Integer> seeds = new ArrayList<>();

    static {
        File file = new File("in");
        List<String> lines;
        try {
            lines = IOUtils.readLines(new FileReader(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (String line : lines) {
            seeds.add(Integer.parseInt(line));
        }
    }

    int currentSeedIndex = 0;

    int currentSeed = 0;

    private final AtomicBoolean searchStarted = new AtomicBoolean();

    @Inject(method = "openScreen", at = @At("TAIL"))
    public void onOpenScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof TitleScreen) {
            if (initCount > 0) {
                CreateWorldScreen createWorldScreen = new CreateWorldScreen(screen);
                MinecraftClient.getInstance().openScreen(createWorldScreen);
                Field seedText;
                try {
                    seedText = MoreOptionsDialog.class.getDeclaredField("seedTextField");
                    seedText.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
                if (currentSeedIndex >= seeds.size()) {
                    return;
                }
                try {
                    ((TextFieldWidget) seedText.get(createWorldScreen.moreOptionsDialog)).setText(String.valueOf(seeds.get(currentSeedIndex)));
                    currentSeed = seeds.get(currentSeedIndex);
                    System.out.println(currentSeed);
                    currentSeedIndex++;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                Field cheatsEnabled;
                try {
                    cheatsEnabled = CreateWorldScreen.class.getDeclaredField("cheatsEnabled");
                    cheatsEnabled.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
                try {
                    cheatsEnabled.set(createWorldScreen, true);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                Method createLevel;
                try {
                    createLevel = createWorldScreen.getClass().getDeclaredMethod("createLevel");
                    createLevel.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                try {
                    createLevel.invoke(createWorldScreen);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            initCount++;
        } else if (screen == null && !searchStarted.get()) {
            ServerWorld serverWorld = MinecraftClient.getInstance().getServer().getOverworld();
            assert serverWorld != null;
            ClientWorld world = MinecraftClient.getInstance().world;
            assert world != null;
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            assert player != null;

            int playerX = (int) player.getX();
            int playerY = (int) player.getY();
            int playerZ = (int) player.getZ();
            searchStarted.set(true);
            new Thread(() -> {
                while (MinecraftClient.getInstance().currentScreen != null) {

                }
                int totalIron = 0;
                int totalDiamonds = 0;
                boolean ironPickaxe = false;
                boolean lavaPool = false;
                BlockPos villageChestPos = new BlockPos(0, 0, 0);
                int villageChestCount = 0;
                ((PlayerEntity) serverWorld.getEntity(player.getUuid())).setGameMode(GameMode.CREATIVE);
                for (int i = playerX - 150; i < playerX + 150; i++) {
                    for (int k = playerZ - 150; k < playerZ + 150; k++) {
                        for (int j = playerY - 10; j < playerY + 20; j++) {
                            while (!world.isChunkLoaded(new BlockPos(i, j, k))) {
                                System.out.print("");
                            }

                            BlockState block = world.getBlockState(new BlockPos(i, j, k));
                            if (block.getBlock() == Blocks.CHEST && world.getBlockState(new BlockPos(i, j + 1, k)).getBlock() == Blocks.AIR) {
                                villageChestPos = new BlockPos(villageChestPos.getX() + i, villageChestPos.getY() + j, villageChestPos.getZ() + k);
                                villageChestCount++;
                                ChestBlockEntity chestBlock = (ChestBlockEntity) world.getBlockEntity(new BlockPos(i, j, k));

                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }

                                serverWorld.getEntity(player.getUuid()).teleport(i, j, k);
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }

                                while (MinecraftClient.getInstance().currentScreen != null) {
                                    System.out.print("");
                                }
                                BlockHitResult blockHitResult = new BlockHitResult(new Vec3d(0, 0, 0), Direction.NORTH, chestBlock.getPos(), false);
                                MinecraftClient.getInstance().interactionManager.interactBlock(player, world, Hand.MAIN_HAND, blockHitResult);

                                while (MinecraftClient.getInstance().currentScreen == null) {
                                    System.out.print("");
                                }

                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }

                                GenericContainerScreen containerScreen = (GenericContainerScreen) MinecraftClient.getInstance().currentScreen;
                                for (Slot slot : containerScreen.getScreenHandler().slots) {
                                    ItemStack stack = slot.getStack();
                                    if (stack.getItem() == Items.IRON_INGOT) {
                                        totalIron += stack.getCount();
                                    } else if (stack.getItem() == Items.DIAMOND) {
                                        totalDiamonds += stack.getCount();
                                    } else if (stack.getItem() == Items.IRON_PICKAXE) {
                                        ironPickaxe = true;
                                    }
                                }
                            }
                        }
                    }
                }

                if (villageChestCount > 0) {
                    int averageVillageX = villageChestPos.getX() / villageChestCount;
                    int averageVillageY = villageChestPos.getY() / villageChestCount;
                    int averageVillageZ = villageChestPos.getZ() / villageChestCount;
                    for (int i = averageVillageX - 100; i < averageVillageX + 100; i++) {
                        for (int k = averageVillageZ - 100; k < averageVillageZ + 100; k++) {
                            for (int j = averageVillageY - 10; j < averageVillageY + 20; j++) {
                                while (!world.isChunkLoaded(new BlockPos(i, j, k))) {
                                    System.out.print("");
                                }

                                BlockState block = world.getBlockState(new BlockPos(i, j, k));
                                if (j > 60 && block.getBlock() == Blocks.LAVA && world.getBlockState(new BlockPos(i + 1, j, k)).getBlock() == Blocks.LAVA && world.getBlockState(new BlockPos(i, j, k + 1)).getBlock() == Blocks.LAVA && world.getBlockState(new BlockPos(i, j + 1, k)).isAir() && world.getBlockState(new BlockPos(i, j + 2, k)).isAir() && world.getBlockState(new BlockPos(i, j + 3, k)).isAir() && world.getBlockState(new BlockPos(i, j + 4, k)).isAir()) {
                                    //System.out.println(new BlockPos(i, j, k));
                                    lavaPool = true;
                                }
                            }
                        }
                    }
                }

                //4000000
                if ((totalIron >= 3 || totalDiamonds >= 3 || ironPickaxe) && lavaPool) {
                    File file = new File("out");
                    try {
                        FileWriter writer = new FileWriter(file, true);
                        writer.append(String.valueOf(currentSeed));
                        writer.append('\n');
                        writer.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                world.disconnect();
//                MinecraftClient.getInstance().disconnect(new SaveLevelScreen(new TranslatableText("menu.savingLevel")));
                reset.set(true);
                
                searchStarted.set(false);
            }).start();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        if (reset.get()) {
            MinecraftClient.getInstance().openScreen(new TitleScreen());
            reset.set(false);
        }
    }

}
