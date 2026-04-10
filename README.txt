# CipolloTiers

A Forge 1.20.1 mod for CipolloLand that adds a dual tier system to the server — one for Roleplay rank and one for PvP rank. Each player always displays a combined prefix in chat and above their head.

---

## Features

- **Two independent tier systems**: Rol (R1–R5) and PvP (P1–P5)
- **Always-visible prefix**: `[R2|P3] PlayerName` in chat and above the player's head
- **First-join GUI**: New players choose their Rol and PvP tier through a two-step selection screen
- **Admin commands**: Admins can assign, change or reset any tier at any time
- **NBT persistence**: Tier data is saved per player and survives server restarts

---

## Commands

| Command | Permission | Description |
|---|---|---|
| `/tier setrol <player> <1-5>` | Op level 2 | Assign a Rol tier to a player |
| `/tier setpvp <player> <1-5>` | Op level 2 | Assign a PvP tier to a player |
| `/tier check <player>` | Op level 2 | Check a player's current prefix |
| `/tier reset <player>` | Op level 2 | Reset a player's tiers — they will see the selection GUI on next login |

> `/tierself` is an internal command used by the GUI. It is not intended for manual use.

---

## Installation

1. Download the `.jar` from the [Releases](../../releases) page
2. Place it in your server's `mods/` folder
3. Make sure you are running **Forge 1.20.1**
4. Start the server — no configuration needed

---

## How it works

When a player joins for the first time, a selection screen appears asking them to choose their Rol tier (R1–R5), followed by their PvP tier (P1–P5). Once selected, the prefix `[Rx|Px]` appears permanently next to their name in chat and above their head in the world. Admins can override or reset any tier at any time using the `/tier` commands.

---

## Requirements

- Minecraft 1.20.1
- Forge 1.20.1

---

## Changelog

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
