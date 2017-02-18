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
package org.ballerinalang.bre.nonblocking;

import org.ballerinalang.bre.ConnectorVarLocation;
import org.ballerinalang.bre.ConstantLocation;
import org.ballerinalang.bre.ServiceVarLocation;
import org.ballerinalang.bre.StackVarLocation;
import org.ballerinalang.bre.StructVarLocation;
import org.ballerinalang.bre.WorkerVarLocation;
import org.ballerinalang.model.Annotation;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.BTypeMapper;
import org.ballerinalang.model.BallerinaAction;
import org.ballerinalang.model.BallerinaConnectorDef;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.ConstDef;
import org.ballerinalang.model.ImportPackage;
import org.ballerinalang.model.LinkedNode;
import org.ballerinalang.model.LinkedNodeVisitor;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.expressions.AddExpression;
import org.ballerinalang.model.expressions.AndExpression;
import org.ballerinalang.model.expressions.BinaryExpression;
import org.ballerinalang.model.expressions.DivideExpr;
import org.ballerinalang.model.expressions.EqualExpression;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.GreaterEqualExpression;
import org.ballerinalang.model.expressions.GreaterThanExpression;
import org.ballerinalang.model.expressions.LessEqualExpression;
import org.ballerinalang.model.expressions.LessThanExpression;
import org.ballerinalang.model.expressions.ModExpression;
import org.ballerinalang.model.expressions.MultExpression;
import org.ballerinalang.model.expressions.NotEqualExpression;
import org.ballerinalang.model.expressions.OrExpression;
import org.ballerinalang.model.expressions.ResourceInvocationExpr;
import org.ballerinalang.model.expressions.SubtractExpression;
import org.ballerinalang.model.invokers.MainInvoker;
import org.ballerinalang.model.values.BException;
import org.ballerinalang.model.values.BValue;

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

    public abstract void continueExecution();

    @Override
    public void visit(BLangProgram bLangProgram) {

    }

    @Override
    public void visit(BLangPackage bLangPackage) {

    }

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
