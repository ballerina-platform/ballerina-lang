package io.ballerina.cli.cmd;

import io.ballerina.cli.task.Task;
import io.ballerina.projects.Project;

import java.io.IOException;

import static io.ballerina.cli.cmd.Constants.BACKUP;
import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;

public class RestoreCachedArtifactsTask implements Task {
    @Override
    public void execute(Project project) {
        try {
            CommandUtil.copyOneDirectoryUp(project.targetDir().resolve(BACKUP));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw createLauncherException("unable to restore the cache: " + e.getMessage());
        }
    }
}
