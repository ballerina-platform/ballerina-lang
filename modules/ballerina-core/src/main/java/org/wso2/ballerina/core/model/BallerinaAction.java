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
 * @since 1.0.0
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
    private BlockStmt actionBody;
    private Position actionLocation;

    private int stackFrameSize;

    public BallerinaAction(SymbolName name,
                           Position location,
                           Annotation[] annotations,
                           Parameter[] parameters,
                           BType[] returnTypes,
                           ConnectorDcl[] connectorDcls,
                           VariableDcl[] variableDcls,
                           Worker[] workers,
                           BlockStmt actionBody) {

        this.name = name;
        this.actionLocation = location;
        this.annotations = annotations;
        this.parameters = parameters;
        this.returnTypes = returnTypes;
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
    public BType[] getReturnTypes() {
        return returnTypes;
    }

    @Override
    public int getStackFrameSize() {
        return stackFrameSize;
    }

    @Override
    public void setStackFrameSize(int stackFrameSize) {
        this.stackFrameSize = stackFrameSize;
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
    
    public BlockStmt getActionBody() {
        return actionBody;
    }

    public void setActionBody(BlockStmt actionBody) {
        this.actionBody = actionBody;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Position getLocation() {
        return actionLocation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLocation(Position location) {
        this.actionLocation = location;
    }
}
