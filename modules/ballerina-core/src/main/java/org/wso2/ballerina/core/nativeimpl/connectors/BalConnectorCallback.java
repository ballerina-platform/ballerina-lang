/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.ballerina.core.nativeimpl.connectors;

import org.wso2.ballerina.core.interpreter.BLangInterpreter;
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.runtime.core.DefaultBalCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * A class used by connectors to receive response from external system and correlate request context with response
 */
public class BalConnectorCallback extends DefaultBalCallback {

    public BValue valueRef;

    private AssignStmt callerStatement;

    private BlockStmt calleeStatement;

    private BLangInterpreter bLangInterpreter;

    private ControlStack controlStack;

    public BalConnectorCallback(ControlStack controlStack, AssignStmt callerStatement, BlockStmt calleeStatement,
            BLangInterpreter bLangInterpreter) {
        super(null);
        this.controlStack = controlStack;
        this.callerStatement = callerStatement;
        this.calleeStatement = calleeStatement;
        this.bLangInterpreter = bLangInterpreter;
    }

    @Override
    public void done(CarbonMessage carbonMessage) {
        BMessage bMessage = new BMessage(carbonMessage);
        valueRef = bMessage;
        //context.getControlStack().setValue(4, valueRef);
        controlStack.setReturnValueNew(0, valueRef);
        Expression lExpr = callerStatement.getLExpr();
        lExpr.accept(bLangInterpreter);

        BValue rValue = getValueNew(callerStatement.getRExpr());
        setValueNew(lExpr, rValue);
        calleeStatement.accept(bLangInterpreter);

    }

    private BValue getValueNew(Expression expr) {
        if (expr instanceof BasicLiteral) {
            return ((BasicLiteral) expr).getbValueNew();
        }
        return controlStack.getValueNew(expr.getOffset());
    }

    private void setValueNew(Expression expr, BValue bValue) {
        controlStack.setValueNew(expr.getOffset(), bValue);
    }

}
