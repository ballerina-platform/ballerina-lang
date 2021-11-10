package io.ballerina.projects;

import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.internal.repositories.FileSystemRepository;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Test file system repository validation.
 *
 * @since 2.0.0
 */
public class FileSystemRepositoryValidationTests {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources");
    private static final Path TEST_REPO = RESOURCE_DIRECTORY.resolve("test-repo");
    private FileSystemRepository fileSystemRepository;

    @BeforeSuite
    public void setup() {
        fileSystemRepository = new FileSystemRepository(new Environment() {
            @Override
            public <T> T getService(Class<T> clazz) {
                return null;
            }
        }, TEST_REPO);
    }

    @Test
    public void testGetPackageVersions() {

        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("hevayo"), PackageName.from("package_d"), null),
                PackageDependencyScope.DEFAULT);
        Collection<PackageVersion> versions = fileSystemRepository.getPackageVersions(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertEquals(versions.size(), 2);
        Assert.assertFalse(versions.contains(PackageVersion.from("0.1.0")));
        Assert.assertTrue(versions.contains(PackageVersion.from("0.1.2")));
        Assert.assertTrue(versions.contains(PackageVersion.from("0.1.3")));
        Assert.assertFalse(versions.contains(PackageVersion.from("0.2.0")));
    }

}
