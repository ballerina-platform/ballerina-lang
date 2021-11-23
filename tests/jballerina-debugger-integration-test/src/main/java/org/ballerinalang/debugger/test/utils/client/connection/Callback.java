/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.debugger.test.utils.client.connection;

/**
 * This interface represents a callback to report back a success or a
 * failure state back to the originator.
 *
 * @since 2.0.0
 */
public interface Callback {

    /**
     * This should be called when you want to notify that your operation
     * is done successfully.
     *
     * @param result
     */
    void notifySuccess(Object result);

    /**
     * This should be called to notify the listener that your operation
     * failed with a specific error.
     */
    void notifyFailure(Exception e);
}
