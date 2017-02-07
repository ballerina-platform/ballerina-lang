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
import org.wso2.ballerina.core.model.ConstDef;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.values.BConnector;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;

/**
 * {@code RuntimeEnvironment} represents the runtime environment of a Ballerina application.
 *
 * @since 0.8.0
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

        int staticMemOffset = 0;
        for (ConstDef constant : bFile.getConstants()) {

            // TODO Evaluate the constant expression here.
            staticMemory.setValue(staticMemOffset, constant.getValue());
            staticMemOffset++;
        }

//        for (Service service : bFile.getServices()) {
//            for (ConnectorDcl connectorDcl : service.getConnectorDcls()) {
//                BConnector bConnector = populateConnectorDcls(staticMemory, connectorDcl);
//                staticMemory.setValue(staticMemOffset, bConnector);
//                staticMemOffset++;
//            }
//
//            staticMemOffset = initVariableDcls(staticMemory, staticMemOffset, service);
//        }

        return new RuntimeEnvironment(staticMemory);
    }

//    private static int initVariableDcls(StaticMemory staticMemory, int staticMemOffset, Service service) {
//        for (VariableDef variableDef : service.getVariableDefs()) {
//            staticMemory.setValue(staticMemOffset, variableDef.getType().getDefaultValue());
//            staticMemOffset++;
//        }
//        return staticMemOffset;
//    }

    private static BConnector populateConnectorDcls(StaticMemory staticMemory, ConnectorDcl connectorDcl) {
        Connector connector = connectorDcl.getConnector();
        Expression[] argExprs = connectorDcl.getArgExprs();
        BValue[] bValueRefs = null;

        int offset = 0;
        if (connector instanceof AbstractNativeConnector) {
            //TODO Fix Issue#320
            bValueRefs = new BValue[argExprs.length];
            populateConnectorArgs(staticMemory, argExprs, bValueRefs, offset);
            AbstractNativeConnector nativeConnector = ((AbstractNativeConnector) connector).getInstance();
            nativeConnector.init(bValueRefs);
            connector = nativeConnector;

        } else {
//            BallerinaConnectorDef connectorDef = (BallerinaConnectorDef) connector;
//            // sum of, number of arguments, number of declared variables and declared connectors
//            bValueRefs = new BValue[argExprs.length + connectorDef.getVariableDefs().length + connectorDef
//                    .getConnectorDcls().length];
//
//            offset = populateConnectorArgs(staticMemory, argExprs, bValueRefs, offset);
//
//            for (ConnectorDcl connectorDcl1 : connectorDef.getConnectorDcls()) {
//                BConnector bConnector = populateConnectorDcls(staticMemory, connectorDcl1);
//                bValueRefs[offset] = bConnector;
//                offset++;
//            }
//
//            for (VariableDef variableDef : connectorDef.getVariableDefs()) {
//                bValueRefs[offset] = variableDef.getType().getDefaultValue();
//                offset++;
//            }
        }

        return new BConnector(connector, bValueRefs);
    }

    private static int populateConnectorArgs(StaticMemory staticMemory,
                                             Expression[] argExpressions,
                                             BValue[] bValueRefs,
                                             int offset) {

        for (int j = 0; j < argExpressions.length; j++) {
            Expression argExpr = argExpressions[j];

            if (argExpr instanceof BasicLiteral) {
                bValueRefs[j] = ((BasicLiteral) argExpr).getBValue();
                offset++;
                continue;

            } else if (argExpr instanceof VariableRefExpr) {
                VariableRefExpr variableRefExpr = (VariableRefExpr) argExpr;
                if (variableRefExpr.getMemoryLocation() instanceof ConstantLocation) {
                    ConstantLocation location = (ConstantLocation) variableRefExpr.getMemoryLocation();
                    bValueRefs[j] = staticMemory.getValue(location.getStaticMemAddrOffset());
                    offset++;
                    continue;
                } else if (variableRefExpr.getMemoryLocation() instanceof ConnectorVarLocation) {
                    ConnectorVarLocation location = (ConnectorVarLocation) variableRefExpr.getMemoryLocation();
                    bValueRefs[j] = staticMemory.getValue(location.getConnectorMemAddrOffset());
                    offset++;
                    continue;
                }
            }

            // This branch shouldn't be hit
            throw new IllegalStateException("Invalid argument in connector declaration");
        }
        return offset;
    }

    /**
     * {@code StaticMemory} represents an statically allocated block of memory which is used to store data
     * which does not change when the program executes.
     *
     * @since 0.8.0
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
