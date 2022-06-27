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

import org.ballerinalang.model.Name;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.Flags;

/**
 * Represents Builtin Subtype of String.
 *
 * @since 1.2.0
 */
public class BStringSubType extends BType {

    public BStringSubType(int tag, Name name) {

        super(tag, null, name, Flags.READONLY);
    }

    public boolean isNullable() {

        return false;
    }

    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {

        return visitor.visit(this, t);
    }

    @Override
    public TypeKind getKind() {

        return TypeKind.STRING;
    }

    @Override
    public void accept(TypeVisitor visitor) {

        visitor.visit(this);
    }

    @Override
    public String toString() {

        return Names.STRING.value + Names.ALIAS_SEPARATOR + name;
    }

    public String getQualifiedTypeName() {

        return Names.BALLERINA_ORG.value + Names.ORG_NAME_SEPARATOR.value
                + Names.LANG.value + Names.DOT.value + Names.STRING.value + Names.ALIAS_SEPARATOR + name;
    }

    public boolean isAnydata() {

        return true;
    }

    public boolean isPureType() {

        return true;
    }
}
