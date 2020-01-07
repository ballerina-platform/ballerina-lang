/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType.NarrowedTypes;
import org.wso2.ballerinalang.compiler.tree.BLangNode;

import java.util.Map;

/**
 * {@code BLangExpression} represents an expression node in Ballerina AST.
 *
 * @since 0.94
 */
public abstract class BLangExpression extends BLangNode implements ExpressionNode {

    /**
     * Implicit cast expression. If the type of this expression is assignable
     * to the expected type and an implicit cast is required, this field is
     * populated with the generated cast expression. The tree rewrite will happen
     * in the 'Desugar' phase.
     */
    public BLangTypeConversionExpr impConversionExpr;

    /**
     * Indicates whether the expression has already been type checked.
     */
    public boolean typeChecked;

    /**
     * Indicates the contextually expected type.
     */
    public BType expectedType;

    public Map<BVarSymbol, NarrowedTypes> narrowedTypeInfo;
}
