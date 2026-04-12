# PrefixTag

A Forge 1.20.1 mod for CipolloLand that adds a dual tier system to the server — one for Roleplay rank and one for PvP rank. Each player always displays a combined prefix in chat and above their head, with colors that reflect their tier level.

---

## Features

- **Two independent tier systems**: Rol (R1–R5) and PvP (P1–P5)
- **Always-visible prefix**: `[R2|P3] PlayerName` in chat and above the player's head
- **Tier colors**: each tier has its own color (green to red scale) — brackets stay white to avoid conflicts with other mods
- **Name color**: admins can set a custom color for each player's name
- **First-join GUI**: new players choose their Rol and PvP tier through a two-step selection screen
- **Admin commands**: admins can assign, change, reset tiers and set name colors at any time
- **Config file**: labels, GUI texts and tier colors are fully customizable via `prefixtag.toml`
- **NBT persistence**: all data is saved per player and survives server restarts

---

## Commands

| Command | Permission | Description |
|---|---|---|
| `/tier setrol <player> <1-5>` | Op level 2 | Assign a Rol tier to a player |
| `/tier setpvp <player> <1-5>` | Op level 2 | Assign a PvP tier to a player |
| `/tier check <player>` | Op level 2 | Check a player's current prefix |
| `/tier reset <player>` | Op level 2 | Reset a player's tiers — they will see the selection GUI on next login |
| `/tier setcolor <player> <color>` | Op level 2 | Set the color of a player's name in chat and nametag |

> `/tierself` is an internal command used by the GUI. It is not intended for manual use.

### Available colors
`black`, `dark_blue`, `dark_green`, `dark_aqua`, `dark_red`, `dark_purple`, `gold`, `gray`, `dark_gray`, `blue`, `green`, `aqua`, `red`, `light_purple`, `yellow`, `white`

---

## Configuration

On first launch, PrefixTag generates a `prefixtag.toml` file in the `config/` folder. You can edit:

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

When a player joins for the first time, a selection screen appears asking them to choose their Rol tier (R1–R5), followed by their PvP tier (P1–P5). Once selected, the prefix `[Rx|Px]` appears permanently next to their name in chat and above their head in the world. Admins can override, reset or recolor any player's data at any time using the `/tier` commands.

---

## Requirements

- Minecraft 1.20.1
- Forge 1.20.1

---

## Changelog

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

This mod is part of the **Toni's Mods** project and is intended for use on CipolloLand. All rights reserved.
