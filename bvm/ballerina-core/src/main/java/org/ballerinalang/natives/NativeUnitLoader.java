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

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @since 0.94
 */
public class NativeUnitLoader {
    
    private NativeElementRepository nativeElementRepo;

    private static NativeUnitLoader instance;
    
    private Map<String, NativeCallableUnit> nativeUnitsCache = new HashMap<>();

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
    
}
