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

    private Map<Set<Character>, TreeMap<Integer, String>> dictionary = new HashMap<>();
    private Set<Character> knownSymbols = new TreeSet<>();


    {
        try (Stream<String> lines = Files.lines(Paths.get(RU_FREQUENCY_DICTIONARY_PATH), Charset.defaultCharset())) {
            initializeDictionary(lines);
            knownSymbols = new TreeSet<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String transform(String source) {
        String russianBySymbol = simpleTransformer.transform(source);
        List<String> words = Splitter.on(" ").splitToList(russianBySymbol);

        List<String> resultWords = words.stream()
                .map(word -> transformRussianSymbolsToWord(word))
                .collect(Collectors.toList());

        String result = Joiner.on(" ").join(resultWords);
        System.out.println(result);
        return result;
    }

    // TODO: now abcMap contains only 1 word per length, should be list
    private void initializeDictionary(Stream<String> lines) {
        lines.forEachOrdered(line -> {
            Set<Character> abc = getAbc(line);
            TreeMap<Integer, String> abcMap = dictionary.get(abc);
            if (abcMap == null) {
                abcMap = new TreeMap<>();
                dictionary.put(abc, abcMap);
            }
            abcMap.put(line.length(), line);

        });
    }

    private String transformRussianSymbolsToWord(String word) {
        Set<Character> abc = getAbc(word);
        TreeMap<Integer, String> abcMap = dictionary.get(abc);
        if (abcMap == null) {
            abcMap = tryGetAbcMapWithKnownSymbols(abc);
            if (abcMap == null) {
                transformedBySymbol++;
                return word;
            }
        }
        Map.Entry<Integer, String> closestAbcEntry = abcMap.ceilingEntry(word.length()-1);
        String result = null;
        if(closestAbcEntry != null){
            result = closestAbcEntry.getValue();
        }
        if (result == null) {
            transformedBySymbol++;
            return word;
        }
        transformedToWord++;
        return result;
    }

    private TreeMap<Integer, String> tryGetAbcMapWithKnownSymbols(Set<Character> abc) {
        for (Map.Entry<Set<Character>, TreeMap<Integer, String>> entry : dictionary.entrySet()) {
            Set<Character> entrySymbols = entry.getKey();
            if (entrySymbols.containsAll(abc)) {
                if (knownSymbols.containsAll(entrySymbols)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private Set<Character> getAbc(String word) {
        Set<Character> symbolSet = new TreeSet<>();
        symbolSet.addAll(Lists.charactersOf(word));

        knownSymbols.addAll(symbolSet);
        return symbolSet;
    }

    public int getTransformedBySymbol() {
        return transformedBySymbol;
    }

    public int getTransformedToWord() {
        return transformedToWord;
    }
}
