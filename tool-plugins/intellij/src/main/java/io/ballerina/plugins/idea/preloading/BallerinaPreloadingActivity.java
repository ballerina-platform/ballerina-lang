/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.ballerina.plugins.idea.preloading;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.util.messages.MessageBusConnection;
import io.ballerina.plugins.idea.sdk.BallerinaSdkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.ballerina.plugins.idea.preloading.LSPUtils.registerServerDefinition;
import static io.ballerina.plugins.idea.preloading.OSUtils.getOperatingSystem;

/**
 * Preloading Activity of ballerina plugin.
 */
public class BallerinaPreloadingActivity extends PreloadingActivity {

    static final Logger LOG = Logger.getInstance(BallerinaPreloadingActivity.class);

    /**
     * Preloading of the ballerina plugin.
     */
    @Override
    public void preload(@NotNull ProgressIndicator indicator) {

        // Stops all running language server instances.
        stopProcesses();
        // Registers language server definitions for initially opened projects.
        registerServerDefinition();

        final MessageBusConnection connect = ApplicationManager.getApplication().getMessageBus().connect();
        connect.subscribe(ProjectManager.TOPIC, new ProjectManagerListener() {
            @Override
            public void projectOpened(@Nullable final Project project) {
                if (project == null) {
                    return;
                }
                if (isBallerinaProject(project)) {
                    // If the opened project root is a ballerina project root, proceeds with SDK validation and auto
                    // detection, if needed.
                    registerServerDefinition(project);
                } else {
                    // Even if the project is not a ballerina project(does not have ballerina project structure),
                    // it might contains ballerina modules or single ballerina source files. Therefore an editor
                    // listener is used to trigger the language server connection and proceed accordingly, when it
                    // detects a ballerina file being opened.
                    watchForBalFiles(project);
                }
            }
        });

        ProjectManager.getInstance().addProjectManagerListener(project -> {
            Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
            if (openProjects.length <= 1) {
                stopProcesses();
            }
            return true;
        });
    }

    /**
     * Stops running language server instances.
     */
    private static void stopProcesses() {
        try {
            String os = getOperatingSystem();
            if (os == null) {
                LOG.error("unsupported operating system");
                return;
            }
            Terminator terminator = new TerminatorFactory().getTerminator(os);
            if (terminator == null) {
                LOG.error("unsupported operating system");
                return;
            }
            terminator.terminate();
        } catch (Exception e) {
            LOG.error("Error occurred when trying to terminate ballerina processes", e);
        }
    }

    private void watchForBalFiles(Project project) {
        EditorFactory.getInstance().addEditorFactoryListener(new BallerinaEditorFactoryListener(project), project);
    }

    /**
     * Validates if a given project is a ballerina project, using ballerina project structure.
     *
     * @param project The IDEA project instance to be validated.
     */
    private static boolean isBallerinaProject(Project project) {
        return !BallerinaSdkUtils.searchForBallerinaProjectRoot(project.getBasePath(), project.getBasePath()).isEmpty();
    }

}
