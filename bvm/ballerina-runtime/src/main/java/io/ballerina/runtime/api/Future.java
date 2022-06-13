/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.api;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.values.MapValueImpl;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A future that will resume the underling strand when completed.
 *
 * @since 2.0.0
 */
public class Future {
    private final Strand strand;
    private final AtomicBoolean visited = new AtomicBoolean();
    Future(Strand strand) {
        this.strand = strand;
    }

    public void complete(Object returnValue) {
        if (visited.getAndSet(true)) {
            throw ErrorCreator.createError(StringUtils.fromString("cannot complete the same future twice."),
                                           new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
        }
        strand.returnValue = returnValue;
        strand.scheduler.unblockStrand(strand);
    }
}
