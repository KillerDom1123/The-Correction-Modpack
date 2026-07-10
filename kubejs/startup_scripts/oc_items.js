// priority: 100
// Old Civilisation story items — registration
// Spec: config/implore.md   Plan: config/implore_implementation_plan.md
// Namespace 'oldcivilisation' is used directly (KubeJS 6 supports custom namespaces).
// Icons are reused from existing items: kubejs/assets/oldcivilisation/models/item/<id>.json
// simply parents each item to an existing vanilla/Create item model (see that folder).
// (The old placeholder PNGs in textures/item/ are now unreferenced.)
// Dynamic tooltips are added client-side (client_scripts/oc_tooltips.js) from NBT { oc_variant / oc_stage }.

StartupEvents.registry('item', event => {
  const oc = (id, name, opts) => {
    opts = opts || {}
    const b = event.create('oldcivilisation:' + id).displayName(name)
    b.maxStackSize(opts.stack !== undefined ? opts.stack : 16)
    if (opts.rarity) b.rarity(opts.rarity)
    if (opts.glow) b.glow(true)
    return b
  }

  // ---- Category 1: Personal artefacts (Survivor voice) ----
  oc('personal_data_chip', 'Personal Data Chip', { rarity: 'uncommon' })
  oc('id_badge', 'Old Civilisation ID Badge', { rarity: 'uncommon' })
  oc('damaged_photo', 'Damaged Photograph', { rarity: 'uncommon' })
  oc('old_currency', 'Old Currency', { stack: 64 })
  oc('employee_handbook', 'Employee Handbook', { rarity: 'uncommon' })

  // ---- Category 2: Technical records (Engineer voice) ----
  oc('incident_report', 'Incident Report Fragment', { rarity: 'uncommon' })
  oc('audio_log', 'Research Audio Log', { rarity: 'rare', stack: 1 })
  oc('containment_label', 'Containment Label', { rarity: 'rare' })

  // ---- Category 3: Magic records (Mage / Spirit voices) ----
  oc('memory_fragment_faded', 'Faded Memory', { rarity: 'uncommon' })
  oc('memory_fragment_distorted', 'Distorted Memory', { rarity: 'rare' })
  oc('memory_fragment_forbidden', 'Forbidden Memory', { rarity: 'epic', glow: true })
  oc('arcane_notes', 'Arcane Research Notes', { rarity: 'uncommon' })
  oc('bound_thought', 'Bound Thought Fragment', { rarity: 'rare' })

  // ---- Category 4: Horror evidence (Horror / Correction voices) ----
  oc('biological_sample', 'Biological Sample', { rarity: 'rare' })
  oc('impossible_bone', 'Impossible Bone Fragment', { rarity: 'rare' })
  oc('corrupted_map_fragment', 'Corrupted Map Fragment', { rarity: 'uncommon' })
  oc('corrupted_map', 'Corrupted Map', { rarity: 'rare', stack: 1 })
  // marquee-horror residues (dropped by the named horrors on death; see oc_interactions.js)
  oc('distorted_replica', 'Distorted Replica', { rarity: 'rare', glow: true })
  oc('deep_echo', 'Deep Echo', { rarity: 'rare' })
  oc('fog_residue', 'Fog Residue', { rarity: 'rare' })
  oc('temporal_residue', 'Temporal Residue', { rarity: 'rare', glow: true })

  // ---- Category 5: Exodus / space (Engineer -> Horror) ----
  oc('colony_manifest', 'Colony Manifest', { rarity: 'rare' })
  oc('navigation_fragment', 'Navigation Fragment', { rarity: 'rare' })
  oc('flight_recorder_launch', 'Flight Recorder Fragment: Launch', { rarity: 'rare' })
  oc('flight_recorder_transit', 'Flight Recorder Fragment: Transit', { rarity: 'rare' })
  oc('flight_recorder_arrival', 'Flight Recorder Fragment: Arrival', { rarity: 'rare' })
  oc('flight_recorder_final', 'Flight Recorder Fragment: [CORRUPTED]', { rarity: 'epic', glow: true })
  oc('hs_lgtfo_mission_data', 'HS-LGTFO Mission Data', { rarity: 'epic', glow: true, stack: 1 })

  // ---- Category 6: Anomalous objects (Correction voice; stateful) ----
  oc('unstable_memory', 'Unstable Memory Crystal', { rarity: 'epic', glow: true, stack: 1 })
  oc('signal_device', 'Unknown Signal Device', { rarity: 'rare', stack: 1 })

  // ---- Endgame ----
  oc('correction_fragment', 'The Correction Fragment', { rarity: 'epic', glow: true, stack: 1 })

  // ---- Sanity: clarity consumables (see server_scripts/oc_sanity.js, docs/sanity_mechanic.md §10) ----
  oc('sedative_ampoule', 'Sedative Ampoule', { rarity: 'uncommon', stack: 8 })
  oc('familiar_photograph', 'Familiar Photograph', { rarity: 'uncommon', stack: 1 })
})
