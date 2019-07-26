/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 **/
package org.ballerinalang.utils;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.CollectionValue;
import org.ballerinalang.jvm.values.IteratorValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BCollection;
import org.ballerinalang.model.values.BIterator;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Get a new iterator for the given value.
 *
 * @since 0.990.4
 */
@BallerinaFunction(orgName = "ballerina",
        packageName = "utils",
        functionName = "iterate",
        args = {@Argument(name = "value", type = TypeKind.ANY)},
        returnType = {@ReturnType(type = TypeKind.ANY)})
public class Iterate extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BValue collection = context.getNullableRefArgument(0);

        if (collection == null) {
            context.setReturnValues(BLangVMErrors.createNullRefException(context.getStrand()));
            return;
        }
        if (!(collection instanceof BCollection)) {
            // Value is a value-type JSON.
            context.setReturnValues(new JSONIterator());
            return;
        }

        context.setReturnValues(((BCollection) collection).newIterator());
    }

    public static Object iterate(Strand strand, Object collection) {
        if (collection == null) {
            // we shouldn't reach here
            throw new NullPointerException();
        }

        if (collection instanceof CollectionValue) {
            return ((CollectionValue) collection).getIterator();
        }

        // Value is a value-type JSON.
        return new EmptyIterator();
    }

    /**
     * Iterator for non-iterable types (r).
     */
    public static class EmptyIterator implements IteratorValue {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            return null;
        }
    }

    /**
     * Iterator for Json types.
     */
    public static class JSONIterator implements BIterator {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public BValue getNext() {
            return null;
        }
    }
}
