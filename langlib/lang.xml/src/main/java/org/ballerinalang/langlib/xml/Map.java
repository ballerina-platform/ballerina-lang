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

package org.ballerinalang.langlib.xml;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.IteratorValue;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.ArrayList;
import java.util.List;

/**
 * Native implementation of lang.xml:map(map&lt;Type&gt;, function).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.xml", functionName = "map",
        args = {
                @Argument(name = "x", type = TypeKind.XML),
                @Argument(name = "func", type = TypeKind.FUNCTION)},
        returnType = {@ReturnType(type = TypeKind.XML)},
        isPublic = true
)
public class Map {

    public static XMLValue<?> map(Strand strand, XMLValue<?> x, FPValue<Object, Object> func) {
        if (x.isSingleton()) {
            return (XMLValue<?>) func.apply(new Object[]{strand, x, true});
        }

        IteratorValue iterator = ((XMLSequence) x).getIterator();
        List<XMLValue<?>> elements = new ArrayList<>();
        while (iterator.hasNext()) {
            XMLValue<?> next = (XMLValue<?>) iterator.next();
            elements.add((XMLValue<?>) func.apply(new Object[]{strand, next, true}));
        }

        ArrayValue elemArray = new ArrayValueImpl(elements.toArray(), new BArrayType(BTypes.typeXML));
        return new XMLSequence(elemArray);
    }
}
