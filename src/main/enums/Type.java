package main.enums;

public enum Type {
    INTERNAL, EXTERNAL;

    public static Type fromDirectoryName(String dir) {
        if (dir == null) return null;
        String d = dir.trim().toLowerCase();
        return switch (d) {
            case "internal" -> INTERNAL;
            case "external" -> EXTERNAL;
            default -> throw new IllegalArgumentException("Unknown type directory: " + dir);
        };
    }

    public String toDirectoryName() {
        return this == INTERNAL ? "Internal" : "External";
    }
}
