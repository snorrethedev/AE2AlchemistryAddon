package com.gmail.snorrethedev.ae2alchemistryaddon;

import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.inventories.ItemTransfer;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.capabilities.Capabilities;
import appeng.crafting.pattern.AEProcessingPattern;
import com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipe;
import com.smashingmods.alchemistry.common.recipe.compactor.CompactorRecipe;
import com.smashingmods.alchemylib.api.blockentity.processing.AbstractInventoryBlockEntity;
import com.smashingmods.alchemylib.api.blockentity.processing.AbstractProcessingBlockEntity;
import com.smashingmods.alchemylib.api.item.IngredientStack;
import com.smashingmods.alchemylib.api.recipe.AbstractProcessingRecipe;
import com.smashingmods.alchemylib.api.storage.ProcessingSlotHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public abstract class AbstractAlchemistryAutocraftingProvider implements ICraftingMachine, ICapabilityProvider {

    private AbstractInventoryBlockEntity blockEntity;

    public AbstractAlchemistryAutocraftingProvider(AbstractInventoryBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputs, Direction ejectionDirection) {
        if (!blockEntity.getInputHandler().isEmpty() || blockEntity.isProcessingPaused()) return false;
        if (patternDetails instanceof AEProcessingPattern processingPattern) {
            if (!setRecipe(processingPattern, inputs)) return false;
            processingPattern.pushInputsToExternalInventory(inputs, new CombinerPatternInputSink());
            addOutputHandlerForCraft(ejectionDirection);
            return true;
        }
        return false;
    }

    /**
     * Adds an AutocraftingOutputHandler to the DelegatingOutputHandler that pushes the results into the inventory
     * located at ejectionDirection
     * @param ejectionDirection The Direction for the crafts target inventory
     */
    private void addOutputHandlerForCraft(Direction ejectionDirection) {
        final BlockEntity te = Objects.requireNonNull(this.blockEntity.getLevel()).getBlockEntity(this.blockEntity.getBlockPos().relative(ejectionDirection));
        ItemTransfer targetInventory = InternalInventory.wrapExternal(te, ejectionDirection.getOpposite());
        assert targetInventory != null;
        setOutputHandler(new AutocraftingOutputHandler(targetInventory));
    }

    /**
     * Tries to find the AbstractProcessingRecipe set the locked
     * recipe in the alchemistry machine
     * @param processingPattern The recipe to find and set
     * @return true if the recipe was found and set, false if no recipe for this pattern was found
     */
    private boolean setRecipe(AEProcessingPattern processingPattern, KeyCounter[] inputs) {
        blockEntity.setRecipeLocked(false);
        AbstractProcessingRecipe recipe = getRecipeFromPattern(processingPattern, inputs);
        if (recipe == null) {
            return false;
        }
        blockEntity.setRecipe(recipe);
        blockEntity.setRecipeLocked(true);
        return true;
    }

    /**
     * To avoid setting the output handler all the time using costly reflective operations, we check if its already a
     * DelegatingOutputHandler and set it only if necessary.
     * @param processingSlotHandler The delegated output handler to be used instead of the original.
     */
    private void setOutputHandler(ProcessingSlotHandler processingSlotHandler) {
        if (blockEntity.getOutputHandler() instanceof DelegatingOutputHandler delegatingOutputHandler) {
            delegatingOutputHandler.addOutputHandler(processingSlotHandler);
        } else {
            new DelegatingOutputHandler(this.blockEntity);
            setOutputHandler(processingSlotHandler);
        }
    }

    /**
     * Will try to find the alchemistry recipe of the provided ProcessingPattern.
     * @param patternDetails The pattern providing the recipe for AE2
     * @return The AbstractProcessing
     */
    private AbstractProcessingRecipe getRecipeFromPattern(AEProcessingPattern patternDetails, KeyCounter[] inputs) {
        for (AbstractProcessingRecipe combinerRecipe : blockEntity.getAllRecipes()) {
            if (matchOutput(patternDetails, combinerRecipe)
                && matchInputs(inputs, combinerRecipe)) {
                return combinerRecipe;
            }
        }
        return null;
    }

    private boolean matchOutput(AEProcessingPattern patternDetails, AbstractProcessingRecipe abstractProcessingRecipe) {
        return Objects.equals(GenericStack.fromItemStack((ItemStack) abstractProcessingRecipe.getOutput()), patternDetails.getPrimaryOutput());
    }

    private boolean matchInputs(KeyCounter[] inputs, AbstractProcessingRecipe abstractProcessingRecipe) {
        List<IngredientStack> recipeInputs = new ArrayList<>();
        if (abstractProcessingRecipe instanceof CombinerRecipe combinerRecipe) {
            recipeInputs.addAll(combinerRecipe.getInput());
        } else if (abstractProcessingRecipe instanceof  CompactorRecipe compactorRecipe) {
            recipeInputs.add(compactorRecipe.getInput());
        }

        for (var inputList : inputs) {
            //We assume the list always has exactly entry, the input for this slot
            for (var input : inputList) {
                recipeInputs.removeIf((ri) -> {
                    GenericStack inputStack = new GenericStack(input.getKey(), input.getLongValue());
                    return ri.toStacks().stream().anyMatch((is) -> Objects.equals(GenericStack.fromItemStack(is), inputStack));
                });
            }
        }
        //If we matched all required inputs to possible inputs, we can assume the list of inputs to be equal
        return recipeInputs.isEmpty();
    }

    @Override
    public boolean acceptsPlans() {
        return !blockEntity.isProcessingPaused();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (Capabilities.CRAFTING_MACHINE == cap) {
            return Capabilities.CRAFTING_MACHINE.orEmpty(cap, LazyOptional.of(() -> this));
        }
        return LazyOptional.empty();
    }

    class CombinerPatternInputSink implements IPatternDetails.PatternInputSink {

        private int slot;
        @Override
        public void pushInput(AEKey key, long amount) {
            ProcessingSlotHandler inputHandler = blockEntity.getInputHandler();
            ItemStack itemStack = ItemStack.of(key.toTag());
            itemStack.setCount((int) amount);
            inputHandler.insertItem(slot, itemStack, false);
            slot++;
        }
    }

    static class DelegatingOutputHandler extends ProcessingSlotHandler {
        private Stack<ProcessingSlotHandler> handlersToUse = new Stack<>();

        public DelegatingOutputHandler(ProcessingSlotHandler original) {
            super(original.getSlots());
            handlersToUse.push(original);
        }

        public DelegatingOutputHandler(AbstractInventoryBlockEntity blockEntity) {
            super(blockEntity.getOutputHandler().getSlots());
            try {
                Class<? extends AbstractProcessingBlockEntity> entityClass = blockEntity.getClass();
                Field outputHandlerField = getOutputHandlerField(entityClass);
                if (outputHandlerField != null) {
                    ProcessingSlotHandler originalOutputHandler = (ProcessingSlotHandler) FieldUtils.readField(outputHandlerField, blockEntity, true);
                    handlersToUse.push(originalOutputHandler);
                    FieldUtils.writeField(outputHandlerField, blockEntity, this, true);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        private Field getOutputHandlerField(Class<?> clazz) {
            Field field = FieldUtils.getDeclaredField(clazz, "outputHandler", true);
            if (field != null) {
                return field;
            }
            if (clazz.getSuperclass() == null) {
                return null;
            }
            return getOutputHandlerField(clazz.getSuperclass());
        }

        public void addOutputHandler(ProcessingSlotHandler handler) {
            handlersToUse.push(handler);
        }

        @Override
        public void setOrIncrement(int pSlot, ItemStack pItemStack) {
            ProcessingSlotHandler handler = handlersToUse.pop();
            handler.setOrIncrement(pSlot, pItemStack);
            if (handlersToUse.isEmpty()) {
                handlersToUse.push(handler);
            }
        }

    }

    class AutocraftingOutputHandler extends ProcessingSlotHandler {
        private final ItemTransfer target;
        public AutocraftingOutputHandler(ItemTransfer target) {
            super(0);
            this.target = target;
        }

        @Override
        public void setOrIncrement(int pSlot, ItemStack pItemStack) {
            target.addItems(pItemStack);
        }
    }
}
