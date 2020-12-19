/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Represents an union type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaUnionTypeSymbol extends AbstractTypeSymbol implements UnionTypeSymbol {

    private List<TypeSymbol> memberTypes;

    public BallerinaUnionTypeSymbol(CompilerContext context, ModuleID moduleID, BUnionType unionType) {
        super(context, TypeDescKind.UNION, moduleID, unionType);
    }

    public BallerinaUnionTypeSymbol(CompilerContext context, ModuleID moduleID, BFiniteType finiteType) {
        super(context, TypeDescKind.UNION, moduleID, finiteType);
    }

    @Override
    public List<TypeSymbol> memberTypeDescriptors() {
        if (this.memberTypes == null) {
            List<TypeSymbol> members = new ArrayList<>();

            if (this.getBType().tag == TypeTags.UNION) {
                TypesFactory typesFactory = TypesFactory.getInstance(this.context);

                for (BType memberType : ((BUnionType) this.getBType()).getMemberTypes()) {
                    members.add(typesFactory.getTypeDescriptor(memberType));
                }
            } else {
                for (BLangExpression value : ((BFiniteType) this.getBType()).getValueSpace()) {
                    members.add(new BallerinaSingletonTypeSymbol(this.context, moduleID(), value, value.type));
                }
            }

            this.memberTypes = Collections.unmodifiableList(members);
        }

        return this.memberTypes;
    }

    @Override
    public String signature() {
        List<TypeSymbol> memberTypes = this.memberTypeDescriptors();
        if (memberTypes.size() == 2 &&
                memberTypes.get(1).typeKind() == TypeDescKind.NIL) {
            return memberTypes.get(0).signature() + "?";
        } else {
            StringJoiner joiner = new StringJoiner("|");
            memberTypes.forEach(typeDescriptor -> joiner.add(typeDescriptor.signature()));
            return joiner.toString();
        }
    }
}
