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
 */

package org.ballerinalang.net.websub;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.Scheduler;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.FutureValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import static org.ballerinalang.mime.util.MimeConstants.ENTITY;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.HttpConstants.REQUEST;
import static org.ballerinalang.net.http.HttpUtil.extractEntity;
import static org.ballerinalang.net.http.HttpUtil.populateEntityBody;
import static org.ballerinalang.net.http.HttpUtil.populateInboundRequest;

/**
 * Util class for WebSub.
 */
public class WebSubUtils {

    public static final String WEBSUB_ERROR_CODE = "{ballerina/websub}WebSubError";

    static ObjectValue getHttpRequest(HttpCarbonMessage httpCarbonMessage) {
        ObjectValue httpRequest = BallerinaValues.createObjectValue(PROTOCOL_PACKAGE_HTTP, REQUEST);
        ObjectValue inRequestEntity = BallerinaValues.createObjectValue(PROTOCOL_PACKAGE_MIME, ENTITY);
        ObjectValue mediaType = BallerinaValues.createObjectValue(PROTOCOL_PACKAGE_MIME, MEDIA_TYPE);


        populateInboundRequest(httpRequest, inRequestEntity, mediaType, httpCarbonMessage);
        populateEntityBody(httpRequest, inRequestEntity, true, true);
        return httpRequest;
    }

    // TODO: 8/1/18 Handle duplicate code
    @SuppressWarnings("unchecked")
    static MapValue<String, ?> getJsonBody(ObjectValue httpRequest) {
        ObjectValue entityObj = extractEntity(httpRequest);
        if (entityObj != null) {
            Object dataSource = EntityBodyHandler.getMessageDataSource(entityObj);
            String stringPayload;
            if (dataSource != null) {
                stringPayload = MimeUtil.getMessageAsString(dataSource);
            } else {
                stringPayload = EntityBodyHandler.constructStringDataSource(entityObj);
                EntityBodyHandler.addMessageDataSource(entityObj, stringPayload);
                // Set byte channel to null, once the message data source has been constructed
                entityObj.addNativeData(ENTITY_BYTE_CHANNEL, null);
            }

            Object result = JSONParser.parse(stringPayload);
            if (result instanceof MapValue) {
                return (MapValue<String, ?>) result;
            }
            throw new BallerinaConnectorException("Non-compatible payload received for payload key based dispatching");
        } else {
            throw new BallerinaConnectorException("Error retrieving payload for payload key based dispatching");
        }
    }

    public static AttachedFunction getAttachedFunction(ObjectValue service, String functionName) {
        AttachedFunction attachedFunction = null;
        String functionFullName = service.getType().getName() + "." + functionName;
        for (AttachedFunction function : service.getType().getAttachedFunctions()) {
            //TODO test the name of resource
            if (functionFullName.contains(function.getName())) {
                attachedFunction = function;
            }
        }
        return attachedFunction;
    }

    /**
     * Create WebSub specific error record with '{ballerina/websub}WebSubError' as error code.
     *
     * @param errMsg  Actual error message
     * @return Ballerina error value
     */
    public static ErrorValue createError(String errMsg) {
        return BallerinaErrors.createError(WEBSUB_ERROR_CODE, errMsg);
    }

    /**
     * This method will execute Ballerina util function blocking manner.
     *
     * @param scheduler   current scheduler
     * @param classLoader normal classLoader
     * @param className   which the function resides/ or file name
     * @param methodName  to be invokable unit
     * @param paramValues to be passed to invokable unit
     * @return return values
     */
    public static Object executeFunction(Scheduler scheduler, ClassLoader classLoader, String className,
                                         String methodName, Object... paramValues) {
        try {
            Class<?> clazz = classLoader.loadClass("ballerina.websub." + className);
            int paramCount = paramValues.length * 2 + 1;
            Class<?>[] jvmParamTypes = new Class[paramCount];
            Object[] jvmArgs = new Object[paramCount];
            jvmParamTypes[0] = Strand.class;
            jvmArgs[0] = scheduler;
            for (int i = 0, j = 1; i < paramValues.length; i++) {
                jvmArgs[j] = paramValues[i];
                jvmParamTypes[j++] = getJvmType(paramValues[i]);
                jvmArgs[j] = true;
                jvmParamTypes[j++] = boolean.class;
            }
            Method method = clazz.getDeclaredMethod(methodName, jvmParamTypes);
            Function<Object[], Object> func = args -> {
                try {
                    return method.invoke(null, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new BallerinaException(methodName + " function invocation failed: " + e.getMessage());
                }
            };
            CountDownLatch completeFunction = new CountDownLatch(1);
            FutureValue futureValue = scheduler.schedule(jvmArgs, func, null, new CallableUnitCallback() {
                @Override
                public void notifySuccess() {
                    completeFunction.countDown();
                }

                @Override
                public void notifyFailure(ErrorValue error) {
                    completeFunction.countDown();
                }
            }, new HashMap<>());
            completeFunction.await();
            return futureValue.result;
        } catch (NoSuchMethodException | ClassNotFoundException | InterruptedException e) {
            throw new BallerinaException("invocation failed: " + e.getMessage());
        }
    }

    private static Class<?> getJvmType(Object paramValue) {
        if (paramValue instanceof MapValue) {
            return MapValue.class;
        } else if (paramValue instanceof ObjectValue) {
            return ObjectValue.class;
        } else if (paramValue instanceof Boolean) {
            return boolean.class;
        } else if (paramValue instanceof String) {
            return String.class;
        } else {
            // This is done temporarily, until blocks are added here for all possible cases.
            throw new RuntimeException("unknown param type: " + paramValue.getClass());
        }
    }
}
