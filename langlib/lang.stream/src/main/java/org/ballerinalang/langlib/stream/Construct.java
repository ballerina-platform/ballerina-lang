/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.stream;

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BFunctionType;
import org.ballerinalang.jvm.types.BStreamType;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.IteratorValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.jvm.values.api.BFunctionPointer;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native implementation of lang.stream:construct(typeDesc, function).
 *
 * @since 1.2.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.stream", functionName = "construct",
        args = {
                @Argument(name = "td", type = TypeKind.TYPEDESC),
                @Argument(name = "func", type = TypeKind.FUNCTION)
        },
        returnType = {@ReturnType(type = TypeKind.STREAM)}
)
public class Construct {

    public static StreamValue construct(Strand strand, TypedescValue td, FPValue<Object, Object> func) {
        BFunctionType functionType = (BFunctionType) func.getType();
        IteratorValue iterator = new Iterator(func);
        return new StreamValue(new BStreamType(td.getDescribingType()), iterator, null, null);
    }

    static class Iterator implements IteratorValue {
        BFunctionPointer<Object, Object> genFunc;

        Iterator(BFunctionPointer<Object, Object> genFunc) {
            this.genFunc = genFunc;
        }

        @Override
        public Object next() {
            Strand strand = Scheduler.getStrand();
            MapValueImpl record = (MapValueImpl) this.genFunc.call(new Object[]{strand});
            if (record != null) {
                return record.get("value");
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BString bStringValue() {
            return null;
        }
    }

}
