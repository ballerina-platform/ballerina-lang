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
package org.ballerinalang.connector.impl;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.connector.api.ConnectorFutureListener;
import org.ballerinalang.model.values.BValue;

/**
 * {@code BConnectorFuture} This is the implementation for the {@code ConnectorFuture} API.
 *
 * @since 0.94
 */
public class BConnectorFuture implements ConnectorFuture {
    private ConnectorFutureListener connectorFutureListener;

    private BValue response;
    private BallerinaConnectorException ex;

    @Override
    public void setConnectorFutureListener(ConnectorFutureListener futureListener) {
        this.connectorFutureListener = futureListener;
        if (response != null) {
            connectorFutureListener.notifySuccess(response);
        } else if (ex != null) {
            connectorFutureListener.notifyFailure(ex);
        }
        response = null;
        ex = null;
    }

    protected void notifySuccess(BValue response) {
        //if the future listener already exist, notify right away. if not store until listener registration.
        if (connectorFutureListener != null) {
            connectorFutureListener.notifySuccess(response);
        } else {
            this.response = response;
        }
    }

    protected void notifyFailure(BallerinaConnectorException ex) {
        //if the future listener already exist, notify right away. if not store until listener registration.
        if (connectorFutureListener != null) {
            connectorFutureListener.notifyFailure(ex);
        } else {
            this.ex = ex;
        }
    }

}
