package com.voronovsd.text.transformer;

import java.util.Set;

public interface TextTransformer {
    /**
     * Transforms source String into target String
     * @param source
     * @return target String
     */
    String transform(String source);

    /**
     * Transforms source String into target String
     * @param source
     * @param targetSymbols - symbols that need to be saved in source
     * @return target String
     */
    String transformWithTargetSymbols(String source, Set<Character> targetSymbols);
}
