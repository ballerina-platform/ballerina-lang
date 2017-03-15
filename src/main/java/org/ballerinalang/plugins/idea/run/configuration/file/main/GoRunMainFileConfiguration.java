/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.plugins.idea.run.configuration.file.main;

import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.ide.scratch.ScratchFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import org.ballerinalang.plugins.idea.run.configuration.file.GoRunFileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

public class GoRunMainFileConfiguration extends GoRunFileConfiguration {

    public GoRunMainFileConfiguration(Project project, String name, @NotNull ConfigurationType configurationType) {
        super(project, name, configurationType);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        super.checkBaseConfiguration();
        super.checkFileConfiguration();
        //Todo - Check for main function
    }

    @NotNull
    @Override
    protected GoRunMainFileRunningState newRunningState(@NotNull ExecutionEnvironment env, @NotNull Module module) {
        String path = getFilePath();
        if (!"bal".equals(PathUtil.getFileExtension(path))) {
            VirtualFile f = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
            if (f != null && f.getFileType() == ScratchFileType.INSTANCE) {
                String suffixWithoutExt = "." + UUID.randomUUID().toString().substring(0, 4);
                String suffix = suffixWithoutExt + ".bal";
                String before = f.getName();
                String beforeWithoutExt = FileUtil.getNameWithoutExtension(before);
                ApplicationManager.getApplication().runWriteAction(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            f.rename(this, before + suffix);
                        } catch (IOException ignored) {
                        }
                    }
                });
                setFilePath(path + suffix);
                setName(getName().replace(beforeWithoutExt, beforeWithoutExt + suffixWithoutExt));
            }
        }
        return new GoRunMainFileRunningState(env, module, this);
    }
}
