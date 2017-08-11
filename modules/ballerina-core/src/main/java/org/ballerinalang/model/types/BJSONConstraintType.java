/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.types;

import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.SymbolScope;

/**
 * {@code BJSONConstraintType} represents a JSON document constrained by a struct definition.
 *
 * @since 0.9.0
 */
public class BJSONConstraintType extends BJSONType {

    protected BType constraint;

    public BJSONConstraintType(BType constraint) {
        super("c-".concat(BTypes.typeJSON.getName()),
                BTypes.typeJSON.getPackagePath(),
                BTypes.typeJSON.getSymbolScope());
        this.constraint = constraint;
    }

    /**
     * Create a {@code BJSONConstraintType} which represents the boolean type.
     *
     * @param typeName string name of the type
     */
    BJSONConstraintType(String typeName, String pkgPath, SymbolScope symbolScope) {
        super(typeName, pkgPath, symbolScope);
    }

    public BType getConstraint() {
        return this.constraint;
    }

    public void setConstraint(StructDef constraint) {
        this.constraint = constraint;
    }

    @Override
    public String toString() {
        return "json" + "<" + constraint.getName() + ">";
    }

    @Override
    public TypeSignature getSig() {
        return new TypeSignature(TypeSignature.SIG_CJSON, constraint.getPackagePath(), constraint.getName());
    }

    @Override
    public int getTag() {
        return TypeTags.C_JSON_TAG;
    }

    public boolean equals(Object obj) {
        if (obj instanceof BJSONConstraintType) {
            boolean constraintEqual = constraint.equals(((BJSONConstraintType) obj).getConstraint());
            return super.equals(obj) && constraintEqual;
        } else {
            return super.equals(obj);
        }
    }

}
