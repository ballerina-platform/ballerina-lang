package org.ballerinalang.test.packaging;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageSourceEntry;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wso2.ballerinalang.compiler.packaging.Patten.path;

public class PattenTest {
    private static <I> Converter<I> mockResolver(I start,
                                                 BiFunction<I, String, I> combine,
                                                 Function<I, Stream<I>> expand,
                                                 Function<I, Stream<I>> expandBal) {
        return new Converter<I>() {
            @Override
            public I combine(I i, String pathPart) {
                return combine.apply(i, pathPart);
            }

            @Override
            public Stream<I> latest(I i) {
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
            public Stream<PackageSourceEntry> finalize(I i, PackageID id) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Test
    public void testReduction() {
        Converter<String> mock = mockResolver("root-dir",
                                              (a, b) -> a + " > " + b,
                                              null,
                                              null);
        Patten subject = new Patten(path("hello", "world"));

        List<String> strings = subject.convert(mock).collect(Collectors.toList());

        Assert.assertEquals(strings, Collections.singletonList("root-dir > hello > world"));
    }

    @Test
    public void testDirExpansion() {
        Converter<String> mock = mockResolver("root-dir",
                                              null,
                                              s -> Stream.of(s + " > cache1",
                                                             s + " > cache2",
                                                             s + " > cache3"),
                                              null);
        Patten subject = new Patten(Patten.LATEST_VERSION_DIR);

        List<String> strings = subject.convert(mock).collect(Collectors.toList());

        Assert.assertEquals(strings, Arrays.asList("root-dir > cache1",
                                                   "root-dir > cache2",
                                                   "root-dir > cache3"));
    }

    @Test
    public void testSiblingWildcard() {
        Patten subject = new Patten(path("first", "second"), Patten.LATEST_VERSION_DIR);

        Patten result = subject.sibling(path("third"));

        Assert.assertEquals("$/first/second/third", result.toString());
    }

    @Test
    public void testSiblingPath() {
        Patten subject = new Patten(path("first", "second", "third-a"));

        Patten result = subject.sibling(path("third-b"));

        Assert.assertEquals("$/first/second/third-b", result.toString());
    }

    @Test
    public void testBalExpansion() {
        Converter<String> mock = mockResolver("project-dir",
                                              null,
                                              null,
                                              s -> Stream.of(s + " > dir1 > x.bal",
                                                             s + " > y.bal",
                                                             s + " > dir2 > dir3 > f.bal"));
        Patten subject = new Patten(Patten.WILDCARD_SOURCE);

        List<String> strings = subject.convert(mock).collect(Collectors.toList());

        Assert.assertEquals(strings, Arrays.asList("project-dir > dir1 > x.bal",
                                                   "project-dir > y.bal",
                                                   "project-dir > dir2 > dir3 > f.bal"));
    }

    /**
     * Disabled because it fails in JVM 8
     * See: https://bugs.openjdk.java.net/browse/JDK-8075939
     */
    @Test(enabled = false)
    public void testLazy() {
        Converter<String> mock = mockResolver("root-dir",
                                              null,
                                              s -> Stream.concat(Stream.of("", ""),
                                                                 Stream.generate(() -> {
                                                                     Assert.fail("method called. Hence not lazy.");
                                                                     return "";
                                                                 })),
                                              null);
        Patten subject = new Patten(Patten.LATEST_VERSION_DIR);

        List<String> strings = subject.convert(mock).limit(1).collect(Collectors.toList());

        Assert.assertTrue(strings.isEmpty());
    }

    @Test
    public void testReductionAndExpansion() {
        Converter<String> mock = mockResolver("my-dir",
                                              (a, b) -> a + " > " + b,
                                              s -> Stream.of(s + " > cache1",
                                                             s + " > cache2",
                                                             s + " > cache3"),
                                              q -> Stream.of(q + " > dir1 > x.bal",
                                                             q + " > y.bal",
                                                             q + " > dir2 > dir3 > f.bal"));
        Patten subject = new Patten(path("hello"), Patten.LATEST_VERSION_DIR, path("world"), Patten.WILDCARD_SOURCE);

        List<String> strings = subject.convert(mock).collect(Collectors.toList());

        Assert.assertEquals(strings, Arrays.asList("my-dir > hello > cache1 > world > dir1 > x.bal",
                                                   "my-dir > hello > cache1 > world > y.bal",
                                                   "my-dir > hello > cache1 > world > dir2 > dir3 > f.bal",
                                                   "my-dir > hello > cache2 > world > dir1 > x.bal",
                                                   "my-dir > hello > cache2 > world > y.bal",
                                                   "my-dir > hello > cache2 > world > dir2 > dir3 > f.bal",
                                                   "my-dir > hello > cache3 > world > dir1 > x.bal",
                                                   "my-dir > hello > cache3 > world > y.bal",
                                                   "my-dir > hello > cache3 > world > dir2 > dir3 > f.bal"));
    }
}
