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

package org.ballerinalang.langlib.string;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

/**
 * Native implementation of lang.string.StringIterator:next().
 *
 * @since 1.0
 */
public class Next {

    private Next() {
    }

    //TODO: refactor hard coded values
    public static Object next(BObject stringObject) {
        BIterator charIterator = (BIterator) stringObject.getNativeData("&iterator&");
        if (charIterator == null) {
            BString bString = ((BString) stringObject.get(StringUtils.fromString("m")));
            charIterator = bString.getIterator();
            stringObject.addNativeData("&iterator&", charIterator);
        }

        if (charIterator.hasNext()) {
            Object element = StringUtils.fromString((String) charIterator.next());
            return ValueCreator
                    .createRecordValue(ValueCreator.createRecordValue(PredefinedTypes.STRING_ITR_NEXT_RETURN_TYPE),
                            element);
        }
        return null;
    }
}
