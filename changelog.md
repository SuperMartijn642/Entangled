### Entangled 1.3.18
- Removed blacklist entries for Applied Energistics and Refined Storage blocks
- Networking capabilities from Refined Storage will no longer be passed on by entangled blocks
- Fixed already bound entangled block items not being placeable
- Fixed crash when generating tooltip for entangled blocks bound to a custom dimension
- Fixed entangled binder's selected block not actually clearing when it says so

### Entangled 1.3.17
- Added a tag for invalid target blocks
- Added a tag blocks and block entities which should not be rendered
- Blocks from Applied Energistics and Refined Storage are now blacklisted by default to avoid buggy behaviour
- Improved Waila/Jade message coloring
- Fixed some ways to bypass the `maxDistance` and `allowDimensional` config options
- Fixed some cases where the incorrect dimension id was checked

### Entangled 1.3.16a
- Fixed bound block names in Waila and The One Probe tips
- Fixed selected block highlight offset when far away from spawn

### Entangled 1.3.16
- Fixed some blocks not visually connecting to the entangled block

### Entangled 1.3.15
- Errors from rendering blocks inside entangled blocks will now be reported instead of crashing the game

### Entangled 1.3.14
- Fixed pipes sometimes disconnecting

### Entangled 1.3.13a
- Fixed crash when saving entangled block data

### Entangled 1.3.13
- Updated Brazilian Portuguese translations (thanks to FITFC!)

### Entangled 1.3.12
- Updated to core library 1.1
- Fix rare server crash when the area of the bound block is unloaded
