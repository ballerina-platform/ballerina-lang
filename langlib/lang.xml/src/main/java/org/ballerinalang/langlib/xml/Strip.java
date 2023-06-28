/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.ballerinalang.langlib.xml;

import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.errors.ErrorHelper;

/**
 * Strips the insignificant parts of the an xml value.
 * Comment items, processing instruction items are considered insignificant.
 * After removal of comments and processing instructions, the text is grouped into
 * the biggest possible chunks (i.e., only elements cause division into multiple chunks)
 * and a chunk is considered insignificant if the entire chunk is whitespace.
 * 
 * @since 0.88
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.xml",
//        functionName = "strip",
//        returnType = {@ReturnType(type = TypeKind.XML)},
//        isPublic = true
//)
public class Strip {

    private static final String OPERATION = "strip xml";

    public static BXml strip(BXml xml) {
        try {
            return (BXml) xml.strip();
        } catch (Throwable e) {
            ErrorHelper.handleXMLException(OPERATION, e);
        }
        return null;
    }
}
