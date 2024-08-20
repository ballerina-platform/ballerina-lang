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

import io.ballerina.types.Env;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.definition.StreamDefinition;
import org.ballerinalang.model.types.StreamType;
import org.ballerinalang.model.types.Type;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemTypeHelper;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * {@code BStreamType} represents stream data in Ballerina.
 *
 * @since 1.2.0
 */
public class BStreamType extends BBuiltInRefType implements StreamType {

    public BType constraint;
    public BType completionType;

    public final Env env;
    private StreamDefinition d = null;

    public BStreamType(Env env, int tag, BType constraint, BType completionType, BTypeSymbol tsymbol) {
        super(tag, tsymbol);
        this.constraint = constraint;
        this.completionType = completionType != null ? completionType : BType.createNilType();
        this.env = env;
    }

    @Override
    public BType getConstraint() {
        return constraint;
    }

    @Override
    public Type getCompletionType() {
        return completionType;
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

        return super.toString() + "<" + constraint + ((completionType.tag == TypeTags.NIL)
                ? ">" : "," + completionType + ">");
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public SemType semType() {
        if (constraint == null || constraint instanceof BNoType) {
            return PredefinedType.STREAM;
        }

        if (d != null) {
            return d.getSemType(env);
        }

        d = new StreamDefinition();
        SemType valueTy = SemTypeHelper.semType(constraint);
        SemType completionTy = SemTypeHelper.semType(completionType);
        return d.define(env, valueTy, completionTy);
    }
}
