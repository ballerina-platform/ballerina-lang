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
package org.ballerinalang.net.jms;

import org.ballerinalang.bre.BallerinaTransactionContext;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.transport.jms.wrappers.SessionWrapper;
import org.wso2.carbon.transport.jms.contract.JMSClientConnector;
import org.wso2.carbon.transport.jms.exception.JMSConnectorException;

import javax.jms.JMSException;

/**
 *
 */
public class JMSTransactionContext implements BallerinaTransactionContext {
    private SessionWrapper sessionWrapper;
    private JMSClientConnector clientConnector;
    private boolean xaConn;

    public JMSTransactionContext(SessionWrapper sessionWrapper, JMSClientConnector clientConnector, boolean isXAConn) {
        this.sessionWrapper = sessionWrapper;
        this.clientConnector = clientConnector;
        this.xaConn = isXAConn;
    }

    public SessionWrapper getSessionWrapper() {
        return this.sessionWrapper;
    }

    @Override
    public void commit() {
        try {
            if (sessionWrapper != null && sessionWrapper.getSession().getTransacted()) {
                sessionWrapper.getSession().commit();
            }
        } catch (JMSException e) {
            throw new BallerinaException("transaction commit failed:" + e.getMessage());
        }
    }

    @Override
    public void rollback() {
        try {
            if (sessionWrapper != null && sessionWrapper.getSession().getTransacted()) {
                sessionWrapper.getSession().rollback();
            }
        } catch (JMSException e) {
            throw new BallerinaException("transaction rollback failed:" + e.getMessage());
        }
    }

    @Override
    public void close() {
        if(sessionWrapper != null) {
            try {
                clientConnector.releaseSession(sessionWrapper);
                sessionWrapper = null;
            } catch (JMSConnectorException e) {
                throw new BallerinaException("JMS Session release failed:" + e.getMessage());
            }
        }
    }

    @Override
    public boolean isXAConnection() {
        return this.xaConn;
    }

}
