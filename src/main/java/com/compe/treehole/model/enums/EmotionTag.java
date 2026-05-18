package com.compe.treehole.model.enums;

import java.util.Arrays;

public enum EmotionTag {
    CALM("#7EC8E3", "平静"),
    SAD("#6C7BFF", "难过"),
    ANXIOUS("#F6C177", "焦虑"),
    ANGRY("#FF6B6B", "生气"),
    HOPEFUL("#7BD389", "有希望");

    private final String color;
    private final String label;

    EmotionTag(String color, String label) {
        this.color = color;
        this.label = label;
    }

    public String color() {
        return color;
    }

    public String label() {
        return label;
    }

    public static EmotionTag safeValueOf(String value) {
        if (value == null || value.isBlank()) {
            return CALM;
        }
        return Arrays.stream(values())
                .filter(tag -> tag.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(CALM);
    }
}
