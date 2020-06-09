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
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
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
    public BIntersectionType immutableType;

    public BXMLType(BType constraint, BTypeSymbol tsymbol) {
        super(TypeTags.XML, tsymbol);
        this.constraint = constraint;
    }

    public BXMLType(BType constraint, BTypeSymbol tsymbol, int flags) {
        super(TypeTags.XML, tsymbol, flags);
        this.constraint = constraint;
    }

    @Override
    public String toString() {
        String stringRep;
        if (constraint != null && !(constraint.tag == TypeTags.UNION &&
                constraint instanceof BUnionType &&
                ((BUnionType) constraint).getMemberTypes().size() == 4)) {
            stringRep = Names.XML.value + "<" + constraint + ">";
        } else {
            stringRep = Names.XML.value;
        }

        return !Symbols.isFlagOn(flags, Flags.READONLY) ? stringRep : stringRep.concat(" & readonly");
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
    public BIntersectionType getImmutableType() {
        return this.immutableType;
    }
}
