/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.LinkedHashSet;

/**
 * Checks if the valid parameter type is passed to the main function.
 * <p>
 * This is introduced to handle cyclic unions.
 *
 * @since slp4
 */
public class MainParameterVisitor {
    private final boolean option;

    public MainParameterVisitor(boolean option) {
        this.option = option;
    }

    public boolean visit(BType type) {
        if (type.tag == TypeTags.ARRAY) {
            return isOperandType(((BArrayType) type).eType);
        }
        return isOperandTypeOrUnion(type);
    }

    private boolean isOperandTypeOrUnion(BType type) {
        return isOperandType(type) || isUnionWithNil(type);
    }

    private boolean isUnionWithNil(BType type) {
        if (type.tag == TypeTags.UNION) {
            BUnionType bUnionType = (BUnionType) type;
            LinkedHashSet<BType> memberTypes = bUnionType.getMemberTypes();
            if (memberTypes.size() == 2) {
                boolean result = hasNil(memberTypes);
                if (result) {
                    for (BType memberType : memberTypes) {
                        result &= isOperandType(memberType) || memberType.tag == TypeTags.NIL;
                    }
                }
                return result;
            }
        }
        return false;
    }

    private boolean hasNil(LinkedHashSet<BType> memberTypes) {
        boolean result = false;
        for (BType memberType : memberTypes) {
            if (memberType.tag == TypeTags.NIL) {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean isOperandType(BType type) {
        switch (type.tag) {
            case TypeTags.INT:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
            case TypeTags.STRING:
                return true;
            case TypeTags.BOOLEAN:
                return option;
            case TypeTags.TYPEREFDESC:
                return isOperandType(((BTypeReferenceType) type).constraint);
            default:
                return false;
        }
    }
}
