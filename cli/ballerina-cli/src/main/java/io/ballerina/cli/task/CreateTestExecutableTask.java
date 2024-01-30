package io.ballerina.cli.task;

import io.ballerina.cli.utils.BuildTime;
import io.ballerina.cli.utils.TestUtils;
import io.ballerina.projects.*;
import io.ballerina.projects.Module;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.test.runtime.entity.TestSuite;
import org.ballerinalang.testerina.core.TestProcessor;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR;

public class CreateTestExecutableTask extends CreateExecutableTask {
    private final RunTestsTask runTestsTask;

    public CreateTestExecutableTask(PrintStream out, String output, RunTestsTask runTestsTask) {
        super(out, output);
        this.runTestsTask = runTestsTask;   //to use the getCmdArgs() method
    }

    @Override
    public void execute(Project project) {
        this.out.println();

        this.currentDir = Paths.get(System.getProperty(USER_DIR));
        Target target = getTarget(project);

        try {
            PackageCompilation pkgCompilation = project.currentPackage().getCompilation();
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(pkgCompilation, JvmTarget.JAVA_17);
            JarResolver jarResolver = jBallerinaBackend.jarResolver();

            List<Diagnostic> diagnostics = new ArrayList<>();

            long start = 0;
            if (project.buildOptions().dumpBuildTime()) { //TODO: change to dumpTestBuildTime??
                start = System.currentTimeMillis();
            }

            HashSet<JarLibrary> testExecDependencies = new HashSet<>();

            for (ModuleDescriptor moduleDescriptor :
                    project.currentPackage().moduleDependencyGraph().toTopologicallySortedList()) {

                Module module = project.currentPackage().module(moduleDescriptor.name());
                Path testExecutablePath = getTestExecutablePath(target, module);

                testExecDependencies.addAll(jarResolver
                        .getJarFilePathsRequiredForTestExecution(module.moduleName())
                );

                List<String> allArgs = this.runTestsTask.getAllTestArgs(target,
                        project.currentPackage().packageName().toString(),
                        module.moduleName().toString(),
                        project,
                        moduleDescriptor
                );

                //create the fat jar for the test module
                Path generatedTestArtifact = jBallerinaBackend.generateTestArtifact(
                        JBallerinaBackend.OutputType.TEST,
                        testExecutablePath,
                        module.moduleName(),
                        allArgs
                );

                if (generatedTestArtifact != null) {
                    diagnostics.addAll(jBallerinaBackend.getEmitResult(
                            testExecutablePath,
                            generatedTestArtifact,
                            false, ArtifactType.TEST).diagnostics().diagnostics()
                    );
                }
                else {
                    diagnostics.addAll(jBallerinaBackend.getFailedEmitResult(
                            null)
                            .diagnostics().diagnostics()
                    );
                }
            }

            //create the single fat jar for all the test modules
            EmitResult result = jBallerinaBackend.emit(
                    JBallerinaBackend.OutputType.TEST,
                    getTestExecutableBasePath(target),
                    testExecDependencies
            );


            if(project.buildOptions().cloud() != null) {
                //if cloud is enabled, we need to create the docker artifacts
                //create the test suite suitable for docker
                Path basePath = getTestExecutableBasePath(target);
                boolean status = createTestSuiteForCloudArtifacts(project, jBallerinaBackend.jarResolver(), target);

                if (status) {
                    //now notify the compilation completion
                    List<Diagnostic> pluginDiagnostics = jBallerinaBackend.
                            notifyCompilationCompletion(basePath, ArtifactType.TEST);
                    diagnostics.addAll(pluginDiagnostics);
                }

            }

            if (project.buildOptions().dumpBuildTime()) {
                BuildTime.getInstance().emitArtifactDuration = System.currentTimeMillis() - start;
                BuildTime.getInstance().compile = false;
            }

            // Print warnings for conflicted jars
            if (!jBallerinaBackend.conflictedJars().isEmpty()) {
                out.println("\twarning: Detected conflicting jar files:");
                for (JBallerinaBackend.JarConflict conflict : jBallerinaBackend.conflictedJars()) {
                    out.println(conflict.getWarning(project.buildOptions().listConflictedClasses()));
                }
            }

            if (!diagnostics.isEmpty()) {
                //  TODO: When deprecating the lifecycle compiler plugin, we can remove this check for duplicates
                //   in JBallerinaBackend diagnostics and the diagnostics added to EmitResult.
                diagnostics = diagnostics.stream()
                        .filter(diagnostic -> !jBallerinaBackend.diagnosticResult().diagnostics().contains(diagnostic))
                        .collect(Collectors.toList());
                if (!diagnostics.isEmpty()) {
                    diagnostics.forEach(d -> out.println("\n" + d.toString()));
                }
            }
        } catch (ProjectException e) {
            throw createLauncherException(e.getMessage());
        }
        // notify plugin
        // todo following call has to be refactored after introducing new plugin architecture
        notifyPlugins(project, target);
    }

    private boolean createTestSuiteForCloudArtifacts(Project project, JarResolver jarResolver, Target target) {
        boolean report = project.buildOptions().testReport();
        boolean coverage = project.buildOptions().codeCoverage();

        TestProcessor testProcessor = new TestProcessor(jarResolver);
        List<String> moduleNamesList = new ArrayList<>();
        Map<String, TestSuite> testSuiteMap = new HashMap<>();
        List<String> updatedSingleExecTests;
        List<String> mockClassNames = new ArrayList<>();

        boolean status = RunTestsTask.createTestSuiteIfHasTests(project, target, testProcessor, testSuiteMap, moduleNamesList,
                mockClassNames, runTestsTask.isRerunTestExecution(), report, coverage);

        if (status) {
            //now write the map to a json file
            try{
                TestUtils.writeToTestSuiteJson(testSuiteMap, target.getTestsCachePath());
                return true;
            }
            catch(IOException e) {
                throw createLauncherException("error while writing to test suite json file: " + e.getMessage());
            }
        }
        else{
            return false;
        }
    }

    private Path getTestExecutablePath(Target target, Module module) {
        Path executablePath;
        try {
            executablePath = target.getTestExecutablePath(module).toAbsolutePath().normalize();
        } catch (IOException e) {
            throw createLauncherException(e.getMessage());
        }
        return executablePath;
    }

    private Path getTestExecutableBasePath(Target target) {
        try {
            return target.getTestExecutableBasePath();
        }
        catch (IOException e) {
            throw createLauncherException(e.getMessage());
        }
    }
}
