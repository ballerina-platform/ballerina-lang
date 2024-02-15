/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.stdlib;

import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BStream;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;

/**
 * This class used to provide java interoperability.
 *
 * @since 2201.4.0
 */
public class ClientAction {

    public ClientAction() {
    }

    public static Object get(BString path, BTypedesc targetType) {
        return new Object();
    }

    public static Object forward(BString path, BObject request, BTypedesc targetType) {
        return new Object();
    }

    public static Object delete(BTypedesc targetType) {
        return new Object();
    }

    public static BStream responses(BTypedesc targetType) {
        return ValueCreator.createStreamValue(TypeCreator.createStreamType(targetType.getDescribingType()));
    }

    public static Object postResource(BArray path, Object message, Object headers,
                                      Object mediaType, BTypedesc targetType) {
        return new Object();
    }
}
