/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package io.ballerina.types.subtypedata;

import io.ballerina.types.EnumerableString;
import io.ballerina.types.EnumerableSubtype;
import io.ballerina.types.EnumerableType;

/**
 * Represent NonCharStringSubtype.
 *
 * @since 3.0.0
 */
public class NonCharStringSubtype extends EnumerableSubtype {

    public boolean allowed;
    public EnumerableString[] values;

    private NonCharStringSubtype(boolean allowed, EnumerableString[] values) {
        this.allowed = allowed;
        this.values = values;
    }

    public static NonCharStringSubtype from(boolean allowed, EnumerableString[] values) {
        return new NonCharStringSubtype(allowed, values);
    }

    @Override
    public boolean allowed() {
        return allowed;
    }

    @Override
    public EnumerableType[] values() {
        return values;
    }
}
