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
package org.wso2.ballerinalang.compiler.bir.codegen.internal;

import org.wso2.ballerinalang.compiler.bir.model.BIRNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for keeping metadata for Java class file.
 *
 * @since 1.3.0
 */
public class JavaClass {

    public final String sourceFileName;
    public final List<BIRNode.BIRFunction> functions;

    public JavaClass(String sourceFileName) {

        this.sourceFileName = sourceFileName;
        this.functions = new ArrayList<>();
    }
}
