/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/


package org.ballerinalang.nativeimpl.lang.jsons;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.ballerinalang.natives.AbstractNativeFunction;

import java.util.EnumSet;
import java.util.Set;

/**
 * Test Class for Testing Ballerina Service.
 */
public abstract class AbstractJSONFunction extends AbstractNativeFunction {

    /**
     * Create a JSON function.
     */
    public AbstractJSONFunction() {
        super();
        // Configure jayway jsonpath with Jackson provider
        Configuration.setDefaults(new JacksonDefaultConfiguration());
    }

    /**
     * Set Jackson provider as the default configuration for Jayway.
     */
    private static class JacksonDefaultConfiguration implements Configuration.Defaults {
        private final JsonProvider jsonProvider = new JacksonJsonNodeJsonProvider();
        private final MappingProvider mappingProvider = new JacksonMappingProvider();

        @Override
        public JsonProvider jsonProvider() {
            return jsonProvider;
        }

        @Override
        public MappingProvider mappingProvider() {
            return mappingProvider;
        }

        @Override
        public Set<Option> options() {
            return EnumSet.noneOf(Option.class);
        }
    }
}
