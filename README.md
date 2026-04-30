# PrefixTag
A Forge 1.20.1 mod that adds a fully configurable dual tier system to your server — one for Roleplay rank and one for PvP rank. Each player always displays a combined prefix in chat and above their head, with colors that reflect their tier level.

---

## Features
- **Two independent tier systems**: Rol and PvP, each with a configurable number of tiers (2–10)
- **Always-visible prefix**: `[R2|P3] PlayerName` in chat and above the player's head
- **Tier colors**: each tier has its own configurable color — brackets stay white to avoid conflicts with other mods
- **Name color**: admins can set a custom color for each player's name
- **Custom display name**: players can replace their username with a custom name via `/tierself setname`
- **Online indicator**: a green `●` can be toggled left of the prefix per player
- **First-join GUI**: new players choose their Rol and PvP tier through a two-step selection screen that adapts to the configured tier count
- **Admin commands**: admins can assign, change, reset tiers, set name colors, and manage custom names at any time
- **Config file**: tier count, labels, GUI texts and tier colors are fully customizable via `prefixtag.toml`
- **NBT persistence**: all data is saved per player and survives server restarts

---

## Commands

### Admin — Op level 2

| Command | Description |
|---|---|
| `/tier setrol <player> <1-10>` | Assign a Rol tier to a player |
| `/tier setpvp <player> <1-10>` | Assign a PvP tier to a player |
| `/tier check <player>` | Check a player's current prefix and display name |
| `/tier reset <player>` | Reset a player's tiers — they will see the selection GUI on next login |
| `/tier setcolor <player> <color>` | Set the color of a player's name in chat and nametag |
| `/tier clearname <player>` | Remove a player's custom display name |
| `/tier online <player> <true\|false>` | Toggle the green ● indicator left of the prefix |

### Player — No permissions required

| Command | Description |
|---|---|
| `/tierself setname <name>` | Set a custom display name (no color codes allowed) |
| `/tierself clearname` | Remove your custom display name |

### Available colors
`black`, `dark_blue`, `dark_green`, `dark_aqua`, `dark_red`, `dark_purple`, `gold`, `gray`, `dark_gray`, `blue`, `green`, `aqua`, `red`, `light_purple`, `yellow`, `white`

---

## Configuration
On first launch, PrefixTag generates a `prefixtag.toml` file in the `config/` folder. You can edit:
- **Tier count** — set between 2 and 10 tiers independently for Rol and PvP
- **Tier labels** — change `R1`, `R2`... to anything you want (e.g. `Novice`, `Master`)
- **PvP tier labels** — same for `P1`, `P2`...
- **GUI texts** — customize the title and description of the selection screen
- **Tier colors** — set the color for each tier level

---

## Installation
1. Download the `.jar` from the [Releases](../../releases) page
2. Place it in your server's `mods/` folder
3. Make sure you are running **Forge 1.20.1**
4. Start the server — the config file will be generated automatically

---

## How it works
When a player joins for the first time, a selection screen appears asking them to choose their Rol tier, followed by their PvP tier. Once selected, the prefix `[Rx|Px]` appears permanently next to their name in chat and above their head in the world. Admins can override, reset or recolor any player's data at any time using the `/tier` commands. Players can also set a custom display name that replaces their username while keeping their assigned name color.

---

## Requirements
- Forge 1.20.1

---

## Changelog

### v1.4
- Rol and PvP tier counts are now independently configurable (2–10) via `prefixtag.toml`
- Tier selection GUI adapts dynamically, displaying buttons in rows of up to 3
- Players can set a custom display name with `/tierself setname <name>`
- Players can remove their custom name with `/tierself clearname`
- Admins can remove a player's custom name with `/tier clearname <player>`
- Custom names respect the color assigned by admins automatically

### v1.3
- Added toggleable online indicator (`●`) left of the prefix
- Added `/tier online <player> <true|false>` command

### v1.2
- Added tier colors (green to red scale)
- Added `/tier setcolor <player> <color>` command
- Added configurable tier labels, GUI texts via `prefixtag.toml`
- Brackets and separator stay white to avoid conflicts with other mods

### v1.1
- Added prefix above player's head (nametag)
- Added `/tier reset <player>` command
- Removed forced colors to avoid conflicts with other mods

### v1.0
- Initial release
- Dual tier system (Rol + PvP)
- First-join GUI with two-step selection
- Admin commands: setrol, setpvp, check
- NBT persistence

---

## License
This mod is part of the **Toni's Mods** project. MIT License
