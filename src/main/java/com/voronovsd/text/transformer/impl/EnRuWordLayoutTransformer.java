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
    private static final int MAX_ADDED_SYMBOLS_PER_WORD = 2;
    private TextTransformer simpleTransformer = new EnRuSimpleTextLayoutTransformer();
    private int transformedBySymbol;
    private int transformedToWord;
    private int transformedToWordPart;

    private Map<Set<Character>, TreeMap<Integer, String>> dictionary = new HashMap<>();
    private Set<Character> knownSymbols = new TreeSet<>();
    private Set<Character> targetSymbols = new TreeSet<>();
    private List<Set<Character>> fingerSets = new ArrayList<>();
    private String[] fingerStrings = new String[]{
            "йфя",
            "цыч",
            "увс",
            "камепи",
            "нртгоь",
            "шлб",
            "щдю",
            "зж.хэъ"
    };

    {
        try (Stream<String> lines = Files.lines(Paths.get(RU_FREQUENCY_DICTIONARY_PATH), Charset.defaultCharset())) {
            initializeDictionary(lines);
            knownSymbols.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(String fingerString: fingerStrings){
            Character[] charObjectArray = fingerString.chars()
                    .mapToObj(c -> (char)c)
                    .toArray(Character[]::new);
            fingerSets.add(new TreeSet<>(Arrays.asList(charObjectArray)));
        }
    }

    // TODO: use lesson description to find out target symbols and try to save only these, adding all other known.

    @Override
    public String transformWithTargetSymbols(String source, Set<Character> targetSymbols) {
        String russianBySymbol = simpleTransformer.transform(source);
        List<String> words = Splitter.on(" ").splitToList(russianBySymbol);

        knownSymbols.addAll(getAbc(russianBySymbol));
        knownSymbols.remove(' ');
        this.targetSymbols = targetSymbols;

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

    // TODO: if start from capital letter - lowercase, transform, capitalize first letter.
    private String transformRussianSymbolsToWord(String word) {
        Set<Character> abc = getAbc(word);
        if(isForOneFinger(abc)){
            return word;
        }

        StringBuilder notTransformed = new StringBuilder(word);
        StringBuilder transformed = new StringBuilder();
        int passCount = 0;
        while(notTransformed.length() > 0){
            transformed.append(transformPart(notTransformed));
            passCount++;
        }
        if(passCount == 1){
            transformedToWord++;
        }
        return transformed.toString();
    }

    private String transformPart(StringBuilder notTransformed) {
        String tempWord = notTransformed.toString();
        String result = null;
        boolean transformedToWordPartSuccessfully = false;
        while(tempWord.length() > 1){
            TreeMap<Integer, String> abcMap = tryGetAbcMap(tempWord);
            if(abcMap != null){
                Map.Entry<Integer, String> closestAbcEntry = abcMap.ceilingEntry(tempWord.length()-1);
                if(closestAbcEntry != null && closestAbcEntry.getKey() <= tempWord.length() + MAX_ADDED_SYMBOLS_PER_WORD){
                    result = closestAbcEntry.getValue();
                    transformedToWordPartSuccessfully = true;
                    break;
                }
            }
            tempWord = tempWord.substring(0, tempWord.length() - 1);
        }
        notTransformed.delete(0, tempWord.length());

        if(transformedToWordPartSuccessfully){
            transformedToWordPart++;
        } else {
            transformedBySymbol++;
            result = tempWord;
        }
        return result;
    }

    private TreeMap<Integer, String> tryGetAbcMap(String result) {
        Set<Character> abc = getAbc(result);
        TreeMap<Integer, String> abcMap = dictionary.get(abc);
        if(abcMap != null){
            return abcMap;
        }
        abcMap = tryGetAbcMapWithTargetSymbols(abc);
        if(abcMap != null){
            return abcMap;
        }
        return tryGetAbcMapWithAllKnownSymbols(abc);
    }

    private boolean isForOneFinger(Set<Character> abc) {
        for(Set<Character> fingerSet: fingerSets){
            if(fingerSet.containsAll(abc)){
                return true;
            }
        }
        return false;
    }

    private TreeMap<Integer, String> tryGetAbcMapWithAllKnownSymbols(Set<Character> abc) {
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
    private TreeMap<Integer, String> tryGetAbcMapWithTargetSymbols(Set<Character> abc) {
        for (Map.Entry<Set<Character>, TreeMap<Integer, String>> entry : dictionary.entrySet()) {
            Set<Character> entrySymbols = entry.getKey();
            if (entrySymbols.containsAll(abc)) {
                if (targetSymbols.containsAll(entrySymbols)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private Set<Character> getAbc(String word) {
        return new TreeSet<>(Lists.charactersOf(word));
    }

    public int getTransformedBySymbol() {
        return transformedBySymbol;
    }

    public int getTransformedToWord() {
        return transformedToWord;
    }

    public int getTransformedToWordPart() {
        return transformedToWordPart;
    }
}
