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
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.configurable.providers.ConfigDetails;
import io.ballerina.runtime.internal.launch.LaunchUtils;
import io.ballerina.runtime.internal.scheduling.RuntimeRegistry;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.values.FPValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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

    private final Scheduler scheduler;
    private final Module rootModule;

    public BalRuntime(Module rootModule) {
        this.scheduler = new Scheduler(this);
        this.rootModule = rootModule;
    }

    @Override
    public void init() {
        try {
            invokeConfigInit();
            scheduler.startIsolatedWorker("$moduleInit", rootModule, "$moduleInit", null, null);
        } catch (ClassNotFoundException e) {
            throw ErrorCreator.createError(StringUtils.fromString(String.format("module '%s' does not exist",
                    rootModule)));
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw ErrorCreator.createError(StringUtils.fromString("error occurred while initializing the ballerina " +
                    "module due to " + e.getMessage()), e);
        }
    }

    @Override
    public void start() {
        call(rootModule, "$moduleStart");
    }

    @Override
    public void stop() {
        try {
            scheduler.poison();
            invokeModuleStop();
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw ErrorCreator.createError(StringUtils.fromString("error occurred during module stop due to " +
                    e.getMessage()), e);
        }
    }

    @Override
    public Object call(Module module, String functionName, Object... args) {
        return scheduler.call(module, functionName, Scheduler.getStrand(), args);
    }

    @Override
    public Object call(BObject object, String methodName, Object... args) {
        return scheduler.call(object, methodName, Scheduler.getStrand(), args);
    }

    @Override
    public BFuture startIsolatedWorker(Module module, String functionName, String strandName, StrandMetadata metadata,
                                       Map<String, Object> properties, Object... args) {
        return scheduler.startIsolatedWorker(functionName, module, strandName, metadata, properties, args);
    }

    @Override
    public BFuture startIsolatedWorker(BObject object, String methodName, String strandName, StrandMetadata metadata,
                                       Map<String, Object> properties, Object... args) {
        validateArgs(object, methodName);
        return scheduler.startIsolatedWorker(object, methodName, strandName, metadata, properties, args);
    }

    @Override
    public BFuture startIsolatedWorker(FPValue fp, String strandName, StrandMetadata metadata,
                                       Map<String, Object> properties, Object... args) {
        return scheduler.startIsolatedWorker(fp, strandName, metadata, properties, args);
    }

    @Override
    public BFuture startNonIsolatedWorker(Module module, String functionName, String strandName,
                                          StrandMetadata metadata,
                                          Map<String, Object> properties, Object... args) {
        return scheduler.startNonIsolatedWorker(functionName, module, strandName, metadata, properties, args);
    }

    @Override
    public BFuture startNonIsolatedWorker(BObject object, String methodName, String strandName,
                                          StrandMetadata metadata, Map<String, Object> properties, Object... args) {
        validateArgs(object, methodName);
        return scheduler.startNonIsolatedWorker(object, methodName, strandName, metadata, properties, args);
    }

    @Override
    public BFuture startNonIsolatedWorker(FPValue fp, String strandName, StrandMetadata metadata,
                                          Map<String, Object> properties, Object... args) {
        return scheduler.startNonIsolatedWorker(fp, strandName, metadata, properties, args);
    }

    @Override
    public void registerListener(BObject listener) {
        scheduler.getRuntimeRegistry().registerListener(listener);
    }

    @Override
    public void deregisterListener(BObject listener) {
        scheduler.getRuntimeRegistry().deregisterListener(listener);
    }

    @Override
    public void registerStopHandler(BFunctionPointer stopHandler) {
        scheduler.getRuntimeRegistry().registerStopHandler(stopHandler);
    }

    private void invokeConfigInit() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        Class<?> configClass = loadClass(CONFIGURATION_CLASS_NAME);
        ConfigDetails configDetails = LaunchUtils.getConfigurationDetails();
        String funcName = Utils.encodeFunctionIdentifier("$configureInit");
        Method method = configClass.getDeclaredMethod(funcName, Map.class, String[].class, Path[].class, String.class);
        method.invoke(null, new HashMap<>(), new String[]{}, configDetails.paths, configDetails.configContent);
    }

    private void validateArgs(BObject object, String methodName) {
        if (object == null) {
            throw ErrorCreator.createError(StringUtils.fromString("object cannot be null"));
        }
        if (methodName == null) {
            throw ErrorCreator.createError(StringUtils.fromString("method name cannot be null"));
        }
    }

    private void invokeModuleStop() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        Class<?> configClass = loadClass(MODULE_INIT_CLASS_NAME);
        Method method = configClass.getDeclaredMethod("$currentModuleStop", RuntimeRegistry.class);
        method.invoke(null, scheduler.getRuntimeRegistry());
    }

    private Class<?> loadClass(String className) throws ClassNotFoundException {
        String name = getFullQualifiedClassName(this.rootModule, className);
        return Class.forName(name);
    }

    private static String getFullQualifiedClassName(Module module, String className) {
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
