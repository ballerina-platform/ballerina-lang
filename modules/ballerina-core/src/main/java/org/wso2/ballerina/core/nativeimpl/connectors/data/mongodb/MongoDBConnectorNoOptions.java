/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.nativeimpl.connectors.data.mongodb;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;

/**
 * MongoDB data connector w/o options.
 */
@SuppressWarnings("rawtypes")
@BallerinaConnector(
        packageName = "ballerina.data.mongodb",
        connectorName = MongoDBConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "host", type = TypeEnum.STRING),
                @Argument(name = "database", type = TypeEnum.STRING)
        })
@Component(
        name = "ballerina.data.connectors.mongodbnooptions",
        immediate = true,
        service = AbstractNativeConnector.class)
public class MongoDBConnectorNoOptions implements ServiceFactory {
    
    @Override
    public Object getService(Bundle bundle, ServiceRegistration serviceRegistration) {
        return new MongoDBConnectorNoOptions();
    }

    @Override
    public void ungetService(Bundle arg0, ServiceRegistration arg1, Object arg2) {        
    }
    
}
