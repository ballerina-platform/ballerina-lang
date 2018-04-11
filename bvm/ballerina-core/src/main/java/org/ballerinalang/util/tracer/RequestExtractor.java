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

import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Extractor that goes through the carrier map.
 */
public class RequestExtractor implements TextMap {

    private Map<String, String> headers;

    public RequestExtractor(String headers) {
        this.headers = new HashMap<>();
        if (headers != null) {
            String decodedString = new String(Base64.getDecoder().decode(headers));
            for (String keyValueHeader: decodedString.split(",")) {
                String[] splitKeyValueHeader = keyValueHeader.split("=");
                this.headers.put(splitKeyValueHeader[0], splitKeyValueHeader[1]);
            }
        }
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return this.headers.entrySet().iterator();
    }

    @Override
    public void put(String s, String s1) {
        throw new UnsupportedOperationException("This class should be used only with Tracer.extract()!");
    }

}
