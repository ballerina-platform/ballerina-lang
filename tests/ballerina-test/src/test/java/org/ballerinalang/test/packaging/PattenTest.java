package org.ballerinalang.test.packaging;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.Resolver;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wso2.ballerinalang.compiler.packaging.Patten.path;

public class PattenTest {
    @Test
    public void testReduction() {
        Resolver<String> mock = mockResolver("root-dir",
                                             (a, b) -> a + " > " + b,
                                             null,
                                             null);
        Patten subject = new Patten(path("hello", "world"));

        List<String> strings = subject.convert(mock).collect(Collectors.toList());

        Assert.assertEquals(strings, Collections.singletonList("root-dir > hello > world"));
    }

    @Test
    public void testExpansion() {
        Patten subject = new Patten(Patten.WILDCARD_DIR);
        Resolver<String> mock = mockResolver("root-dir",
                                             null,
                                             s -> Stream.of(s + " > cache1",
                                                            s + " > cache2",
                                                            s + " > cache3"),
                                             null);
        List<String> strings = subject.convert(mock).collect(Collectors.toList());
        Assert.assertEquals(strings, Arrays.asList("root-dir > cache1",
                                                   "root-dir > cache2",
                                                   "root-dir > cache3"));
    }

    /**
     * Disabled because it fails in JVM 8
     * See: https://bugs.openjdk.java.net/browse/JDK-8075939
     */
    @Test(enabled = false)
    public void testLazy() {
        Patten subject = new Patten(Patten.WILDCARD_DIR);
        Resolver<String> mock = mockResolver("root-dir",
                                             null,
                                             s -> Stream.concat(Stream.of("", ""),
                                                                Stream.generate(() -> {
                                                                    Assert.fail("method called. Hence not lazy.");
                                                                    return "";
                                                                })),
                                             null);

        List<String> strings = subject.convert(mock).limit(1).collect(Collectors.toList());
        Assert.assertTrue(strings.isEmpty());
    }

    @Test
    public void testReductionAndExpansion() {
        Patten subject = new Patten(path("hello"), Patten.WILDCARD_DIR, path("world"));
        Resolver<String> mock = mockResolver("root-dir",
                                             (a, b) -> a + " > " + b,
                                             s -> Stream.of(s + " > cache1",
                                                            s + " > cache2",
                                                            s + " > cache3"),
                                             null);
        List<String> strings = subject.convert(mock).collect(Collectors.toList());
        Assert.assertEquals(strings, Arrays.asList("root-dir > hello > cache1 > world",
                                                   "root-dir > hello > cache2 > world",
                                                   "root-dir > hello > cache3 > world"));
    }

    private static <I> Resolver<I> mockResolver(I start,
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

}