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
public class PackageName {
    private final String packageNameStr;
    public static final String LANG_LIB_PACKAGE_NAME_PREFIX = "lang";

    private PackageName(String packageNameStr) {
        this.packageNameStr = packageNameStr;
    }

    public static PackageName from(@Nullable String packageNameStr) {
        // TODO Check whether the packageName is a valid Ballerina identifier
        return new PackageName(packageNameStr);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PackageName that = (PackageName) o;
        return packageNameStr.equals(that.packageNameStr);
    }

    public String value() {
        return packageNameStr;
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageNameStr);
    }

    @Override
    public String toString() {
        return packageNameStr;
    }
}
