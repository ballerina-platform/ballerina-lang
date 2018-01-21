/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.transport.http.netty.contractimpl;

import org.wso2.transport.http.netty.contract.HttpOperationStatusFuture;
import org.wso2.transport.http.netty.contract.HttpOperationStatusListener;
import org.wso2.transport.http.netty.contract.OperationStatus;

/**
 * Implementation of the client connector future.
 */
public class HttpOperationStatusFutureImpl implements HttpOperationStatusFuture {

    private HttpOperationStatusListener operationStatusListener = null;
    private OperationStatus operationStatus;

    @Override
    public void setHttpConnectorListener(HttpOperationStatusListener operationStatusListener) {
        this.operationStatusListener = operationStatusListener;
        if (this.operationStatus != null) {
            notifyHttpOperationListener(this.operationStatus);
            this.operationStatus =  null;
        }
    }

    @Override
    public void removeHttpOperationListener() {
        this.operationStatusListener = null;
    }

    @Override
    public void notifyHttpOperationListener(OperationStatus operationStatus) {
        if (operationStatusListener == null) {
            this.operationStatus = operationStatus;
        } else {
            operationStatusListener.onOperationComplete(this.operationStatus);
        }
    }
}
