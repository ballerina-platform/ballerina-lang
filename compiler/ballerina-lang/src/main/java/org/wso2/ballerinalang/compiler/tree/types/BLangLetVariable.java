/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.ballerinalang.compiler.tree.types;

import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeEntry;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;

/**
 * Represents a variable declarations used in let expression.
 *
 * @since 1.2.0
 */
public class BLangLetVariable extends BLangNodeEntry {

    // TODO: #AST_CLEAN
    // BLangNodes
    public VariableDefinitionNode definitionNode;

    @Override
    public String toString() {
        return String.valueOf(this.definitionNode);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
        return modifier.transform(this, props);
    }
}
