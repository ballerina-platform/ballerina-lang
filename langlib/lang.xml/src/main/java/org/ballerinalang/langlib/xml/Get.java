/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlSequence;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;

import java.util.List;

/**
 * Returns the item of `x` with index `i`.
 * This differs from `x[i]` in that it panics if `x` does not have an item with index `i`.
 *
 * @since 1.2.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.xml",
//        functionName = "get",
//        args = {@Argument(name = "BXML", type = TypeKind.XML),
//                @Argument(name = "i", type = TypeKind.INT)},
//        returnType = {@ReturnType(type = TypeKind.XML)},
//        isPublic = true
//)
public class Get {

    public static final int LENGTH_OF_ONE = 1;

    public static BXml get(BXml xmlVal, long i) {
        // Handle single xml items
        XmlNodeType nodeType = xmlVal.getNodeType();
        switch (nodeType) {
            case ELEMENT:
            case COMMENT:
            case TEXT:
            case PI:
                if (i == 0) {
                    return xmlVal;
                }
                throw ErrorHelper.getRuntimeException(
                        ErrorCodes.XML_SEQUENCE_INDEX_OUT_OF_RANGE, LENGTH_OF_ONE, i);
        }

        // Handle xml sequence
        List<BXml> childrenList = ((BXmlSequence) xmlVal).getChildrenList();
        int size = childrenList.size();
        if (i < 0 || i >= size) {
            throw ErrorHelper.getRuntimeException(ErrorCodes.XML_SEQUENCE_INDEX_OUT_OF_RANGE, size, i);
        }

        return (BXml) childrenList.get((int) i);
    }
}
