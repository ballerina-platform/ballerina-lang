/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.projects.balo;

import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.internal.PackageConfigCreator;

import java.nio.file.Path;

/**
 * {@code BaloProject} represents a Ballerina project instance created from a balr.
 *
 * @since 2.0.0
 */
public class BaloProject extends Project {

    /**
     * Loads a BaloProject from the provided balo path.
     *
     * @param balrPath Balo path
     * @return balo project
     */
    public static BaloProject loadProject(ProjectEnvironmentBuilder environmentBuilder, Path balrPath) {
        PackageConfig packageConfig = PackageConfigCreator.createBalrProjectConfig(balrPath);
        BaloProject balrProject = new BaloProject(environmentBuilder, balrPath);
        balrProject.addPackage(packageConfig);
        return balrProject;
    }

    private BaloProject(ProjectEnvironmentBuilder environmentBuilder, Path baloPath) {
        super(ProjectKind.BALR_PROJECT, baloPath, environmentBuilder);
    }
}
