package org.wso2.ballerinalang.compiler.packaging.resolve;


import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Resolver<T> {


    T combine(T t, String pathPart);

    Stream<T> expand(T t);

    Stream<T> expandBal(T t);

    T start();

    Stream<Path> finalize(T t);
}
