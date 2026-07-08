# Extremely dangerous: a real fight, not just a scare.
attribute @s minecraft:generic.max_health base set 150
attribute @s minecraft:generic.attack_damage base set 12
attribute @s minecraft:generic.armor base set 10
attribute @s minecraft:generic.knockback_resistance base set 1
# Top the angel off to its new max health
data merge entity @s {Health:150.0f}
tag @s add wa_buffed
