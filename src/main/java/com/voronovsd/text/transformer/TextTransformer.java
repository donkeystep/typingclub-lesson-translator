package com.voronovsd.text.transformer;

public interface TextTransformer {
    /**
     * Transforms source String into target String
     * @param source
     * @return target String
     */
    String transform(String source);
}
