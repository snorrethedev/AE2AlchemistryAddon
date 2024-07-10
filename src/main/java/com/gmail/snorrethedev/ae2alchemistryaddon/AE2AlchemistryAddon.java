package com.gmail.snorrethedev.ae2alchemistryaddon;

import com.mojang.logging.LogUtils;
import com.smashingmods.alchemistry.common.block.combiner.CombinerBlockEntity;
import com.smashingmods.alchemistry.common.block.compactor.CompactorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AE2AlchemistryAddon.MODID)
public class AE2AlchemistryAddon
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "ae2alchemistryaddon";

   public static final ResourceLocation COMBINER_CAP = new ResourceLocation(AE2AlchemistryAddon.MODID, "combinerautocraftingprovider");
   public static final ResourceLocation COMPACTOR_CAP = new ResourceLocation(AE2AlchemistryAddon.MODID, "compactorautocraftingprovider");



    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public AE2AlchemistryAddon()
    {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, this::addCap);

    }

    private void addCap(AttachCapabilitiesEvent<BlockEntity> event) {
        var be = event.getObject();
        if (be instanceof CombinerBlockEntity combiner) {
            event.addCapability(COMBINER_CAP, new CombinerAutocraftingProvider(combiner));
            LOGGER.debug(String.format("Attached autocrafting capability to combiner at pos %s", blockPosToString(combiner)));
        }
        if (be instanceof CompactorBlockEntity compactor) {
            event.addCapability(COMPACTOR_CAP, new CompactorAutocraftingProvider(compactor));
            LOGGER.debug(String.format("Attached autocrafting capability to compactor at pos %s", blockPosToString(compactor)));
        }
    }

    private String blockPosToString(BlockEntity blockEntity) {
        BlockPos blockPos = blockEntity.getBlockPos();
        return "x = " + blockPos.getX() + " | y =" + blockPos.getY() + " | z = " + blockPos.getZ();
    }
}
