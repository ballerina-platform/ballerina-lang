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

package org.ballerinalang.langlib.xml;

import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.util.exceptions.BLangExceptionHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Get all the elements-type items of a xml.
 * 
 * @since 0.88
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.xml",
//        functionName = "elements",
//        returnType = {@ReturnType(type = TypeKind.XML)},
//        isPublic = true
//)
public class Elements {

    private static final String OPERATION = "get elements from xml";

    public static BXML elements(BXML xml, Object name) {
        try {
            if (name instanceof BString) {
                return (BXML) xml.elements(((BString) name).getValue());
            }
            return (BXML) xml.elements();
        } catch (Throwable e) {
            BLangExceptionHelper.handleXMLException(OPERATION, e);
        }

        return null;
    }

    private static BXML generateCodePointSequence(BXML value) {
        List<BXML> list = new ArrayList<>();
        BIterator bIterator = value.getIterator();
        while (bIterator.hasNext()) {
            list.add((BXML) bIterator.next());
        }
        return ValueCreator.createXMLSequence(list);
    }
}
