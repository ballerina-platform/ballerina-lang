/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.ballerina.core.interpreter.nonblocking;

import org.wso2.ballerina.core.interpreter.ConnectorVarLocation;
import org.wso2.ballerina.core.interpreter.ConstantLocation;
import org.wso2.ballerina.core.interpreter.ServiceVarLocation;
import org.wso2.ballerina.core.interpreter.StackVarLocation;
import org.wso2.ballerina.core.interpreter.StructVarLocation;
import org.wso2.ballerina.core.interpreter.WorkerVarLocation;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BTypeMapper;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnectorDef;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.ConstDef;
import org.wso2.ballerina.core.model.ImportPackage;
import org.wso2.ballerina.core.model.LinkedNode;
import org.wso2.ballerina.core.model.LinkedNodeVisitor;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.StructDef;
import org.wso2.ballerina.core.model.VariableDef;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.AndExpression;
import org.wso2.ballerina.core.model.expressions.BinaryExpression;
import org.wso2.ballerina.core.model.expressions.DivideExpr;
import org.wso2.ballerina.core.model.expressions.EqualExpression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.GreaterEqualExpression;
import org.wso2.ballerina.core.model.expressions.GreaterThanExpression;
import org.wso2.ballerina.core.model.expressions.LessEqualExpression;
import org.wso2.ballerina.core.model.expressions.LessThanExpression;
import org.wso2.ballerina.core.model.expressions.ModExpression;
import org.wso2.ballerina.core.model.expressions.MultExpression;
import org.wso2.ballerina.core.model.expressions.NotEqualExpression;
import org.wso2.ballerina.core.model.expressions.OrExpression;
import org.wso2.ballerina.core.model.expressions.ResourceInvocationExpr;
import org.wso2.ballerina.core.model.expressions.SubtractExpression;
import org.wso2.ballerina.core.model.invokers.MainInvoker;
import org.wso2.ballerina.core.model.values.BException;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * Interface for Implementing LinkedNode based Executors.
 */
public abstract class BLangExecutionVisitor implements LinkedNodeVisitor {

    /* Memory Locations */
    public abstract BValue access(ConnectorVarLocation connectorVarLocation);

    public abstract BValue access(ConstantLocation constantLocation);

    public abstract BValue access(ServiceVarLocation serviceVarLocation);

    public abstract BValue access(StackVarLocation stackVarLocation);

    public abstract BValue access(StructVarLocation structVarLocation);

    public abstract BValue access(WorkerVarLocation workerVarLocation);

    public abstract void execute(ResourceInvocationExpr resourceInvocationExpr);

    public abstract void execute(FunctionInvocationExpr functionInvocationExpr);

    public abstract void handleBException(BException exception);

    public abstract void continueExecution(LinkedNode linkedNode);

    public void visit(BallerinaFile bFile) {
    }

    @Override
    public void visit(ImportPackage importPkg) {
    }

    @Override
    public void visit(ConstDef constant) {
    }

    @Override
    public void visit(Service service) {
    }

    @Override
    public void visit(BallerinaConnectorDef connector) {
    }

    @Override
    public void visit(Resource resource) {
    }

    @Override
    public void visit(BallerinaFunction function) {
    }

    @Override
    public void visit(BTypeMapper typeMapper) {
    }

    @Override
    public void visit(BallerinaAction action) {
    }

    @Override
    public void visit(Worker worker) {
    }

    @Override
    public void visit(Annotation annotation) {
    }

    @Override
    public void visit(ParameterDef parameterDef) {
    }

    @Override
    public void visit(ConnectorDcl connectorDcl) {
    }

    @Override
    public void visit(VariableDef variableDef) {
    }

    @Override
    public void visit(StructDef structDef) {
    }

    @Override
    public void visit(AddExpression addExpr) {
        visitBinaryExpression(addExpr);
    }

    @Override
    public void visit(AndExpression andExpression) {
        visitBinaryExpression(andExpression);
    }

    @Override
    public void visit(DivideExpr divideExpr) {
        visitBinaryExpression(divideExpr);
    }

    @Override
    public void visit(ModExpression modExpression) {
        visitBinaryExpression(modExpression);
    }

    @Override
    public void visit(EqualExpression equalExpression) {
        visitBinaryExpression(equalExpression);
    }

    @Override
    public void visit(GreaterEqualExpression greaterEqualExpression) {
        visitBinaryExpression(greaterEqualExpression);
    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpression) {
        visitBinaryExpression(greaterThanExpression);
    }

    @Override
    public void visit(LessEqualExpression lessEqualExpression) {
        visitBinaryExpression(lessEqualExpression);
    }

    @Override
    public void visit(LessThanExpression lessThanExpression) {
        visitBinaryExpression(lessThanExpression);
    }

    @Override
    public void visit(MultExpression multExpression) {
        visitBinaryExpression(multExpression);
    }

    @Override
    public void visit(NotEqualExpression notEqualExpression) {
        visitBinaryExpression(notEqualExpression);
    }

    @Override
    public void visit(OrExpression orExpression) {
        visitBinaryExpression(orExpression);
    }

    @Override
    public void visit(SubtractExpression subtractExpression) {
        visitBinaryExpression(subtractExpression);
    }

    @Override
    public void visit(StackVarLocation stackVarLocation) {
    }

    @Override
    public void visit(ServiceVarLocation serviceVarLocation) {
    }

    @Override
    public void visit(ConnectorVarLocation connectorVarLocation) {
    }

    @Override
    public void visit(ConstantLocation constantLocation) {
    }

    @Override
    public void visit(MainInvoker mainInvoker) {
    }

    @Override
    public void visit(StructVarLocation structVarLocation) {
    }

    @Override
    public void visit(WorkerVarLocation workerVarLocation) {

    }

    private void visitBinaryExpression(BinaryExpression expression) {
        try {
            this.visit(expression);
        } catch (RuntimeException e) {
            handleBException(new BException(e.getMessage()));
        }
    }
}
