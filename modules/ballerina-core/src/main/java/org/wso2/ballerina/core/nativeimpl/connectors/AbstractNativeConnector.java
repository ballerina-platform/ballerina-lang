/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Position;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.NativeConstruct;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents Native Ballerina Connector.
 */
public abstract class AbstractNativeConnector implements Connector, NativeConstruct {

    private static final Logger log = LoggerFactory.getLogger(AbstractNativeConnector.class);
    private SymbolName symbolName;
    private String packageName;
    private String connectorName;
    private List<Parameter> parameters;
    private Position connectorLocation;

    public AbstractNativeConnector() {
        parameters = new ArrayList<>();
        buildModel();
    }

    /*
     * Build Native Action Model using Java annotation.
     */
    private void buildModel() {
        BallerinaConnector connector = this.getClass().getAnnotation(BallerinaConnector.class);
        packageName = connector.packageName();
        connectorName = connector.connectorName();
        String symName = packageName + ":" + connectorName;
        symbolName = new SymbolName(symName);
        Arrays.stream(connector.args()).
                forEach(argument -> {
                    try {
                        parameters.add(new Parameter(BTypes.getType(argument.type().getName()),
                                new SymbolName(argument.name())));
                    } catch (BallerinaException e) {
                        // TODO: Fix this when TypeC.getType method is improved.
                        log.error("Internal Error..! Error while processing Parameters for Native ballerina" +
                                " Connector {}:{}.", packageName, connectorName, e);
                    }
                });
    }

    public abstract boolean init(BValue[] bValueRefs);

    public SymbolName getSymbolName() {
        return symbolName;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters.toArray(new Parameter[parameters.size()]);
    }


    //TODO Fix Issue#320
    /**
     * Get an instance of the Connector
     *
     * @return an instance
     */
    public abstract AbstractNativeConnector  getInstance();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Position getConnectorLocation() {
        return connectorLocation;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnectorLocation(Position location) {
        this.connectorLocation = location;
    }
}
