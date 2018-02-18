package com.voronovsd.text.transformer.impl;

import com.voronovsd.text.transformer.TextTransformer;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseEnRuSimpleTextLayoutTransformer implements TextTransformer {

    protected Map<Character, Character> ruEnMap = new HashMap<>();

    {
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
        ruEnMap.put('\'', 'э');
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
    }

    @Override
    public String transform(String source) {
        StringBuilder resultBuilder = new StringBuilder();
        for (char ch : source.toCharArray()) {
            boolean upperCase = Character.isUpperCase(ch);
            Character resultCh = getTransformMap().get(Character.toLowerCase(ch));
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

    protected abstract Map<Character, Character> getTransformMap();
}
