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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeFlags;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.values.BmpStringValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Map;


/**
 * Native implementation of lang.string.StringIterator:next().
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.string", functionName = "next",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "StringIterator",
                structPackage = "ballerina/lang.string"),
        returnType = {@ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class Next {
    public static final String IS_STRING_VALUE_PROP = "ballerina.bstring";
    public static final boolean USE_BSTRING = System.getProperty(IS_STRING_VALUE_PROP) != null;

    //TODO: refactor hard coded values
    public static Object next(Strand strand, ObjectValue m) {
        StringCharacterIterator stringCharacterIterator = (StringCharacterIterator) m.getNativeData("&iterator&");
        if (stringCharacterIterator == null) {
            String s = USE_BSTRING ? ((BString) m.get(StringUtils.fromString("m"))).getValue() :
                    (String) m.get(new BmpStringValue("m"));
            stringCharacterIterator = new StringCharacterIterator(s);
            m.addNativeData("&iterator&", stringCharacterIterator);
        }

        if (stringCharacterIterator.current() != CharacterIterator.DONE) {
            char character = stringCharacterIterator.current();
            stringCharacterIterator.next();
            Map<String, BField> fields = new HashMap<>();
            fields.put("value", new BField(BTypes.typeString, "value", Flags.PUBLIC + Flags.REQUIRED));
            BRecordType recordType = new BRecordType("$$returnType$$", null, 0, fields,
                    null, true, TypeFlags.asMask(TypeFlags.PURETYPE, TypeFlags.ANYDATA));
            Object charAsStr = USE_BSTRING ? StringUtils.fromString(String.valueOf(character)) :
                               String.valueOf(character);
            return BallerinaValues.createRecord(new MapValueImpl<>(recordType), charAsStr);
        }

        return null;
    }
}
