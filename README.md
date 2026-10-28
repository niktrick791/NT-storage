# NT-storage

[![Language: English](https://img.shields.io/badge/Language-English-blue.svg)](#)
[![Deutsch](https://img.shields.io/badge/Sprache-Deutsch-orange.svg)](README_DE.md)
[![Platform: Paper / Purpur / Spigot](https://img.shields.io/badge/Platform-Paper%20%7C%20Purpur%20%7C%20Spigot-green.svg)](#)
[![Minecraft: 1.8.8 - 1.21.4+](https://img.shields.io/badge/Minecraft-1.8.8--1.21.4%2B-brightgreen.svg)](#)
[![Java: 8+](https://img.shields.io/badge/Java-8%2B-red.svg)](#)
[![Donate with PayPal](https://img.shields.io/badge/Donate-PayPal-00457C.svg?logo=paypal&logoColor=white)](https://paypal.me/niktrick)
[![Support on Ko-fi](https://img.shields.io/badge/Support-Ko--fi-FF5E5B.svg?logo=kofi&logoColor=white)](https://ko-fi.com/niktrick)

[🇩🇪 Zur deutschen Version wechseln](README_DE.md) | [🇬🇧 English Version](README.md)

---

Universal, high-efficiency wireless item storage & strict container filtering plugin for Minecraft **1.8.8 to 1.21.4+** (Paper, Purpur, Spigot).

Provides a pipe-free wireless sorting pipeline connecting containers across chunks via Ender Hoppers, with strict item filtering that blocks wrong items from entering designated chests via shift-clicking, dragging, hotbar swapping, or vanilla hopper transfer.

---

## Features

- **📡 Wireless Sorting Pipeline**:
  - Connect intake chests and destination chests wirelessly without bulky redstone clocks, water channels, or laggy hopper lines.
  - Automatically transports matching items from Input chests into Output chests, with fallback overflow support.
- **🔒 Strict Material Filtering**:
  - Guarantees each registered storage chest strictly accepts only its assigned Minecraft items.
  - Blocks invalid insertions on Shift-clicks, cursor dragging, hotbar number keys, and container hopper transactions.
  - Double-chest aware: guards both halves automatically.
- **🛠️ Survival Craftable Wireless Hoppers**:
  - **Input Hopper**: 1x Hopper + 1x Ender Pearl (draws items into the system).
  - **Output Hopper**: 1x Hopper + 1x Ender Pearl + 1x Chest (delivers filtered items into target chests).
  - **Fallback Hopper**: 1x Hopper + 1x Ender Pearl + 1x Redstone (receives unassigned overflow items).
- **🖥️ In-Game Interactive GUI**:
  - `/storage gui` (or `/lager gui`) opens a full graphical management menu to inspect hoppers, configure item filters, view crafting recipes, and monitor sorting metrics.
- **🌐 Bilingual & Live Language Switching**:
  - Fully supports English and German.
  - Switch anytime live via `/storage lang <de|en>` or `/lager lang <de|en>` and in `config.yml`.
- **💾 Async Persistence & Lightweight Performance**:
  - Data persists automatically to YAML with zero chunk lag.

---

## Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/storage gui` | Opens the main wireless storage GUI | *None* |
| `/storage recipe` | Displays the crafting recipe for wireless hoppers | *None* |
| `/storage status` | Displays real-time sorting statistics | *None* |
| `/storage list` | Lists all your registered chests and hoppers | *None* |
| `/storage givehopper <player> [type]` | Gives a player wireless hoppers | `niktrick.storage.admin` |
| `/storage input` | Registers targeted chest as an input chest | `niktrick.storage.admin` |
| `/storage register` | Registers targeted chest as a storage chest | `niktrick.storage.admin` |
| `/storage unregister` | Removes targeted chest from the system | `niktrick.storage.admin` |
| `/storage map <MATERIAL>` | Assigns a material filter to targeted chest | `niktrick.storage.admin` |
| `/storage unmap <MATERIAL>` | Removes a material filter from targeted chest | `niktrick.storage.admin` |
| `/storage fallback` | Sets targeted chest as the overflow fallback chest | `niktrick.storage.admin` |
| `/storage reload` | Reloads `config.yml` and storage data | `niktrick.storage.admin` |
| `/storage lang <de\|en>` | Changes plugin language live (Global) | `niktrick.storage.admin` |

*(All commands also support the `/lager` alias).*

---

## Configuration (`config.yml`)

```yaml
# Language setting: "en" for English, "de" for German
language: "en"

enabled: true

sorting:
  scan-interval-ticks: 10
  max-items-per-cycle: 128

storage:
  load-chunks: false

effects:
  particles: true
  sounds: true
```

---

## Compatibility

- **Minecraft**: 1.8.8, 1.12.2, 1.16.5, 1.17, 1.18, 1.19, 1.20, 1.21.x+
- **Server Software**: Paper, Purpur, Spigot
- **Java**: 8 to 25+ (compiled to Java 8 bytecode for maximum compatibility)
---

## 💖 Support & Donations

If you enjoy this plugin, consider supporting future development:
- [PayPal Donation](https://paypal.me/niktrick)
- [Support on Ko-fi](https://ko-fi.com/niktrick)
- Donations are voluntary and directly help maintain multi-version compatibility!
