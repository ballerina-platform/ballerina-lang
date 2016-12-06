/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.model.expressions;

import org.wso2.ballerina.interpreter.Context;
import org.wso2.ballerina.model.Operator;
import org.wso2.ballerina.model.values.BValueRef;
import org.wso2.ballerina.utils.TriFunction;

/**
 * {@code BinaryArithmeticExpression} is the base class for any binary arithmetic expression
 *
 * @since 1.0.0
 */
public class BinaryArithmeticExpression extends BinaryExpression {

    protected TriFunction<Context, Expression, Expression, BValueRef> evalFunc;

    public BinaryArithmeticExpression(Expression lExpr, Operator op, Expression rExpr) {
        super(lExpr, op, rExpr);
    }

    public void setEvalFunc(TriFunction<Context, Expression, Expression, BValueRef> evalFunc) {
        this.evalFunc = evalFunc;
    }

    public BValueRef evaluate(Context ctx) {
        return evalFunc.apply(ctx, lExpr, rExpr);
    }

}
