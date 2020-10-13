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

import io.ballerina.jvm.api.BStringUtils;
import io.ballerina.jvm.api.values.BString;
import io.ballerina.jvm.api.values.BXML;
import io.ballerina.jvm.util.exceptions.BLangExceptionHelper;
import io.ballerina.jvm.util.exceptions.RuntimeErrors;

/**
 * Create XML processing instruction.
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.xml",
//        functionName = "getTarget",
//        args = {
//                @Argument(name = "xmlValue", type = TypeKind.XML)},
//        returnType = {@ReturnType(type = TypeKind.STRING)},
//        isPublic = true
//)
public class GetTarget {

    public static BString getTarget(BXML xmlValue) {
        if (!IsProcessingInstruction.isProcessingInstruction(xmlValue)) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.XML_FUNC_TYPE_ERROR,
                    "getTarget", "processing instruction");
        }

        return BStringUtils.fromString(XMLValueUtil.getTarget(xmlValue));
    }
}
