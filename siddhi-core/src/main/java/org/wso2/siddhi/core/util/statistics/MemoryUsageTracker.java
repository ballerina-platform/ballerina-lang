/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.siddhi.core.util.statistics;

/**
 * Estimate the memory usage of set of object
 */
public interface MemoryUsageTracker {

    /**
     * Register the object that needs to be measured the memory usage
     *
     * @param object Object
     * @param name   An unique value to identify the object.
     */
    void registerObject(Object object, String name);

    /**
     * @param object Object
     * @return Name of the mem tracker
     */
    String getName(Object object);
}
