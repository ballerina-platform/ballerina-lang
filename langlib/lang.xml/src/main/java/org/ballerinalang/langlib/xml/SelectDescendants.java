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

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.runtime.internal.scheduling.Strand;
import org.ballerinalang.langlib.xml.utils.XmlUtils;

/**
 * Searches in children recursively for elements matching the name and returns a sequence containing them all.
 * Does not search within a matched result.
 * 
 * @since 0.92
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.xml",
//        functionName = "selectDescendants",
//        args = {@Argument(name = "qname", type = TypeKind.ARRAY)},
//        returnType = {@ReturnType(type = TypeKind.XML)},
//        isPublic = true
//)
public class SelectDescendants {

    private static final String OPERATION = "select descendants from xml";

    public static BXml selectDescendants(Strand strand, BXml xml, BArray qnames) {
        try {
            // todo: this need to support list of qnames.
            String qname = qnames.getString(0);
            return (BXml) xml.descendants(XmlUtils.getList(qname));
        } catch (Throwable e) {
            ErrorHelper.handleXMLException(OPERATION, e);
        }
        return null;
    }
}
