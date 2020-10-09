/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.ballerinalang.langlib.xml;

import io.ballerina.jvm.TypeChecker;
import io.ballerina.jvm.XMLFactory;
import io.ballerina.jvm.api.TypeFlags;
import io.ballerina.jvm.api.TypeTags;
import io.ballerina.jvm.api.Types;
import io.ballerina.jvm.api.types.Type;
import io.ballerina.jvm.types.BUnionType;
import io.ballerina.jvm.util.exceptions.BLangExceptionHelper;
import io.ballerina.jvm.util.exceptions.RuntimeErrors;
import io.ballerina.jvm.values.XMLValue;

import java.util.Arrays;

/**
 * Set the children of an XML if its a singleton. Error otherwise.
 * Any existing children will be removed.
 * 
 * @since 0.88
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.xml",
//        functionName = "setChildren",
//        args = {@Argument(name = "children", type = TypeKind.UNION)},
//        isPublic = true
//)
public class SetChildren {

    private static final String OPERATION = "set children to xml element";

    public static void setChildren(XMLValue xml, Object children) {
        if (!IsElement.isElement(xml)) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.XML_FUNC_TYPE_ERROR, "setChildren", "element");
        }

        Type childrenType = TypeChecker.getType(children);
        if (childrenType.getTag() == TypeTags.STRING_TAG) {
            XMLValue xmlText = XMLFactory.createXMLText((String) children);
            children = xmlText;
        } else if (TypeTags.isXMLTypeTag(childrenType.getTag())) {
            BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE,
                    new BUnionType(Arrays.asList(Types.TYPE_XML, Types.TYPE_STRING),
                                   TypeFlags.asMask(TypeFlags.ANYDATA, TypeFlags.PURETYPE)),
                                                     childrenType);
        }

        try {
            xml.setChildren((XMLValue) children);
        } catch (Throwable e) {
            BLangExceptionHelper.handleXMLException(OPERATION, e);
        }
    }
}
