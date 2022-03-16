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

package org.ballerinalang.bre.bvm;

import org.ballerinalang.bre.Context;

/**
 * {@code {@link BlockingNativeCallableUnit}} represents an abstract implementation of
 * functions and actions.
 *
 * @since 0.964.0
 */
public abstract class BlockingNativeCallableUnit {

    public abstract void execute(Context context);

    /**
     * Returns whether this callable unit is executed in blocking manner or not.
     * 
     * @return Flag indicating whether the callable unit is executed in blocking manner or not.
     */
    public boolean isBlocking() {
        return true;
    }
}
