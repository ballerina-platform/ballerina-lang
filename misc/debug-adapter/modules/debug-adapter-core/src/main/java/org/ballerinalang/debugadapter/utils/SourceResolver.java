/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.utils;

import com.sun.jdi.Location;
import io.ballerina.projects.Project;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Abstract implementation of Ballerina debug source resolver.
 *
 * @since 2.0.0
 */
public abstract class SourceResolver {

    protected final Project sourceProject;

    SourceResolver(Project sourceProject) {
        this.sourceProject = sourceProject;
    }

    /**
     * Returns whether this resolver instance is capable of resolving the given JDI source location.
     *
     * @param location JDI source location
     * @return true if this resolver instance is capable of resolving the given JDI source location
     */
    abstract boolean isSupported(Location location);

    /**
     * Each concrete class must implement their own resolving logic.
     *
     * @param location JDI source location
     * @return absolute path of the source file location
     */
    abstract Optional<Path> resolve(Location location);

}
