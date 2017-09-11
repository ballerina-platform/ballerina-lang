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

package org.ballerinalang.net.http.util;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.util.MessageUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.runtime.message.BallerinaMessageDataSource;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.messaging.MapCarbonMessage;
import org.wso2.carbon.messaging.MessageDataSource;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Provide util methods for http request/response
 */
public class RequestResponseUtil {
    public static BValue[] addHeader(Context context, AbstractNativeFunction abstractNativeFunction, Logger log) {
        BStruct requestStruct  = ((BStruct) abstractNativeFunction.getRefArgument(context, 0));
        String headerName = abstractNativeFunction.getStringArgument(context, 0);
        String headerValue = abstractNativeFunction.getStringArgument(context, 1);
        // Add new header.
        HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) requestStruct
                .getNativeData(Constants.TRANSPORT_MESSAGE);

        List<Header> headerList = new ArrayList<>();
        headerList.add(new Header(headerName, headerValue));
        httpCarbonMessage.setHeaders(headerList);

        if (log.isDebugEnabled()) {
            log.debug("Add " + headerName + " to header with value: " + headerValue);
        }

        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] clone(Context context, AbstractNativeFunction abstractNativeFunction, Logger log) {
        if (log.isDebugEnabled()) {
            log.debug("Invoke message clone.");
        }
        //        BMessage msg = (BMessage) getRefArgument(context, 0);
        BStruct requestStruct  = ((BStruct) abstractNativeFunction.getRefArgument(context, 0));
        HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) requestStruct
                .getNativeData(Constants.TRANSPORT_MESSAGE);
        BStruct clonedRequestStruct = createRequestStruct(context);
        HTTPCarbonMessage clonedHttpRequest = createHttpCarbonMessage(httpCarbonMessage);
        clonedRequestStruct.addNativeData(Constants.TRANSPORT_MESSAGE, clonedHttpRequest);
        return abstractNativeFunction.getBValues(clonedRequestStruct);
    }

    public static BValue[] getBinaryPayload(Context context, AbstractNativeFunction abstractNativeFunction,
            Logger log) {
        BBlob result;
        try {
            BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
            HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) requestStruct
                    .getNativeData(Constants.TRANSPORT_MESSAGE);
            if (httpCarbonMessage.isAlreadyRead()) {
                result = new BBlob((byte[]) httpCarbonMessage.getMessageDataSource().getDataObject());
            } else {
                result = new BBlob(toByteArray(httpCarbonMessage.getInputStream()));
            }
            if (log.isDebugEnabled()) {
                log.debug("Payload in String:" + result.stringValue());
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving string payload from message: " + e.getMessage());
        }
        return abstractNativeFunction.getBValues(result);
    }

    public static BValue[] getHeader(Context context, AbstractNativeFunction abstractNativeFunction, Logger log) {
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) requestStruct
                .getNativeData(Constants.TRANSPORT_MESSAGE);

        String headerName = abstractNativeFunction.getStringArgument(context, 0);
        String headerValue = httpCarbonMessage.getHeader(headerName);

//        if (headerValue == null) {
//            TODO: should NOT handle error for null headers, need to return `ballerina null`
//            ErrorHandler.handleUndefineHeader(headerName);
//        }
        return abstractNativeFunction.getBValues(new BString(headerValue));
    }

    public static BValue[] getJsonPayload(Context context, AbstractNativeFunction abstractNativeFunction, Logger log) {
        BJSON result = null;
        try {
            // Accessing First Parameter Value.
            //            BMessage msg = (BMessage) getRefArgument(ctx, 0);
            BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
            HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) requestStruct
                    .getNativeData(Constants.TRANSPORT_MESSAGE);

            if (httpCarbonMessage.isAlreadyRead()) {
                MessageDataSource payload = httpCarbonMessage.getMessageDataSource();
                if (payload instanceof BJSON) {
                    result = (BJSON) payload;
                } else {
                    // else, build the JSON from the string representation of the payload.
                    result = new BJSON(httpCarbonMessage.getMessageDataSource().getMessageAsString());
                }
            } else {
                result = new BJSON(httpCarbonMessage.getInputStream());
                httpCarbonMessage.setMessageDataSource(result);
                httpCarbonMessage.setAlreadyRead(true);
            }
        } catch (Throwable e) {
            //            ErrorHandler.handleJsonException(OPERATION, e);
        }

        // Setting output value.
        return abstractNativeFunction.getBValues(result);
    }

    public static BValue[] getProperty(Context context, AbstractNativeFunction abstractNativeFunction, Logger log) {
        BMessage msg = (BMessage) abstractNativeFunction.getRefArgument(context, 0);
        String propertyName = abstractNativeFunction.getStringArgument(context, 0);

        Object propertyValue = msg.getProperty(propertyName);

        if (propertyValue == null) {
            return AbstractNativeFunction.VOID_RETURN;
        }

        if (propertyValue instanceof String) {
            return abstractNativeFunction.getBValues(new BString((String) propertyValue));
        } else {
            throw new BallerinaException("Property value is of unknown type : " + propertyValue.getClass().getName());
        }
    }

    public static BValue[] getStringPayload(Context context, AbstractNativeFunction abstractNativeFunction,
            Logger log) {
        BString result;
        try {
            BMessage msg = (BMessage) abstractNativeFunction.getRefArgument(context, 0);
            if (msg.isAlreadyRead()) {
                result = new BString(msg.getMessageDataSource().getMessageAsString());
            } else {
                String payload = MessageUtils.getStringFromInputStream(msg.value().getInputStream());
                result = new BString(payload);
                msg.setMessageDataSource(payload);
                msg.setAlreadyRead(true);
            }
            if (log.isDebugEnabled()) {
                log.debug("Payload in String:" + result.stringValue());
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving string payload from message: " + e.getMessage());
        }
        return abstractNativeFunction.getBValues(result);
    }

    public static BValue[] getXMLPayload(Context context, AbstractNativeFunction abstractNativeFunction, Logger log) {
        BMessage msg = (BMessage) abstractNativeFunction.getRefArgument(context, 0);
        CarbonMessage carbonMessage = msg.value();
        String mapKey = abstractNativeFunction.getStringArgument(context, 0);
        String mapValue = null;
        if (carbonMessage instanceof MapCarbonMessage) {
            mapValue = ((MapCarbonMessage) carbonMessage).getValue(mapKey);
        }
        if (mapValue == null) {
            throw new BallerinaException("Given property " + mapKey + " is not found in the Map message");
        }
        return abstractNativeFunction.getBValues(new BString(mapValue));
    }

    public static BValue[] getStringValue(Context context, AbstractNativeFunction abstractNativeFunction, Logger log) {
        BXML result = null;
        try {
            // Accessing First Parameter Value.
            BMessage msg = (BMessage) abstractNativeFunction.getRefArgument(context, 0);

            if (msg.isAlreadyRead()) {
                MessageDataSource payload = msg.getMessageDataSource();
                if (payload instanceof BXML) {
                    // if the payload is already xml, return it as it is.
                    result = (BXML) payload;
                } else {
                    // else, build the xml from the string representation of the payload.
                    result = XMLUtils.parse(msg.getMessageDataSource().getMessageAsString());
                }
            } else {
                result = XMLUtils.parse(msg.value().getInputStream());
                msg.setMessageDataSource(result);
                msg.setAlreadyRead(true);
            }
        } catch (Throwable e) {
            //            ErrorHandler.handleJsonException(OPERATION, e);
        }
        // Setting output value.
        return abstractNativeFunction.getBValues(result);
    }

    private static BStruct createRequestStruct(Context context) {
        //gather package details from natives
        PackageInfo sessionPackageInfo = context.getProgramFile().getPackageInfo("ballerina.net.http");
        StructInfo sessionStructInfo = sessionPackageInfo.getStructInfo("Request");

        //create session struct
        BStructType structType = sessionStructInfo.getType();
        BStruct bStruct = new BStruct(structType);

        return bStruct;
    }

    private static HTTPCarbonMessage createHttpCarbonMessage(HTTPCarbonMessage httpCarbonMessage) {
        HTTPCarbonMessage clonedHttpCarbonMessage;
        if (httpCarbonMessage.getMessageDataSource() != null &&
                httpCarbonMessage.getMessageDataSource() instanceof BallerinaMessageDataSource) {
            // Clone the headers and the properties map of this message
            clonedHttpCarbonMessage = httpCarbonMessage.cloneCarbonMessageWithOutData(httpCarbonMessage);
            clonedHttpCarbonMessage.setMessageDataSource(((BallerinaMessageDataSource) httpCarbonMessage
                    .getMessageDataSource()).clone());
        } else {
            clonedHttpCarbonMessage = httpCarbonMessage.cloneCarbonMessageWithData(httpCarbonMessage);
        }
        return clonedHttpCarbonMessage;
    }

    private static byte[] toByteArray(InputStream input) throws IOException {
        byte[] buffer = new byte[4096];
        int n1;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (; -1 != (n1 = input.read(buffer));) {
            output.write(buffer, 0, n1);
        }
        byte[] bytes = output.toByteArray();
        output.close();
        return bytes;
    }

    public static BValue[] removeAllHeaders(Context context, AbstractNativeFunction abstractNativeFunction,
                                            Logger log) {
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);

        HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) requestStruct
                .getNativeData(org.ballerinalang.net.http.Constants.TRANSPORT_MESSAGE);
        httpCarbonMessage.getHeaders().clear();
        return abstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] removeHeader(Context context, AbstractNativeFunction abstractNativeFunction,
                                        Logger log) {
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        String headerName = abstractNativeFunction.getStringArgument(context, 0);

        HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) requestStruct
                .getNativeData(org.ballerinalang.net.http.Constants.TRANSPORT_MESSAGE);
        httpCarbonMessage.removeHeader(headerName);
        if (log.isDebugEnabled()) {
            log.debug("Remove header:" + headerName);
        }
        return abstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] setHeader(Context context, AbstractNativeFunction abstractNativeFunction,
                                      Logger log) {
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        String headerName = abstractNativeFunction.getStringArgument(context, 0);
        String headerValue = abstractNativeFunction.getStringArgument(context, 1);

        HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) requestStruct
                .getNativeData(org.ballerinalang.net.http.Constants.TRANSPORT_MESSAGE);
        httpCarbonMessage.setHeader(headerName, headerValue);

        if (log.isDebugEnabled()) {
            log.debug("Set " + headerName + " header with value: " + headerValue);
        }
        return abstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] setJsonPayload(Context context, AbstractNativeFunction abstractNativeFunction,
                                          Logger log) {
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        BJSON payload = (BJSON) abstractNativeFunction.getRefArgument(context, 1);

        HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) requestStruct
                .getNativeData(org.ballerinalang.net.http.Constants.TRANSPORT_MESSAGE);
        httpCarbonMessage.setMessageDataSource(payload);
        httpCarbonMessage.setHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        return abstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] setProperty(Context context, AbstractNativeFunction abstractNativeFunction,
                                       Logger log) {
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        String propertyName = abstractNativeFunction.getStringArgument(context, 0);
        String propertyValue = abstractNativeFunction.getStringArgument(context, 1);

        if (propertyName != null && propertyValue != null) {
            HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) requestStruct
                    .getNativeData(org.ballerinalang.net.http.Constants.TRANSPORT_MESSAGE);
            httpCarbonMessage.setProperty(propertyName, propertyValue);
        }
        return abstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] setStringPayload(Context context, AbstractNativeFunction abstractNativeFunction,
                                          Logger log) {
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        String payload = abstractNativeFunction.getStringArgument(context, 0);

        HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) requestStruct
                .getNativeData(org.ballerinalang.net.http.Constants.TRANSPORT_MESSAGE);
        StringDataSource stringDataSource = new StringDataSource(payload);
        httpCarbonMessage.setMessageDataSource(stringDataSource);
        httpCarbonMessage.setHeader(Constants.CONTENT_TYPE, Constants.TEXT_PLAIN);
        if (log.isDebugEnabled()) {
            log.debug("Setting new payload: " + payload);
        }
        return abstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] setXMLPayload(Context context, AbstractNativeFunction abstractNativeFunction,
                                         Logger log) {
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        BXML payload = (BXML) abstractNativeFunction.getRefArgument(context, 1);

        HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) requestStruct
                .getNativeData(org.ballerinalang.net.http.Constants.TRANSPORT_MESSAGE);
        httpCarbonMessage.setMessageDataSource(payload);
        httpCarbonMessage.setHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_XML);
        return abstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] getContentLenghth(Context context, AbstractNativeFunction abstractNativeFunction,
            Logger log) {
        int contentLength = -1;
        BMessage bMsg = (BMessage) abstractNativeFunction.getRefArgument(context, 0);
        String lengthStr = (String) bMsg.getHeader(Constants.HTTP_CONTENT_LENGTH);

        try {
            contentLength = Integer.parseInt(lengthStr);
        } catch (NumberFormatException e) {
            throw new BallerinaException("Invalid content length");
        }
        return abstractNativeFunction.getBValues(new BInteger(contentLength));
    }
}
