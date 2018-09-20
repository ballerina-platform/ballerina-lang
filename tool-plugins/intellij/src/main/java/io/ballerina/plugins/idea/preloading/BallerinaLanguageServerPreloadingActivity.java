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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.project.VetoableProjectManagerListener;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

import static io.ballerina.plugins.idea.preloading.OperatingSystemUtils.getOperatingSystem;

/**
 * Preloading Activity of ballerina plugin.
 */
public class BallerinaLanguageServerPreloadingActivity extends PreloadingActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger(BallerinaLanguageServerPreloadingActivity.class);

    /**
     * Preloading of the plugin
     */
    @Override
    public void preload(@NotNull ProgressIndicator indicator) {

        //Tries to register language server definition, if a ballerina project is being opened initially.
        registerServerDefinition();

        final MessageBusConnection connect = ApplicationManager.getApplication().getMessageBus().connect();
        connect.subscribe(ProjectManager.TOPIC, new ProjectManagerListener() {
            @Override
            public void projectOpened(@Nullable final Project projectFromCommandLine) {
                registerServerDefinition();
            }
        });

        ProjectManager.getInstance().addProjectManagerListener(new VetoableProjectManagerListener() {
            @Override
            public boolean canClose(@NotNull Project project) {
                Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
                if (openProjects.length <= 1) {
                    stopProcesses();
                }
                return true;
            }
        });
    }

    /**
     * Registered language server definition using currently opened ballerina projects.
     * @return Returns true if a definition is registered successfully.
     */
    public boolean registerServerDefinition() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : openProjects) {
            if (registerServerDefinition(project)) {
                return true;
            }
        }
        return false;
    }

    public boolean registerServerDefinition(Project project) {
        String balSdkPath = getBallerinaSdk(project);
        if (balSdkPath != null) {
            return doRegister(balSdkPath);
        }
        return false;
    }

    public boolean doRegister(@NotNull String sdkPath) {

        String os = OperatingSystemUtils.getOperatingSystem();
        if (os != null) {
            String args[] = new String[1];
            if (os.equals(OperatingSystemUtils.UNIX) || os.equals(OperatingSystemUtils.MAC)) {
                args[0] = Paths.get(sdkPath, "/lib/resources/composer/language-server-launcher.sh").toString();
            } else if (os.equals(OperatingSystemUtils.WINDOWS)) {
                args[0] = Paths.get(sdkPath, "/lib/resources/composer/language-server-launcher.bat").toString();
            }

            if (args.length > 0) {
                LanguageServerDefinition$.MODULE$.register(new RawCommandServerDefinition("bal", args));
                LOGGER.info("registered language server definition using Sdk path: " + sdkPath);
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Stops running language server instances.
     */
    public void stopProcesses() {
        try {
            String os = getOperatingSystem();
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

    @Nullable
    private String getBallerinaSdk(Project project) {
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        String sdkPath = projectSdk.getHomePath();

        //returns sdk path if it contains "ballerina".
        return (sdkPath != null && !sdkPath.equals("") && sdkPath.toLowerCase().contains("ballerina")) ? sdkPath : null;
    }
}
