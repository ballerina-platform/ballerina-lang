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

import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import org.ballerinalang.model.Name;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

/**
 * Represents Builtin Subtype of integer.
 *
 * @since 1.2.0
 */
public class BIntSubType extends BType {

    /**
     * @deprecated Use {@code SymbolTable#signed32IntType}, which is created per compiler context.
     */
    @Deprecated(since = "2201.14.1", forRemoval = true)
    public static final BIntSubType SIGNED32 = newSigned32();
    /**
     * @deprecated Use {@code SymbolTable#signed16IntType}, which is created per compiler context.
     */
    @Deprecated(since = "2201.14.1", forRemoval = true)
    public static final BIntSubType SIGNED16 = newSigned16();
    /**
     * @deprecated Use {@code SymbolTable#signed8IntType}, which is created per compiler context.
     */
    @Deprecated(since = "2201.14.1", forRemoval = true)
    public static final BIntSubType SIGNED8 = newSigned8();
    /**
     * @deprecated Use {@code SymbolTable#unsigned32IntType}, which is created per compiler context.
     */
    @Deprecated(since = "2201.14.1", forRemoval = true)
    public static final BIntSubType UNSIGNED32 = newUnsigned32();
    /**
     * @deprecated Use {@code SymbolTable#unsigned16IntType}, which is created per compiler context.
     */
    @Deprecated(since = "2201.14.1", forRemoval = true)
    public static final BIntSubType UNSIGNED16 = newUnsigned16();
    /**
     * @deprecated Use {@code SymbolTable#unsigned8IntType}, which is created per compiler context.
     */
    @Deprecated(since = "2201.14.1", forRemoval = true)
    public static final BIntSubType UNSIGNED8 = newUnsigned8();

    private BIntSubType(int tag, Name name, SemType semType) {
        super(tag, null, name, Flags.READONLY, semType);
    }

    public static BIntSubType newSigned32() {
        return new BIntSubType(TypeTags.SIGNED32_INT, Names.SIGNED32, SemTypes.SINT32);
    }

    public static BIntSubType newSigned16() {
        return new BIntSubType(TypeTags.SIGNED16_INT, Names.SIGNED16, SemTypes.SINT16);
    }

    public static BIntSubType newSigned8() {
        return new BIntSubType(TypeTags.SIGNED8_INT, Names.SIGNED8, SemTypes.SINT8);
    }

    public static BIntSubType newUnsigned32() {
        return new BIntSubType(TypeTags.UNSIGNED32_INT, Names.UNSIGNED32, SemTypes.UINT32);
    }

    public static BIntSubType newUnsigned16() {
        return new BIntSubType(TypeTags.UNSIGNED16_INT, Names.UNSIGNED16, SemTypes.UINT16);
    }

    public static BIntSubType newUnsigned8() {
        return new BIntSubType(TypeTags.UNSIGNED8_INT, Names.UNSIGNED8, SemTypes.UINT8);
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.INT;
    }

    @Override
    public String toString() {
        return Names.INT.value + Names.ALIAS_SEPARATOR + name;
    }

    @Override
    public String getQualifiedTypeName() {
        return Names.BALLERINA_ORG.value + Names.ORG_NAME_SEPARATOR.value
                + Names.LANG.value + Names.DOT.value + Names.INT.value + Names.ALIAS_SEPARATOR + name;
    }
}
