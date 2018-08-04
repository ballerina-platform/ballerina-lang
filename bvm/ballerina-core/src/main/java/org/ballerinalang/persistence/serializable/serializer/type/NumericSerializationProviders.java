/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.persistence.serializable.serializer.type;

import org.ballerinalang.persistence.serializable.serializer.TypeInstanceProvider;
import org.ballerinalang.persistence.serializable.serializer.TypeSerializationProvider;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumericSerializationProviders {

    public static void register(TypeInstanceProvider registry) {
        registry.addTypeProvider(bigDecimalProvider);
        registry.addTypeProvider(bigIntegerProvider);
    }


    static TypeSerializationProvider bigDecimalProvider = new TypeSerializationProvider() {
        @Override
        public String getTypeName() {
            return BigDecimal.class.getSimpleName();
        }

        @Override
        public Object newInstance() {
            return new BigDecimal(0);
        }

        @Override
        public Class getTypeClass() {
            return BigDecimal.class;
        }
    };

    static TypeSerializationProvider bigIntegerProvider = new TypeSerializationProvider() {
        @Override
        public String getTypeName() {
            return BigInteger.class.getSimpleName();
        }

        @Override
        public Object newInstance() {
            return new BigInteger("0");
        }

        @Override
        public Class getTypeClass() {
            return BigInteger.class;
        }
    };
}
