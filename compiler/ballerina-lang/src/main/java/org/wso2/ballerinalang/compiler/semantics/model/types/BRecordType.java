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
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.types.RecordType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code BRecordType} represents record type in Ballerina.
 *
 * @since 0.971.0
 */
public class BRecordType extends BStructureType implements RecordType {

    private static final String SPACE = " ";
    private static final String RECORD = "record";
    private static final String CLOSE_LEFT = "{|";
    private static final String SEMI = ";";
    private static final String CLOSE_RIGHT = "|}";
    private static final String DOLLAR = "$";
    private static final String REST = "...";
    public static final String OPTIONAL = "?";
    public static final String EMPTY = "";
    public boolean sealed;
    public BType restFieldType;

    public BRecordType(BTypeSymbol tSymbol) {
        super(TypeTags.RECORD, tSymbol);
        this.fields = new ArrayList<>();
    }

    public String getDesc() {
        return TypeDescriptor.SIG_STRUCT + getQualifiedTypeName() + ";";
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.RECORD;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {

        if (tsymbol.name != null && (tsymbol.name.value.isEmpty() || tsymbol.name.value.startsWith(DOLLAR))) {
            // Try to print possible shape. But this may fail with self reference hence avoid .
            int count = 0;
            Map<BType, String> typeNames = new HashMap<>();
            StringBuilder sb = new StringBuilder();
            sb.append(RECORD).append(SPACE);
            sb.append(CLOSE_LEFT);
            for (BField field : fields) {
                sb.append(SPACE);
                int tag = field.type.tag;
                if (tag < TypeTags.XML || tag == TypeTags.NIL || tag == TypeTags.ANY || tag == TypeTags.ANYDATA) {
                    sb.append(field.type);
                } else {
                    if (typeNames.containsKey(field.type)) {
                        sb.append(typeNames.get(field.type));
                    } else {
                        String typeName = DOLLAR + field.type.getKind().typeName() + count++;
                        sb.append(typeName);
                        typeNames.put(field.type, typeName);
                    }
                }
                sb.append(SPACE).append(field.name)
                        .append(Symbols.isOptional(field.symbol) ? OPTIONAL : EMPTY).append(SEMI);
            }
            if (sealed) {
                sb.append(SPACE).append(CLOSE_RIGHT);
                return sb.toString();
            }
            sb.append(SPACE).append(restFieldType).append(REST).append(SEMI);
            sb.append(SPACE).append(CLOSE_RIGHT);
            return sb.toString();
        }
        return this.tsymbol.toString();
    }
}
