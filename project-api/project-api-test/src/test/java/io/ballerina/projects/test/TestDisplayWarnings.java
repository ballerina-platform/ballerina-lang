package io.ballerina.projects.test;

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.BuildOptionsBuilder;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains tests related to displaying warning of build project and dependencies.
 *
 * @since 2.0.0
 */
public class TestDisplayWarnings {

    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/projects_for_warning_tests");
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        // Compile and cache the package with warning
        compileResult = BCompileUtil.compileAndCacheBala("projects_for_warning_tests/package_with_warning");
    }

    @BeforeMethod
    public void deleteCache() {
        Path cachePath = Paths.get("build/repo/cache/foo/package_with_warning");
        if (Files.exists(cachePath)) {
            ProjectUtils.deleteDirectory(cachePath);
        }
    }

    @Test
    public void testWarningsInBuildProject() {
        Assert.assertEquals(compileResult.getDiagnosticResult().diagnosticCount(), 1);
        Assert.assertEquals(compileResult.getDiagnosticResult().warningCount(), 1);
    }

    @Test
    public void testWarningsOfDependencies() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("package_dependency_has_warning");

        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        PackageCompilation compilation = project.currentPackage().getCompilation();
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 0);
        Assert.assertFalse(compilation.diagnosticResult().hasWarnings());

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().diagnosticCount(), 0);
        Assert.assertFalse(jBallerinaBackend.diagnosticResult().hasWarnings());
    }

    @Test
    public void testShowWarningsOfDependencies() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("package_dependency_has_warning");

        BuildOptions buildOptions = new BuildOptionsBuilder().showAllWarnings(true).build();
        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath, buildOptions);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        PackageCompilation compilation = project.currentPackage().getCompilation();
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 1);
        Assert.assertEquals(compilation.diagnosticResult().warningCount(), 1);

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().diagnosticCount(), 1);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().warningCount(), 1);
    }

    @Test(description = "default module uses a deprecated function of a non-default module")
    public void testWarningsOfMultiModulePackage() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("multi_module_package_with_warning");

        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        PackageCompilation compilation = project.currentPackage().getCompilation();
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 1);
        Assert.assertEquals(compilation.diagnosticResult().warningCount(), 1);
        Assert.assertEquals(compilation.diagnosticResult().warnings().stream().findFirst().get().message(),
                "usage of construct 'modWithWarning:helloInternal()' is deprecated");

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().diagnosticCount(), 1);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().warningCount(), 1);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().warnings().stream().findFirst().get().message(),
                "usage of construct 'modWithWarning:helloInternal()' is deprecated");
    }

    @Test(description = "a non-default module uses a deprecated function of another non-default module")
    public void testWarningsOfMultiModulePackage2() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("multi_module_package_with_warning2");

        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        PackageCompilation compilation = project.currentPackage().getCompilation();
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 1);
        Assert.assertEquals(compilation.diagnosticResult().warningCount(), 1);
        Assert.assertEquals(compilation.diagnosticResult().warnings().stream().findFirst().get().message(),
                "usage of construct 'modWithWarning:helloInternal2()' is deprecated");

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().diagnosticCount(), 1);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().warningCount(), 1);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().warnings().stream().findFirst().get().message(),
                "usage of construct 'modWithWarning:helloInternal2()' is deprecated");
    }

    @Test (description = "default module uses a deprecated function of a non-default module with showAllWarnings flag")
    public void testShowWarningsOfMultiModulePackage() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("multi_module_package_with_warning");

        BuildOptions buildOptions = new BuildOptionsBuilder().showAllWarnings(true).build();
        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath, buildOptions);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        PackageCompilation compilation = project.currentPackage().getCompilation();
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 2);
        Assert.assertEquals(compilation.diagnosticResult().warningCount(), 2);

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().diagnosticCount(), 2);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().warningCount(), 2);
    }

    @Test (description =
            "a non-default module uses a deprecated function of another non-default module with showAllWarnings flag")
    public void testShowWarningsOfMultiModulePackage2() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("multi_module_package_with_warning2");

        BuildOptions buildOptions = new BuildOptionsBuilder().showAllWarnings(true).build();
        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath, buildOptions);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        PackageCompilation compilation = project.currentPackage().getCompilation();
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 2);
        Assert.assertEquals(compilation.diagnosticResult().warningCount(), 2);

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().diagnosticCount(), 2);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().warningCount(), 2);
    }

    @Test
    public void testWarningsOfDependenciesSingleFile() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("dependency_with_warning.bal");

        SingleFileProject project = null;
        try {
            project = SingleFileProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        PackageCompilation compilation = project.currentPackage().getCompilation();
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 0);
        Assert.assertFalse(compilation.diagnosticResult().hasWarnings());

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().diagnosticCount(), 0);
        Assert.assertFalse(jBallerinaBackend.diagnosticResult().hasWarnings());
    }

    @Test
    public void testShowWarningsOfDependenciesSingleFile() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("dependency_with_warning.bal");

        SingleFileProject project = null;
        BuildOptions buildOptions = new BuildOptionsBuilder().showAllWarnings(true).build();
        try {
            project = SingleFileProject.load(projectPath, buildOptions);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        PackageCompilation compilation = project.currentPackage().getCompilation();
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 1);
        Assert.assertEquals(compilation.diagnosticResult().warningCount(), 1);

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().diagnosticCount(), 1);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().warningCount(), 1);
    }

    @Test
    public void testSingleFileWithMultipleWarnings() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("file_with_multiple_warnings.bal");

        SingleFileProject project = null;
        try {
            project = SingleFileProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        PackageCompilation compilation = project.currentPackage().getCompilation();
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 1);
        Assert.assertEquals(compilation.diagnosticResult().warningCount(), 1);

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().diagnosticCount(), 1);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().warningCount(), 1);
    }

    @Test
    public void testShowSingleFileWithMultipleWarnings() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("file_with_multiple_warnings.bal");

        SingleFileProject project = null;
        BuildOptions buildOptions = new BuildOptionsBuilder().showAllWarnings(true).build();
        try {
            project = SingleFileProject.load(projectPath, buildOptions);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        PackageCompilation compilation = project.currentPackage().getCompilation();
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 2);
        Assert.assertEquals(compilation.diagnosticResult().warningCount(), 2);

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().diagnosticCount(), 2);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().warningCount(), 2);
    }
}
