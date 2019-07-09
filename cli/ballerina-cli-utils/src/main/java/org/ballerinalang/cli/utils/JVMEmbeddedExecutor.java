/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.cli.utils;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.bre.bvm.Strand;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.spi.EmbeddedExecutor;
import org.ballerinalang.util.EmbeddedExecutorError;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Function;

/**
 * This represents the Ballerina module provider.
 *
 * @since 0.964
 */
@JavaSPIService("org.ballerinalang.spi.EmbeddedExecutor")
public class JVMEmbeddedExecutor implements EmbeddedExecutor {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<EmbeddedExecutorError> executeFunction(String clazzName, String... args) {
        URL resource = JVMEmbeddedExecutor.class.getClassLoader().getResource("META-INF/ballerina/" + programArg);
        if (resource == null) {
            throw new BLangCompilerException("missing internal modules when executing");
        }
        
        try {
            URI balxResource = resource.toURI();
            BValue returns = ExecutorUtils.executeFunction(balxResource, args);
            // Check if the return is an error
            if (returns instanceof BError) {
                BError bError = (BError) returns;
                return Optional.of(createEmbeddedExecutorError(bError));
            } else {
                return Optional.empty();
            }
        } catch (URISyntaxException e) {
            throw new BLangCompilerException("error reading balx path in executor.");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void executeService(String clazzName) {
        URL resource = JVMEmbeddedExecutor.class.getClassLoader().getResource("META-INF/ballerina/" + balxPath);
        if (resource == null) {
            throw new BLangCompilerException("missing internal modules when executing");
        }
        
        try {
            URI balxResource = resource.toURI();
            ExecutorUtils.executeService(balxResource);
        } catch (URISyntaxException e) {
            throw new BLangCompilerException("error reading balx path in executor.");
        }
    }
    
    private static void runOnSchedule(Class<?> initClazz, BLangIdentifier name, org.ballerinalang.jvm.Scheduler scheduler1) {
        String funcName = cleanupFunctionName(name);
        try {
            final Method method = initClazz.getDeclaredMethod(funcName, Strand.class);
            Scheduler scheduler = scheduler1;
            //TODO fix following method invoke to scheduler.schedule()
            Function<Object[], Object> func = objects -> {
                try {
                    return method.invoke(null, objects[0]);
                } catch (InvocationTargetException e) {
                    throw (RuntimeException) e.getTargetException();
                } catch (IllegalAccessException e) {
                    throw new BallerinaException("Method has private access", e);
                }
            };
            final FutureValue out = scheduler.schedule(new Object[1], func, null, null, null);
            scheduler.start();
            final Throwable t = out.panic;
            if (t != null) {
                if (t instanceof org.ballerinalang.jvm.util.exceptions.BLangRuntimeException) {
                    throw new org.ballerinalang.util.exceptions.BLangRuntimeException(t.getMessage());
                }
                if (t instanceof org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException) {
                    throw new org.ballerinalang.util.exceptions.BLangRuntimeException(t.getMessage());
                }
                if (t instanceof ErrorValue) {
                    throw new org.ballerinalang.util.exceptions.BLangRuntimeException(
                            "error: " + ((ErrorValue) t).getPrintableStackTrace());
                }
                throw (RuntimeException) t;
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Error while invoking function '" + funcName + "'", e);
        }
    }
    
    private static String cleanupFunctionName(BLangIdentifier name) {
        return name.value.replaceAll("[.:/<>]", "_");
    }
    
    
    /**
     * Creates an error object for the embedded executor.
     * @param bError The error from the execution.
     * @return Created embedded executor error.
     */
    private EmbeddedExecutorError createEmbeddedExecutorError(BError bError) {
        EmbeddedExecutorError error = new EmbeddedExecutorError();
        error.setMessage(bError.getReason());
        BError cause = bError.cause;
        if (cause != null) {
            error.setCause(createEmbeddedExecutorError(cause));
        }
        return error;
    }
}