package com.compe.treehole.dto;

import java.time.LocalDate;
import java.util.List;

public record StarMapResponse(
        LocalDate periodStart,
        LocalDate periodEnd,
        String dominantEmotion,
        String summaryText,
        List<StarPoint> points
) {
    public record StarPoint(
            double x,
            double y,
            int radius,
            String color,
            String emotionTag,
            LocalDate sourceDate,
            double intensity,
            String label
    ) {
    }
}
