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
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.Optional;

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
    private static final String REST = "...";
    public static final String OPTIONAL = "?";
    public static final String EMPTY = "";
    public static final String READONLY = "readonly";
    public boolean sealed;
    public BType restFieldType;
    public Boolean isAnyData = null;

    public BRecordType mutableType;

    private BIntersectionType intersectionType = null;

    public BRecordType(BTypeSymbol tSymbol) {
        super(TypeTags.RECORD, tSymbol);
    }

    public BRecordType(BTypeSymbol tSymbol, long flags) {
        super(TypeTags.RECORD, tSymbol, flags);
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

        if (shouldPrintShape()) {
            // Try to print possible shape. But this may fail with self reference hence avoid .
            StringBuilder sb = new StringBuilder();
            sb.append(RECORD).append(SPACE).append(CLOSE_LEFT);
            for (BField field : fields.values()) {
                sb.append(SPACE);

                if (Symbols.isFlagOn(field.symbol.flags, Flags.READONLY)) {
                    sb.append(READONLY).append(SPACE);
                }

                sb.append(field.type).append(SPACE).append(field.name)
                        .append(Symbols.isOptional(field.symbol) ? OPTIONAL : EMPTY).append(SEMI);
            }
            if (sealed) {
                sb.append(SPACE).append(CLOSE_RIGHT);
                return !Symbols.isFlagOn(this.flags, Flags.READONLY) ? sb.toString() :
                        sb.toString().concat(" & readonly");
            }
            sb.append(SPACE).append(restFieldType).append(REST).append(SEMI).append(SPACE).append(CLOSE_RIGHT);
            return !Symbols.isFlagOn(this.flags, Flags.READONLY) ? sb.toString() : sb.toString().concat(" & readonly");
        }
        return this.tsymbol.toString();
    }

    @Override
    public Optional<BIntersectionType> getIntersectionType() {
        return Optional.ofNullable(this.intersectionType);
    }

    @Override
    public void setIntersectionType(BIntersectionType intersectionType) {
        this.intersectionType = intersectionType;
    }
}
