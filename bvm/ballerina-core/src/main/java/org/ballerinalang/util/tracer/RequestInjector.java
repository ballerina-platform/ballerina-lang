/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.util.tracer;

import io.opentracing.propagation.TextMap;

import java.util.Iterator;
import java.util.Map;

/**
 * Injector that injects span context headers.
 */
public class RequestInjector implements TextMap {

    private final Map<String, String> carrier;
    private final String prefix;

    public RequestInjector(String prefix, Map<String, String> carrier) {
        this.prefix = prefix;
        this.carrier = carrier;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("This class should be used only with Tracer.inject()!");

    }

    @Override
    public void put(String key, String value) {
        carrier.put(prefix + key, value);
    }
}
