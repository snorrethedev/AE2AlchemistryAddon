package com.gmail.snorrethedev.ae2alchemistryaddon;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.stacks.AEItemKey;
import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.common.block.combiner.CombinerBlockEntity;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;

public class CombinerAutocraftingProvider extends AbstractAlchemistryAutocraftingProvider {

    public CombinerAutocraftingProvider(CombinerBlockEntity combinerBlockEntity) {
        super(combinerBlockEntity);
    }


    @Override
    public PatternContainerGroup getCraftingMachineInfo() {
        return new PatternContainerGroup(
                AEItemKey.of(
                        ForgeRegistries.ITEMS.getValue(new ResourceLocation(Alchemistry.MODID, "combiner"))),
                MutableComponent.create(new LiteralContents("Combiner")),
                new ArrayList<>());
        //return PatternContainerGroup.nothing();
        //return new PatternContainerGroup(AEItemKey.of(ForgeRegistries.ITEMS))
    }
}
