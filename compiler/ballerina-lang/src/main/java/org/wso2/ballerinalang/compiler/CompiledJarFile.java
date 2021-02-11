/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler;

import org.wso2.ballerinalang.compiler.bir.codegen.stringgen.JvmBStringConstant;

import java.util.Map;
import java.util.Optional;

/**
 * A wrapper class for keeping code generated binary content and metadata of a program jar file.
 *
 * @since 2.0.0
 */
public class CompiledJarFile {

    private String mainClassName;
    private Map<String, byte[]> jarEntries;
    private Map<String, JvmBStringConstant> bStringVarMap;

    public CompiledJarFile(Map<String, byte[]> jarEntries) {

        this.jarEntries = jarEntries;
    }

    public CompiledJarFile(String mainClassName, Map<String, byte[]> jarEntries,
                           Map<String, JvmBStringConstant> bStringVarMap) {

        this.mainClassName = mainClassName;
        this.jarEntries = jarEntries;
        this.bStringVarMap = bStringVarMap;
    }

    public Map<String, byte[]> getJarEntries() {

        return jarEntries;
    }

    public Optional<String> getMainClassName() {

        return Optional.ofNullable(mainClassName);
    }

    public Map<String, JvmBStringConstant> getbStringVarMap() {
        return bStringVarMap;
    }
}
