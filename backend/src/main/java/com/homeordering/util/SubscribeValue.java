package com.homeordering.util;

/**
 * 按微信订阅消息关键词类型截断文案，避免超长被拒。
 */
public final class SubscribeValue {

    private SubscribeValue() {
    }

    public static String fit(String keyword, String raw) {
        String type = keywordType(keyword);
        String value = plain(raw);
        return switch (type) {
            case "time", "date" -> value.isEmpty() ? "2020-01-01 00:00" : value;
            case "thing" -> clip(value.isEmpty() ? "无" : value, 20);
            case "name" -> head(value.isEmpty() ? "家人" : value, 10);
            case "phrase" -> head(chineseOnly(value), 5);
            case "number", "letter", "character_string" -> head(value.isEmpty() ? "0" : value, 32);
            case "phone_number" -> head(value.isEmpty() ? "0" : value, 17);
            case "car_number" -> head(value.isEmpty() ? "无" : value, 8);
            case "symbol" -> head(value.isEmpty() ? "无" : value, 5);
            default -> clip(value.isEmpty() ? "无" : value, 20);
        };
    }

    static String keywordType(String keyword) {
        if (keyword == null) {
            return "";
        }
        String key = keyword.trim().toLowerCase();
        int end = key.length();
        while (end > 0 && Character.isDigit(key.charAt(end - 1))) {
            end--;
        }
        return key.substring(0, end);
    }

    private static String chineseOnly(String value) {
        StringBuilder builder = new StringBuilder();
        value.codePoints().forEach(cp -> {
            if (cp >= 0x4E00 && cp <= 0x9FFF) {
                builder.appendCodePoint(cp);
            }
        });
        String text = builder.toString();
        return text.isEmpty() ? "新订单" : text;
    }

    private static String head(String value, int max) {
        if (value.codePointCount(0, value.length()) <= max) {
            return value;
        }
        return value.substring(0, value.offsetByCodePoints(0, max));
    }

    private static String plain(String raw) {
        if (raw == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        raw.codePoints().forEach(cp -> {
            if (cp == '\n' || cp == '\r' || cp == '\t') {
                builder.append(' ');
            } else if (cp >= 0x20) {
                builder.appendCodePoint(cp);
            }
        });
        return builder.toString().trim().replaceAll(" {2,}", " ");
    }

    private static String clip(String value, int max) {
        if (value.codePointCount(0, value.length()) <= max) {
            return value;
        }
        int end = value.offsetByCodePoints(0, Math.max(1, max - 1));
        return value.substring(0, end) + "…";
    }
}
