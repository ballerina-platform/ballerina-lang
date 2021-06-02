/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.projects.internal.model.CompilerPluginDescriptor;
import io.ballerina.projects.plugins.CompilerPlugin;

import java.util.Objects;

/**
 * This class holds a {@code PackageProvidedCompilerPluginInfo} instance with additional details such the
 * containing package's {@code PackageDescriptor} and the {@code CompilerPluginDescriptor}.
 *
 * @since 2.0.0
 */
class PackageProvidedCompilerPluginInfo extends CompilerPluginInfo {

    private final PackageDescriptor packageDesc;
    private final CompilerPluginDescriptor compilerPluginDesc;

    PackageProvidedCompilerPluginInfo(CompilerPlugin compilerPlugin,
                       PackageDescriptor packageDesc,
                       CompilerPluginDescriptor compilerPluginDesc) {
        super(compilerPlugin, CompilerPluginKind.PACKAGE_PROVIDED);
        this.packageDesc = packageDesc;
        this.compilerPluginDesc = compilerPluginDesc;
    }

    PackageDescriptor packageDesc() {
        return packageDesc;
    }

    CompilerPluginDescriptor compilerPluginDesc() {
        return compilerPluginDesc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PackageProvidedCompilerPluginInfo that = (PackageProvidedCompilerPluginInfo) o;
        return Objects.equals(compilerPlugin, that.compilerPlugin) &&
                Objects.equals(packageDesc, that.packageDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(compilerPlugin, packageDesc);
    }
}
