/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ResourceMethodType;
import io.ballerina.runtime.api.types.Type;

import java.util.StringJoiner;

/**d
 * {@code ResourceMethodType} represents a resource function in Ballerina.
 *
 * @since 2.0
 */
public class BResourceMethodType extends BMethodType implements ResourceMethodType {

    public final String accessor;
    public final String[] resourcePath;
    public Type[] pathSegmentTypes;

    public BResourceMethodType(String funcName, Module pkg, BObjectType parent, BFunctionType type,
                               Type[] pathSegmentTypes, long flags, String accessor, String[] resourcePath) {
        super(funcName, pkg, parent, type, flags);
        this.type = type;
        this.pathSegmentTypes = pathSegmentTypes;
        this.flags = flags;
        this.accessor = accessor;
        this.resourcePath = resourcePath;
    }

    @Override
    public String toString() {
        StringJoiner rp = new StringJoiner("/");
        for (String p : resourcePath) {
            rp.add(p);
        }
        StringJoiner sj = new StringJoiner(",", "resource function " + accessor + " " + rp.toString() +
                "(", ") returns (" + type.retType + ")");
        for (int i = 0; i < parameters.length; i++) {
            Type type = parameters[i].type;
            sj.add(type.getName() + " " + parameters[i].name);
        }
        return sj.toString();
    }

    @Deprecated
    @Override
    public String[] getParamNames() {
        String[] paramNames = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            paramNames[i] = parameters[i].name;
        }
        return paramNames;
    }

    @Override
    public String getAccessor() {
        return accessor;
    }

    @Override
    public String[] getResourcePath() {
        return resourcePath;
    }

    @Override
    public <T extends MethodType> MethodType duplicate() {
        return new BResourceMethodType(funcName, pkg, parentObjectType, type, pathSegmentTypes, flags, accessor,
                resourcePath);
    }

    @Deprecated
    @Override
    public Boolean[] getParamDefaultability() {
        Boolean[] paramDefaults = new Boolean[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            paramDefaults[i] = parameters[i].isDefault;
        }
        return paramDefaults;
    }

}
