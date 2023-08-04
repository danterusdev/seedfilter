import os
import sys

no_cubiomes = False
for arg in sys.argv[1:]:
    if arg == "-no-cubiomes":
        no_cubiomes = True

if not no_cubiomes:
    os.system("gcc -O3 -c -Wall -Werror cubiomes/biome_tree.c cubiomes/finders.c cubiomes/generator.c cubiomes/layers.c cubiomes/noise.c cubiomes/quadbase.c cubiomes/util.c")
    os.system("ld -r -o cubiomes.o biome_tree.o finders.o generator.o layers.o noise.o quadbase.o util.o")

os.system("odin build . -o:speed")
