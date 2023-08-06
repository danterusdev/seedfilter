# SeedFilter
A minecraft seed filter that attempts to find good speedrunning seeds.

# Filter Requirements
- Close Village (under 100 blocks)
- Close Bastion (under 200 blocks)
- Close Fortress (under 200 blocks from bastion)
- Village has 3 iron in chests (and 3-5 from iron golem)
- Close, above ground exposed lava pool (under 100 blocks from village center)

# The Process
The filter process is split into two stages:
- **Stage 1:** Uses cubiomes to find seeds with close villages, bastions, and fortresses.
- **Stage 2:** Uses a fabric mod to find seeds (from those selected from stage 1) which have enough iron from blacksmits, and an exposed lava pool nearby.

The process is done in two stages because while cubiomes is significantly faster than a minecraft instance, it does not support a lot of what is needed to determine whether a seed is actually good.
The first stage filters down to so that only about 1/100,000 seeds fit the filter. The second stage filters those good seeds and only about 1/100ish are accepted (this second number is very much an estimate because I have only found I since seed so far).

# Issues
- **Very Slow:** Takes around 20-30ish minutes of searching to find a single seed (a seed that fints the criteria is about 1/10,000,000).
- Doesn't account for terrain (lava pools could be hidden in forests, nether structures hidden in walls).

# The Result
A current example of this filter would be seed 13055089 (the only seed I have found so far, but have not been searching for much time at all).

This seed you spawn in a village that has a blacksmith chests with plenty of iron, a nearby ruined portal to enter the nether, and a close bastion, the fortress is close, however it is buried in a wall, which is not something the algorithm accounts for.
