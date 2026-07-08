--[[
  OLD CIVILISATION ARCHIVE SYSTEM  --  program: archive
  Ships via KubeJS datapack -> merges into CC:Tweaked ROM (available on every computer).

  Design rules (config/implore.md):
    - No single record explains the story. Fragments contradict across categories.
    - Every record answers one question and raises two. Nothing is ever complete.
    - The records are raw in-world voices (survivor / engineer / mage / spirit / Correction).
      This terminal is the *interaction*; it never narrates or interprets.
    - The deepest record ("anomalous") requires recovered media in an attached disk drive.
]]

local function isColor()
  local ok, res = pcall(function() return term.isColour() end)
  return ok and res
end

local function setc(c)
  if isColor() then term.setTextColour(c) else term.setTextColour(colours.white) end
end

local function mediaPresent()
  local ok, res = pcall(function()
    for _, name in ipairs(peripheral.getNames()) do
      if peripheral.getType(name) == "drive" then
        local d = peripheral.wrap(name)
        if d and d.isDiskPresent and d.isDiskPresent() then return true end
      end
    end
    return false
  end)
  return ok and res
end

-- record pools: each entry is a list of lines. Voices are unlabelled on purpose.
local RECORDS = {
  personnel = {
    { "> PERSONNEL FILE 0447", "Name: [REDACTED]", "Division: Infrastructure", "Status: verified deceased", "Note: family relocated prior. File sealed." },
    { "> SHIFT LOG", "\"Transport cancelled again. Third week now.\"", "\"They will not say where the night crews went.\"", "[log ends mid-sentence]" },
    { "> PERSONNEL FILE 1120", "Name: [REDACTED]", "Division: Atmospheric Studies", "Final entry: \"the readings are not seasonal.\"" },
    { "> MESSAGE, PERSONAL", "\"If you find this, do not wait for the all-clear.\"", "\"There will not be one.\"" },
    { "> ATTENDANCE, FLOOR 3", "Present: [static]", "Absent: [static]", "Reconciliation: FAILED. Counts do not match." },
  },
  research = {
    { "> RESEARCH SUMMARY (ENGINEERING DIV.)", "\"The apparatus performed within tolerance.\"", "\"We are not the cause. I will stake my name on it.\"", "-- signature illegible" },
    { "> RESEARCH SUMMARY (ARCANE DIV.)", "\"The engineers believe they opened it.\"", "\"They are mistaken. It was already open.\"", "\"We only learned to notice.\"" },
    { "> CROSS-DIVISION MEMO", "\"Two departments. Two explanations. One event.\"", "\"Both cannot be right. Both are certain.\"", "[remainder purged]" },
    { "> BOUND TESTIMONY (UNVERIFIED SOURCE)", "\"We were not summoned.\"", "\"We were already here.\"" },
  },
  incident = {
    { "> INCIDENT LOG 04", "\"The chamber remained stable.\"", "\"External interference was not identified.\"", "\"It responded before we transmitted.\"" },
    { "> CONTAINMENT DIRECTIVE", "AREA 04:", "DO NOT OPEN.  DO NOT OBSERVE.  DO NOT COMMUNICATE.", "\"Observation is participation.\"" },
    { "> INCIDENT LOG 09", "\"We stopped the experiment.\"", "\"It did not stop.\"", "[data corrupted]" },
    { "> TIMELINE REBUILD", "...continuous until the final period...", "...after which no entries exist...", "...the gap is not damage. the gap is uniform..." },
  },
  evacuation = {
    { "> PROJECT HS-LGTFO // OBJECTIVES", "1. Preserve humanity.", "2. Find somewhere safe.", "3. Preserve the truth.", "[objective 3 flagged: FAILED]" },
    { "> FLIGHT RECORDER // LAUNCH", "\"Ignition nominal. Everyone aboard is silent.\"" },
    { "> FLIGHT RECORDER // ARRIVAL", "\"The world is green. We made it.\"", "\"Something is already here.\"", "\"It has been waiting the whole way.\"" },
    { "> COLONY MANIFEST", "COLONY SHIP: DEPLOYED.", "STATUS: UNKNOWN.", "[no further contact recorded]" },
  },
  anomalous = {
    { "> [DECRYPTING RECOVERED MEDIA...]", "\"It is not an enemy.\"", "\"It does not hate us. It does not know us.\"", "\"It is a repair process.\"" },
    { "> FRAGMENT // UNSOURCED", "\"We called it an attack.\"", "\"It is a correction.\"", "\"The error was us.\"" },
    { "> FRAGMENT // UNSOURCED", "\"There was nowhere untouched.\"", "\"Earth was only the first place we noticed.\"", "[recording continues; cannot be transcribed]" },
  },
}

local MENU = {
  { key = "1", id = "personnel",  label = "personnel" },
  { key = "2", id = "research",   label = "research"  },
  { key = "3", id = "incident",   label = "incident"  },
  { key = "4", id = "evacuation", label = "evacuation"},
  { key = "5", id = "anomalous",  label = "anomalous", locked = true },
}

local function line(text, c)
  setc(c or colours.lightGray)
  print(text)
  sleep(0.04)
end

local function header()
  term.clear(); term.setCursorPos(1, 1)
  setc(colours.green)
  print("OLD CIVILISATION ARCHIVE SYSTEM")
  print("recovered storage protocol // integrity degraded")
  setc(colours.white)
  print("")
end

local function shuffle(t)
  for i = #t, 2, -1 do
    local j = math.random(i)
    t[i], t[j] = t[j], t[i]
  end
  return t
end

local function showCategory(id)
  header()
  setc(colours.green); print("[ " .. id:upper() .. " ]"); setc(colours.white); print("")
  local pool = {}
  for _, e in ipairs(RECORDS[id]) do pool[#pool + 1] = e end
  shuffle(pool)
  local count = math.min(#pool, math.random(1, 2))
  for i = 1, count do
    local entry = pool[i]
    for j, ln in ipairs(entry) do
      local c = colours.lightGray
      if ln:find("REDACTED") or ln:find("static") or ln:find("corrupted") or ln:find("purged") or ln:find("%[") then c = colours.gray end
      if id == "anomalous" then c = (j == 1) and colours.gray or colours.red end
      line(ln, c)
    end
    print("")
  end
  setc(colours.gray); print("-- record ends. cross-reference unavailable --")
  setc(colours.white); print(""); print("Press any key.")
  os.pullEvent("key")
end

local function bootSequence()
  header()
  local steps = {
    "mounting recovered media bus... ok",
    "verifying archive integrity... FAILED (partial)",
    "reconstructing index... 41% recovered",
    "WARNING: portions of this archive were removed deliberately.",
  }
  for _, s in ipairs(steps) do
    local c = s:find("FAILED") and colours.red or (s:find("WARNING") and colours.red or colours.gray)
    line(s, c)
  end
  print(""); setc(colours.white); print("Press any key to browse recovered records.")
  os.pullEvent("key")
end

local function menu()
  while true do
    header()
    local media = mediaPresent()
    setc(colours.gray)
    print("recovered media detected: " .. (media and "YES" or "NO"))
    setc(colours.white); print("")
    print("available records:")
    for _, m in ipairs(MENU) do
      if m.locked and not media then
        setc(colours.gray); print("  [" .. m.key .. "] " .. m.label .. "   (ENCRYPTED)")
      else
        setc(colours.white); print("  [" .. m.key .. "] " .. m.label)
      end
    end
    setc(colours.gray); print("  [q] quit")
    setc(colours.green); write("\n> "); setc(colours.white)
    local input = read()
    if input == "q" or input == "quit" or input == "exit" then
      term.clear(); term.setCursorPos(1, 1); return
    end
    for _, m in ipairs(MENU) do
      if input == m.key or input == m.id then
        if m.locked and not mediaPresent() then
          header()
          setc(colours.red); print("ENCRYPTED RECORD.")
          setc(colours.gray); print("Insert recovered media into an attached disk drive,")
          print("then try again.")
          setc(colours.white); print(""); print("Press any key.")
          os.pullEvent("key")
        else
          showCategory(m.id)
        end
        break
      end
    end
  end
end

-- entry point
math.randomseed(os.epoch and os.epoch("utc") or os.time())
bootSequence()
menu()
