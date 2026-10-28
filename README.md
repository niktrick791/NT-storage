# 📦 niktrick-storage — Strict Storage Filters & Wireless Item Sorting

[![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Purpur-brightgreen.svg)](#)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21+-blue.svg)](#)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](#)

A standalone Minecraft Paper/Purpur plugin that adds **strict material filters** and an intelligent wireless hopper sorting system to chests, double chests, hoppers, and containers.

It guarantees that only designated items are placed into registered storage chests, preventing wrong items from cluttering your sorting system through shift-clicking, dragging, hotbar swapping, hopper pushes, or copper golem transfers.

---

## ✨ Features

- **🔒 Strict Material Filtering**:
  - Each registered chest only accepts its assigned Minecraft materials.
  - No accidental insertions: blocks invalid items on Shift-clicks, cursor swaps, drag gestures, hotbar numbers, off-hand swaps, and hopper item movements.
  - Double-chest recognition: guards both halves automatically and prevents accidental asymmetric double-chest extensions.
- **📡 Wireless Sorting & Special Hoppers**:
  - Craftable special hoppers (Input, Storage, Leftover / Overflow) with Ender Pearl recipes.
  - Automatically transports matching items from input chests into designated storage chests wirelessly.
- **🖥️ In-Game Interactive GUI**:
  - `/storage menu` or `/storage info` opens interactive chest management, filter configuration, and hopper inspection.
- **⚡ High Performance & Hardened**:
  - Validated with over 120 automated test assertions across all Minecraft container events.

---

## 📜 Commands & Permissions

| Command | Permission | Description |
|---|---|---|
| `/storage info` | `niktrick.storage.use` | Displays mapped materials for the target chest |
| `/storage register` | `niktrick.storage.use` | Registers target chest in the storage system |
| `/storage map <Material>` | `niktrick.storage.use` | Maps a material to the target chest |
| `/storage unmap <Material>` | `niktrick.storage.use` | Removes a material mapping from the chest |
| `/storage menu` | `niktrick.storage.use` | Opens the storage GUI menu |
| `/storage reload` | `niktrick.storage.admin` | Reloads `config.yml` and `storage.yml` |

*(All commands also work via the `/lager` alias)*

---

## 📦 Installation

1. Drop `niktrick-storage-1.0.0.jar` into your server's `plugins/` folder.
2. Restart your server.
3. Target a chest and run `/storage register` and `/storage map DIAMOND`!
