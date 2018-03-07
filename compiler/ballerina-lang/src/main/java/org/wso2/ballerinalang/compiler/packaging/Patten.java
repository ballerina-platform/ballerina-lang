package org.wso2.ballerinalang.compiler.packaging;


import org.wso2.ballerinalang.compiler.packaging.resolve.Resolver;

import java.nio.file.Path;
import java.util.stream.Stream;

public class Patten {
    public static final Part WILDCARD = new Part();
    public static final Part BAL_SANS_TEST_AND_RES = new Part();
    private static final Resolver<String> STRING_RESOLVER =
            Resolver.build("$",
                           (a, b) -> (a + "/" + b),
                           t -> Stream.of(t + "/*"),
                           t -> Stream.of(t + "/**~test~resources/*.bal"));

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
            if (part == WILDCARD) {
                aggregate = aggregate.flatMap(resolver::expand);
            } else if (part == BAL_SANS_TEST_AND_RES) {
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

    @Override
    public boolean equals(Object o) {
        // Only used for testing, so only an
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Patten patten = (Patten) o;

        return toString().equals(patten.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
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
