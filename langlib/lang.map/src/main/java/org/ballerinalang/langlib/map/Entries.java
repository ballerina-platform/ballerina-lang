/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.map;

import io.ballerina.runtime.api.BValueCreator;
import io.ballerina.runtime.api.Types;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.types.BMapType;
import io.ballerina.runtime.types.BTupleType;

import java.util.Arrays;

import static org.ballerinalang.langlib.map.util.MapLibUtils.getFieldType;

/**
 * Native implementation of lang.map:get(map&lt;Type&gt;, string).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.map",
//        functionName = "entries",
//        args = {@Argument(name = "m", type = TypeKind.MAP)},
//        returnType = {@ReturnType(type = TypeKind.MAP)},
//        isPublic = true
//)
public class Entries {

    public static BMap<?, ?> entries(BMap<?, ?> m) {
        Type newFieldType = getFieldType(m.getType(), "entries()");
        BTupleType entryType = new BTupleType(Arrays.asList(Types.TYPE_STRING, newFieldType));
        BMapType entryMapConstraint = new BMapType(entryType);
        BMap<BString, Object> entries = BValueCreator.createMapValue(entryMapConstraint);

        m.entrySet().forEach(entry -> {
            BArray entryTuple = BValueCreator.createTupleValue(entryType);
            entryTuple.add(0, entry.getKey());
            entryTuple.add(1, entry.getValue());
            entries.put((BString) entry.getKey(), entryTuple);
        });

        return entries;
    }

}
