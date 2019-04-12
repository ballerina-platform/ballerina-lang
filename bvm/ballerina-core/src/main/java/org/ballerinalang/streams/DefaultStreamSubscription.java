/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.streams;

import org.ballerinalang.bre.bvm.BVMExecutor;
import org.ballerinalang.model.values.BClosure;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BStream;
import org.ballerinalang.model.values.BValue;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@link DefaultStreamSubscription} represents a stream subscription in Ballerina.
 *
 * @since 0.995.0
 */
public class DefaultStreamSubscription extends StreamSubscription {

    private BStream stream;
    private BFunctionPointer functionPointer;

    DefaultStreamSubscription(BStream stream, BFunctionPointer functionPointer,
                              StreamSubscriptionManager streamSubscriptionManager) {
        super(streamSubscriptionManager);
        this.stream = stream;
        this.functionPointer = functionPointer;
    }

    public void execute(BValue value) {
        List<BValue> argsList =
                functionPointer.getClosureVars().stream().map(BClosure::value).collect(Collectors.toList());
        argsList.add(value);
        BVMExecutor.executeFunction(functionPointer.value().getPackageInfo().getProgramFile(),
                                    functionPointer.value(), argsList.toArray(new BValue[0]));
    }

    public BStream getStream() {
        return stream;
    }
}
