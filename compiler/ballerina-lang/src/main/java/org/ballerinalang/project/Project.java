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
package org.ballerinalang.project;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;

/**
 * Defines functionality of the Project.
 *
 * @since 2.0.0
 */
public interface Project {

    CompilerContext.Key<Project> PROJECT_KEY = new CompilerContext.Key<>();

    /**
     * Returns true if the module exists in the project.
     *
     * @param moduleId Module Id
     * @return the status whether the module exists in the project.
     */
    public boolean isModuleExists(PackageID moduleId);

    /**
     * Returns the .balo path.
     *
     * @param moduleId Module Id
     * @return Path to the .balo.
     * @throws InvalidModuleException when project does not contains the module.
     */
    public Path getBaloPath(PackageID moduleId) throws InvalidModuleException;
}
