/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.jms.actions;

import org.ballerinalang.bre.BallerinaTransactionContext;
import org.ballerinalang.bre.BallerinaTransactionManager;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.actions.ClientConnectorFuture;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.natives.connectors.BalConnectorCallback;
import org.ballerinalang.net.jms.JMSTransactionContext;
import org.ballerinalang.net.jms.JMSUtils;
import org.ballerinalang.net.jms.actions.utils.Constants;
import org.ballerinalang.util.DistributedTxManagerProvider;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.jms.contract.JMSClientConnector;
import org.wso2.carbon.transport.jms.exception.JMSConnectorException;
import org.wso2.carbon.transport.jms.impl.JMSConnectorFactoryImpl;
import org.wso2.carbon.transport.jms.utils.JMSConstants;
import org.wso2.carbon.transport.jms.wrappers.SessionWrapper;
import org.wso2.carbon.transport.jms.wrappers.XASessionWrapper;

import java.util.Map;
import javax.jms.Message;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

/**
 * {@code Post} is the send action implementation of the JMS Connector.
 */
@BallerinaAction(
        packageName = "ballerina.net.jms",
        actionName = "send",
        connectorName = Constants.CONNECTOR_NAME,
        args = { @Argument(name = "jmsClientConnector", type = TypeEnum.CONNECTOR),
                 @Argument(name = "destinationName", type = TypeEnum.STRING),
                 @Argument(name = "msgType", type = TypeEnum.STRING),
                 @Argument(name = "m", type = TypeEnum.MESSAGE)},
        returnType = {@ReturnType(type = TypeEnum.BOOLEAN)},
        connectorArgs = {
                @Argument(name = "properties", type = TypeEnum.MAP)
        })
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "SEND action implementation of the JMS Connector") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "connector",
        value = "Connector") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "destinationName",
        value = "Destination Name") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "msgType",
        value = "Message Type") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "message",
        value = "Message") })
public class Send extends AbstractJMSAction {
    private static final Logger log = LoggerFactory.getLogger(Send.class);

    @Override
    public ConnectorFuture execute(Context context) {

        // Extract argument values
        BConnector bConnector = (BConnector) getRefArgument(context, 0);
        BStruct messageStruct = ((BStruct) getRefArgument(context, 1));
        String destination = getStringArgument(context, 0);

        CarbonMessage carbonMessage = (CarbonMessage) messageStruct
                .getNativeData(org.ballerinalang.net.jms.Constants.TRANSPORT_MESSAGE);
        Message jmsMessage = (Message) carbonMessage.getProperty(org.ballerinalang.net.jms.Constants.JMS_API_MESSAGE);

        validateParams(bConnector);

        // Set return value to the current frame
        BValue valueRef = new BBoolean(true);
        context.getControlStackNew().currentFrame.returnValues[0] = valueRef;

        // Get the map of properties.
        BMap<String, BString> properties = (BMap<String, BString>) bConnector.getRefField(0);

        Map<String, String> propertyMap = JMSUtils.preProcessJmsConfig(properties);
        String connectorKey = propertyMap.get(Constants.JMS_CONNECTOR_KEY);
        propertyMap.remove(Constants.JMS_CONNECTOR_KEY);
        propertyMap.put(JMSConstants.PARAM_DESTINATION_NAME, destination);

        boolean isTransacted = Boolean.FALSE;
        if (propertyMap.get(JMSConstants.PARAM_ACK_MODE) != null) {
            //Todo: do we need this?
            //if the JMS transacted send is outside of the Ballerina transaction block, make it non-jms-transaction
            //           if((JMSConstants.SESSION_TRANSACTED_MODE.equals(propertyMap.get(JMSConstants.PARAM_ACK_MODE)))
            // && !context
            //                    .isInTransaction()) {
            //                propertyMap.put(JMSConstants.PARAM_ACK_MODE, JMSConstants.AUTO_ACKNOWLEDGE_MODE);
            //            }
            isTransacted = (JMSConstants.SESSION_TRANSACTED_MODE.equals(propertyMap.get(JMSConstants.PARAM_ACK_MODE)))
                    && context.isInTransaction();
        }

        try {
            JMSClientConnector jmsClientConnector = new JMSConnectorFactoryImpl()
                    .createClientConnector(propertyMap);
            if (log.isDebugEnabled()) {
                log.debug("Sending JMS Message to " + propertyMap.get(JMSConstants.PARAM_DESTINATION_NAME));
            }
            if (!isTransacted) {
                jmsClientConnector.send(jmsMessage, destination);
            } else {
                SessionWrapper sessionWrapper;
                BallerinaTransactionManager ballerinaTxManager = context.getBallerinaTransactionManager();
                BallerinaTransactionContext txContext = ballerinaTxManager.getTransactionContext(connectorKey);
                // if transaction initialization has not yet been done
                // (if this is the first transacted action happens from this particular connector with this
                // transaction block)
                if (txContext == null) {
                    sessionWrapper = jmsClientConnector.acquireSession();
                    txContext = new JMSTransactionContext(sessionWrapper, jmsClientConnector, false);
                    ballerinaTxManager.registerTransactionContext(connectorKey, txContext);

                    //Handle XA initialization
                    if (sessionWrapper instanceof XASessionWrapper) {
                        initializeXATransaction(ballerinaTxManager, (XASessionWrapper) sessionWrapper);
                    }
                } else {
                    sessionWrapper = ((JMSTransactionContext) txContext).getSessionWrapper();
                }
                jmsClientConnector.sendTransactedMessage(jmsMessage, destination, sessionWrapper);
            }
        } catch (JMSConnectorException e) {
            throw new BallerinaException("Failed to send message. " + e.getMessage(), e, context);
        }
        ClientConnectorFuture future = new ClientConnectorFuture();
        future.notifySuccess();
        return future;
    }

    private void initializeXATransaction(BallerinaTransactionManager ballerinaTxManager,
            XASessionWrapper xASessionWrapper) {
        /* Atomikos transaction manager initialize only distributed transaction is present.*/
        if (!ballerinaTxManager.hasXATransactionManager()) {
            TransactionManager transactionManager = DistributedTxManagerProvider.getInstance().getTransactionManager();
            ballerinaTxManager.setXATransactionManager(transactionManager);
        }
        if (!ballerinaTxManager.isInXATransaction()) {
            ballerinaTxManager.beginXATransaction();
        }
        Transaction tx = ballerinaTxManager.getXATransaction();
        try {
            if (tx != null) {
                XAResource xaResource = xASessionWrapper.getXASession().getXAResource();
                tx.enlistResource(xaResource);
            }
        } catch (SystemException | RollbackException e) {
            throw new BallerinaException(
                    "error in enlisting distributed transaction resources: " + e.getCause().getMessage(), e);
        }
    }

    @Override
    public void validate(BalConnectorCallback connectorCallback) {
        //Not needed for jms.
    }

}
