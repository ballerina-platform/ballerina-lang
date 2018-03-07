package org.wso2.ballerinalang.compiler.packaging.resolve;


import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Resolver<T> {

    static <I> Resolver<I> build(I start,
                                 BiFunction<I, String, I> combine,
                                 Function<I, Stream<I>> expand,
                                 Function<I, Stream<I>> expandBal) {
        return new Resolver<I>() {
            @Override
            public I combine(I i, String pathPart) {
                return combine.apply(i, pathPart);
            }

            @Override
            public Stream<I> expand(I i) {
                return expand.apply(i);
            }

            @Override
            public Stream<I> expandBal(I i) {
                return expandBal.apply(i);
            }

            @Override
            public I start() {
                return start;
            }

            @Override
            public Stream<Path> finalize(I i) {
                throw new UnsupportedOperationException();
            }
        };
    }

    T combine(T t, String pathPart);

    Stream<T> expand(T t);

    Stream<T> expandBal(T t);

    T start();

    Stream<Path> finalize(T t);
}
