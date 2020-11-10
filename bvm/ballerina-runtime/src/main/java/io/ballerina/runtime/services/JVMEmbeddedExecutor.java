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
package io.ballerina.runtime.services;

import io.ballerina.runtime.annotation.JavaSPIService;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;
import io.ballerina.runtime.services.spi.EmbeddedExecutor;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.util.ArgumentParser;
import io.ballerina.runtime.util.RuntimeUtils;
import io.ballerina.runtime.util.exceptions.BLangRuntimeException;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import io.ballerina.runtime.values.ArrayValue;
import io.ballerina.runtime.values.ErrorValue;
import io.ballerina.runtime.values.FutureValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;

/**
 * This represents the Ballerina module provider.
 *
 * @since 0.964
 */
@JavaSPIService("io.ballerina.runtime.services.spi.EmbeddedExecutor")
public class JVMEmbeddedExecutor implements EmbeddedExecutor {

    private static final String MODULE_INIT_CLASS = ".$_init";

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<RuntimeException> executeMainFunction(String moduleName, String moduleVersion, String strandName,
                                                          StrandMetadata metaData, String[] args) {
        try {
            final Scheduler scheduler = new Scheduler(false);
            runInitOnSchedule(moduleName, moduleVersion, scheduler, strandName, metaData);
            runMainOnSchedule(moduleName, moduleVersion, scheduler, strandName, metaData, args);
            scheduler.immortal = true;
            new Thread(scheduler::start).start();
            return Optional.empty();
        } catch (RuntimeException e) {
            return Optional.of(e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<RuntimeException> executeService(String moduleName, String moduleVersion, String strandName,
                                                     StrandMetadata metaData) {
        try {
            final Scheduler scheduler = new Scheduler(false);
            runInitOnSchedule(moduleName, moduleVersion, scheduler, strandName, metaData);
            runStartOnSchedule(moduleName, moduleVersion, scheduler, strandName, metaData);
            scheduler.immortal = true;
            new Thread(scheduler::start).start();
            return Optional.empty();
        } catch (RuntimeException e) {
            return Optional.of(e);
        }
    }

    /**
     * Executes the __start_ function of the module.
     *
     * @param moduleName    The name of the module.
     * @param moduleVersion Version of the module.
     * @param scheduler     The scheduler.
     * @param strandName    name for newly creating strand which is used to execute the function pointer.
     * @param metaData      meta data of new strand.
     * @throws RuntimeException When an error occurs invoking or within the function.
     */
    private void runStartOnSchedule(String moduleName, String moduleVersion, Scheduler scheduler, String strandName,
                                    StrandMetadata metaData)
            throws RuntimeException {
        try {
            Class<?> initClazz = Class.forName("ballerina." + moduleName + "." +
                                                       moduleVersion.replace(".", "_") + MODULE_INIT_CLASS);
            final Method initMethod = initClazz.getDeclaredMethod("$moduleStart", Strand.class);
            //TODO fix following method invoke to scheduler.schedule()
            Function<Object[], Object> func = objects -> {
                try {
                    return initMethod.invoke(null, objects[0]);
                
                } catch (InvocationTargetException e) {
                    throw (RuntimeException) e.getTargetException();
                } catch (IllegalAccessException e) {
                    throw new BallerinaException("Method has private access", e);
                }
            };
            final FutureValue out = scheduler.schedule(new Object[1], func, null, null, null,
                                                       PredefinedTypes.TYPE_NULL, strandName, metaData);
            scheduler.start();
            final Throwable t = out.panic;
            if (t != null) {
                if (t instanceof io.ballerina.runtime.util.exceptions.BLangRuntimeException) {
                    throw new BLangRuntimeException(t.getMessage());
                }
                if (t instanceof io.ballerina.runtime.util.exceptions.BallerinaConnectorException) {
                    throw new BLangRuntimeException(t.getMessage());
                }
                if (t instanceof ErrorValue) {
                    throw new BLangRuntimeException(
                            "error: " + ((ErrorValue) t).getPrintableStackTrace().replaceAll("\\{}", ""));
                }
                throw (RuntimeException) t;
            }
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException("Error while invoking main function: " + moduleName, e);
        }
    }

    /**
     * Executes the <module_name>.main function of a module.
     *
     * @param moduleName    The name of the module.
     * @param moduleVersion Version of the module.
     * @param scheduler     The scheduler which executes the function.
     * @param strandName    name for newly creating strand which is used to execute the function pointer.
     * @param metaData      meta data of new strand.
     * @param stringArgs    The string arguments for the function.
     * @throws RuntimeException When an error occurs invoking or within the function.
     */
    private static void runMainOnSchedule(String moduleName, String moduleVersion, Scheduler scheduler,
                                          String strandName, StrandMetadata metaData, String[] stringArgs)
            throws RuntimeException {
        try {
            Class<?> mainClass = Class.forName("ballerina." + moduleName + "." +
                                                       moduleVersion.replace(".", "_") + "." + moduleName);
            final Method mainMethod = mainClass.getDeclaredMethod("main", Strand.class, ArrayValue.class,
                    boolean.class);
            Object[] entryFuncArgs =
                    ArgumentParser.extractEntryFuncArgs(new RuntimeUtils.ParamInfo[]{
                            new RuntimeUtils.ParamInfo(false,
                                    "%1",
                                    new BArrayType(PredefinedTypes.TYPE_STRING, stringArgs.length))
                    }, stringArgs, true);
            
            //TODO fix following method invoke to scheduler.schedule()
            Function<Object[], Object> func = objects -> {
                try {
                    return mainMethod.invoke(null, entryFuncArgs);
                    
                } catch (InvocationTargetException e) {
                    throw (RuntimeException) e.getTargetException();
                } catch (IllegalAccessException e) {
                    throw new BallerinaException("Method has private access", e);
                }
            };
            final FutureValue out = scheduler.schedule(entryFuncArgs, func, null, null, null,
                                                       PredefinedTypes.TYPE_NULL, strandName, metaData);
            scheduler.start();
            final Throwable t = out.panic;
            if (t != null) {
                if (t instanceof io.ballerina.runtime.util.exceptions.BLangRuntimeException) {
                    throw new BLangRuntimeException(t.getMessage());
                }
                if (t instanceof io.ballerina.runtime.util.exceptions.BallerinaConnectorException) {
                    throw new BLangRuntimeException(t.getMessage());
                }
                if (t instanceof ErrorValue) {
                    throw new BLangRuntimeException(
                            "error: " + ((ErrorValue) t).getPrintableStackTrace().replaceAll("\\{}", ""));
                }
                throw (RuntimeException) t;
            }
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException("Error while invoking main function: " + moduleName, e);
        }
    }

    /**
     * Executes the __init_ function of the module.
     *
     * @param moduleName    The name of the module.
     * @param moduleVersion Version of the module.
     * @param scheduler     The scheduler which executes the function.
     * @param strandName    name for newly creating strand which is used to execute the function pointer.
     * @param metaData      meta data of new strand.
     * @throws RuntimeException When an error occurs invoking or within the function.
     */
    private static void runInitOnSchedule(String moduleName, String moduleVersion, Scheduler scheduler,
                                          String strandName, StrandMetadata metaData)
            throws RuntimeException {
        try {
            Class<?> initClazz = Class.forName("ballerina." + moduleName + "." +
                                                       moduleVersion.replace(".", "_") + MODULE_INIT_CLASS);
            final Method initMethod = initClazz.getDeclaredMethod("$moduleInit", Strand.class);
            //TODO fix following method invoke to scheduler.schedule()
            Function<Object[], Object> func = objects -> {
                try {
                    return initMethod.invoke(null, objects[0]);
                
                } catch (InvocationTargetException e) {
                    throw (RuntimeException) e.getTargetException();
                } catch (IllegalAccessException e) {
                    throw new BallerinaException("Method has private access", e);
                }
            };
            final FutureValue out = scheduler.schedule(new Object[1], func, null, null, null,
                                                       PredefinedTypes.TYPE_NULL, strandName, metaData);
            scheduler.start();
            final Throwable t = out.panic;
            if (t != null) {
                if (t instanceof io.ballerina.runtime.util.exceptions.BLangRuntimeException) {
                    throw new BLangRuntimeException(t.getMessage());
                }
                if (t instanceof io.ballerina.runtime.util.exceptions.BallerinaConnectorException) {
                    throw new BLangRuntimeException(t.getMessage());
                }
                if (t instanceof ErrorValue) {
                    throw new BLangRuntimeException(
                            "error: " + ((ErrorValue) t).getPrintableStackTrace().replaceAll("\\{}", ""));
                }
                throw (RuntimeException) t;
            }
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException("Error while invoking main function: " + moduleName, e);
        }
    }
}
