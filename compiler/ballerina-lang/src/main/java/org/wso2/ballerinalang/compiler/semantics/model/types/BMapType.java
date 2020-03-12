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

import org.ballerinalang.model.types.ConstrainedType;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * @since 0.94
 */
public class BMapType extends BBuiltInRefType implements ConstrainedType {

    public BType constraint;

    public BMapType(int tag, BType constraint, BTypeSymbol tsymbol) {
        super(tag, tsymbol);
        this.constraint = constraint;
    }

    @Override
    public BType getConstraint() {
        return constraint;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        if (constraint.tag == TypeTags.ANY) {
            return super.toString();
        }

        return super.toString() + "<" + constraint + ">";
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    public final boolean isAnydata() {
        return this.constraint.isPureType();
    }
}
