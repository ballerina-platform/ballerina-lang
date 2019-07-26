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
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.util.ArgumentParser;
import org.ballerinalang.jvm.util.RuntimeUtils;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.FutureValue;
import org.ballerinalang.spi.EmbeddedExecutor;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    public Optional<RuntimeException> executeMainFunction(String moduleName, String[] args) {
        try {
            final Scheduler scheduler = new Scheduler(4, false);
            runInitOnSchedule(moduleName, scheduler);
            runMainOnSchedule(moduleName, scheduler, args);
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
    public Optional<RuntimeException> executeService(String moduleName) {
        try {
            final Scheduler scheduler = new Scheduler(4, false);
            runInitOnSchedule(moduleName, scheduler);
            runStartOnSchedule(moduleName, scheduler);
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
     * @param moduleName The name of the module.
     * @param scheduler  The scheduler.
     * @throws RuntimeException When an error occurs invoking or within the function.
     */
    private void runStartOnSchedule(String moduleName, Scheduler scheduler) throws RuntimeException {
        try {
            Class<?> initClazz = Class.forName("ballerina." + moduleName + ".___init");
            final Method initMethod = initClazz.getDeclaredMethod("ballerina_" + moduleName + "__start_", Strand.class);
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
     * @param moduleName The name of the module.
     * @param scheduler  The scheduler which executes the function.
     * @param stringArgs The string arguments for the function.
     * @throws RuntimeException When an error occurs invoking or within the function.
     */
    private static void runMainOnSchedule(String moduleName, Scheduler scheduler, String[] stringArgs)
            throws RuntimeException {
        try {
            Class<?> mainClass = Class.forName("ballerina." + moduleName + "." + moduleName);
            final Method mainMethod = mainClass.getDeclaredMethod("main", Strand.class, ArrayValue.class,
                    boolean.class);
            Object[] entryFuncArgs =
                    ArgumentParser.extractEntryFuncArgs(new RuntimeUtils.ParamInfo[]{
                            new RuntimeUtils.ParamInfo(false,
                                    "%1",
                                    new BArrayType(BTypes.typeString, stringArgs.length))
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
            final FutureValue out = scheduler.schedule(entryFuncArgs, func, null, null, null);
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
     * @param moduleName The name of the module.
     * @param scheduler  The scheduler which executes the function.
     * @throws RuntimeException When an error occurs invoking or within the function.
     */
    private static void runInitOnSchedule(String moduleName, Scheduler scheduler) throws RuntimeException {
        try {
            Class<?> initClazz = Class.forName("ballerina." + moduleName + ".___init");
            final Method initMethod = initClazz.getDeclaredMethod("ballerina_" + moduleName + "__init_", Strand.class);
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
                            "error: " + ((ErrorValue) t).getPrintableStackTrace().replaceAll("\\{}", ""));
                }
                throw (RuntimeException) t;
            }
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException("Error while invoking main function: " + moduleName, e);
        }
    }
}
