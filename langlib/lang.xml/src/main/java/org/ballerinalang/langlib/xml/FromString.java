/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;

/**
 * Converts a XML to the corresponding JSON representation.
 *
 * @since 0.90
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.xml",
//        functionName = "fromString",
//        args = {@Argument(name = "string", type = TypeKind.STRING)},
//        returnType = {@ReturnType(type = TypeKind.XML)},
//        isPublic = true
//)
public class FromString {

    public static Object fromString(BString string) {
        try {
            return XMLFactory.parse(string.getValue());
        } catch (BError BError) {
            return BError;
        }
    }
}
