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
package io.samjs.plugins.badsad;

import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;

import java.io.PrintStream;

/**
 * A {@code CompilerPlugin} that throws runtime error when initializing.
 *
 * @since 2.0.0
 */
public class BadSadCompilerPlugin20 extends CompilerPlugin {

    @Override
    public void init(CompilerPluginContext pluginContext) {
        PrintStream out = System.out;
        out.println(getStr().toCharArray());
    }

    String getStr() {
        return null;
    }
}
