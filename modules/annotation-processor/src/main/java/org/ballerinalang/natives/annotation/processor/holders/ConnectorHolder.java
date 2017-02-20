/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.natives.annotation.processor.holders;

import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.natives.annotation.processor.Utils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaConnector;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO to hold Ballerina Connector
 */
public class ConnectorHolder {
    
    private BallerinaConnector balConnector;
    private String connectorClassName;
    private List<ActionHolder> actions;
    private List<AnnotationHolder> annotations;
    
    
    public ConnectorHolder(BallerinaConnector balConnector, String className) {
        this.balConnector = balConnector;
        this.connectorClassName = className;
        this.actions = new ArrayList<>();
        this.annotations = new ArrayList<>();
    }
    
    public void addAction(ActionHolder action) {
        this.actions.add(action);
    }
    
    public ActionHolder[] getActions() {
        return actions.toArray(new ActionHolder[0]);
    }
    
    public BallerinaConnector getBalConnector() {
        return balConnector;
    }
    
    public String getClassName() {
        return connectorClassName;
    }

    public void setAnnotations(BallerinaAnnotation[] annotations) {
        this.annotations = Utils.getAnnotations(annotations);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        Utils.appendAnnotationStrings(sb, annotations);
        sb.append(annotations.size() > 0 ? "\n" : "");
        sb.append("native connector ").append(balConnector.connectorName());
        Utils.getInputParams(balConnector.args(), sb);
        sb.append(" {\n");
        for (ActionHolder action : actions) {
            BallerinaAction ballerinaAction = action.getBalAction();
            sb.append(action.getAnnotations().size() > 0 ? "\n\t" : "");
            Utils.appendAnnotationStrings(sb, action.getAnnotations(), "\n\t");
            sb.append("\n\tnative action ").append(ballerinaAction.actionName()).append(" (");
            for (int i = 1; i <= ballerinaAction.args().length; i++) {
                Argument arg = ballerinaAction.args()[i - 1];
                sb.append(TypeEnum.CONNECTOR.getName()
                                .equals(Utils.getArgumentType(arg.type(), arg.elementType(), arg.structType())) ?
                                balConnector.connectorName() :
                                Utils.getArgumentType(arg.type(), arg.elementType(), arg.structType())).append(" ")
                        .append(arg.name());
                if (i != ballerinaAction.args().length) {
                    sb.append(", ");
                }
            }
            sb.append(")");
            Utils.appendReturnParams(ballerinaAction.returnType(), sb);
            sb.append(";\n");
        }

        sb.append("\n}");
        return sb.toString();
    }
}
