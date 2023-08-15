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
import io.ballerina.runtime.internal.scheduling.Strand;

/**
 * Check whether the XML sequence contains only a single element.
 * 
 * @since 0.88
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.xml",
//        functionName = "isSingleton",
//        returnType = {@ReturnType(type = TypeKind.BOOLEAN)},
//        isPublic = true
//)
public class IsSingleton {

    private static final String OPERATION = "check xml is singleton";

    public static boolean isSingleton(Strand strand, BXml xml) {
        try {
            return xml.isSingleton();
        } catch (Throwable e) {
            ErrorHelper.handleXMLException(OPERATION, e);
        }

        return false;
    }
}
