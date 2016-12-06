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
import org.wso2.ballerina.model.Identifier;
import org.wso2.ballerina.model.values.BValueRef;

import java.util.function.Function;

/**
 * {@code VariableRefExpr} represents a variable reference in Ballerina
 *
 * @since 1.0.0
 */
public class VariableRefExpr extends AbstractExpression {

    private Identifier identifier;

    private Function<Context, BValueRef> evalFunction;

    public VariableRefExpr(Identifier identifier) {
        this.identifier = identifier;
    }

    public void setEvalFunction(Function<Context, BValueRef> evalFunction) {
        this.evalFunction = evalFunction;
    }

    public BValueRef evaluate(Context ctx) {
        return evalFunction.apply(ctx);
    }

    public static Function<Context, BValueRef> createGetParamValueFunc(int index) {
        return context -> context.getControlStack().getCurrentFrame().parameters[index];
    }

    public static Function<Context, BValueRef> createGetLocalValueFunc(int index) {
        return context -> context.getControlStack().getCurrentFrame().localVariables[index];
    }
}
