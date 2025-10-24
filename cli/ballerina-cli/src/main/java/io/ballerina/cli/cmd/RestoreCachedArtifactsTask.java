package io.ballerina.cli.cmd;

import io.ballerina.cli.task.Task;
import io.ballerina.projects.Project;
import io.ballerina.projects.util.ProjectConstants;

import java.io.IOException;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;

public class RestoreCachedArtifactsTask implements Task {
    @Override
    public void execute(Project project) {
        try {
            CommandUtil.copyOneDirectoryUp(project.targetDir().resolve(ProjectConstants.EXEC_BACKUP_DIR_NAME));
        } catch (IOException e) {
            throw createLauncherException("unable to restore the cache: " + e.getMessage());
        }
    }
}
