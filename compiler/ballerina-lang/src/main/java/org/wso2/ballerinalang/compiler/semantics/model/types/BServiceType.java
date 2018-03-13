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

import org.ballerinalang.model.types.ServiceType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * {@code {@link BStructType}} represents the type of a service in Ballerina.
 *
 * @since 0.965.0
 */
public class BServiceType extends BType implements ServiceType {

//    TODO : Fix me.
//    public BType endpointType;

    public BServiceType(BTypeSymbol tsymbol) {
        super(TypeTags.SERVICE, tsymbol);
    }

    public String getDesc() {
        return TypeDescriptor.SIG_SERVICE + getQualifiedTypeName() + ";";
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.SERVICE;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        return Names.DEFAULT_PACKAGE.equals(tsymbol.pkgID.name) ? tsymbol.name.value : getQualifiedTypeName();
    }
}
