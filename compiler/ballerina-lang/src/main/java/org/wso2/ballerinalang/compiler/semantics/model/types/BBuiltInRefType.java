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
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.types.ReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;

import static org.wso2.ballerinalang.compiler.util.TypeTags.ANY;
import static org.wso2.ballerinalang.compiler.util.TypeTags.ANYDATA;
import static org.wso2.ballerinalang.compiler.util.TypeTags.CHANNEL;
import static org.wso2.ballerinalang.compiler.util.TypeTags.FUTURE;
import static org.wso2.ballerinalang.compiler.util.TypeTags.JSON;
import static org.wso2.ballerinalang.compiler.util.TypeTags.MAP;
import static org.wso2.ballerinalang.compiler.util.TypeTags.STREAM;
import static org.wso2.ballerinalang.compiler.util.TypeTags.TABLE;
import static org.wso2.ballerinalang.compiler.util.TypeTags.XML;

/**
 * @since 0.94
 */
public class BBuiltInRefType extends BType implements ReferenceType {

    public BBuiltInRefType(int tag, BTypeSymbol tsymbol) {
        super(tag, tsymbol);
    }

    @Override
    public String getDesc() {
        switch (tag) {
            case JSON:
            case XML:
            case TABLE:
            case STREAM:
            case MAP:
            case FUTURE:
            case CHANNEL:
                return TypeDescriptor.SIG_REFTYPE + getKind().typeName() + ";";
            case ANY:
                return TypeDescriptor.SIG_ANY;
            case ANYDATA:
                return TypeDescriptor.SIG_ANYDATA;
            default:
                return null;
        }
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public TypeKind getKind() {
        switch (tag) {
            case JSON:
                return TypeKind.JSON;
            case XML:
                return TypeKind.XML;
            case TABLE:
                return TypeKind.TABLE;
            case STREAM:
                return TypeKind.STREAM;
            case ANY:
                return TypeKind.ANY;
            case ANYDATA:
                return TypeKind.ANYDATA;
            case MAP:
                return TypeKind.MAP;
            case FUTURE:
                return TypeKind.FUTURE;
            case CHANNEL:
                return TypeKind.CHANNEL;
            default:
                return TypeKind.OTHER;
        }
    }
}
