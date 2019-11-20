/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.mime.nativeimpl;

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;

import static org.ballerinalang.mime.util.EntityBodyHandler.isStreamingRequired;

/**
 * Get the entity body as a string.
 *
 * @since 0.963.0
 */
public class GetText extends AbstractGetPayloadHandler {

    public static Object getText(ObjectValue entityObj) {
        NonBlockingCallback callback = null;
        String result = null;
        try {
            Object dataSource = EntityBodyHandler.getMessageDataSource(entityObj);
            if (dataSource != null) {
                return MimeUtil.getMessageAsString(dataSource);
            }

            if (isStreamingRequired(entityObj)) {
                result = EntityBodyHandler.constructStringDataSource(entityObj);
                updateDataSource(entityObj, result);
            } else {
                callback = new NonBlockingCallback(Scheduler.getStrand());
                constructNonBlockingDataSource(callback, entityObj, SourceType.TEXT);
            }
        } catch (Exception ex) {
            if (ex instanceof ErrorValue) {
                return createParsingEntityBodyFailedErrorAndNotify(callback,
                        "Error occurred while extracting text data from entity", (ErrorValue) ex);
            }
            return createParsingEntityBodyFailedErrorAndNotify(callback,
                                                               "Error occurred while extracting text data from entity" +
                                                                       " : " + getErrorMsg(ex), null);
        }
        return result;
    }
}
