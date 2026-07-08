// priority: 100
// Old Civilisation story items — dynamic tooltips
// Text is chosen from a pool by NBT { oc_variant }, fixed at drop time (server-side), so it never flickers.
// Stateful items read NBT { oc_stage } instead.
// RULE (implore.md): no single line explains the plot; every fragment answers one question and raises two.
// VOICES stay raw/in-world here — the FTB quest book is the modern cataloguer, these items are the evidence.

// style markers: leading '»' = corrupted/redacted (dark, italic); leading '~' = anomalous (dark italic); else gray
const OC_POOLS = {
  // ---- Personal (Survivor) ----
  'oldcivilisation:personal_data_chip': [
    ['"Reminder: pick up supplies after work."', '"Ask about the transport delays."'],
    ['"They cancelled transport again today."', '"Third time this month."'],
    ['"Nobody is saying why."', '"The kids keep asking. I don\'t answer."'],
    ['"If you\'re hearing this I already left."', '»[ the rest is silence ]'],
    ['"The winters were never this harsh."', '"We keep the fire lit through the day now."'],
  ],
  'oldcivilisation:id_badge': [
    ['Division: Infrastructure', '»Identity verified: deceased.'],
    ['Division: Energy', '»Identity verified: deceased.'],
    ['Division: Atmospheric Studies', '»Identity verified: deceased.'],
    ['Department: Reality Stabilisation Research', 'Name: »[REDACTED]', '»Identity verified: deceased.'],
  ],
  'oldcivilisation:damaged_photo': [
    ['A family outside a facility.', '"Three people visible."'],
    ['A skyline that no longer exists.', '"Taken from the west platform."'],
    ['The same family. Same day.', '~"Four shadows detected."'],
    ['»[ image degraded ]', '~"The count does not match."'],
  ],
  'oldcivilisation:old_currency': [
    ['"Value meaningless. History priceless."'],
  ],
  'oldcivilisation:employee_handbook': [
    ['Section 1: Safety procedures.', 'Section 2: Reporting hours.'],
    ['Section 7: Emergency evacuation routes.', '"Familiarise yourself with the nearest exit."'],
    ['Appendix: Building anomalies.', '~"If you hear your name from an empty room, do not respond."'],
  ],

  // ---- Technical (Engineer) ----
  'oldcivilisation:incident_report': [
    ['"The chamber remained stable."'],
    ['"The chamber remained stable after external interference."'],
    ['"External interference was not identified."', '»[ source field left blank ]'],
    ['"Recommend the experiment be cleared of fault."', '"We did not cause this."'],
    ['"Atmospheric readings continue to change."', '"The instruments are not faulty. We checked."'],
  ],
  'oldcivilisation:audio_log': [
    ['» Right-click to play recording.', '"Day 42. We have confirmed the signal is responding."'],
    ['» Right-click to play recording.', '"Day 61. It responds faster than we transmit."'],
    ['» Right-click to play recording.', '~"Day 90. It answered a question we had not asked yet."'],
  ],
  'oldcivilisation:containment_label': [
    ['CONTAINMENT AREA 04', 'DO NOT OPEN. DO NOT OBSERVE.', 'DO NOT COMMUNICATE.'],
    ['CONTAINMENT AREA 11', '»[ contents field redacted ]', 'OBSERVATION IS PARTICIPATION.'],
    ['CONTAINMENT AREA 04', '~"It is aware of the label."'],
  ],

  // ---- Magic (Mage / Spirit) ----
  'oldcivilisation:memory_fragment_faded': [
    ['A classroom.', 'A lecture about magic.'],
    ['A harvest festival.', 'Lanterns on the water.'],
    ['A quiet street at dusk.', 'Someone is waiting to cross.'],
  ],
  'oldcivilisation:memory_fragment_distorted': [
    ['The same classroom.', '~Everyone is facing the wrong direction.'],
    ['The festival again.', '~The lanterns fall upward.'],
    ['~The street repeats.', '~The same person crosses, and crosses, and crosses.'],
  ],
  'oldcivilisation:memory_fragment_forbidden': [
    ['~The sky opens.', '~Something notices.'],
    ['~It is not a door.', '~It is a correction.'],
    ['»[ this memory should not persist ]', '~It remembers being seen.'],
  ],
  'oldcivilisation:arcane_notes': [
    ['"The engineers believe they created the breach."', '"They are mistaken."'],
    ['"Reality was not torn. It was answered."'],
    ['"We reached beyond our limits.", she wrote.', '"We should not have tried."'],
  ],
  'oldcivilisation:bound_thought': [
    ['"We were not summoned."', '"We were already here."'],
    ['"You call it a ritual."', '"We call it noticing you."'],
    ['~"You were warned. The warning was also us."'],
  ],

  // ---- Horror (Horror / Correction) ----
  'oldcivilisation:biological_sample': [
    ['"Cellular structure constantly changes."'],
    ['"Sample refuses to hold a single form."', '"It is not decaying. It is revising."'],
    ['~"The sample matches nothing. Then it matches everything."'],
  ],
  'oldcivilisation:impossible_bone': [
    ['"No creature matches this anatomy."'],
    ['"Load-bearing in a direction that does not exist."'],
    ['~"It is not a fossil. It has not happened yet."'],
  ],
  'oldcivilisation:corrupted_map_fragment': [
    ['A torn corner of a map.', '»Coordinates do not resolve.'],
    ['Ink that rearranges when unwatched.', 'Combine the fragments.'],
    ['~A place is marked here.', '~Nothing is there. Yet.'],
  ],
  'oldcivilisation:corrupted_map': [
    ['A continent no survey records.', 'A city beneath the ocean.', '~Structures underground, arranged to be seen from above.'],
  ],

  // ---- Exodus (Engineer -> Horror) ----
  'oldcivilisation:colony_manifest': [
    ['COLONY SHIP: SUCCESSFULLY DEPLOYED.', 'COLONY STATUS: UNKNOWN.'],
    ['MANIFEST: 40,000 souls. Genetic archive. Seed vault.', '»Last contact: not recorded.'],
  ],
  'oldcivilisation:navigation_fragment': [
    ['DESTINATION: HABITABLE WORLD.', '~UPDATE: SIGNAL DETECTED THERE.'],
    ['COURSE LOCKED. FUEL NOMINAL.', '~"Why is it already ahead of us?"'],
  ],
  'oldcivilisation:flight_recorder_launch': [
    ['"Ignition nominal. We are leaving."', '"Everyone on board is quiet."'],
  ],
  'oldcivilisation:flight_recorder_transit': [
    ['"Weeks in transit. Morale holding."', '"The instruments hear something outside."'],
  ],
  'oldcivilisation:flight_recorder_arrival': [
    ['"Orbit achieved. The world is green."', '"We made it. We actually made it."'],
  ],
  'oldcivilisation:flight_recorder_final': [
    ['»[ recording heavily corrupted ]', '~"Something was already here."', '~"It has been waiting the whole way."'],
  ],

  // ---- Endgame (single line; deliberately incomplete) ----
  'oldcivilisation:correction_fragment': [
    ['"A piece of something that removed everything else."'],
  ],

  // ---- marquee-horror residues ----
  'oldcivilisation:distorted_replica': [['"It copied something."', '"We do not know what."']],
  'oldcivilisation:deep_echo': [['"Recording origin:"', '~Below known geological limits."']],
  'oldcivilisation:fog_residue': [['"Composition unknown."', '~It has not evaporated in weeks.']],
  'oldcivilisation:temporal_residue': [['~This object is older than it was a moment ago.', '"Do not look away from it."']],

  // ---- assembled exodus record ----
  'oldcivilisation:hs_lgtfo_mission_data': [[
    'Assembled from every recovered fragment.',
    '"Preserve humanity. Find somewhere safe. Preserve the truth."',
    '~[ of three objectives, only the third failed ]',
  ]],
}

// stateful items: tooltip depends on NBT oc_stage (set server-side as the player uncovers more)
const OC_STAGED = {
  'oldcivilisation:unstable_memory': [
    ['A damaged magical crystal.'],
    ['A damaged magical crystal.', 'It is warm now.'],
    ['~It remembers you.'],
    ['~It remembers you.', '~It is remembering things you have not done.'],
  ],
  'oldcivilisation:signal_device': [
    ['» Right-click to sample the signal.', 'SIGNAL DETECTED. SOURCE: UNKNOWN.'],
    ['» Right-click to sample the signal.', '~SIGNAL SOURCE: LOCAL.'],
  ],
}

function ocInt(item, key) {
  try {
    const nbt = item.nbt
    if (nbt && nbt[key] !== undefined && nbt[key] !== null) return Number(nbt[key]) | 0
  } catch (e) {}
  return 0
}

function ocRender(text, lines) {
  lines.forEach(raw => {
    if (raw.startsWith('»')) text.add(Text.of(raw.substring(1).trim()).darkGray().italic(true))
    else if (raw.startsWith('~')) text.add(Text.of(raw.substring(1).trim()).darkGray().italic(true))
    else text.add(Text.of(raw).gray())
  })
}

ItemEvents.tooltip(tt => {
  // variant-based pools
  Object.keys(OC_POOLS).forEach(id => {
    const pool = OC_POOLS[id]
    tt.addAdvanced(id, (item, advanced, text) => {
      const v = ((ocInt(item, 'oc_variant') % pool.length) + pool.length) % pool.length
      ocRender(text, pool[v])
    })
  })
  // stage-based pools
  Object.keys(OC_STAGED).forEach(id => {
    const stages = OC_STAGED[id]
    tt.addAdvanced(id, (item, advanced, text) => {
      const s = Math.max(0, Math.min(stages.length - 1, ocInt(item, 'oc_stage')))
      ocRender(text, stages[s])
    })
  })
})

// #2 — lore tooltips on recovered AE2 architecture: "I am reconstructing, not inventing."
ItemEvents.tooltip(tt => {
  const rec = s => Text.of(s).darkGray().italic(true)
  tt.add('ae2:controller', rec('A familiar architecture. The original creator is unknown.'))
  tt.add('ae2:inscriber', rec('The pattern was already here. We only pressed it again.'))
  tt.add('ae2:drive', rec('Recovered storage protocol. It remembers formats we never wrote.'))
})
