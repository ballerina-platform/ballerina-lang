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

package io.ballerina.projects.bala;

import io.ballerina.projects.DocumentId;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.internal.PackageConfigCreator;

import java.nio.file.Path;

/**
 * {@code BalaProject} represents a Ballerina project instance created from a bala.
 *
 * @since 2.0.0
 */
public class BalaProject extends Project {

    /**
     * Loads a BalaProject from the provided bala path.
     *
     * @param balaPath Bala path
     * @return bala project
     */
    public static BalaProject loadProject(ProjectEnvironmentBuilder environmentBuilder, Path balaPath) {
        PackageConfig packageConfig = PackageConfigCreator.createBalaProjectConfig(balaPath);
        BalaProject balaProject = new BalaProject(environmentBuilder, balaPath);
        balaProject.addPackage(packageConfig);
        return balaProject;
    }

    private BalaProject(ProjectEnvironmentBuilder environmentBuilder, Path balaPath) {
        super(ProjectKind.BALA_PROJECT, balaPath, environmentBuilder);
    }

    @Override
    public DocumentId documentId(Path file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save() {
    }
}
