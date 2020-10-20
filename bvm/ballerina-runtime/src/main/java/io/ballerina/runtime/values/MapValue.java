/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.values;

import io.ballerina.runtime.api.values.BMap;

/**
 * <p>
 * An interface for MapValue. This is created only for the purpose of java bytecode verifier to pass at runtime when
 * we used the implementation of MapValue.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 *  
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @since 0.995.0
 */
public interface MapValue<K, V> extends RefValue, CollectionValue, BMap<K, V> {

}
