package com.voronovsd.text.transformer.impl;

import java.util.Map;

public class EnRuSimpleTextLayoutTransformer extends BaseEnRuSimpleTextLayoutTransformer{
    @Override
    protected Map<Character, Character> getTransformMap() {
        return ruEnMap;
    }
}
