/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.transactions;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import io.ballerina.runtime.values.FutureValue;
import io.ballerina.runtime.values.MapValue;
import io.ballerina.runtime.values.ObjectValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import static io.ballerina.runtime.transactions.TransactionConstants.COORDINATOR_ABORT_TRANSACTION;
import static io.ballerina.runtime.transactions.TransactionConstants.TRANSACTION_BLOCK_CLASS_NAME;
import static io.ballerina.runtime.transactions.TransactionConstants.TRANSACTION_PACKAGE_FQN;
import static io.ballerina.runtime.transactions.TransactionConstants.TRANSACTION_PACKAGE_NAME;
import static io.ballerina.runtime.transactions.TransactionConstants.TRANSACTION_PACKAGE_VERSION;
import static io.ballerina.runtime.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;

/**
 * Utility methods used in transaction handling.
 *
 * @since 1.0
 */
public class TransactionUtils {

    private static final StrandMetadata TRX_METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX,
                                                                          TRANSACTION_PACKAGE_NAME,
                                                                          TRANSACTION_PACKAGE_VERSION, "onAbort");

    public static void notifyTransactionAbort(Strand strand, String globalTransactionId, String transactionBlockId) {
        executeFunction(strand.scheduler, TransactionUtils.class.getClassLoader(), TRANSACTION_PACKAGE_FQN,
                        TRANSACTION_BLOCK_CLASS_NAME, COORDINATOR_ABORT_TRANSACTION, globalTransactionId,
                        TRX_METADATA, globalTransactionId, transactionBlockId);
    }
    
    /**
     * This method will execute Ballerina util function blocking manner.
     *
     * @param scheduler   current scheduler
     * @param classLoader normal classLoader
     * @param packageName packageName
     * @param className   which the function resides/ or file name
     * @param methodName  to be invokable unit
     * @param strandName  name for newly creating strand which is used to execute the function pointer.
     * @param metaData meta data of new strand.
     * @param paramValues to be passed to invokable unit
     * @return return values
     */
    public static Object executeFunction(Scheduler scheduler, ClassLoader classLoader, String packageName,
                                         String className, String methodName, String strandName,
                                         StrandMetadata metaData, Object... paramValues) {
        try {
            Class<?> clazz = classLoader.loadClass(packageName + "." + className);
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
            FutureValue futureValue = scheduler.schedule(jvmArgs, func, null, new Callback() {
                @Override
                public void notifySuccess() {
                    completeFunction.countDown();
                }

                @Override
                public void notifyFailure(BError error) {
                    completeFunction.countDown();
                }
            }, new HashMap<>(), PredefinedTypes.TYPE_ANY, strandName, metaData);
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
