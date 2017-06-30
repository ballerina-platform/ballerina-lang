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
import org.ballerinalang.natives.annotation.processor.Constants;
import org.ballerinalang.natives.annotation.processor.Utils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO to hold Ballerina Connector.
 */
public class ConnectorHolder {
    
    private String connectorName;
    private Argument[] connectorArgs;
    private List<ActionHolder> actions;
    private List<AnnotationHolder> annotations;
    
    
    public ConnectorHolder(String connectorName,
                           Argument[] connectorArgs) {
        this.connectorName = connectorName;
        this.connectorArgs = connectorArgs.clone();
        this.actions = new ArrayList<>();
        this.annotations = new ArrayList<>();
    }

    public Argument[] getConnectorArgs() {
        return this.connectorArgs.clone();
    }

    public void addAction(ActionHolder action) {
        this.actions.add(action);
    }
    
    public ActionHolder[] getActions() {
        return actions.toArray(new ActionHolder[0]);
    }

    public void setAnnotations(BallerinaAnnotation[] annotations) {
        this.annotations = Utils.getAnnotations(annotations);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        Utils.appendAnnotationStrings(sb, annotations);
        sb.append(annotations.size() > 0 ? "\n" : "");
        sb.append("connector ").append(connectorName);
        Utils.getInputParams(connectorArgs, sb);
        sb.append(" {\n");
        for (ActionHolder action : actions) {
            BallerinaAction ballerinaAction = action.getBalAction();
            //ignore init action from adding to native.bal
            if (ballerinaAction.actionName().equals(Constants.NATIVE_INIT_ACTION_NAME)) {
                continue;
            }
            sb.append(action.getAnnotations().size() > 0 ? "\n\t" : "");
            Utils.appendAnnotationStrings(sb, action.getAnnotations(), "\n\t");
            sb.append("\n\tnative action ").append(ballerinaAction.actionName()).append(" (");
            for (int i = 1; i <= ballerinaAction.args().length; i++) {
                Argument arg = ballerinaAction.args()[i - 1];
                sb.append(TypeEnum.CONNECTOR.getName()
                                .equals(Utils.getArgumentType(arg.type(), arg.elementType(), arg.structType(),
                                        arg.arrayDimensions())) ?
                                connectorName :
                                Utils.getArgumentType(arg.type(), arg.elementType(), arg.structType(),
                                        arg.arrayDimensions())).append(" ")
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
