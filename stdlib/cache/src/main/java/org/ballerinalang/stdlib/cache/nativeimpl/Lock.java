/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.cache.nativeimpl;

import org.ballerinalang.jvm.values.ObjectValue;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class to handle ballerina external functions in Cache library.
 */
public class Lock {
    private static AtomicBoolean locked = new AtomicBoolean(false);
    private static final String LOCKED = "Locked";

    public static void init(ObjectValue list) {
        list.addNativeData(LOCKED, locked);
    }

    public static boolean lock(ObjectValue list) {
        locked = (AtomicBoolean) list.getNativeData(LOCKED);
        return locked.compareAndSet(false, true);
    }
}
