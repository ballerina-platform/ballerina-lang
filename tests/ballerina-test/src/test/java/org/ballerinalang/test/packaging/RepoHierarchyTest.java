package org.ballerinalang.test.packaging;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchyBuilder;
import org.wso2.ballerinalang.compiler.packaging.Resolution;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.FileSystemSourceInput;
import org.wso2.ballerinalang.compiler.packaging.converters.StringConverter;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;
import org.wso2.ballerinalang.compiler.util.Name;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.wso2.ballerinalang.compiler.packaging.RepoHierarchyBuilder.node;

public class RepoHierarchyTest {

    private Path tempFile;

    @BeforeClass
    public void createTemp() throws IOException {
        tempFile = Files.createTempFile("bal-unit-repo-hierarchy-", "");
    }

    @Test
    public void testFullOrder() {
        PackageID nonExisting = newPackageID("bad", "ok.pkg", "5");
        ArrayList<Integer> order = new ArrayList<>();
        RepoHierarchy subject = createSubject(order);

        Resolution resolution = subject.resolve(nonExisting);

        Assert.assertEquals(resolution, Resolution.NOT_FOUND);
        Assert.assertEquals(order, Arrays.asList(0, 1, 2, 3, 4));
    }

    @Test
    public void testIdempotence() {
        RepoHierarchy subject;
        Resolution resolution;

        // Part 1
        PackageID repo0sPkg = newPackageID("easy", "too", "0");
        ArrayList<Integer> order = new ArrayList<>();
        subject = createSubject(order);

        resolution = subject.resolve(repo0sPkg);

        Assert.assertNotEquals(resolution, Resolution.NOT_FOUND);

        // Part 2
        order.clear();
        subject = resolution.resolvedBy;

        subject.resolve(repo0sPkg);

        Assert.assertNotEquals(resolution, Resolution.NOT_FOUND);
    }

    @Test
    public void testResolvingFromAnIntermediate() {
        RepoHierarchy subject;
        Resolution resolution;

        // Part 1
        PackageID repo1sPkg = newPackageID("good", "i.am", "1");
        ArrayList<Integer> order = new ArrayList<>();
        subject = createSubject(order);

        resolution = subject.resolve(repo1sPkg);

        Assert.assertNotEquals(resolution, Resolution.NOT_FOUND);
        Assert.assertEquals(order, Arrays.asList(0, 1));

        // Part 2
        order.clear();
        PackageID repo4sPkg = newPackageID("ugly", "ok.pkg", "4");
        subject = resolution.resolvedBy;

        subject.resolve(repo4sPkg);

        Assert.assertNotEquals(resolution, Resolution.NOT_FOUND);
        Assert.assertEquals(order, Arrays.asList(1, 2, 3, 4));
    }

    @AfterClass
    public void deleteTemp() throws IOException {
        Files.delete(tempFile);
    }

    private Repo mockRepo(int i, List<Integer> order) {
        return new Repo() {
            @Override
            public Patten calculate(PackageID pkg) {
                order.add(i);
                if (String.valueOf(i).equals(pkg.version.value)) {
                    return new Patten(Patten.path("pkg" + i));
                } else {
                    return Patten.NULL;
                }
            }

            @Override
            public Converter getConverterInstance() {
                return new StringConverter() {
                    @Override
                    public Stream<CompilerInput> finalize(String s, PackageID id) {
                        return Stream.of(new FileSystemSourceInput(tempFile));
                    }
                };
            }

            @Override
            public String toString() {
                return "Repo-" + i;
            }
        };
    }

    /**
     * <pre>
     *
     *  easy/too:0        good/i.am:1                                     ugly/ok.pkg:4
     *      |                 |           ,--- projectCacheRepo ---.            |
     *      v                 v          /            2             \           v
     * projectSource --- projectRepo ---<                            >--- homeCacheRepo
     *            0           1          \                          /           4
     *                                    `------- homeRepo -------'
     *                                                3
     * </pre>
     * Expected lookup order
     * <p>
     * <p>
     * 0. projectSource
     * 1. projectRepo
     * 2. homeRepo
     * 3. projectCacheRepo
     * 4. homeCacheRepo
     *
     * @param order list to remember order of invocation into
     */
    private RepoHierarchy createSubject(List<Integer> order) {
        Repo projectSource = mockRepo(0, order);
        Repo projectRepo = mockRepo(1, order);
        Repo projectCacheRepo = mockRepo(2, order);
        Repo homeRepo = mockRepo(3, order);
        Repo homeCacheRepo = mockRepo(4, order);

        RepoHierarchyBuilder.RepoNode homeCacheNode = node(homeCacheRepo);
        return RepoHierarchyBuilder.build(node(projectSource,
                                               node(projectRepo, null /* null nodes should be ignored */,
                                                    node(projectCacheRepo, homeCacheNode),
                                                    node(homeRepo, homeCacheNode))));
    }

    private PackageID newPackageID(String org, String pkg, String version) {
        return new PackageID(new Name(org), new Name(pkg), new Name(version));
    }

}

