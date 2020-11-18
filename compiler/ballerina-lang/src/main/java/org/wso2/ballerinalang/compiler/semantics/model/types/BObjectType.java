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

import org.ballerinalang.model.types.ObjectType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

/**
 * {@code BObjectType} represents object type in Ballerina.
 *
 * @since 0.971.0
 */
public class BObjectType extends BStructureType implements ObjectType {

    private static final String OBJECT = "object";
    private static final String SPACE = " ";
    private static final String PUBLIC = "public";
    private static final String PRIVATE = "private";
    private static final String LEFT_CURL = "{";
    private static final String RIGHT_CURL = "}";
    private static final String SEMI_COLON = ";";
    private static final String READONLY = "readonly";

    public BIntersectionType immutableType;
    public BObjectType mutableType = null;

    public BTypeIdSet typeIdSet = BTypeIdSet.emptySet();

    public BObjectType(BTypeSymbol tSymbol) {
        super(TypeTags.OBJECT, tSymbol);
    }

    public BObjectType(BTypeSymbol tSymbol, int flags) {
        super(TypeTags.OBJECT, tSymbol, flags);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.OBJECT;
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

        if (shouldPrintShape(tsymbol.name)) {
            StringBuilder sb = new StringBuilder();

            int symbolFlags = tsymbol.flags;
            if (Symbols.isFlagOn(symbolFlags, Flags.ISOLATED)) {
                sb.append("isolated ");
            }

            sb.append(OBJECT).append(SPACE).append(LEFT_CURL);
            for (BField field : fields.values()) {
                int flags = field.symbol.flags;
                if (Symbols.isFlagOn(flags, Flags.PUBLIC)) {
                    sb.append(SPACE).append(PUBLIC);
                } else if (Symbols.isFlagOn(flags, Flags.PRIVATE)) {
                    sb.append(SPACE).append(PRIVATE);
                }

                if (Symbols.isFlagOn(flags, Flags.FINAL)) {
                    sb.append(SPACE).append("final");
                }

                sb.append(SPACE).append(field.type).append(SPACE).append(field.name).append(";");
            }
            BObjectTypeSymbol objectSymbol = (BObjectTypeSymbol) this.tsymbol;
            for (BAttachedFunction fun : objectSymbol.attachedFuncs) {
                if (Symbols.isFlagOn(fun.symbol.flags, Flags.PUBLIC)) {
                    sb.append(SPACE).append(PUBLIC);
                } else if (Symbols.isFlagOn(fun.symbol.flags, Flags.PRIVATE)) {
                    sb.append(SPACE).append(PRIVATE);
                }
                sb.append(SPACE).append(fun).append(SEMI_COLON);
            }
            sb.append(SPACE).append(RIGHT_CURL);

            if (Symbols.isFlagOn(symbolFlags, Flags.READONLY)) {
                sb.append(" & readonly");
            }

            return sb.toString();
        }
        return this.tsymbol.toString();
    }

    @Override
    public BIntersectionType getImmutableType() {
        return this.immutableType;
    }
}
