package main

import "core:c"
import "core:fmt"
import "core:math"

foreign import cubiomes "cubiomes.o"

//Generator :: struct {
//    mc: c.int,
//    dimension: c.int,
//    flags: u32,
//    seed: u64,
//    sha: u64,
//
//    biome_data: struct #raw_union {
//        beta: struct {
//            biome_noise: Biome_Noise_Beta,
//        },
//        middle: struct {
//            layer_stack: Layer_Stack,
//            xlayer: [5]Layer,
//            entry: ^Layer,
//        },
//        modern: struct {
//            biome_noise: Biome_Noise,
//        }
//    },
//    nether_noise: Nether_Noise,
//    end_noise: End_Noise,
//}
//
//Biome_Noise_Beta :: struct {
//}

Generator :: struct {
}

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
}

MC_1_16_1 :: 19
DIMENSION_OVERWORLD :: 0
MUSHROOM_FIELDS :: 14

VILLAGE :: 5

distance :: proc(pos1: ^Pos, pos2: ^Pos) -> c.int {
    return cast(i32) math.sqrt(cast(f64) math.pow(cast(f64) (pos2.x - pos1.x), 2) + math.pow(cast(f64) (pos2.z - pos1.z), 2))
}

main :: proc() {
    generator := cast(^Generator) new([65536]u8)
    setupGenerator(generator, MC_1_16_1, 0)

    seed: u64
    for {
        applySeed(generator, DIMENSION_OVERWORLD, seed)

        spawn := estimateSpawn(generator, cast(^u64) c.NULL)

        pos: Pos
        getStructurePos(VILLAGE, MC_1_16_1, seed, 0, 0, &pos)
        if isViableStructurePos(VILLAGE, generator, pos.x, pos.z, 0) > 0 && distance(&spawn, &pos) < 50 {
            fmt.printf("seed: {}, pos: {}\n", seed, pos)
            break
        }
        seed += 1
    }
}