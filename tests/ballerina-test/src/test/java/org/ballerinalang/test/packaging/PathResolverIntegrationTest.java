package org.ballerinalang.test.packaging;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.resolve.PathResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wso2.ballerinalang.compiler.packaging.Patten.WILDCARD_DIR;
import static org.wso2.ballerinalang.compiler.packaging.Patten.path;

public class PathResolverIntegrationTest {

    private Path tempDirectory;
    private Path tempFile;

    @BeforeClass
    public void setup() throws IOException {
        tempDirectory = Files.createTempDirectory("bal-unit-test-patten-resolver-");
        Path deep = tempDirectory.resolve(Paths.get("very", "deep", "path#to the", "dir.bal"));
        Files.createDirectories(deep);
        tempFile = Files.createFile(deep.resolve("tempFile.bal"));
    }

    @Test
    public void testWildcard() {
        Patten patten = new Patten(WILDCARD_DIR, path("deep"), WILDCARD_DIR, path("dir.bal", "tempFile.bal"));
        PathResolver subject = new PathResolver(tempDirectory);

        Stream<Path> pathStream = patten.convert(subject);

        List<Path> paths = pathStream.collect(Collectors.toList());
        Assert.assertEquals(paths.size(), 1);
        Assert.assertEquals(paths.get(0).toString(), tempFile.toString());
    }

    @Test
    public void testBalSearch() {
        Patten patten = new Patten(path("very"), Patten.WILDCARD_BAL);
        PathResolver subject = new PathResolver(tempDirectory);

        Stream<Path> pathStream = patten.convert(subject);

        List<Path> paths = pathStream.collect(Collectors.toList());
        Assert.assertEquals(paths.size(), 1);
        Assert.assertEquals(paths.get(0).toString(), tempFile.toString());
    }

    @AfterClass
    public void teardown() throws IOException {
        Files.delete(tempFile);
        Files.walk(tempDirectory)
             .sorted(Comparator.reverseOrder())
             .filter(Files::isDirectory)
             .forEach(path -> {
                 try {
                     Files.delete(path);
                 } catch (IOException e) {
                     Assert.fail(e.getMessage(), e);
                 }
             });
    }
}