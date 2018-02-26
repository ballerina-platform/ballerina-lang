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

import java.util.List;
import java.util.Map;

/**
 * TracerManager to load OpenTracerManager.
 */
public interface TracerManager {

    List<Object> buildSpan(long invocationId, String spanName, Map<String, Object> spanContextMap,
                           Map<String, String> tags, boolean makeActive, String serviceName);

    void finishSpan(List<Object> span, Map<String, Object> parent, String serviceName);

    Map<String, Object> extract(Object format, Map<String, String> httpHeaders, String serviceName);

    Map<String, String> inject(Map<String, Object> activeSpanMap, Object format, String serviceName);

    Map<String, Object> getActiveSpanMap(String serviceName);

}
