package org.wso2.ballerinalang.compiler.packaging.converters;

import java.net.URI;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Provide functions need to covert a patten to steam of by paths, by downloading them as url .
 */
public class URIConverter implements Converter<URI> {

    private final URI base;

    public URIConverter(URI base) {
        this.base = base;
    }

    @Override
    public URI start() {
        return base;
    }

    @Override
    public URI combine(URI s, String p) {
        return s.resolve(p + '/');
    }

    @Override
    public Stream<URI> expand(URI u) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<URI> expandBal(URI u) {
        throw new UnsupportedOperationException();

    }

    public Stream<Path> finalize(URI u) {
        return Stream.of();
    }

    @Override
    public String toString() {
        return base.toString();
    }

}
