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

import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.scheduling.AsyncUtils;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;

import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.runtime.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.util.BLangConstants.XML_LANG_LIB;
import static org.ballerinalang.util.BLangCompilerConstants.XML_VERSION;

/**
 * Native implementation of lang.xml:forEach(map&lt;Type&gt;, function).
 *
 * @since 1.0
 */
public class ForEach {

    private static final StrandMetadata METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX, XML_LANG_LIB,
                                                                      XML_VERSION, "forEach");

    public static void forEach(BXML x, BFunctionPointer<Object, Object> func) {
        if (x.isSingleton()) {
            func.asyncCall(new Object[]{Scheduler.getStrand(), x, true}, METADATA);
            return;
        }
        AtomicInteger index = new AtomicInteger(-1);
        Strand parentStrand = Scheduler.getStrand();
        AsyncUtils
                .invokeFunctionPointerAsyncIteratively(func, null, METADATA, x.size(),
                        () -> new Object[]{parentStrand, x.getItem(index.incrementAndGet()),
                                true},
                        result -> {
                                                       }, () -> null, Scheduler.getStrand().scheduler);
    }
}
