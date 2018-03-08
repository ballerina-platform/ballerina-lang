package org.wso2.ballerinalang.compiler.packaging.resolve;

import java.nio.file.Path;
import java.util.stream.Stream;

public class StringResolver implements Resolver<String> {

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
