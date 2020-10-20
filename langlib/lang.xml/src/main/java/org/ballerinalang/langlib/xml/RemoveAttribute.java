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

import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.scheduling.Strand;
import io.ballerina.runtime.util.exceptions.BLangExceptionHelper;

/**
 * Remove an attribute from an XML.
 * 
 * @since 0.95
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.xml",
//        functionName = "removeAttribute",
//        args = {@Argument(name = "qname", type = TypeKind.STRING)},
//        isPublic = true
//)
public class RemoveAttribute {

    private static final String OPERATION = "remove attribute";

    public static void removeAttribute(Strand strand, BXML xml, String qname) {
        try {
            xml.removeAttribute(qname);
        } catch (Throwable e) {
            BLangExceptionHelper.handleXMLException(OPERATION, e);
        }

    }
}
