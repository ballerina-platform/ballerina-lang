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
package io.ballerina.compiler.linter.impl;

import io.ballerina.compiler.linter.impl.codeactions.CheckUsageCodeAction;
import io.ballerina.compiler.linter.impl.codeactions.FloatingPointLiteralCodeAction;
import io.ballerina.compiler.linter.impl.codeactions.QualifiedIdentifierCodeAction;
import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;

/**
 * A built-in compiler plugin, that implements Language Linter rules.
 *
 * @since 2.0.0
 */
public class CompilerLinter extends CompilerPlugin {

    @Override
    public void init(CompilerPluginContext pluginContext) {

        registerCodeActions(pluginContext);
    }

    private void registerCodeActions(CompilerPluginContext pluginContext) {

        pluginContext.addCodeAction(new CheckUsageCodeAction());
        pluginContext.addCodeAction(new FloatingPointLiteralCodeAction());
        pluginContext.addCodeAction(new QualifiedIdentifierCodeAction());
    }
}
