/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langlib.xml;

import io.ballerina.runtime.XMLFactory;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.api.values.BXMLQName;

/**
 * Create XML element from tag name and children sequence.
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.xml",
//        functionName = "createElement",
//        args = {
//                @Argument(name = "name", type = TypeKind.STRING),
//                @Argument(name = "children", type = TypeKind.STRING)},
//        returnType = {@ReturnType(type = TypeKind.XML)},
//        isPublic = true
//)
public class CreateElement {

    public static BXML createElement(BString name, BXML children) {
        BXMLQName xmlqName = ValueCreator.createXMLQName(name);
        String temp = null;
        BXML xmlElement = XMLFactory.createXMLElement(xmlqName, temp);
        xmlElement.setChildren(getChildren(children));
        return xmlElement;
    }

    private static BXML getChildren(BXML children) {
        if (children == null) {
            return ValueCreator.createXMLSequence();
        }
        return children;
    }
}
