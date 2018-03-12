/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;

/**
 * Class represents native callable constructs.
 * Known constructs:
 * <ul>
 * <li>Native Functions</li>
 * <li>Native Actions</li>
 * </ul>
 * 
 * @since 0.946.0
 *
 */
public interface NativeCallableUnit {

    /**
     * Executes the implementation of the callable unit. Any class implementing this {@code AbstractNativeCallableUnit}
     * should implement the execute method.
     *
     * @param context Current context instance
     * @param callback Callback to return the response values
     */
    public void execute(Context context, CallableUnitCallback callback);

    /**
     * Returns whether this callable unit is executed in blocking manner or not.
     * 
     * @return Flag indicating whether the callable unit is executed in blocking manner or not.
     */
    public boolean isBlocking();
    
}
