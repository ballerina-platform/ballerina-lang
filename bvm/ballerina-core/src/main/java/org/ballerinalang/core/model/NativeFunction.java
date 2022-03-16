/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.core.model;

import org.ballerinalang.core.model.types.SimpleTypeName;

/**
 * {@link NativeFunction} represents any native function in ballerina.
 */
public interface NativeFunction extends Function {

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
}
