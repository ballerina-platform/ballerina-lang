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

import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeFlags;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.util.Arrays;

import static org.ballerinalang.util.BLangCompilerConstants.XML_VERSION;

/**
 * Set the children of an XML if its a singleton. Error otherwise.
 * Any existing children will be removed.
 * 
 * @since 0.88
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.xml", version = XML_VERSION,
        functionName = "setChildren",
        args = {@Argument(name = "children", type = TypeKind.UNION)},
        isPublic = true
)
public class SetChildren {

    private static final String OPERATION = "set children to xml element";

    public static void setChildren(Strand strand, XMLValue xml, Object children) {
        if (!IsElement.isElement(strand, xml)) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.XML_FUNC_TYPE_ERROR, "setChildren", "element");
        }

        BType childrenType = TypeChecker.getType(children);
        if (childrenType.getTag() == TypeTags.STRING_TAG) {
            XMLValue xmlText = XMLFactory.createXMLText((String) children);
            children = xmlText;
        } else if (TypeTags.isXMLTypeTag(childrenType.getTag())) {
            BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE,
                    new BUnionType(Arrays.asList(BTypes.typeXML, BTypes.typeString),
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
