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

import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.util.concurrent.atomic.AtomicInteger;

import static org.ballerinalang.util.BLangCompilerConstants.XML_VERSION;

/**
 * Native implementation of lang.xml:forEach(map&lt;Type&gt;, function).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.xml", version = XML_VERSION, functionName = "forEach",
        args = {
                @Argument(name = "x", type = TypeKind.XML),
                @Argument(name = "func", type = TypeKind.FUNCTION)},
        isPublic = true
)
public class ForEach {

    public static void forEach(Strand strand, XMLValue x, FPValue<Object, Object> func) {
        if (x.isSingleton()) {
            func.asyncCall(new Object[]{strand, x, true});
            return;
        }
        AtomicInteger index = new AtomicInteger(-1);
        BRuntime.getCurrentRuntime()
                .invokeFunctionPointerAsyncIteratively(func, x.size(),
                                                       () -> new Object[]{strand, x.getItem(index.incrementAndGet()),
                                                               true},
                                                       result -> {
                                                       }, () -> null);
    }
}
