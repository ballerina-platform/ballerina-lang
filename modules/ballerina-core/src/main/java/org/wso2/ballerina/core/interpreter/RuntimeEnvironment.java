/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.interpreter;

import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.Const;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.values.BConnector;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;

/**
 * {@code RuntimeEnvironment} represents the runtime environment of a Ballerina application
 *
 * @since 1.0.0
 */
public class RuntimeEnvironment {
    private StaticMemory staticMemory;

    private RuntimeEnvironment(StaticMemory staticMemory) {
        this.staticMemory = staticMemory;
    }

    public StaticMemory getStaticMemory() {
        return staticMemory;
    }

    public static RuntimeEnvironment get(BallerinaFile bFile) {
        StaticMemory staticMemory = new StaticMemory(bFile.getSizeOfStaticMem());

        int index = -1;
        for (Const constant : bFile.getConstants()) {
            index++;
            staticMemory.setValue(index, constant.getValue());
        }

        for (Service service : bFile.getServices()) {
            for (ConnectorDcl connectorDcl : service.getConnectorDcls()) {
                index++;

                Connector connector = connectorDcl.getConnector();
                Expression[] argExpressions = connectorDcl.getArgExprs();
                BValue[] bValueRefs = new BValue[argExpressions.length];
                for (int j = 0; j < argExpressions.length; j++) {

                    Expression argExpr = argExpressions[j];
                    if (argExpr instanceof BasicLiteral) {
                        bValueRefs[j] = ((BasicLiteral) argExpr).getBValue();
                        continue;

                    } else if (argExpr instanceof VariableRefExpr) {
                        VariableRefExpr variableRefExpr = (VariableRefExpr) argExpr;
                        if (variableRefExpr.getLocation() instanceof ConstantLocation) {
                            ConstantLocation location = (ConstantLocation) variableRefExpr.getLocation();
                            bValueRefs[j] = staticMemory.getValue(location.getStaticMemAddrOffset());
                            continue;
                        }
                    }

                    // This branch shouldn't be hit
                    throw new IllegalStateException("Invalid argument in connector declaration");
                }

                if (connector instanceof AbstractNativeConnector) {
                    //TODO Fix Issue#320
                    AbstractNativeConnector nativeConnector = ((AbstractNativeConnector) connector).getInstance();
                    nativeConnector.init(bValueRefs);
                    connector = nativeConnector;
                }

                BConnector connectorValue = new BConnector(connector, bValueRefs);
                staticMemory.setValue(index, connectorValue);
            }

            for (VariableDcl variableDcl : service.getVariableDcls()) {
                index++;
                staticMemory.setValue(index, variableDcl.getType().getDefaultValue());
            }
        }

        return new RuntimeEnvironment(staticMemory);
    }

    /**
     * {@code StaticMemory} represents an statically allocated block of memory which is used to store data
     * which does not change when the program executes
     *
     * @since 1.0.0
     */
    public static class StaticMemory {
        BValue[] memSlots;

        StaticMemory(int sizeofStaticMemory) {
            memSlots = new BValue[sizeofStaticMemory];
        }

        public void setValue(int address, BValue bValue) {
            memSlots[address] = bValue;
        }

        public BValue getValue(int address) {
            return memSlots[address];
        }
    }
}
