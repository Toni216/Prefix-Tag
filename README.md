# 🏷️ PrefixTag
This mod adds a fully configurable dual tier system to your server — one for Roleplay rank and one for PvP rank. Each player always displays a combined prefix in chat and above their head, with colors that reflect their tier level.

---

## ⚙️ Features
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

## 🛠️ Configuration

On first launch, a `prefixtag.toml` file is generated in your `config/` folder.

```toml
[tier_count]
  rol_tier_count = 5
  pvp_tier_count = 5

[rol_labels]
	rol1 = "R1"
	rol2 = "R2"
	rol3 = "R3"
	rol4 = "R4"
	rol5 = "R5"
	rol6 = "R6"
	rol7 = "R7"
	rol8 = "R8"
	rol9 = "R9"
	rol10 = "R10"

[pvp_labels]
	pvp1 = "P1"
	pvp2 = "P2"
	pvp3 = "P3"
	pvp4 = "P4"
	pvp5 = "P5"
	pvp6 = "P6"
	pvp7 = "P7"
	pvp8 = "P8"
	pvp9 = "P9"
	pvp10 = "P10"

[gui_texts]
	rol_title = "Select your Rol rank"
	rol_desc = "Choose the Rol rank you want. You can read about each rank in the designated Discord channel."
	pvp_title = "Select your PvP rank"
	pvp_desc = "Choose the PvP rank you want. You can read about each rank in the designated Discord channel."

[tier_colors]
	tier1_color = "green"
	tier2_color = "yellow"
	tier3_color = "gold"
	tier4_color = "red"
	tier5_color = "dark_red"
	tier6_color = "aqua"
	tier7_color = "light_purple"
	tier8_color = "blue"
	tier9_color = "dark_purple"
	tier10_color = "dark_gray"
```

## 💻 Commands

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

### Available colors
`black`, `dark_blue`, `dark_green`, `dark_aqua`, `dark_red`, `dark_purple`, `gold`, `gray`, `dark_gray`, `blue`, `green`, `aqua`, `red`, `light_purple`, `yellow`, `white`

### Player — No permissions required

| Command | Description |
|---|---|
| `/tierself setname <name>` | Set a custom display name (no color codes allowed) |
| `/tierself clearname` | Remove your custom display name |

---


## 🔧 Requirements
- Forge 1.20.1

---

## 📦 Installation
1. Download the `.jar` file
2. Place it in your `mods/` folder
3. Make sure you are running **Forge 1.20.1**
4. Start the world — the config file will be generated automatically

## 👤 Author

Made by me for CipolloLand server.
