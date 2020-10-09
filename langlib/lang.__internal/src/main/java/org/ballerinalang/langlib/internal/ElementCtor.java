/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.internal;

import io.ballerina.jvm.XMLFactory;
import io.ballerina.jvm.api.values.BString;
import io.ballerina.jvm.values.MapValue;
import io.ballerina.jvm.values.XMLItem;
import io.ballerina.jvm.values.XMLQName;
import io.ballerina.jvm.values.XMLValue;

/**
 * XML Element constructor function.
 *
 * @since 2.0.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.__internal", version = "0.1.0", functionName = "elementCtor",
//        args = {
//                @Argument(name = "name", type = TypeKind.STRING),
//                @Argument(name = "attributeMap", type = TypeKind.MAP),
//                @Argument(name = "children", type = TypeKind.XML)
//        },
//        returnType = {@ReturnType(type = TypeKind.XML)}
//)
public class ElementCtor {

    public static XMLValue elementCtor(BString name, MapValue<BString, BString> attributeMap,
                                       XMLValue children) {
        XMLItem xmlElement = (XMLItem) XMLFactory.createXMLElement(new XMLQName(name), (BString) null);
        xmlElement.setChildren(children);
        xmlElement.setAttributes(attributeMap);
        return xmlElement;
    }
}
