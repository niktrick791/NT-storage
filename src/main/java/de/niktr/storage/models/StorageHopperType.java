package de.niktr.storage.models;

public enum StorageHopperType {
    INPUT("Eingangstrichter", "§b", "Zieht Items ein und leitet sie drahtlos weiter"),
    OUTPUT("Ausgangstrichter", "§a", "Empfängt Filter-Items drahtlos in die Zielkiste"),
    FALLBACK("Restetrichter", "§e", "Empfängt alle Items ohne spezielle Zuordnung");

    private final String displayName;
    private final String color;
    private final String description;

    StorageHopperType(String displayName, String color, String description) {
        this.displayName = displayName;
        this.color = color;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisplayName(boolean isDe) {
        if (!isDe) {
            switch (this) {
                case INPUT: return "Input Hopper";
                case OUTPUT: return "Output Hopper";
                case FALLBACK: return "Fallback Hopper";
            }
        }
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public String getDescription() {
        return description;
    }

    public String getDescription(boolean isDe) {
        if (!isDe) {
            switch (this) {
                case INPUT: return "Pulls items in and forwards them wirelessly";
                case OUTPUT: return "Receives filtered items wirelessly into target chest";
                case FALLBACK: return "Receives all items without specific destination";
            }
        }
        return description;
    }

    public String getFormattedName() {
        return color + "§l" + displayName;
    }

    public String getFormattedName(boolean isDe) {
        return color + "§l" + getDisplayName(isDe);
    }
}
