package main

import "core:c"
import "core:fmt"
import "core:math"

foreign import cubiomes "cubiomes.o"

Generator :: struct {}

Pos :: struct {
    x: c.int,
    z: c.int,
}

foreign cubiomes {
    setupGenerator :: proc "c"(g: ^Generator, mc: c.int, flags: u32) ---
    applySeed :: proc "c"(g: ^Generator, dimension: c.int, seed: u64) ---
    getBiomeAt :: proc "c"(g: ^Generator, scale: c.int, x: c.int, y: c.int, z: c.int) -> c.int ---
    getStructurePos :: proc "c"(structure_type: c.int, mc: c.int, seed: u64, regX: c.int, regZ: c.int, pos: ^Pos) -> c.int ---
    isViableStructurePos :: proc "c"(structType: c.int, generator: ^Generator, blockX: c.int, blockZ: c.int, flags: u32) -> c.int ---
    getSpawn :: proc "c"(generator: ^Generator) -> Pos ---
    estimateSpawn :: proc "c"(generator: ^Generator, rng: ^u64) -> Pos ---
    getHouseList :: proc(houses: [^]c.int, seed: u64, chunkX: c.int, chunkZ: c.int) -> u64 ---;
}

MC_1_16_1 :: 19

DIMENSION_OVERWORLD :: 0
DIMENSION_NETHER :: -1

MUSHROOM_FIELDS :: 14

VILLAGE :: 5
FORTRESS :: 18
BASTION :: 19

BLACKSMITH :: 7
HOUSE_TYPE_COUNT :: 9

distance :: proc(pos1: ^Pos, pos2: ^Pos) -> c.int {
    return cast(i32) math.sqrt(cast(f64) math.pow(cast(f64) (pos2.x - pos1.x), 2) + math.pow(cast(f64) (pos2.z - pos1.z), 2))
}

main :: proc() {
    generator := cast(^Generator) new([65536]u8)
    setupGenerator(generator, MC_1_16_1, 0)

    applySeed(generator, DIMENSION_NETHER, 0)

    seed: u64 = 0
    for {
        //if seed % 100 == 0 {
        //    fmt.printf("Checking seed {}...\n", seed)
        //}

        applySeed(generator, DIMENSION_OVERWORLD, seed)

        spawn := estimateSpawn(generator, cast(^u64) c.NULL)

        viable := true

        village_pos: Pos
        if getStructurePos(VILLAGE, MC_1_16_1, seed, 0, 0, &village_pos) == 0 || isViableStructurePos(VILLAGE, generator, village_pos.x, village_pos.z, 0) == 0 || distance(&spawn, &village_pos) > 100 {
            viable = false
        }

        // Only works for versions before 1.14?
        //if viable {
        //    houses: [HOUSE_TYPE_COUNT]c.int
        //    getHouseList(cast([^]c.int) &houses, seed, village_pos.x / 16, village_pos.z / 16)

        //    if houses[BLACKSMITH] == 0 {
        //        viable = false
        //    }
        //}

        applySeed(generator, DIMENSION_NETHER, seed)

        spawn_nether := Pos { spawn.x / 8, spawn.z / 8 }

        has_valid_bastion := false
        bastion_regx: c.int = 1
        bastion_regy: c.int = 1

        bastion_pos: Pos
        bastion_check: for x in cast(c.int) -1..=0 {
            for y in cast(c.int) -1..=0 {
                if getStructurePos(BASTION, MC_1_16_1, seed, x, y, &bastion_pos) > 0 && isViableStructurePos(BASTION, generator, bastion_pos.x, bastion_pos.z, 0) > 0 && distance(&spawn_nether, &bastion_pos) < 200 {
                    bastion_regx, bastion_regy = x, y
                    has_valid_bastion = true
                    break bastion_check
                }
            }
        }

        if !has_valid_bastion {
            viable = false
        }

        has_valid_fortress := false

        fortress_pos: Pos
        fortress_check: for x in cast(c.int) -1..=0 {
            for y in cast(c.int) -1..=0 {
                if x == bastion_regx && y == bastion_regy { continue }

                if getStructurePos(FORTRESS, MC_1_16_1, seed, 0, -1, &fortress_pos) > 0 && isViableStructurePos(BASTION, generator, fortress_pos.x, fortress_pos.z, 0) == 0 && isViableStructurePos(FORTRESS, generator, fortress_pos.x, fortress_pos.z, 0) > 0 && distance(&fortress_pos, &bastion_pos) < 200 {
                    has_valid_fortress = true
                    break fortress_check
                }
            }
        }

        if !has_valid_fortress {
            viable = false
        }

        if viable {
            fmt.printf("Found Seed: {}\n", seed)
        }
        seed += 1
    }
}
