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

import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.util.exceptions.RuntimeErrors;

/**
 * Returns the string giving the expanded name of provided xml element.
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.xml",
//        functionName = "getName",
//        args = {@Argument(name = "BXML", type = TypeKind.XML)},
//        returnType = {@ReturnType(type = TypeKind.STRING)},
//        isPublic = true
//)
public class GetName {

    private static final String OPERATION = "get element name in xml";

    public static BString getName(BXML xmlVal) {
        if (!IsElement.isElement(xmlVal)) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.XML_FUNC_TYPE_ERROR, "getName", "element");
        }
        try {
            return StringUtils.fromString(xmlVal.getElementName());
        } catch (Throwable e) {
            BLangExceptionHelper.handleXMLException(OPERATION, e);
        }

        return null;
    }
}
