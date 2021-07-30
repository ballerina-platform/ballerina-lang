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
package org.wso2.ballerinalang.compiler.tree.matchpatterns;

import org.ballerinalang.model.tree.matchpatterns.MatchPatternNode;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

import java.util.HashMap;
import java.util.Map;

/**
 * Represent match-pattern.
 *
 * @since 2.0.0
 */
public abstract class BLangMatchPattern extends BLangNode implements MatchPatternNode {

    public BLangExpression matchExpr; // TODO : should changed as action or expr
    public boolean matchGuardIsAvailable;
    public boolean isLastPattern;
    public Map<String, BVarSymbol> declaredVars = new HashMap<>();
    public Scope scope;
}
