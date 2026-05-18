package com.compe.treehole.learning;

public class MethodPractice {

    public static void main(String[] args) {
        String message = "I feel anxious today, but I want to say it out";
        String emotionTag = "ANXIOUS";

        System.out.println("is blank message: " + isBlankMessage(message));
        System.out.println("is valid message length: " + isValidMessageLength(message));
        System.out.println("emotion color: " + getEmotionColor(emotionTag));
    }

    public static boolean isBlankMessage(String message) {
        return message == null || message.trim().isEmpty();
    }

    public static boolean isValidMessageLength(String message) {
        if (message == null) {
            return false;
        }
        return message.length() >= 1 && message.length() <= 2000;
    }

    public static String getEmotionColor(String emotionTag) {
        if ("CALM".equals(emotionTag)) {
            return "#7EC8E3";
        }
        if ("SAD".equals(emotionTag)) {
            return "#6C7BFF";
        }
        if ("ANXIOUS".equals(emotionTag)) {
            return "#F6C177";
        }
        if ("ANGRY".equals(emotionTag)) {
            return "#FF6B6B";
        }
        if ("HOPEFUL".equals(emotionTag)) {
            return "#7BD389";
        }
        return "#CCCCCC";
    }
}
