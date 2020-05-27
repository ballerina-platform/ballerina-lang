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

package org.ballerinalang.langlib.xml;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.IteratorValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.util.BLangCompilerConstants.XML_VERSION;

/**
 * Native implementation of lang.xml.XMLIterator:next().
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.xml", version = XML_VERSION, functionName = "next",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "XMLIterator", structPackage = "ballerina/lang.xml"),
        returnType = {@ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class Next {
    //TODO: refactor hard coded values
    public static Object next(Strand strand, ObjectValue m) {
        IteratorValue xmlIterator = (IteratorValue) m.getNativeData("&iterator&");

        if (xmlIterator == null) {
            xmlIterator = ((XMLValue) m.get(StringUtils.fromString("m"))).getIterator();
            m.addNativeData("&iterator&", xmlIterator);
        }

        if (xmlIterator.hasNext()) {
            Object xmlValue = xmlIterator.next();
            return BallerinaValues.createRecord(new MapValueImpl<>(BTypes.xmlItrNextReturnType), xmlValue);
        }

        return null;
    }
}
