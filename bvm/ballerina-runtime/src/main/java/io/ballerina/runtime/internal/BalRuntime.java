/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal;

import io.ballerina.identifier.Utils;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.concurrent.StrandMetadata;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.configurable.providers.ConfigDetails;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.runtime.internal.launch.LaunchUtils;
import io.ballerina.runtime.internal.scheduling.RuntimeRegistry;
import io.ballerina.runtime.internal.scheduling.Scheduler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static io.ballerina.identifier.Utils.encodeNonFunctionIdentifier;
import static io.ballerina.runtime.api.constants.RuntimeConstants.ANON_ORG;
import static io.ballerina.runtime.api.constants.RuntimeConstants.CONFIGURATION_CLASS_NAME;
import static io.ballerina.runtime.api.constants.RuntimeConstants.DOT;
import static io.ballerina.runtime.api.constants.RuntimeConstants.MODULE_INIT_CLASS_NAME;

/**
 * Internal implementation of the API used by the interop users to control Ballerina runtime behavior.
 *
 * @since 2201.6.0
 */
public class BalRuntime extends Runtime {

    public final Scheduler scheduler;
    public final Module rootModule;
    public final RuntimeRegistry runtimeRegistry;
    private final CompletableFuture<Void> stopFuture = new CompletableFuture<>();
    public boolean moduleInitialized = false;
    public boolean moduleStarted = false;
    public boolean moduleStopped = false;

    public BalRuntime(Module rootModule) {
        this.scheduler = new Scheduler(this);
        this.rootModule = rootModule;
        this.runtimeRegistry = new RuntimeRegistry(this.scheduler);
    }

    @Override
    public Object init() {
        handleAlreadyCalled(moduleInitialized, "init");
        try {
            this.invokeConfigInit();
            Object result = scheduler.callFunction(rootModule, "$moduleInit", new StrandMetadata(true, null));
            this.moduleInitialized = true;
            return result;
        } catch (ClassNotFoundException e) {
            throw ErrorCreator.createError(StringUtils.fromString(String.format("module '%s' does not exist",
                    rootModule)));
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw ErrorCreator.createError(StringUtils.fromString("error occurred while initializing the ballerina " +
                    "module due to " + e.getMessage()), e);
        }
    }

    @Override
    public Object start() {
        handleCallBeforeModuleInit("start");
        handleAlreadyCalled(moduleStarted, "start");
        Object result = this.scheduler.callFunction(rootModule, "$moduleStart", null);
        this.moduleStarted = true;
        return result;
    }

    @Override
    public void stop() {
        handleCallBeforeModuleInit("stop");
        handleAlreadyCalled(moduleStopped, "stop");
        try {
            this.gracefulExit();
            this.invokeModuleStop();
            this.moduleStopped = true;
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw ErrorCreator.createError(StringUtils.fromString("error occurred during module stop due to " +
                    e.getMessage()), e);
        }
    }

    @Override
    public Object callFunction(Module module, String functionName, StrandMetadata metadata, Object... args) {
        this.handleCallBeforeModuleInit(functionName);
        this.validateArgs(module, functionName);
        return this.scheduler.callFunction(module, functionName, metadata, args);
    }

    @Override
    public Object callMethod(BObject object, String methodName, StrandMetadata metadata, Object... args) {
        this.handleCallBeforeModuleInit(object, methodName);
        this.validateArgs(object, methodName);
        return this.scheduler.callMethod(object, methodName, metadata, args);
    }

    @Override
    public void registerListener(BObject listener) {
        this.handleCallBeforeModuleInit("registerListener");
        this.runtimeRegistry.registerListener(listener);
    }

    @Override
    public void deregisterListener(BObject listener) {
        this.handleCallBeforeModuleInit("deregisterListener");
        this.runtimeRegistry.deregisterListener(listener);
    }

    @Override
    public void registerStopHandler(BFunctionPointer stopHandler) {
        this.handleCallBeforeModuleInit("registerStopHandler");
        this.runtimeRegistry.registerStopHandler(stopHandler);
    }


    @SuppressWarnings("unused")
    /*
     * Used for codegen wait for listeners
     */
    public void waitOnListeners(boolean listenerDeclarationFound) {
        if (!listenerDeclarationFound && this.runtimeRegistry.listenerQueue.isEmpty()) {
            return;
        }
        try {
            this.stopFuture.get();
        } catch (Throwable e) {
            throw ErrorCreator.createError(e);
        }
    }

    public void gracefulExit() {
        this.stopFuture.complete(null);
    }

    private void invokeConfigInit() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        Class<?> configClass = loadClass(CONFIGURATION_CLASS_NAME);
        ConfigDetails configDetails = LaunchUtils.getConfigurationDetails();
        String funcName = Utils.encodeFunctionIdentifier("$configureInit");
        Method method = configClass.getDeclaredMethod(funcName, Map.class, String[].class, Path[].class, String.class
                , BalRuntime.class);
        method.invoke(null, new HashMap<>(), new String[]{}, configDetails.paths, configDetails.configContent, this);
    }

    private void handleCallBeforeModuleInit(String functionName) {
        if (!moduleInitialized) {
            throw ErrorHelper.getRuntimeException(ErrorCodes.INVALID_FUNCTION_INVOCATION_BEFORE_MODULE_INIT,
                    functionName);
        }
    }

    private void handleCallBeforeModuleInit(BObject object, String methodName) {
        if (!moduleInitialized) {
            throw ErrorHelper.getRuntimeException(ErrorCodes.INVALID_FUNCTION_INVOCATION_BEFORE_MODULE_INIT,
                    object.getOriginalType().getName() + ":" + methodName);
        }
    }

    private void handleAlreadyCalled(boolean isAlreadyCalled, String functionName) {
        if (isAlreadyCalled) {
            throw ErrorHelper.getRuntimeException(ErrorCodes.FUNCTION_ALREADY_CALLED, functionName);
        }
    }

    private void validateArgs(Module module, String functionName) {
        if (module == null) {
            throw ErrorCreator.createError(StringUtils.fromString("module cannot be null"));
        }
        if (functionName == null) {
            throw ErrorCreator.createError(StringUtils.fromString("function name cannot be null"));
        }
    }

    private void validateArgs(BObject object, String methodName) {
        if (object == null) {
            throw ErrorCreator.createError(StringUtils.fromString("object cannot be null"));
        }
        if (methodName == null) {
            throw ErrorCreator.createError(StringUtils.fromString("method name cannot be null"));
        }
    }

    void invokeModuleStop() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        Class<?> configClass = loadClass(MODULE_INIT_CLASS_NAME);
        Method method = configClass.getDeclaredMethod("$currentModuleStop", BalRuntime.class);
        method.invoke(null, this);
    }

    protected Class<?> loadClass(String className) throws ClassNotFoundException {
        String name = getFullQualifiedClassName(this.rootModule, className);
        return Class.forName(name);
    }

    protected static String getFullQualifiedClassName(Module module, String className) {
        String orgName = module.getOrg();
        String packageName = module.getName();
        if (!DOT.equals(packageName)) {
            className = encodeNonFunctionIdentifier(packageName) + "." + module.getMajorVersion() + "." + className;
        }
        if (!ANON_ORG.equals(orgName)) {
            className = encodeNonFunctionIdentifier(orgName) + "." + className;
        }
        return className;
    }
}
