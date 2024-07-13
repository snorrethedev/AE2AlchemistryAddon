package com.gmail.snorrethedev.ae2alchemistryaddon;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import com.smashingmods.alchemistry.common.block.compactor.CompactorBlockEntity;

public class CompactorAutocraftingProvider extends AbstractAlchemistryAutocraftingProvider {
    public CompactorAutocraftingProvider(CompactorBlockEntity compactorBlockEntity) {
        super(compactorBlockEntity);
    }

    @Override
    public PatternContainerGroup getCraftingMachineInfo() {
        return PatternContainerGroup.nothing();
    }
}
