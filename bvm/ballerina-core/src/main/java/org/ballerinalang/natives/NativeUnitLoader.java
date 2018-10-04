/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.natives;

import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.natives.NativeElementRepository.NativeFunctionDef;
import org.ballerinalang.spi.NativeElementProvider;
import org.ballerinalang.util.debugger.VMDebugServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @since 0.94
 */
public class NativeUnitLoader {

    private static final Logger logger = LoggerFactory.getLogger(VMDebugServerHandler.class);

    private NativeElementRepository nativeElementRepo;

    private static NativeUnitLoader instance;
    
    private Map<String, NativeCallableUnit> nativeUnitsCache = new HashMap<>();

    private List<Callable<Integer>> shutdownHooks = new ArrayList<>();

    public static NativeUnitLoader getInstance() {
        if (instance == null) {
            instance = new NativeUnitLoader();
        }
        return instance;
    }

    public NativeUnitLoader() {
        this.nativeElementRepo = new NativeElementRepository();
        ServiceLoader.load(NativeElementProvider.class).forEach(e -> e.populateNatives(this.nativeElementRepo));
    }

    public NativeElementRepository getNativeElementRepository() {
        return nativeElementRepo;
    }
        
    public NativeCallableUnit loadNativeFunction(String pkgName, String functionName) {
        String key = NativeElementRepository.functionToKey(pkgName, functionName);
        NativeCallableUnit result = this.nativeUnitsCache.get(key);
        if (result == null) {
            NativeFunctionDef functionDef = this.nativeElementRepo.lookupNativeFunction(pkgName, functionName);
            if (functionDef != null) {
                try {
                    result = (NativeCallableUnit) Class.forName(functionDef.getClassName()).newInstance();
                    this.nativeUnitsCache.put(key, result);
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    throw new RuntimeException("Error in loading native function: " + e.getMessage(), e);
                }
            }
        }
        return result;
    }
    
    public NativeCallableUnit loadNativeAction(String pkgName, String connectorName, String actionName) {
        String key = NativeElementRepository.actionToKey(pkgName, connectorName, actionName);
        NativeCallableUnit result = this.nativeUnitsCache.get(key);
        if (result == null) {
            NativeFunctionDef actionDef = this.nativeElementRepo.lookupNativeAction(pkgName, 
                    connectorName, actionName);
            if (actionDef != null) {
                try {
                    result = (NativeCallableUnit) Class.forName(actionDef.getClassName()).newInstance();
                    this.nativeUnitsCache.put(key, result);
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    throw new RuntimeException("Error in loading native action: " + e.getMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * Registers a shutdown hook.
     *
     * @param hook a {@link Callable} shutdown hook
     */
    public void addShutdownHook(Callable<Integer> hook) {
        shutdownHooks.add(hook);
    }

    /**
     * Notify all shutdown hooks to shutdown.
     *
     * @param timeout  maximum timeout
     * @param timeUnit time unit of the timeout
     */
    public void notifyAllShutdownHooks(long timeout, TimeUnit timeUnit) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            // Execute all shutdown hooks with max-time limit
            executorService.invokeAll(shutdownHooks, timeout, timeUnit);
        } catch (InterruptedException e) {
            logger.error("Notifying shutdown hooks process interrupted", e);
        }
        executorService.shutdownNow();
    }
}
