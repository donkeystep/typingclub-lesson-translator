package com.voronovsd.text.transformer.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.voronovsd.text.transformer.TextTransformer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class EnRuWordLayoutTransformer implements TextTransformer {
    private static final String RU_FREQUENCY_DICTIONARY_PATH = "resources/freq_rus_utf.txt";
    private TextTransformer simpleTransformer = new EnRuSimpleTextLayoutTransformer();
    private int transformedBySymbol;
    private int transformedToWord;

    private Map<String, TreeMap<Integer, String>> dictionary = new HashMap<>();

    {
        try (Stream<String> lines = Files.lines(Paths.get(RU_FREQUENCY_DICTIONARY_PATH), Charset.defaultCharset())) {
            initializeDictionary(lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: remember symbols of previous sources and add them to abc if nothing found
    // TODO: reconsider abc method to return "abcd" for "ac"
    @Override
    public String transform(String source) {
        String russianBySymbol = simpleTransformer.transform(source);
        List<String> words = Splitter.on(" ").splitToList(russianBySymbol);

        List<String> resultWords = words.stream()
                .map(word -> transformRussianSymbolsToWord(word))
                .collect(Collectors.toList());

        return Joiner.on(" ").join(resultWords);
    }

     // TODO: now abcMap contains only 1 word per length, should be list
    private void initializeDictionary(Stream<String> lines) {
        lines.forEachOrdered(line -> {
            String abc = getAbc(line);
            TreeMap<Integer, String> abcMap = dictionary.get(abc);
            if(abcMap == null){
                abcMap = new TreeMap<>();
                dictionary.put(abc, abcMap);
            }
            abcMap.put(line.length(), line);

        });
    }

    private String transformRussianSymbolsToWord(String word) {
        TreeMap<Integer, String> abcMap = dictionary.get(getAbc(word));
        if(abcMap == null){
            transformedBySymbol++;
            return word;
        }
        String result = abcMap.get(word.length());
        if(result == null){
            transformedBySymbol++;
            return word;
        }
        transformedToWord++;
        return result;
    }

    private String getAbc(String word) {
        Set<Character> symbolSet = new TreeSet<>();
        symbolSet.addAll(Lists.charactersOf(word));

        return Joiner.on("").join(symbolSet);
    }

    public int getTransformedBySymbol() {
        return transformedBySymbol;
    }

    public int getTransformedToWord() {
        return transformedToWord;
    }
}
