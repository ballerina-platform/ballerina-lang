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

import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.SemType;

public final class RuntimeTypeTestContext implements TypeTestContext<SemType> {

    private final TypeTestEnv<SemType> env;

    private RuntimeTypeTestContext(TypeTestEnv<SemType> env) {
        this.env = env;
    }

    public static synchronized RuntimeTypeTestContext from(TypeTestEnv<SemType> env) {
        return new RuntimeTypeTestContext(env);
    }

    @Override
    public TypeTestEnv<SemType> getEnv() {
        return env;
    }

    @Override
    public Object getInnerEnv() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Object getInnerContext() {
        return new Context();
    }
}
