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

import io.ballerina.types.Env;
import io.ballerina.types.SemType;

import java.util.Map;

public class CompilerTypeTestEnv implements TypeTestEnv<SemType> {

    private final Env env;

    private CompilerTypeTestEnv(Env env) {
        this.env = env;
    }

    public static synchronized CompilerTypeTestEnv from(Env env) {
        return new CompilerTypeTestEnv(env);
    }

    @Override
    public Map<String, SemType> getTypeNameSemTypeMap() {
        return env.getTypeNameSemTypeMap();
    }

    @Override
    public void addTypeDef(String value, SemType semtype) {
        env.addTypeDef(value, semtype);
    }

    @Override
    public Object getInnerEnv() {
        return env;
    }
}
