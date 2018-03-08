package org.wso2.ballerinalang.compiler.packaging;


import org.wso2.ballerinalang.compiler.packaging.resolve.Resolver;
import org.wso2.ballerinalang.compiler.packaging.resolve.StringResolver;

import java.nio.file.Path;
import java.util.stream.Stream;

public class Patten {
    public static final Part WILDCARD_DIR = new Part();
    public static final Part WILDCARD_BAL = new Part();
    public static final Part WILDCARD_BAL_WITH_TEST = new Part();
    public static final Patten NULL = new Patten() {
        @Override
        public <T> Stream<T> convert(Resolver<T> resolver) {
            return Stream.of();
        }
    };

    private static final Resolver<String> STRING_RESOLVER = new StringResolver();

    private final Part[] parts;

    public Patten(Part... parts) {
        this.parts = parts;
    }

    public static Part path(String... path) {
        return new Part(path);
    }

    public <T> Stream<T> convert(Resolver<T> resolver) {
        Stream<T> aggregate = Stream.of(resolver.start());
        for (Part part : parts) {
            if (part == WILDCARD_DIR) {
                aggregate = aggregate.flatMap(resolver::expand);
            } else if (part == WILDCARD_BAL) {
                aggregate = aggregate.flatMap(resolver::expandBal);
            } else if (part == WILDCARD_BAL_WITH_TEST) {
                aggregate = aggregate.flatMap(resolver::expandBal);
            } else {
                aggregate = aggregate.map(t -> callReduceForEach(t, part.values, resolver));
            }
        }
        return aggregate;
    }

    @SuppressWarnings("unchecked")
    public Stream<Path> convertToPaths(Resolver resolver) {
        return convert(resolver).flatMap(resolver::finalize);
    }

    public String toString() {
        return convert(STRING_RESOLVER).findFirst()
                                       .orElse("#");
    }

    //TODO: replace with Stream#combine(U, BiFunction<U,? super T,U>, BinaryOperator<U>) ?
    private <I> I callReduceForEach(I i, String[] values, Resolver<I> resolver) {
        I j = i;
        for (String value : values) {
            j = resolver.combine(j, value);
        }
        return j;
    }


    public static class Part {
        private final String[] values;

        private Part(String[] path) {
            this.values = path;
        }

        private Part() {
            values = new String[]{};
        }
    }

}
