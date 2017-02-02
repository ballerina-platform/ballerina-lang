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

import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.types.BType;

/**
 * An {@code Action} is a operation (function) that can be executed against a connector.
 * <p/>
 * The structure of an action definition is as follows:
 * [ActionAnnotations]
 * action ActionName (ConnectorName VariableName[, ([ActionParamAnnotations] TypeName VariableName)+]) (TypeName*)
 * [throws exception] {
 * ConnectionDeclaration;*
 * VariableDeclaration;*
 * WorkerDeclaration;*
 * Statement;+
 * }
 *
 * @since 0.8.0
 */
@SuppressWarnings("unused")
public class BallerinaAction implements Action, Node {

    private SymbolName name;
    private Annotation[] annotations;
    private Parameter[] parameters;
    private ConnectorDcl[] connectorDcls;
    private VariableDcl[] variableDcls;
    private Worker[] workers;
    private BType[] returnTypes;
    private Parameter[] returnParams;
    private BlockStmt actionBody;
    private Position actionLocation;

    private int stackFrameSize;

    public BallerinaAction(SymbolName name,
                           Position location,
                           Annotation[] annotations,
                           Parameter[] parameters,
                           Parameter[] returnParams,
                           ConnectorDcl[] connectorDcls,
                           VariableDcl[] variableDcls,
                           Worker[] workers,
                           BlockStmt actionBody) {

        this.name = name;
        this.actionLocation = location;
        this.annotations = annotations;
        this.parameters = parameters;
        this.returnParams = returnParams;
        this.connectorDcls = connectorDcls;
        this.variableDcls = variableDcls;
        this.workers = workers;
        this.actionBody = actionBody;
    }

    @Override
    public String getName() {
        return name.getName();
    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public SymbolName getSymbolName() {
        return name;
    }

    @Override
    public void setSymbolName(SymbolName symbolName) {
        name = symbolName;
    }

    @Override
    public Parameter[] getReturnParameters() {
        return returnParams;
    }

    @Override
    public int getStackFrameSize() {
        return stackFrameSize;
    }

    @Override
    public void setStackFrameSize(int stackFrameSize) {
        this.stackFrameSize = stackFrameSize;
    }

    @Override
    public BlockStmt getCallableUnitBody() {
        return actionBody;
    }

    public VariableDcl[] getVariableDcls() {
        return variableDcls;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public ConnectorDcl[] getConnectorDcls() {
        return connectorDcls;
    }

    public Worker[] getWorkers() {
        return workers;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Position getLocation() {
        return actionLocation;
    }
}
