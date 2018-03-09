package org.wso2.ballerinalang.compiler.packaging.resolve;


import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Contains methods used to convert patten in to set of usable objects (eg: set of paths)
 * using an intimidated representation of type T.
 * <p>
 * T can be a Path itself.
 *
 * @param <T> Type of intermediate repartition of the patten
 */
public interface Converter<T> {

    T combine(T t, String pathPart);

    Stream<T> expand(T t);

    Stream<T> expandBal(T t);

    T start();

    Stream<Path> finalize(T t);
}
