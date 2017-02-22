/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.model;

import org.ballerinalang.model.types.SimpleTypeName;

/**
 * {@link NativeUnit} represents any Native callable unit in ballerina.
 *
 * @since 0.8.0
 */
public interface NativeUnit {

    /**
     * Get the simple Type Names of the return parameters.
     *
     * @return  Simple Type Names of the return parameters
     */
    SimpleTypeName[] getReturnParamTypeNames();

    /**
     * Get the simple Type Names of the input input arguments.
     *
     * @return   Simple Type Names of the input input arguments.
     */
    SimpleTypeName[] getArgumentTypeNames();

    /**
     * Get the names of the input arguments.
     *
     * @return Names of the input arguments.
     */
    String[] getArgumentNames();

    /**
     * Set the types of the return parameters of this callable unit.
     *
     * @param returnParamTypes  Types of the return parameters of this callable unit
     */
    void setReturnParamTypeNames(SimpleTypeName[] returnParamTypes);

    /**
     * Set the types of the input arguments of this callable unit.
     *
     * @param argTypes  Types of the input arguments of this callable unit
     */
    void setArgTypeNames(SimpleTypeName[] argTypes);

    /**
     * Set the names of the input arguments of this callable unit.
     *
     * @param argNames Names of the input arguments of this callable unit
     */
    void setArgNames(String[] argNames);

    /**
     * Set the name of this callable unit.
     *
     * @param name  Name of this callable unit.
     */
    void setName(String name);

    /**
     * Set the package of this callable unit.
     *
     * @param packagePath   Package of this callable unit
     */
    void setPackagePath(String packagePath);

    void setStackFrameSize(int frameSize);

    void setSymbolName(SymbolName symbolName);
}
