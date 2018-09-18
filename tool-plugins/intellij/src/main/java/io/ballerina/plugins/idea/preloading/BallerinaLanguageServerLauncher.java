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

import com.github.gtache.lsp.client.languageserver.serverdefinition.LanguageServerDefinition$;
import com.github.gtache.lsp.client.languageserver.serverdefinition.RawCommandServerDefinition;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.project.VetoableProjectManagerListener;
import com.intellij.util.messages.MessageBusConnection;
import io.ballerina.plugins.idea.sdk.BallerinaSdkService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Preloading Activity of ballerina plugin.
 */
public class BallerinaLanguageServerLauncher extends PreloadingActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger(BallerinaLanguageServerLauncher.class);

    @Override
    public void preload(@NotNull ProgressIndicator indicator) {

        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        String sdkPath = "";
        for (Project project : openProjects) {
            Module[] projectModules = ModuleManager.getInstance(project).getModules();
            for (Module module:projectModules){
                if (BallerinaSdkService.getInstance(project).isBallerinaModule(module)) {
                    sdkPath = BallerinaSdkService.getInstance(project).getSdkHomePath(null);
                }
            }

        }

        if (!sdkPath.equals("")) {
            String[] command = { "/home/nino/Desktop/ls-launcher/launcher.sh" };
            LanguageServerDefinition$.MODULE$.register(new RawCommandServerDefinition("bal", command));
        }

        final MessageBusConnection connect = ApplicationManager.getApplication().getMessageBus().connect();
        connect.subscribe(ProjectManager.TOPIC, new ProjectManagerListener() {
            @Override
            public void projectOpened(@Nullable final Project projectFromCommandLine) {
                System.out.print("");
            }
        });

        ProjectManager.getInstance().addProjectManagerListener(new VetoableProjectManagerListener() {
            @Override
            public boolean canClose(@NotNull Project project) {
                Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
                if (openProjects.length <= 1) {
                    stopLanguageServerProcesses();
                }
                return true;
            }
        });

        ProjectManager.getInstance().addProjectManagerListener(new ProjectManagerListener() {
            @Override
            public void projectOpened(Project project) {
                System.out.print("h");
            }
        });
    }

    /**
     * Stops running language server instances.
     */
    public void stopLanguageServerProcesses() {
        try {
            String os = LaunchUtils.getOperatingSystem();
            if (os == null) {
                LOGGER.error("unsupported operating system");
                return;
            }
            Terminator terminator = new TerminatorFactory().getTerminator(os);
            if (terminator == null) {
                LOGGER.error("unsupported operating system");
                return;
            }
            terminator.terminate();

        } catch (Exception e) {
            LOGGER.error("Error occured", e);
        }
    }
}
