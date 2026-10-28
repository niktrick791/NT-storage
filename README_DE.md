# NT-storage

[![Sprache: Deutsch](https://img.shields.io/badge/Sprache-Deutsch-orange.svg)](#)
[![English](https://img.shields.io/badge/Language-English-blue.svg)](README.md)
[![Plattform: Paper / Purpur / Spigot](https://img.shields.io/badge/Plattform-Paper%20%7C%20Purpur%20%7C%20Spigot-green.svg)](#)
[![Minecraft: 1.8.8 - 1.21.4+](https://img.shields.io/badge/Minecraft-1.8.8--1.21.4%2B-brightgreen.svg)](#)
[![Java: 8+](https://img.shields.io/badge/Java-8%2B-red.svg)](#)

[🇬🇧 Switch to English Version](README.md) | [🇩🇪 Deutsche Version](README_DE.md)

---

Universelles, hocheffizientes drahtloses Lager- und striktes Behälter-Filter-Plugin für Minecraft **1.8.8 bis 1.21.4+** (Paper, Purpur, Spigot).

Ermöglicht eine komplett leitungsfreie drahtlose Sortierung von Kisten über Chunks hinweg mittels Ender-Trichtern und schützt Lagerkisten durch strikte Filter davor, dass falsche Items per Shift-Klick, Drag & Drop, Hotbar-Taste oder Vanilla-Trichter hineingelangen.

---

## Features

- **📡 Drahtloses Sortiersystem**:
  - Verbinde Eingangskisten und Zielkisten kabellos ohne riesige Trichter-Pipelines, Wasserstraßen oder lag-intensive Redstone-Uhren.
  - Befördert passende Items automatisch in definierte Lagerkisten, inklusive Restekisten-Unterstützung (Overflow Fallback).
- **🔒 Strikte Materialfilter**:
  - Garantiert, dass jede registrierte Lagerkiste ausnahmslos nur die ihr zugewiesenen Items akzeptiert.
  - Blockiert ungültiges Einsortieren bei Shift-Klick, Maus-Ziehen, Hotbar-Zifferntasten und Trichter-Weitergabe.
  - Doppelkisten-Erkennung: Beide Hälften werden automatisch synchron geschützt.
- **🛠️ Survival-Crafting-Rezepte**:
  - **Eingangstrichter**: 1x Trichter + 1x Enderperle (saugt Items ein und leitet sie drahtlos weiter).
  - **Ausgangstrichter**: 1x Trichter + 1x Enderperle + 1x Kiste (empfängt Filter-Items drahtlos in die Zielkiste).
  - **Restetrichter**: 1x Trichter + 1x Enderperle + 1x Redstone (empfängt alle nicht zugewiesenen Items).
- **🖥️ Interaktives In-Game GUI**:
  - `/lager gui` (oder `/storage gui`) öffnet das Hauptmenü zur Verwaltung von Trichtern, Konfiguration von Filtern, Anzeige der Crafting-Rezepte und Einsicht der Statistiken.
- **🌐 Zweisprachig & Live im Spiel umschaltbar**:
  - Vollständige deutsche und englische Unterstützung.
  - Live per In-Game-Befehl `/lager lang <de|en>` umschaltbar sowie in der `config.yml`.
- **💾 Asynchrone Speicherung & Maximale Performance**:
  - Alle Kisten und Trichter werden persistent in YAML gespeichert und belasten den Server nicht.

---

## Befehle & Permissions

| Befehl | Beschreibung | Permission |
| :--- | :--- | :--- |
| `/lager gui` | Öffnet das grafische Hauptmenü | *Keine* |
| `/lager recipe` | Zeigt das Crafting-Rezept für Wireless-Trichter | *Keine* |
| `/lager status` | Zeigt Echtzeit-Sortierstatistiken an | *Keine* |
| `/lager list` | Listet deine registrierten Kisten und Trichter auf | *Keine* |
| `/lager givehopper <spieler> [typ]` | Gibt einem Spieler Wireless-Trichter | `niktrick.storage.admin` |
| `/lager input` | Registriert die anvisierte Kiste als Eingangskiste | `niktrick.storage.admin` |
| `/lager register` | Registriert die anvisierte Kiste als Lagerkiste | `niktrick.storage.admin` |
| `/lager unregister` | Entfernt die anvisierte Kiste aus dem System | `niktrick.storage.admin` |
| `/lager map <MATERIAL>` | Weist der anvisierten Kiste einen Materialfilter zu | `niktrick.storage.admin` |
| `/lager unmap <MATERIAL>` | Entfernt einen Materialfilter von der Kiste | `niktrick.storage.admin` |
| `/lager fallback` | Setzt die anvisierte Kiste als Restekiste (Fallback) | `niktrick.storage.admin` |
| `/lager reload` | Lädt Konfiguration und Speicherdaten neu | `niktrick.storage.admin` |
| `/lager lang <de\|en>` | Ändert die Sprache des Plugins live (Global) | `niktrick.storage.admin` |

*(Alle Befehle funktionieren auch über den Alias `/storage`).*

---

## Konfiguration (`config.yml`)

```yaml
# Spracheinstellung: "en" für Englisch, "de" für Deutsch
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

## Kompatibilität

- **Minecraft**: 1.8.8, 1.12.2, 1.16.5, 1.17, 1.18, 1.19, 1.20, 1.21.x+
- **Server-Software**: Paper, Purpur, Spigot
- **Java**: 8 bis 25+ (kompiliert auf Java 8 Bytecode für maximale Kompatibilität)
