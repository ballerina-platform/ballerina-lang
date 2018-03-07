package org.wso2.ballerinalang.compiler.packaging.resolve;

import java.nio.file.Path;
import java.util.stream.Stream;

public class URIResolver implements Resolver<StringBuilder> {

    @Override
    public StringBuilder combine(StringBuilder stringBuilder, String pathPart) {
        return null;
    }

    @Override
    public Stream<StringBuilder> expand(StringBuilder stringBuilder) {
        return null;
    }

    @Override
    public Stream<StringBuilder> expandBal(StringBuilder stringBuilder) {
        return null;
    }

    @Override
    public StringBuilder start() {
        return new StringBuilder();
    }

    public Stream<Path> finalize(StringBuilder stringBuilder) {
        return null;
    }

}
