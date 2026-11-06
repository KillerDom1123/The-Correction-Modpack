# clock: 1t
# Spooky Dookie override (OpenLoader pack IncendiumClockTrim) of Incendium 5.3.5 clocks/main: fewer whole-world
# @e scans per tick. Changed lines are marked "trim". Re-diff against the jar's copy whenever Incendium updates.
# See docs/CONFIG_CHANGES.md ("Incendium clock trim").

schedule function incendium:clocks/main 1t replace

function incendium:technical/main
execute as @a at @s run function incendium:player/main
function incendium:castle/main

# mob init
execute as @e[type=#incendium:mobs_no_player, tag=!in.checked] at @s run function incendium:entity/mob/init

# trim (B): the missing-id fallback runs every 10 ticks instead of every tick. Mobs set up by the line above already
# have their id. The fallback serves mobs that arrive pre-tagged in.checked (castle round-table room, sanctum, sentry
# blaze summons) and mobs whose id was cleared by an id-counter reset; they now get it within 0.5 s.
scoreboard players add $trim.eid in.dummy 1
execute if score $trim.eid in.dummy matches 10.. run scoreboard players set $trim.eid in.dummy 0
execute if score $trim.eid in.dummy matches 0 as @e[type=#incendium:mobs_no_player] unless score @s in.eid matches 0..32767 run function incendium:technical/entity_id/check
execute as @e[type=#incendium:mobs_no_player, tag=in.ticking_entity] at @s run function incendium:entity/mob/main

# trim (A): in.frozen is only ever set (to 120) by item/chilling_blade/on_hit, which this pack makes restart a
# 120-tick timer. While the timer is 0 no mob can have in.frozen 1.., so both whole-world scans are skipped.
execute if score $trim.frozen in.dummy matches 1.. run scoreboard players remove @e[type=#incendium:mobs,scores={in.frozen=1..}] in.frozen 1
execute if score $trim.frozen in.dummy matches 1.. at @e[type=#incendium:mobs,scores={in.frozen=1..},predicate=incendium:random/10] run particle minecraft:snowflake ~ ~1.6 ~ 0.1 0.05 0.1 .1 5 force
execute if score $trim.frozen in.dummy matches 1.. run scoreboard players remove $trim.frozen in.dummy 1

# other
execute as @e[type=#incendium:other, tag=!in.checked] at @s run function incendium:entity/other/init

execute as @e[type=#incendium:other, tag=in.ticking_entity] at @s run function incendium:entity/other/main

execute if entity @a[tag=nbs_borderofli] run function incendium:border_of_life/tick

# shield cloud effect clear
# trim (A): @a instead of @e. Only players ever get in.cd_shield (player init/main and the shield functions run as the player).
execute if entity @a[scores={in.cd_shield=1..},predicate=incendium:holding/radiation_shield] run effect clear @a[scores={in.cd_shield=1..},predicate=incendium:holding/radiation_shield] poison

execute if entity @a[scores={in.cd_shield=1..},predicate=incendium:holding/necrotic_shield] run effect clear @a[scores={in.cd_shield=1..},predicate=incendium:holding/necrotic_shield] wither
