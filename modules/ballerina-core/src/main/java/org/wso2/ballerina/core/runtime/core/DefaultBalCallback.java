/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.core.runtime.core;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.MessageValue;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Default implementation of {@code BalCallback}
 */
public class DefaultBalCallback implements BalCallback {

    protected CarbonCallback parentCallback;

    public DefaultBalCallback(CarbonCallback parentCallback) {
        this.parentCallback = parentCallback;
    }

    @Override
    public void done(CarbonMessage carbonMessage) {
        parentCallback.done(carbonMessage);

    }

    @Override
    public void done(Context context) {
        BValueRef bValueRef = context.getControlStack().getReturnValue(0);
        if (bValueRef != null && bValueRef.getBValue() instanceof MessageValue) {
            parentCallback.done(((MessageValue) bValueRef.getBValue()).getValue());
        }
    }

    @Override
    public CarbonCallback getParentCallback() {
        return parentCallback;
    }

}
