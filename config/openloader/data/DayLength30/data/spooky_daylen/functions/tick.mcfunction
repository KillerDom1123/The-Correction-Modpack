# Advance world time 2 game-ticks per 3 real ticks => 24000 ticks over 36000 real ticks = ~30 min/day.
scoreboard players add #t spooky_daylen 1
execute if score #t spooky_daylen matches 1..2 run time add 1
execute if score #t spooky_daylen matches 3.. run scoreboard players set #t spooky_daylen 0
