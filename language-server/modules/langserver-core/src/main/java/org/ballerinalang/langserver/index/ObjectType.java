/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.index;

import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.util.Flags;

/**
 * Enum for ObjectType.
 */
public enum ObjectType {
    CLIENT(1),
    SERVER(2),
    OBJECT(3);

    private int type;

    ObjectType(int type) {
        this.type = type;
    }

    public int getValue() {
        return this.type;
    }

    /**
     * Get the enum value from ordinal.
     *
     * @param ordinal   ordinal value to query.
     * @return {@link ObjectType}   Object Type
     */
    public static ObjectType get(int ordinal) {
        for (ObjectType objectType : ObjectType.values()) {
            if (objectType.getValue() == ordinal) {
                return objectType;
            }
        }
        return null;
    }

    /**
     * Get the enum value from {@link BObjectTypeSymbol}.
     *
     * @param objectTypeSymbol      object type symbol
     * @return {@link ObjectType}   Object Type
     */
    public static ObjectType get(BObjectTypeSymbol objectTypeSymbol) {
        if ((objectTypeSymbol.type.tsymbol.flags & Flags.CLIENT) == Flags.CLIENT) {
            return ObjectType.CLIENT;
        } else {
            return ObjectType.OBJECT;
        }
    }
}
