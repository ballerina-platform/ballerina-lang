package org.ballerinalang.test.packaging;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.PathConverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wso2.ballerinalang.compiler.packaging.Patten.LATEST_VERSION_DIR;
import static org.wso2.ballerinalang.compiler.packaging.Patten.path;

public class PathConverterIntegrationTest {

    private Path tempDirectory;
    private Path tempFile;
    private Path tempSemVerFile;
    private Path tempNonSourceFile;

    @BeforeClass
    public void setup() throws IOException {
        tempDirectory = Files.createTempDirectory("bal-unit-test-patten-resolver-");
        Path deep = tempDirectory.resolve(Paths.get("very", "deep", "path#to the", "dir.bal"));
        Files.createDirectories(deep);
        Path semVer = tempDirectory.resolve(Paths.get("1.2.3", "path#to the", "dir.bal"));
        Files.createDirectories(semVer);
        Path semVerLatest = tempDirectory.resolve(Paths.get("1.5.0", "path#to the", "dir.bal"));
        Files.createDirectories(semVerLatest);
        tempNonSourceFile = Files.createFile(deep.resolve("my.balo"));
        tempFile = Files.createFile(deep.resolve("tempFile.bal"));
        tempSemVerFile = Files.createFile(semVerLatest.resolve("tempFile.bal"));
    }

    @Test
    public void testWildcardWithSemVer() {
        Patten patten = new Patten(LATEST_VERSION_DIR, path("path#to the", "dir.bal", "tempFile.bal"));
        PathConverter subject = new PathConverter(tempDirectory);

        Stream<Path> pathStream = patten.convert(subject);

        List<Path> paths = pathStream.collect(Collectors.toList());
        Assert.assertEquals(paths.size(), 1);
        Assert.assertEquals(paths.get(0).toString(), tempSemVerFile.toString());
    }

    @Test
    public void testWildcard() {
        Patten patten = new Patten(LATEST_VERSION_DIR, path("deep"), LATEST_VERSION_DIR, path("dir.bal",
                                                                                              "tempFile.bal"));
        PathConverter subject = new PathConverter(tempDirectory);

        Stream<Path> pathStream = patten.convert(subject);

        List<Path> paths = pathStream.collect(Collectors.toList());
        Assert.assertEquals(paths.size(), 0);
    }

    @Test
    public void testBalSearch() {
        Patten patten = new Patten(path("very"), Patten.WILDCARD_SOURCE);
        PathConverter subject = new PathConverter(tempDirectory);

        Stream<Path> pathStream = patten.convert(subject);

        List<Path> paths = pathStream.collect(Collectors.toList());
        Assert.assertEquals(paths.size(), 1);
        Assert.assertEquals(paths.get(0).toString(), tempFile.toString());
    }

    @AfterClass
    public void teardown() throws IOException {
        Files.delete(tempFile);
        Files.delete(tempSemVerFile);
        Files.delete(tempNonSourceFile);
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
