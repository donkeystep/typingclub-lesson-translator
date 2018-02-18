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
import com.voronovsd.text.transformer.TextTransformer;
import com.voronovsd.text.transformer.impl.EnRuSimpleTextLayoutTransformer;
import com.voronovsd.text.transformer.impl.EnRuWordLayoutTransformer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transforms English lesson plan to another layout.
 * Takes source file from SOURCE_LESSON_PLAN_FILE_PATH
 * Creates target file at TARGET_LESSON_PLAN_FILE_PATH
 *
 * Fields transformed are:
 * - lesson.name
 * - lesson.instruction.body
 * - lesson.instruction.title
 * - lesson.instruction.character
 * - lesson.text1
 */
public class LessonPlanLayoutTransformer {
    public static final String NAME = "name";
    public static final String SPACE = " ";
    public static final String BODY = "body";
    public static final String TITLE = "title";
    public static final String CHR = "chr";
    public static final String TEXT_1 = "text1";
    public static final String SOURCE_LESSON_PLAN_FILE_PATH = "./resources/lessonPlans/export_lesson_plan_en.json";
    public static final String TARGET_LESSON_PLAN_FILE_PATH = "./resources/lessonPlans/export_lesson_plan_ru.json";

    private static EnRuWordLayoutTransformer textTransformer = new EnRuWordLayoutTransformer();
    private static TextTransformer simpleTransformer = new EnRuSimpleTextLayoutTransformer();

    public static void main(String[] args) throws IOException {
        Files.write(Paths.get(TARGET_LESSON_PLAN_FILE_PATH), transformLessonPlanJson().getBytes());
        System.out.println("Transformed by symbol: " + textTransformer.getTransformedBySymbol());
        System.out.println("Transformed to word part: " + textTransformer.getTransformedToWordPart());
        System.out.println("Transformed to word: " + textTransformer.getTransformedToWord());
    }

    private static String transformLessonPlanJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //Read from file
        JsonNode lessonPlan = null;
        try {
            lessonPlan = mapper.readValue(new File(SOURCE_LESSON_PLAN_FILE_PATH), JsonNode.class);
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
        Set<Character> targetSymbols = new TreeSet<>();
        mutableLesson.set(NAME, convertSingleSymbolsExtractTarget(lesson.get(NAME), targetSymbols));

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
            mutableTyping.set(TEXT_1, convertTextNode(typing.get(TEXT_1), targetSymbols));
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
        return "[" + simpleTransformer.transform(partToChange) + "]";
    }

    private static JsonNode convertSingleSymbols(JsonNode node) {
        return convertSingleSymbolsExtractTarget(node, null);
    }

    private static JsonNode convertSingleSymbolsExtractTarget(JsonNode node, Set<Character> targetSymbols) {
        String text = node.asText();
        String[] parts = text.split(SPACE);
        List<String> resultParts = new ArrayList<>();

        for (String part : parts) {
            if (part.length() == 1 && !part.equals("&")) {
                part = textTransformer.transform(part);
                if(targetSymbols != null){
                    targetSymbols.add(part.charAt(0));
                }
            }
            resultParts.add(part);
        }
        return new TextNode(Joiner.on(SPACE).join(resultParts));
    }

    /**
     * Converts whole text content of a node to another layout.
     *
     * @param node
     * @param targetSymbols
     * @return
     */
    private static JsonNode convertTextNode(JsonNode node, Set<Character> targetSymbols) {
        String text = node.asText();

        return new TextNode(textTransformer.transformWithTargetSymbols(text, targetSymbols));
    }
}
