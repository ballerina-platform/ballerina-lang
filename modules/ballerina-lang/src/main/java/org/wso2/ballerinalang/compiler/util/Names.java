/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.util;

import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;

/**
 * @since 0.94
 */
public class Names {

    public static final CompilerContext.Key<Names> NAMES_KEY =
            new CompilerContext.Key<>();

    public static final Name EMPTY = new Name("");
    public static final Name INVALID = new Name("><");
    public static final Name DEFAULT_VERSION = new Name("0.0.0");
    public static final Name CAST_OP = new Name("(<type>)");
    public static final Name CONVERSION_OP = new Name("<<type>>");
    public static final Name MSG = new Name("msg");
    public static final Name ERROR = new Name("error");

    public CompilerContext context;

    public static Names getInstance(CompilerContext context) {
        Names names = context.get(NAMES_KEY);
        if (names == null) {
            names = new Names(context);
            context.put(NAMES_KEY, names);
        }
        return names;
    }

    private Names(CompilerContext context) {
        this.context = context;
        this.context.put(NAMES_KEY, this);
    }

    public Name fromIdNode(BLangIdentifier identifier) {
        // identifier.value cannot be null
        return identifier.value.equals("") ? EMPTY : new Name(identifier.value);
    }

    public Name fromString(String value) {
        // value cannot be null
        return value.equals("") ? EMPTY : new Name(value);
    }

    public Name fromTypeKind(TypeKind typeKind) {
        return fromString(typeKind.typeName());
    }
}
