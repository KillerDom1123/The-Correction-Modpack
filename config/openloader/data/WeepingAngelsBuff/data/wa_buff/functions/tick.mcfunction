# Buff any newly-spawned Weeping Angel exactly once (tagged so it isn't re-applied)
execute as @e[type=weeping_angels:weeping_angel,tag=!wa_buffed] run function wa_buff:apply
