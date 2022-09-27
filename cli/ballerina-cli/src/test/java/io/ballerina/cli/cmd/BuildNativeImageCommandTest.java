package io.ballerina.cli.cmd;

import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.util.ProjectConstants;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;

/**
 * Build native command tests.
 *
 * @since 2.0.0
 */
public class BuildNativeImageCommandTest extends BaseCommandTest {

    private Path testResources;
    private Path testDistCacheDirectory;
    ProjectEnvironmentBuilder projectEnvironmentBuilder;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("native-image-resources");
            Path testBuildDirectory = Paths.get("build").toAbsolutePath();
            this.testDistCacheDirectory = testBuildDirectory.resolve(DIST_CACHE_DIRECTORY);
            Path customUserHome = Paths.get("build", "user-home");
            Environment environment = EnvironmentBuilder.getBuilder().setUserHome(customUserHome).build();
            projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
            URI testResourcesURI = Objects.requireNonNull(
                    getClass().getClassLoader().getResource("test-resources")).toURI();
            Files.walkFileTree(Paths.get(testResourcesURI), new TestCommandTest.Copy(Paths.get(testResourcesURI),
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Test valid ballerina project with native image")
    public void testBuildProjectWithNativeImage() {
        Path projectPath = this.testResources.resolve("nativeimageProject");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        buildCommand.execute();

        Assert.assertTrue(projectPath.resolve("target").resolve("native").resolve("winery").toFile().exists());
    }

    @Test(description = "Test build valid ballerina file with native image", enabled = false)
    public void testBuildBalFileWithNativeImage() {
        // Fails due to
        // Error: Invalid classpath entry /var/folders/0_/8461jmg92gvfcy16rbv19mqh0000gn/T/b7a-cmd-test
        // -17337270728504112456427046681678614/native-image-resources/valid-test-bal-file/main.jar
        //Caused by: java.util.zip.ZipException: ZIP file can't be opened as a file system because entry
        // "/resources/$anon/./0/openapi-spec.yaml" has a '.' or '..' element in its name

        Path projectPath = this.testResources.resolve("nativeimageSingleFile").resolve("main.bal");

        System.setProperty(ProjectConstants.USER_DIR, this.testResources.resolve("valid-test-bal-file").toString());
        // set valid source root
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, false, true);
        // name of the file as argument
        new CommandLine(buildCommand).parse(projectPath.toString());
        buildCommand.execute();

        Assert.assertTrue(projectPath.resolve("target").resolve("native").resolve("winery").toFile().exists());
    }

}
