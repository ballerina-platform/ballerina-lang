package io.ballerina.cli.task;

import io.ballerina.cli.utils.BuildTime;
import io.ballerina.projects.*;
import io.ballerina.projects.Module;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR;

public class CreateTestExecutableTask extends CreateExecutableTask {

    public CreateTestExecutableTask(PrintStream out, String output) {
        super(out, output);
    }

    @Override
    public void execute(Project project) {
        this.out.println();

        this.currentDir = Paths.get(System.getProperty(USER_DIR));
        Target target = getTarget(project);
        EmitResult emitResult;

        try{
            PackageCompilation pkgCompilation = project.currentPackage().getCompilation();
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(pkgCompilation, JvmTarget.JAVA_17);

            List<Diagnostic> diagnostics = new ArrayList<>();

            long start = 0;
            if (project.buildOptions().dumpBuildTime()) { //TODO: change to dumpTestBuildTime??
                start = System.currentTimeMillis();
            }

            for(ModuleDescriptor moduleDescriptor :
                    project.currentPackage().moduleDependencyGraph().toTopologicallySortedList()) {

                Module module = project.currentPackage().module(moduleDescriptor.name());
                Path testExecutablePath = getTestExecutablePath(target, module);
                emitResult = jBallerinaBackend.emit(JBallerinaBackend.OutputType.TEST, testExecutablePath, module.moduleName());
                diagnostics.addAll(emitResult.diagnostics().diagnostics());
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
        }
        catch(ProjectException e) {
            throw createLauncherException(e.getMessage());
        }
        // notify plugin
        // todo following call has to be refactored after introducing new plugin architecture
        notifyPlugins(project, target);
    }
    private Path getTestExecutablePath(Target target, Module module) {
        Path executablePath;
        try{
            executablePath = target.getTestExecutablePath(module).toAbsolutePath().normalize();
        }
        catch(IOException e){
            throw createLauncherException(e.getMessage());
        }
        return executablePath;
    }
}
