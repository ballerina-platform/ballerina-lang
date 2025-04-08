/*
 *  Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for
 *  the specific language governing permissions and limitations
 *  under the License.
 *
 */

package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.MapValueImpl;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_QUERY_PKG_ID;

/*
    * This class represents the Exception in query execution.
 */
public class QueryException extends RuntimeException {
    private BError error;

    public QueryException(BError error) {
        this.error = createDistinctError("Error", BALLERINA_QUERY_PKG_ID, error);
    }

    public QueryException(BError error, boolean isCompleteEarlyError) {
        this.error = createDistinctError("CompleteEarlyError", BALLERINA_QUERY_PKG_ID, error);
    }

    public QueryException(String message) {
        this.error = ErrorCreator.createError(StringUtils.fromString(message));
    }

    public BError getError() {
        return error;
    }

    public void setError(BError error) {
        this.error = error;
    }

    public BError createDistinctError(String typeIdName, Module typeIdPkg, BError error) {
        MapValueImpl<BString, Object> details = (MapValueImpl<BString, Object>) error.getDetails();
        return new ErrorValue(error.getType(), error.getErrorMessage(),
                error.getCause(), details, typeIdName, typeIdPkg);
    }
}
