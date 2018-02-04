/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.model.iterable;

import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Add.
 *
 * @since 0.96.1
 */
public class Operation {

    public BLangInvocation iExpr;
    public DiagnosticPos pos;
    public IterableKind kind;
    public SymbolEnv env;

    public Operation previous;

    public BType collectionType;
    public List<BType> expectedTypes;
    public List<BType> resultTypes;
    public List<BType> argTypes, retArgTypes;

    /* variables for lambda based operations. */
    public int arity;
    public BLangExpression lambda;
    public BInvokableType lambdaType;


    /* fields required for code generation. */
    public List<BLangVariable> argVars;
    public List<BLangVariable> retVars;

    public Operation(IterableKind iterableKind, BLangInvocation iExpr, List<BType> expTypes, SymbolEnv env) {
        this.iExpr = iExpr;
        this.pos = iExpr.pos;
        this.collectionType = iExpr.expr.type;
        this.kind = iterableKind;
        this.expectedTypes = expTypes;
        this.env = env;
        this.argVars = new ArrayList<>();
        this.retVars = new ArrayList<>();
    }
}
