/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.mime.util;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BValue;

/**
 * {@code DataContext} is the wrapper to hold {@code Context} and {@code CallableUnitCallback}.
 */
public class DataContext {
    private Context context;
    private CallableUnitCallback callback;

    public DataContext(Context context, CallableUnitCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    /**
     * Extract body parts from a given entity.
     *
     * @param errMsg Represent a ballerina entity
     */
    public void createErrorAndNotify(String errMsg) {
        BError error = MimeUtil.createError(this.context, errMsg);
        setReturnValuesAndNotify(error);
    }

    /**
     * Extract body parts from a given entity.
     *
     * @param result Represent a ballerina entity
     */
    public void setReturnValuesAndNotify(BValue result) {
        this.context.setReturnValues(result);
        this.callback.notifySuccess();
    }
}
