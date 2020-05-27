/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.HashMap;

import static org.ballerinalang.util.BLangCompilerConstants.XML_VERSION;

/**
 * Make a deep copy of an XML.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.xml", version = XML_VERSION,
        functionName = "copy",
        returnType = {@ReturnType(type = TypeKind.XML)},
        isPublic = true
)
public class Copy {

    private static final String OPERATION = "copy xml";

    public static XMLValue copy(Strand strand, XMLValue xml) {
        try {
            return (XMLValue) xml.copy(new HashMap<>());
        } catch (Throwable e) {
            BLangExceptionHelper.handleXMLException(OPERATION, e);
        }

        return null;
    }
}
