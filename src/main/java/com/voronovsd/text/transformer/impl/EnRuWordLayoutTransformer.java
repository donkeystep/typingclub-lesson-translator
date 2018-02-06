package com.voronovsd.text.transformer.impl;

import com.voronovsd.text.transformer.TextTransformer;

import java.io.File;


public class EnRuWordLayoutTransformer implements TextTransformer {
    private static final String RU_FREQUENCY_DICTIONARY_PATH = "./resources/freq_rus_utf.txt";

    {
        File frequencyDictionary = new File(RU_FREQUENCY_DICTIONARY_PATH);

    }
    @Override
    public String transform(String source) {
        return ;
    }
}
