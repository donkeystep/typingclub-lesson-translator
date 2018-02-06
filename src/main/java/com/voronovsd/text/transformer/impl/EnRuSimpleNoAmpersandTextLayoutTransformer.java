package com.voronovsd.text.transformer.impl;

import java.util.Map;

public class EnRuSimpleNoAmpersandTextLayoutTransformer extends BaseEnRuSimpleTextLayoutTransformer{

    @Override
    protected Map<Character, Character> getTransformMap() {
        return ruEnMapWithoutAmpersand;
    }
}
