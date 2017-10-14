package com.voronovsd.transformer.layout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LayoutTransformer {
    public static final String NAME = "name";
    public static final String SPACE = " ";
    public static final String BODY = "body";
    public static final String TITLE = "title";
    public static final String CHR = "chr";
    public static final String TEXT_1 = "text1";
    private static Map<Character, Character> ruEnMap = new HashMap<>();
    private static Map<Character, Character> ruEnMapWithoutAmpersand = new HashMap<>();

    static {
        ruEnMap.put('q', 'й');
        ruEnMap.put('w', 'ц');
        ruEnMap.put('e', 'у');
        ruEnMap.put('r', 'к');
        ruEnMap.put('t', 'е');
        ruEnMap.put('y', 'н');
        ruEnMap.put('u', 'г');
        ruEnMap.put('i', 'ш');
        ruEnMap.put('o', 'щ');
        ruEnMap.put('p', 'з');
        ruEnMap.put('[', 'х');
        ruEnMap.put('{', 'Х');
        ruEnMap.put(']', 'ъ');
        ruEnMap.put('}', 'Ъ');
        ruEnMap.put('|', '/');
        ruEnMap.put('`', 'ё');
        ruEnMap.put('~', 'Ё');
        ruEnMap.put('a', 'ф');
        ruEnMap.put('s', 'ы');
        ruEnMap.put('d', 'в');
        ruEnMap.put('f', 'а');
        ruEnMap.put('g', 'п');
        ruEnMap.put('h', 'р');
        ruEnMap.put('j', 'о');
        ruEnMap.put('k', 'л');
        ruEnMap.put('l', 'д');
        ruEnMap.put(';', 'ж');
        ruEnMap.put(':', 'Ж');
        ruEnMap.put('\"', 'э');
        ruEnMap.put('\"', 'Э');
        ruEnMap.put('z', 'я');
        ruEnMap.put('x', 'ч');
        ruEnMap.put('c', 'с');
        ruEnMap.put('v', 'м');
        ruEnMap.put('b', 'и');
        ruEnMap.put('n', 'т');
        ruEnMap.put('m', 'ь');
        ruEnMap.put(',', 'б');
        ruEnMap.put('<', 'Б');
        ruEnMap.put('.', 'ю');
        ruEnMap.put('>', 'Ю');
        ruEnMap.put('/', '.');
        ruEnMap.put('?', ',');
        ruEnMap.put('@', '\"');
        ruEnMap.put('#', '№');
        ruEnMap.put('$', ';');
        ruEnMap.put('^', ':');
        ruEnMap.put('&', '?');

        ruEnMapWithoutAmpersand.putAll(ruEnMap);
        ruEnMapWithoutAmpersand.remove('&');
    }

    public static void main(String[] args) throws IOException {
        Files.write(Paths.get("./resources/lessonPlans/export_lesson_plan_ru.json"), transformLessonPlanJson().getBytes());
    }

    private static String transformLessonPlanJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //Read from file
        JsonNode lessonPlan = null;
        try {
            lessonPlan = mapper.readValue(new File("./resources/lessonPlans/export_lesson_plan_en.json"), JsonNode.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (JsonNode lesson : lessonPlan.get("program").get("lessons")) {
            transformLesson(lesson);
        }
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(lessonPlan);
    }

    private static void transformLesson(JsonNode lesson) {
        ObjectNode mutableLesson = (ObjectNode) lesson;
        mutableLesson.set(NAME, convertSingleSymbolsUsingMap(lesson.get(NAME), ruEnMapWithoutAmpersand));

        JsonNode instructions = lesson.get("instruction").get("inst");
        if (instructions != null) {
            for (JsonNode instruction : instructions) {
                ObjectNode mutableInstruction = (ObjectNode) instruction;
                mutableInstruction.set(BODY, convertInBrackets(instruction.get(BODY)));
                mutableInstruction.set(TITLE, convertInBrackets(instruction.get(TITLE)));
                mutableInstruction.set(CHR, convertSingleSymbols(instruction.get(CHR)));
            }
        }
        JsonNode typing = lesson.get("typing");
        if (typing != null && !(typing instanceof NullNode)) {
            ObjectNode mutableTyping = (ObjectNode) typing;
            mutableTyping.set(TEXT_1, convert(typing.get(TEXT_1)));
        }
    }

    private static JsonNode convertInBrackets(JsonNode node) {
        String text = node.asText();
        Pattern regex = Pattern.compile("\\[\\w]");
        Matcher m = regex.matcher(text);
        StringBuffer result = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(result, convertInBrackets(m.group(0)));
        }
        m.appendTail(result);
        return new TextNode(result.toString());
    }

    private static String convertInBrackets(String source) {
        Preconditions.checkArgument(source.length() == 3);
        String partToChange = source.substring(1, 2);
        return "[" + changeLayoutToRussian(partToChange) + "]";
    }

    private static JsonNode convertSingleSymbols(JsonNode node) {
        return convertSingleSymbolsUsingMap(node, ruEnMap);
    }

    private static JsonNode convertSingleSymbolsUsingMap(JsonNode node, Map<Character, Character> conversionMap) {
        String text = node.asText();
        String[] parts = text.split(SPACE);
        List<String> resultParts = new ArrayList<>();

        for (String part : parts) {
            if (part.length() == 1) {
                part = changeLayoutToRussianUsingMap(part, conversionMap);
            }
            resultParts.add(part);
        }
        return new TextNode(Joiner.on(SPACE).join(resultParts));
    }

    private static JsonNode convert(JsonNode node) {
        String text = node.asText();
        return new TextNode(changeLayoutToRussian(text));
    }

    private static String changeLayoutToRussian(String source) {
        StringBuilder resultBuilder = new StringBuilder();
        for (char ch : source.toCharArray()) {
            boolean upperCase = Character.isUpperCase(ch);
            Character resultCh = ruEnMap.get(Character.toLowerCase(ch));
            if (resultCh != null) {
                if (upperCase) {
                    ch = Character.toUpperCase(resultCh);
                } else {
                    ch = resultCh;
                }
            }
            resultBuilder.append(ch);
        }
        return resultBuilder.toString();
    }

    private static String changeLayoutToRussianUsingMap(String source, Map<Character, Character> conversionMap) {
        StringBuilder resultBuilder = new StringBuilder();
        for (char ch : source.toCharArray()) {
            boolean upperCase = Character.isUpperCase(ch);
            Character resultCh = conversionMap.get(Character.toLowerCase(ch));
            if (resultCh != null) {
                if (upperCase) {
                    ch = Character.toUpperCase(resultCh);
                } else {
                    ch = resultCh;
                }
            }
            resultBuilder.append(ch);
        }
        return resultBuilder.toString();
    }
}
