/*
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

package org.ballerinalang.langlib.internal;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.ArrayList;
import java.util.List;

/**
 * Searches in children recursively for elements matching the name and returns a sequence containing them all.
 * Does not search within a matched result.
 * 
 * @since 0.92
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.__internal", version = "0.1.0",
        functionName = "selectDescendants",
        args = {@Argument(name = "qname", type = TypeKind.ARRAY)},
        returnType = {@ReturnType(type = TypeKind.XML)},
        isPublic = true
)
public class SelectDescendants {

    private static final String OPERATION = "select descendants from xml";

    public static XMLValue selectDescendants(Strand strand, XMLValue xml, ArrayValue qnames) {
        try {
            List<String> qnameList = new ArrayList<>();
            int size = qnames.size();
            for (int i = 0; i < size; i++) {
                String strQname = qnames.getString(i);
                // remove empty namespace in expanded form i.e `{}local => local`
                if (strQname.lastIndexOf('}') == 1) {
                    strQname = strQname.substring(2);
                }
                qnameList.add(strQname);
            }
            return (XMLValue) xml.descendants(qnameList);
        } catch (Throwable e) {
            BLangExceptionHelper.handleXMLException(OPERATION, e);
        }
        return null;
    }
}
