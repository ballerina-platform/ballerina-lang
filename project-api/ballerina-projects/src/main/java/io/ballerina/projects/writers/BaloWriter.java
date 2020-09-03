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

package io.ballerina.projects.writers;

import io.ballerina.projects.Package;

import java.nio.file.Path;

/**
 * {@code BaloWriter} writes a package to balo format.
 *
 * @since 2.0.0
 */
public class BaloWriter {

    /**
     * Write a package to a .balo and return the created .balo path.
     * @param pkg Package to be written as a .balo.
     * @param path Directory where the .balo should be created.
     * @return Newly created balo path
     */
    public static Path write(Package pkg, Path path) {
        Path balo = path.resolve("org-name-any-version.balo");
        // todo check if the given package is compiled properly
        // todo check if the path is a directory
        // Check directory permissions
        // Handle any io errors
        return balo;
    }
}
