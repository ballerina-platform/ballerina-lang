/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.semtype.port.test;

import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.HashMap;
import java.util.Map;

class RuntimeTypeTestEnv implements TypeTestEnv<SemType> {

    private final Map<String, SemType> typeMap = new HashMap<>();
    private final Env env;

    private RuntimeTypeTestEnv(Env env) {
        this.env = env;
    }

    public static synchronized RuntimeTypeTestEnv from(Env env) {
        return new RuntimeTypeTestEnv(env);
    }

    @Override
    public Map<String, SemType> getTypeNameSemTypeMap() {
        return typeMap;
    }

    @Override
    public void addTypeDef(String value, SemType semtype) {
        typeMap.put(value, semtype);
    }

    @Override
    public Object getInnerEnv() {
        return env;
    }
}
