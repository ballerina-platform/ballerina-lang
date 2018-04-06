package org.wso2.ballerinalang.compiler.packaging;


import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageSourceEntry;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.StringConverter;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Patten of a set of resource (usually source files).
 * EG: patten of bal in a jar. url patten. file patten.
 *
 * Made out of 4 types of parts.
 * Path                      - exact match to the a dir or file name
 * Wildcard                  - any dir matches
 * Wildcard source           - marches any source files in the dir or
 * in deeper dir, skipping any tests sources
 * Wildcard source with test - marches any source files in the dir or
 * in deeper dir including test source
 */
public class Patten {
    public static final Part WILDCARD_DIR = new Part();
    public static final Part WILDCARD_SOURCE = new Part();
    public static final Part WILDCARD_SOURCE_WITH_TEST = new Part();
    public static final Patten NULL = new Patten() {
        @Override
        public <T> Stream<T> convert(Converter<T> converter) {
            return Stream.of();
        }
    };

    private static final Converter<String> STRING_CONVERTER = new StringConverter();

    private final Part[] parts;

    public Patten(Part... parts) {
        this.parts = parts;
    }

    public Patten sibling(Part siblingPart) {
        Part last = parts[parts.length - 1];
        Part[] newParts;
        if (last == WILDCARD_DIR || last == WILDCARD_SOURCE || last == WILDCARD_SOURCE_WITH_TEST) {
            newParts = Arrays.copyOf(parts, parts.length);
            newParts[newParts.length - 1] = siblingPart;
        } else {
            newParts = Arrays.copyOf(parts, parts.length + 1);
            String[] newValues = Arrays.copyOf(last.values, last.values.length - 1);
            newParts[newParts.length - 2] = new Part(newValues);
            newParts[newParts.length - 1] = siblingPart;
        }
        return new Patten(newParts);
    }

    public static Part path(String... path) {
        return new Part(path);
    }

    public <T> Stream<T> convert(Converter<T> converter) {
        Stream<T> aggregate = Stream.of(converter.start());
        for (Part part : parts) {
            if (part == WILDCARD_DIR) {
                aggregate = aggregate.flatMap(converter::expand);
            } else if (part == WILDCARD_SOURCE) {
                aggregate = aggregate.flatMap(converter::expandBal);
            } else if (part == WILDCARD_SOURCE_WITH_TEST) {
                //TODO: add test patten converter
                aggregate = aggregate.flatMap(converter::expandBal);
            } else {
                aggregate = aggregate.map(t -> callReduceForEach(t, part.values, converter));
            }
        }
        return aggregate;
    }

    @SuppressWarnings("unchecked")
    public Stream<PackageSourceEntry> convertToSources(Converter converter, PackageID id) {
        return convert(converter).flatMap(t -> converter.finalize(t, id));
    }

    public String toString() {
        return convert(STRING_CONVERTER).findFirst()
                                        .orElse("#");
    }

    //TODO: replace with Stream#combine(U, BiFunction<U,? super T,U>, BinaryOperator<U>) ?
    private <I> I callReduceForEach(I i, String[] values, Converter<I> converter) {
        I j = i;
        for (String value : values) {
            j = converter.combine(j, value);
        }
        return j;
    }


    /**
     * Part of a Patten.
     */
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
