/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.types.TableType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;

/**
 * {@code BTableType} represents a table in Ballerina.
 *
 * @since 1.3.0
 */
public class BTableType extends BType implements TableType {

    public BType constraint;
    public BType keyTypeConstraint;
    public List<String> fieldNameList;
    public DiagnosticPos keyPos;
    public DiagnosticPos constraintPos;

    public BTableType(int tag, BType constraint, BTypeSymbol tSymbol) {
        super(tag, tSymbol);
        this.constraint = constraint;
    }

    public BType getConstraint() {
        return this.constraint;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        if (constraint == null) {
            return super.toString();
        }

        StringBuilder keyStringBuilder = new StringBuilder();
        if (fieldNameList != null) {
            for (String fieldName : fieldNameList) {
                if (!keyStringBuilder.toString().equals("")) {
                    keyStringBuilder.append(", ");
                }
                keyStringBuilder.append(fieldName);
            }
            return super.toString() + "<" + constraint + "> key(" + keyStringBuilder.toString() + ")";
        }

        return (super.toString() + "<" + constraint + "> " +
                ((keyTypeConstraint != null) ? ("key<" + keyTypeConstraint + ">") : "")).trim();
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.TABLE;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }
}
