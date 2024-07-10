# AE2AlchemistryAddon

This addon mod bridges the gap between Alchemistry and Applied Energistics 2. It allows the machines from Alchemistry to
be efficiently automated, without the need to place one machine per recipe and lock this machine to the recipe.

## How does it work

Simply install the mod, place a ME Pattern Provider against either a Combiner or a Compactor and place the encoded
processing recipes inside. The mod will then set the recipe inside the machine when the pattern provider is used to
autocraft something.

## Why only Combiner and Compactor?
In my opinion, the Combiner and Compactor are the only machines that need to have their recipes set, because they can
make different things using the same input ingredients. The fission and fusion multiblocks don't even have a recipe 
selection and afaik the Liquifier and the Atomizer don't have the same problem, that the same input can generate 
different results. Also AE2 doesn't allow liquids to be the primary ingredient in a pattern. 

## Troubleshooting
- Crafts will fail if anything is in the input slots of the machine. This is due to the fact, that the machine expects
the input to be exactly as specified in the recipe (or a multiple thereof).
- Crafts might fail if you try to use substitutions in the recipe. This mod matches the inputs and outputs from 
the recipe to the inputs and outputs from the pattern. If those don't match, the craft is refused. I haven't tested this
however as I'm currently missing a recipe for this specific use case. 
