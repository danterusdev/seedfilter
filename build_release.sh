gcc -O3 -c -Wall -Werror cubiomes/biome_tree.c cubiomes/finders.c cubiomes/generator.c cubiomes/layers.c cubiomes/noise.c cubiomes/quadbase.c cubiomes/util.c
ld -r -o cubiomes.o biome_tree.o finders.o generator.o layers.o noise.o quadbase.o util.o
odin build . -o:speed
