package org.wso2.ballerinalang.compiler.packaging.converters;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Provide functions need to covert a patten to a string.
 */
public class StringConverter implements Converter<String> {

    @Override
    public String combine(String a, String b) {
        return (a + "/" + b);
    }

    @Override
    public Stream<String> expand(String s) {
        return Stream.of(s + "/*");
    }

    @Override
    public Stream<String> expandBal(String t) {
        return Stream.of(t + "/**~test~resources/*.bal");
    }

    @Override
    public String start() {
        return "$";
    }

    @Override
    public Stream<Path> finalize(String s) {
        throw new UnsupportedOperationException();
    }
}
