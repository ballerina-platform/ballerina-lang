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
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;


/**
 * Native implementation of lang.string.StringIterator:next().
 *
 * @since 1.0
 */
public class Next {

    //TODO: refactor hard coded values
    public static Object next(BObject m) {
        StringCharacterIterator stringCharacterIterator = (StringCharacterIterator) m.getNativeData("&iterator&");
        if (stringCharacterIterator == null) {
            String s = ((BString) m.get(StringUtils.fromString("m"))).getValue();
            stringCharacterIterator = new StringCharacterIterator(s);
            m.addNativeData("&iterator&", stringCharacterIterator);
        }

        if (stringCharacterIterator.current() != CharacterIterator.DONE) {
            char character = stringCharacterIterator.current();
            stringCharacterIterator.next();
            Object charAsStr = StringUtils.fromString(String.valueOf(character));
            return ValueCreator.createRecordValue(ValueCreator.createMapValue(
                    PredefinedTypes.STRING_ITR_NEXT_RETURN_TYPE),
                                                  charAsStr);
        }

        return null;
    }
}
