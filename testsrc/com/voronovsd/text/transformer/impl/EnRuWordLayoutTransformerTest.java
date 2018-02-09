package com.voronovsd.text.transformer.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class EnRuWordLayoutTransformerTest {
    private EnRuWordLayoutTransformer  transformer = new EnRuWordLayoutTransformer();

    @Test
    public void testTransformWord(){
        assertEquals("еда", transformer.transform("tlf"));
    }

    @Test
    public void testTransformOneFingerWord(){
        assertEquals("should transform by-symbol", "кам", transformer.transform("rfv"));
        assertEquals("should transform by-symbol", "зж.хэъ", transformer.transform("p;/[']"));
    }

    @Test
    public void testTransformWordWithCapitalLetter(){
        assertEquals("Еда", transformer.transform("Tlf"));
    }

    @Test
    public void testTransformWordMixedSymbols(){
        assertEquals("Еда", transformer.transform("Lft"));
    }

    @Test
    public void testTransformPhrase() {
        assertEquals("еда еда", transformer.transform("Hat Horse Great Him At Home Got Hair About Give He House Lot Gave Get How Game Go Demand Green Happy Good Heard Kept Hurry Help Here Have Head Almost Hello Gold"));
    }

    @Test
    public void testTransformPhraseWithComma(){
        assertEquals("еда, еда", transformer.transform("tlf, tlf"));
    }

    @Test
    public void testTransformWithNotEnoughLength(){
        assertEquals("чабан", transformer.transform("xf,y"));
    }

}
