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
package io.ballerina.projects;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents the name of a {@code Package}.
 *
 * @since 2.0.0
 */
public class ModuleName {

    private final PackageName packageName;
    private final String moduleNamePart;

    private ModuleName(PackageName packageName, @Nullable String moduleNamePart) {
        this.packageName = packageName;
        this.moduleNamePart = moduleNamePart;
    }

    public static ModuleName from(PackageName packageName) {
        // Creates the default moduleName
        return new ModuleName(packageName, null);
    }

    public static ModuleName from(PackageName packageName, @Nullable String moduleNamePart) {
        if (moduleNamePart != null && moduleNamePart.isEmpty()) {
            throw new IllegalArgumentException("moduleNamePart should be a non-empty string or null");
        }
        // TODO Check whether the moduleNamePart is a valid list of identifiers
        return new ModuleName(packageName, moduleNamePart);
    }

    public PackageName packageName() {
        return packageName;
    }

    public String moduleNamePart() {
        return moduleNamePart;
    }

    public boolean isDefaultModuleName() {
        return moduleNamePart == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ModuleName that = (ModuleName) o;
        return packageName.equals(that.packageName) &&
                Objects.equals(moduleNamePart, that.moduleNamePart);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, moduleNamePart);
    }

    @Override
    public String toString() {
        return packageName + (moduleNamePart != null ? "." + moduleNamePart : "");
    }
}
