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
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.natives;

import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.HashMap;
import java.util.Map;

/**
 * This represents a native element repository, that would be populated by any
 * native element provider.
 */
@Deprecated
public class NativeElementRepository {

    private Map<String, NativeFunctionDef> nativeFuncEntries = new HashMap<>();
    
    private Map<String, NativeActionDef> nativeActionEntries = new HashMap<>();

    private static final String ORG_NAME_SEPARATOR = "/";

    public void registerNativeFunction(NativeFunctionDef nativeFuncDef) {
        this.nativeFuncEntries.put(functionToKey(nativeFuncDef.toBvmAlias(),
                                                 nativeFuncDef.getCallableName()), nativeFuncDef);
    }
    
    public void registerNativeAction(NativeActionDef nativeActionDef) {
        this.nativeActionEntries.put(actionToKey(nativeActionDef.toBvmAlias(), nativeActionDef.getConnectorName(),
                                                 nativeActionDef.getCallableName()), nativeActionDef);
    }
    
    public static String functionToKey(String pkgName, String functionName) {
        return "F#" + pkgName + "#" + functionName;
    }
    
    public static String actionToKey(String pkgName, String connectorName, String actionName) {
        return "A#" + pkgName + "#" + connectorName + "#" + actionName;
    }
    
    public NativeFunctionDef lookupNativeFunction(String pkgName, String functionName) {
        return this.nativeFuncEntries.get(functionToKey(pkgName, functionName));
    }
    
    public NativeActionDef lookupNativeAction(String pkgName, String connectorName, String actionName) {
        return this.nativeActionEntries.get(actionToKey(pkgName, connectorName, actionName));
    }

    /**
     * This class represents a native function definition.
     * 
     * @since 0.94
     */
    public static class NativeFunctionDef {

        private String orgName;

        private String pkgName;

        private String version;
        
        private String callableName;
        
        private String className;
        
        private TypeKind[] argTypes;
        
        private TypeKind[] retTypes;

        public NativeFunctionDef(String orgName, String pkgName, String version, String callableName,
                                 TypeKind[] argTypes,
                                 TypeKind[] retTypes, String className) {
            this.orgName = orgName;
            this.pkgName = pkgName;
            this.version = version;
            this.callableName = callableName;
            this.argTypes = argTypes;
            this.retTypes = retTypes;
            this.className = className;
        }

        public String toBvmAlias() {
            if (Names.ANON_ORG.getValue().equals(orgName)) {
                return pkgName + ORG_NAME_SEPARATOR + version;
            }
            // assume anon org can't expose natives
            return orgName + ORG_NAME_SEPARATOR + pkgName + ORG_NAME_SEPARATOR + version;
        }

        public String getCallableName() {
            return callableName;
        }
        
        public String getClassName() {
            return className;
        }
        
        public TypeKind[] getArgTypes() {
            return argTypes;
        }
        
        public TypeKind[] getRetTypes() {
            return retTypes;
        }
        
    }
    
    /**
     * This class represents a native action definition.
     * 
     * @since 0.94
     */
    public static class NativeActionDef extends NativeFunctionDef {

        private String connectorName;

        public NativeActionDef(String orgName, String pkgName, String version, String connectorName, String actionDef,
                               TypeKind[] argTypes, TypeKind[] retTypes, String className) {
            super(orgName, pkgName, version, actionDef, argTypes, retTypes, className);
            this.connectorName = connectorName;
        }

        public String getConnectorName() {
            return connectorName;
        }
        
    }

}
