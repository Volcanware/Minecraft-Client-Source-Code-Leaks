// 
// Decompiled by Procyon v0.5.36
// 

package com.beust.jcommander.converters;

import com.beust.jcommander.IStringConverter;

public class StringConverter implements IStringConverter<String>
{
    @Override
    public String convert(final String s) {
        return s;
    }
}
