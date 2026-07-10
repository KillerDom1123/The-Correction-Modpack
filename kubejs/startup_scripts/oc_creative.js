// priority: 50
// Put every Old Civilisation story item into a dedicated creative tab.
// (KubeJS 6 items are NOT auto-added to any creative tab — without this they are only
//  reachable via /give or JEI.) Kept in its own file so a tab-API hiccup can't affect
//  item registration in oc_items.js.

const OC_ITEMS = [
  // personal
  'personal_data_chip', 'id_badge', 'damaged_photo', 'old_currency', 'employee_handbook',
  // technical
  'incident_report', 'audio_log', 'containment_label',
  // magic
  'memory_fragment_faded', 'memory_fragment_distorted', 'memory_fragment_forbidden', 'arcane_notes', 'bound_thought',
  // horror
  'biological_sample', 'impossible_bone', 'corrupted_map_fragment', 'corrupted_map',
  'distorted_replica', 'deep_echo', 'fog_residue', 'temporal_residue',
  // exodus
  'colony_manifest', 'navigation_fragment',
  'flight_recorder_launch', 'flight_recorder_transit', 'flight_recorder_arrival', 'flight_recorder_final',
  'hs_lgtfo_mission_data',
  // anomalous + endgame
  'unstable_memory', 'signal_device', 'correction_fragment',
  // sanity: clarity consumables
  'sedative_ampoule', 'familiar_photograph',
].map(id => 'oldcivilisation:' + id)

// 1) register the tab (display name + icon)
StartupEvents.registry('creative_mode_tab', event => {
  event.create('oldcivilisation:story')
    .displayName('Old Civilisation')
    .icon(() => Item.of('oldcivilisation:correction_fragment'))
})

// 2) fill it — CreativeTabEvent.add is the confirmed populate path in this KubeJS build
StartupEvents.modifyCreativeTab('oldcivilisation:story', event => {
  OC_ITEMS.forEach(id => event.add(id))
})
