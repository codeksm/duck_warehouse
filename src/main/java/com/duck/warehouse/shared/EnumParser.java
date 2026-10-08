package com.duck.warehouse.shared;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.duck.warehouse.exception.InvalidInputException;

/** Single place where free-text API input becomes an enum (case-insensitive, label or name). */
public final class EnumParser {
    private EnumParser() {}

    public static <E extends Enum<E> & Labeled> E parse(Class<E> type, String field, String raw) {
        if (raw != null) {
            String value = raw.trim();
            for (E constant : type.getEnumConstants()) {
                if (constant.label().equalsIgnoreCase(value) || constant.name().equalsIgnoreCase(value)) {
                    return constant;
                }
            }
        }
        String allowed = Arrays.stream(type.getEnumConstants()).map(Labeled::label).collect(Collectors.joining(", "));
        throw new InvalidInputException("Invalid " + field + " '" + raw + "'. Allowed values: " + allowed);
    }
}
