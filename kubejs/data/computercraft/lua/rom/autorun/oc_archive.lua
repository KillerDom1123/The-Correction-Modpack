-- Subtle boot hint: the recovered machine announces the archive tools it still remembers.
local ok = pcall(function()
  if not term then return end
  local was = term.getTextColour and term.getTextColour() or colours.white
  if term.setTextColour then term.setTextColour(colours.grey) end
  print("recovered archive tools online. type: archive")
  if term.setTextColour then term.setTextColour(was) end
end)
