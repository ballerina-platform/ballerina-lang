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

import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * Data model of an iterable operation, that used for semantic and desugar analysis.
 *
 * @since 0.961.0
 */
public class Operation {

    public BLangInvocation iExpr;
    public DiagnosticPos pos;
    public IterableKind kind;

    public Operation previous = null;

    public BType collectionType;
    public BType expectedType;
    public BType inputType = null;          // Operation's input argument type.
    public BType outputType = null;         // Operation's output argument type.
    public BType resultType = null;         // Reduced value or intermediate collection type.

    /* variables for lambda based operations. */
    public int arity;
    public BInvokableSymbol lambdaSymbol;
    public BInvokableType lambdaType;

    /* fields required for code generation. */
    public BLangVariable argVar = null, retVar = null;

    public Operation(IterableKind iterableKind, BLangInvocation iExpr, BType expType) {
        this.iExpr = iExpr;
        if (iExpr.argExprs.isEmpty()) {
            this.pos = iExpr.pos;
        } else {
            this.pos = iExpr.argExprs.get(0).pos;
        }
        this.collectionType = iExpr.expr.type;
        this.kind = iterableKind;
        this.expectedType = expType;
    }
}
