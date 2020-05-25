/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langlib.string;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.nio.charset.StandardCharsets;

import static org.ballerinalang.util.BLangCompilerConstants.STRING_VERSION;

/**
 * Convert String to byte array.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.string", version = STRING_VERSION,
        functionName = "toBytes",
        args = {@Argument(name = "string", type = TypeKind.STRING),
                @Argument(name = "encoding", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.BYTE)},
        isPublic = true
)
public class ToBytes {

    public static ArrayValue toBytes(Strand strand, BString value) {

        byte[] bytes = value.getValue().getBytes(StandardCharsets.UTF_8);
        return new ArrayValueImpl(bytes);
    }
}
