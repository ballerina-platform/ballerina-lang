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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.transaction;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.transactions.TransactionLocalContext;

/**
 * Extern function transaction:info.
 *
 * @since 2.0.0-preview1
 */
public final class Info {

    private Info() {
    }

    public static BMap<BString, Object> info() {
        Strand strand = Scheduler.getStrand();
        if (IsTransactional.isTransactional()) {
            TransactionLocalContext context = strand.currentTrxContext;
            return (BMap<BString, Object>) context.getInfoRecord();
        }
        throw ErrorCreator.createError(StringUtils
                .fromString("cannot call info() if the strand is not in transaction mode"));
    }
}
