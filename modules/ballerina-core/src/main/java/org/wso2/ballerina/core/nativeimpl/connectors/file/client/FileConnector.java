/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.nativeimpl.connectors.file.client;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;

/**
 * Native File Connector.
 */
@BallerinaConnector(
        packageName = "ballerina.net.file",
        connectorName = org.wso2.ballerina.core.nativeimpl.connectors.file.client.FileConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "fileUri", type = TypeEnum.STRING)
        })
@Component(
        name = "ballerina.net.connectors.file",
        immediate = true,
        service = AbstractNativeConnector.class)
public class FileConnector extends AbstractNativeConnector implements ServiceFactory {

    public static final String CONNECTOR_NAME = "FileConnector";
    private String fileUri;

    @Override
    public boolean init(BValue[] bValueRefs) {
        if (bValueRefs != null && bValueRefs.length == 1) {
            fileUri = bValueRefs[0].stringValue();
        }
        return true;
    }

    @Override
    public FileConnector getInstance() {
        return new FileConnector();
    }

    public String getFileUri() {
        return fileUri;
    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration serviceRegistration) {
        return new FileConnector();
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object o) {
    }
}
