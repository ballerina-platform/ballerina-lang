/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.ballerinalang.model.types.Type;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

/**
 * Represents XML Type.
 *
 * @since 0.961.0
 */
public class BXMLType extends BBuiltInRefType implements SelectivelyImmutableReferenceType {

    public BType constraint;
    public BImmutableXmlType immutableType;

    public BXMLType(BType constraint, BTypeSymbol tsymbol) {
        super(TypeTags.XML, tsymbol);
        this.constraint = constraint;
    }

    protected BXMLType(BType constraint, BTypeSymbol tsymbol, int flags) {
        super(TypeTags.XML, tsymbol, flags);
        this.constraint = constraint;
    }

    @Override
    public String toString() {
        if (constraint != null && !(constraint.tag == TypeTags.UNION &&
                constraint instanceof BUnionType &&
                ((BUnionType) constraint).getMemberTypes().size() == 4)) {
            return Names.XML.value + "<" + constraint + ">";
        }
        return Names.XML.value;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Type getImmutableType() {
        return this.immutableType;
    }

    /**
     * Represent the intersection type `xml & readonly`.
     *
     * @since 1.3.0
     */
    public static class BImmutableXmlType extends BXMLType {

        public BImmutableXmlType(BType constraint, BTypeSymbol tsymbol, int flags) {
            super(constraint, tsymbol, flags);
            this.flags |= Flags.READONLY;
        }

        @Override
        public String toString() {
            return getKind().typeName().concat(" & readonly");
        }
    }
}
