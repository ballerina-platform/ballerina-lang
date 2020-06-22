/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.task;

import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;

/**
 * Package the Choreo extension.
 *
 * @since 2.0.0
 */
public class CopyChoreoExtensionTask implements Task {

    @Override
    public void execute(BuildContext buildContext) {
        String balHomePath = buildContext.get(BuildContextField.HOME_REPO).toString();
        String ballerinaVersion = RepoUtils.getBallerinaPackVersion();
        String runtimeJarName = "ballerina-choreo-extension-rt-" + ballerinaVersion + BLANG_COMPILED_JAR_EXT;
        Path extensionJar = Paths.get(balHomePath, "bre", "lib", runtimeJarName);
        for (BLangPackage module : buildContext.getModules()) {
            buildContext.moduleDependencyPathMap.get(module.packageID).moduleLibs.add(extensionJar);
        }
    }
}
