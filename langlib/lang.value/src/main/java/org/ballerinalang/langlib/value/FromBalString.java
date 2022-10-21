/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.ballerinalang.langlib.value;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;

import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.FROM_BAL_STRING_ERROR;

/**
 * Returns the result of evaluating a Ballerina expression syntax.
 *
 * @since 2.0.0
 */
public class FromBalString {
    public static Object fromBalString(BString value) {
        String str = value.getValue();
        if (str.equals("null")) {
            return null;
        }
        try {
            return StringUtils.parseExpressionStringValue(str, null);
        } catch (BallerinaException e) {
            return ErrorCreator.createError(FROM_BAL_STRING_ERROR, StringUtils.fromString(e.getMessage()));
        } catch (BError bError) {
            return ErrorCreator.createError(FROM_BAL_STRING_ERROR,
                    StringUtils.fromString(bError.getErrorMessage().getValue()));
        }
    }
}
