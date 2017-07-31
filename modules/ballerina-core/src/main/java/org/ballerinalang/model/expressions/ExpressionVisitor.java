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
package org.ballerinalang.model.expressions;

import org.ballerinalang.model.expressions.variablerefs.FieldBasedVarRefExpr;
import org.ballerinalang.model.expressions.variablerefs.IndexBasedVarRefExpr;
import org.ballerinalang.model.expressions.variablerefs.SimpleVarRefExpr;
import org.ballerinalang.model.expressions.variablerefs.XMLAttributesRefExpr;

/**
 * @since 0.92
 */
public interface ExpressionVisitor {

    Expression visit(BasicLiteral basicLiteral);

    Expression visit(NullLiteral nullLiteral);

    Expression visit(SimpleVarRefExpr simpleVarRefExpr);

    Expression visit(FieldBasedVarRefExpr fieldBasedVarRefExpr);

    Expression visit(IndexBasedVarRefExpr indexBasedVarRefExpr);

    Expression visit(UnaryExpression unaryExpression);

    Expression visit(AddExpression addExpr);

    Expression visit(SubtractExpression subtractExpression);

    Expression visit(MultExpression multExpression);

    Expression visit(DivideExpr divideExpr);

    Expression visit(ModExpression modExpression);

    Expression visit(AndExpression andExpression);

    Expression visit(OrExpression orExpression);

    Expression visit(EqualExpression equalExpression);

    Expression visit(NotEqualExpression notEqualExpression);

    Expression visit(GreaterEqualExpression greaterEqualExpression);

    Expression visit(GreaterThanExpression greaterThanExpression);

    Expression visit(LessEqualExpression lessEqualExpression);

    Expression visit(LessThanExpression lessThanExpression);

    Expression visit(FunctionInvocationExpr functionInvocationExpr);

    Expression visit(ActionInvocationExpr actionInvocationExpr);

    Expression visit(TypeCastExpression typeCastExpression);

    Expression visit(TypeConversionExpr typeConversionExpression);

    Expression visit(ArrayInitExpr arrayInitExpr);

    Expression visit(RefTypeInitExpr refTypeInitExpr);

    Expression visit(ConnectorInitExpr connectorInitExpr);

    Expression visit(StructInitExpr structInitExpr);

    Expression visit(MapInitExpr mapInitExpr);

    Expression visit(JSONInitExpr jsonInitExpr);

    Expression visit(JSONArrayInitExpr jsonArrayInitExpr);

    Expression visit(XMLLiteral xmlLiteral);

    Expression visit(XMLElementLiteral xmlElement);

    Expression visit(XMLCommentLiteral xmlComment);

    Expression visit(XMLTextLiteral xmlText);

    Expression visit(XMLPILiteral xmlPI);

    Expression visit(XMLAttributesRefExpr xmlAttributesRefExpr);

    Expression visit(XMLQNameExpr xmlQNameRefExpr);

    Expression visit(XMLSequenceLiteral xmlSequence);

    Expression visit(LambdaExpression lambdaExpr);
}
