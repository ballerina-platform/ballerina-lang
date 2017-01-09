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

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BConnector;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BXML;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;
import org.wso2.ballerina.core.nativeimpl.connectors.file.util.FileConnectorUtils;
import org.wso2.ballerina.core.nativeimpl.connectors.file.util.FileConstants;
import org.wso2.ballerina.core.nativeimpl.connectors.file.util.ResultPayloadCreate;
import org.wso2.ballerina.core.nativeimpl.connectors.http.Constants;
import org.wso2.ballerina.core.nativeimpl.connectors.http.client.Get;
import org.wso2.ballerina.core.nativeimpl.connectors.http.client.HTTPConnector;
import org.wso2.carbon.messaging.CarbonMessage;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;

/**
 * {@code Send} is the Send action implementation of the File Connector
 */
@BallerinaAction(
        packageName = "ballerina.net.file",
        actionName = "send",
        connectorName = FileConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "connector",
                        type = TypeEnum.CONNECTOR),
                @Argument(name = "path", type = TypeEnum.STRING),
                @Argument(name = "message", type = TypeEnum.MESSAGE)
        })
@Component(
        name = "action.netfile.send",
        immediate = true,
        service = AbstractNativeAction.class)
public class Send extends AbstractFileAction {

    private static final Logger logger = LoggerFactory.getLogger(Get.class);

    @Override
    public BValue execute(Context context) {

        logger.debug("Executing Native Action : Send");

        try {
            // Extract Argument values
            BConnector bConnector = (BConnector) getArgument(context, 0);
            String path = getArgument(context, 1).stringValue();
            BMessage bMessage = (BMessage) getArgument(context, 2);

            Connector connector = bConnector.value();
            if (!(connector instanceof FileConnector)) {
                throw new BallerinaException("Need to use a FileConnector as the first argument", context);
            }
            // Prepare the message
            CarbonMessage cMsg = bMessage.value();
            prepareRequest(connector, path, cMsg);
            cMsg.setProperty(Constants.HTTP_METHOD,
                    Constants.HTTP_METHOD_GET);

            // Execute the file related operation
            boolean append = false;
            String[] params = new String[2];
            String address = getArgument(context, 1).stringValue();
            String strAppend = getArgument(context, 2).stringValue();
            params[0] = address;
            params[1] = strAppend;
            if (strAppend != null) {
                append = Boolean.parseBoolean(strAppend);
            }
            boolean resultStatus = sendResponseFile(address, context, append, bMessage, params);
            generateResults(context, resultStatus);

            // Execute the operation
            return executeAction(context, cMsg);
        } catch (Throwable t) {
            throw new BallerinaException("Failed to invoke 'Get' action in " + HTTPConnector.CONNECTOR_NAME
                    + ". " + t.getMessage(), context);
        }
    }


    /**
     * Generate the result
     *
     * @param messageContext The message context that is processed by a handler in the handle method
     * @param resultStatus   Result of the status (true/false)
     */
    private void generateResults(Context messageContext, boolean resultStatus) {
        ResultPayloadCreate resultPayload = new ResultPayloadCreate();
        String response = FileConstants.START_TAG + resultStatus + FileConstants.END_TAG;
        try {
            OMElement element = resultPayload.performSearchMessages(response);
            resultPayload.preparePayload(messageContext, element);
        } catch (XMLStreamException e) {
            throw new BallerinaException(e.getMessage(), e);
        } catch (IOException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    /**
     * Send the files
     *
     * @param address Location for send the file
     * @param append  If the response should be appended to the response file or not
     * @return return a resultStatus
     */
    private boolean sendResponseFile(String address, Context messageContext, boolean append, BMessage message,
                                     String[] params) {
        boolean resultStatus = false;
        FileObject fileObj;
        StandardFileSystemManager manager = null;
        CountingOutputStream os = null;
        if (logger.isDebugEnabled()) {
            logger.debug("File sending started to" + address);
        }
        try {
            manager = FileConnectorUtils.getManager();
            fileObj = manager.resolveFile(address, FileConnectorUtils.init(messageContext, params));
            if (fileObj.getType() == FileType.FOLDER) {
                address = address.concat(FileConstants.DEFAULT_RESPONSE_FILE);
                fileObj = manager.resolveFile(address, FileConnectorUtils.init(messageContext, params));
            }

            // Creating output stream and give the content to that.
            os = new CountingOutputStream(fileObj.getContent().getOutputStream(append));
            if (messageContext != null) {
                BXML bxml = new BXML();
                bxml.setOutputStream(os);
                bxml.serializeData();
                resultStatus = true;
                if (logger.isDebugEnabled()) {
                    logger.debug("File send completed to " + address);
                }
            } else {
                logger.error("Can not send the file to specific address");
            }
        } catch (IOException e) {
            throw new BallerinaException("Unable to send a file/folder.", e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                logger.warn("Can not close the output stream");
            }
            if (manager != null) {
                manager.close();
            }
        }
        return resultStatus;
    }
}
