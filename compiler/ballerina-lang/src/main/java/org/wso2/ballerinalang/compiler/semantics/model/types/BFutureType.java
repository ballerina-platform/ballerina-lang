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

import io.ballerina.types.Env;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import org.ballerinalang.model.types.ConstrainedType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * Represents an future type.
 *
 * @since 0.965.0
 */
public class BFutureType extends BType implements ConstrainedType {

    public BType constraint;
    public boolean workerDerivative;
    private final Env env;

    public BFutureType(Env env, BType constraint) {
        super(TypeTags.FUTURE, null);
        this.constraint = constraint;
        this.env = env;
    }

    public BFutureType(Env env, BType constraint, boolean workerDerivative) {
        this(env, constraint);
        this.workerDerivative = workerDerivative;
    }

    public BFutureType(Env env, BType constraint, SemType semType) {
        this(env, constraint);
        this.semType = semType;
    }

    @Override
    public BType getConstraint() {
        return constraint;
    }

    public void setConstraint(BType constraint) {
        this.constraint = constraint;
        this.semType = null;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.FUTURE;
    }

    @Override
    public String toString() {
        if (constraint.tag == TypeTags.NONE || constraint.tag == TypeTags.SEMANTIC_ERROR
                || constraint.tag == TypeTags.NIL) {
            return super.toString();
        }

        return super.toString() + "<" + constraint + ">";
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public SemType semType() {
        if (this.semType != null) {
            return this.semType;
        }

        if (constraint == null || constraint instanceof BNoType) {
            this.semType = PredefinedType.FUTURE;
        } else {
            this.semType = SemTypes.futureContaining(env, constraint.semType());
        }
        return this.semType;
    }
}
