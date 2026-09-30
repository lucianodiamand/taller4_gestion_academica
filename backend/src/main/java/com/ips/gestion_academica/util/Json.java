package com.ips.gestion_academica.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilidad JSON minima escrita en Java estandar (sin dependencias externas).
 * Soporta los tipos basicos: objeto, arreglo, string, numero, booleano y null.
 */
public final class Json {

    private Json() {
    }

    /** Convierte un string JSON en un grafo de Map/List/String/Number/Boolean/null. */
    public static Object parse(String json) {
        return new Parser(json).parseValue();
    }

    /** Convierte un grafo de Map/List/String/Number/Boolean/null en un string JSON. */
    public static String stringify(Object value) {
        StringBuilder sb = new StringBuilder();
        writeValue(sb, value);
        return sb.toString();
    }

    private static void writeValue(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String s) {
            writeString(sb, s);
        } else if (value instanceof Boolean b) {
            sb.append(b ? "true" : "false");
        } else if (value instanceof Number n) {
            sb.append(n);
        } else if (value instanceof Map<?, ?> map) {
            writeObject(sb, map);
        } else if (value instanceof List<?> list) {
            writeArray(sb, list);
        } else {
            writeString(sb, value.toString());
        }
    }

    private static void writeObject(StringBuilder sb, Map<?, ?> map) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            writeString(sb, String.valueOf(entry.getKey()));
            sb.append(':');
            writeValue(sb, entry.getValue());
        }
        sb.append('}');
    }

    private static void writeArray(StringBuilder sb, List<?> list) {
        sb.append('[');
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            writeValue(sb, list.get(i));
        }
        sb.append(']');
    }

    private static void writeString(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }

    private static final class Parser {

        private final String s;
        private int i;

        Parser(String s) {
            this.s = s;
        }

        Object parseValue() {
            skipWhitespace();
            char c = peek();
            return switch (c) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> {
                    expect("true");
                    yield Boolean.TRUE;
                }
                case 'f' -> {
                    expect("false");
                    yield Boolean.FALSE;
                }
                case 'n' -> {
                    expect("null");
                    yield null;
                }
                default -> parseNumber();
            };
        }

        private Map<String, Object> parseObject() {
            expect('{');
            Map<String, Object> map = new LinkedHashMap<>();
            skipWhitespace();
            if (peek() == '}') {
                i++;
                return map;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                map.put(key, parseValue());
                skipWhitespace();
                char c = next();
                if (c == '}') {
                    break;
                }
                if (c != ',') {
                    throw error("se esperaba ',' o '}'");
                }
            }
            return map;
        }

        private List<Object> parseArray() {
            expect('[');
            List<Object> list = new ArrayList<>();
            skipWhitespace();
            if (peek() == ']') {
                i++;
                return list;
            }
            while (true) {
                list.add(parseValue());
                skipWhitespace();
                char c = next();
                if (c == ']') {
                    break;
                }
                if (c != ',') {
                    throw error("se esperaba ',' o ']'");
                }
            }
            return list;
        }

        private String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = next();
                if (c == '"') {
                    break;
                }
                if (c == '\\') {
                    char esc = next();
                    switch (esc) {
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> sb.append((char) parseUnicode());
                        default -> throw error("escape invalido");
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private int parseUnicode() {
            int code = 0;
            for (int k = 0; k < 4; k++) {
                int digit = Character.digit(next(), 16);
                if (digit < 0) {
                    throw error("unicode invalido");
                }
                code = code * 16 + digit;
            }
            return code;
        }

        private Object parseNumber() {
            int start = i;
            while (i < s.length() && "0123456789-+eE.".indexOf(s.charAt(i)) >= 0) {
                i++;
            }
            String text = s.substring(start, i);
            if (text.indexOf('.') >= 0 || text.indexOf('e') >= 0 || text.indexOf('E') >= 0) {
                return Double.parseDouble(text);
            }
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException e) {
                return Double.parseDouble(text);
            }
        }

        private void skipWhitespace() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
                i++;
            }
        }

        private char peek() {
            if (i >= s.length()) {
                throw error("fin inesperado");
            }
            return s.charAt(i);
        }

        private char next() {
            if (i >= s.length()) {
                throw error("fin inesperado");
            }
            return s.charAt(i++);
        }

        private void expect(char c) {
            if (next() != c) {
                throw error("se esperaba '" + c + "'");
            }
        }

        private void expect(String str) {
            for (int k = 0; k < str.length(); k++) {
                if (next() != str.charAt(k)) {
                    throw error("se esperaba '" + str + "'");
                }
            }
        }

        private IllegalStateException error(String message) {
            return new IllegalStateException("JSON invalido (" + message + ") en posicion " + i);
        }
    }
}
