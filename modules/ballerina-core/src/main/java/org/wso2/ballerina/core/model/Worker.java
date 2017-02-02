/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.ballerina.core.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.values.BValue;

import java.util.concurrent.Future;

/**
 * A {@code worker} is a thread of execution that the integration developer programs as a lifeline.
 * <p>
 *
 * Workers are defined as follows:
 *
 *  worker WorkerName (message m) {
 *      ConnectionDeclaration;*
 *      VariableDeclaration;*
 *      Statement;+
 *      [reply MessageName;]
 *  }
 *
 *  @since 0.8.0
 */
@SuppressWarnings("unused")
public class Worker implements Node, CallableUnit  {

    private static final Logger LOG = LoggerFactory.getLogger(Worker.class);

    private String name;
    private int stackFrameSize;

    private Parameter[] parameters;
    private ConnectorDcl[] connectorDcls;
    private VariableDcl[] variableDcls;
    private BlockStmt workerBody;
    private SymbolName workerName;
    private Position position;

    private Future<BValue> resultFuture;

    public Worker(String name) {
        this.name = name;
    }

    public Worker(SymbolName name,
                    Position position,
                    Parameter[] parameters,
                    ConnectorDcl[] connectorDcls,
                    VariableDcl[] variableDcls,
                    BlockStmt workerBody) {

        this.workerName = name;
        this.position = position;
        this.parameters = parameters;
        this.connectorDcls = connectorDcls;
        this.variableDcls = variableDcls;
        this.workerBody = workerBody;
    }

    public Worker(){}

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the name of the callable unit.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns the symbol name of the callable unit.
     *
     * @return the symbol name
     */
    @Override
    public SymbolName getSymbolName() {
        return this.workerName;
    }

    /**
     * Replaces the symbol name of this callable unit with the specified symbol name.
     *
     * @param symbolName name of the symbol.
     */
    @Override
    public void setSymbolName(SymbolName symbolName) {
        this.workerName = symbolName;

    }

    /**
     * Returns the package name of this callable unit.
     *
     * @return the package name
     */
    @Override
    public String getPackageName() {
        return workerName.getPkgName();
    }

    /**
     * Returns an array of annotations attached this callable unit.
     *
     * @return an array of annotations
     */
    @Override
    public Annotation[] getAnnotations() {
        return new Annotation[0];
    }

    /**
     * Returns an array of parameters of this callable unit.
     *
     * @return an array of parameters
     */
    @Override
    public Parameter[] getParameters() {
        return this.parameters;
    }

    /**
     * Returns an array of variable declarations of this callable unit.
     *
     * @return an array of variable declarations
     */
    @Override
    public VariableDcl[] getVariableDcls() {
        return this.variableDcls;
    }

    /**
     * Get all Connections declared within the BallerinaFunction scope.
     *
     * @return list of all the Connections belongs to a BallerinaFunction
     */
    public ConnectorDcl[] getConnectorDcls() {
        return connectorDcls;
    }

    /**
     * Returns an array of return parameters (values) of this callable unit.
     *
     * @return an array of return parameters
     */
    @Override
    public Parameter[] getReturnParameters() {
        return new Parameter[0];
    }


    /**
     * Returns size of the stack frame which should be allocated for each invocations.
     *
     * @return size of the stack frame
     */
    @Override
    public int getStackFrameSize() {
        return this.stackFrameSize;
    }

    /**
     * Replaces the size of the current stack frame with the specified size.
     *
     * @param frameSize size of the stack frame
     */
    @Override
    public void setStackFrameSize(int frameSize) {
        this.stackFrameSize = frameSize;
    }

    /**
     * Returns the body of the callable unit as a {@code BlockStmt}.
     *
     * @return body of the callable unit
     */
    @Override
    public BlockStmt getCallableUnitBody() {
        return this.workerBody;
    }

    /**
     * Get the location of this function in the ballerina source file.
     * Returns the ballerina file and line number of the function.
     *
     * @return location of this function in the ballerina source file
     */
    @Override
    public Position getLocation() {
        return this.position;
    }


    public Future<BValue> getResultFuture() {
        return resultFuture;
    }

    public void setResultFuture(Future<BValue> resultFuture) {
        this.resultFuture = resultFuture;
    }
}
