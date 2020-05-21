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
package org.ballerina.compiler.api.semantic;

import org.ballerina.compiler.api.symbol.ModuleID;
import org.ballerina.compiler.api.types.ArrayTypeDescriptor;
import org.ballerina.compiler.api.types.BuiltinTypeDescriptor;
import org.ballerina.compiler.api.types.ErrorTypeDescriptor;
import org.ballerina.compiler.api.types.FunctionTypeDescriptor;
import org.ballerina.compiler.api.types.FutureTypeDescriptor;
import org.ballerina.compiler.api.types.MapTypeDescriptor;
import org.ballerina.compiler.api.types.NilTypeDescriptor;
import org.ballerina.compiler.api.types.ObjectTypeDescriptor;
import org.ballerina.compiler.api.types.RecordTypeDescriptor;
import org.ballerina.compiler.api.types.StreamTypeDescriptor;
import org.ballerina.compiler.api.types.TupleTypeDescriptor;
import org.ballerina.compiler.api.types.TypeDescTypeDescriptor;
import org.ballerina.compiler.api.types.TypeDescriptor;
import org.ballerina.compiler.api.types.TypeReferenceTypeDescriptor;
import org.ballerina.compiler.api.types.UnionTypeDescriptor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.util.Flags;

/**
 * Represents a set of factory methods to generate the {@link org.ballerina.compiler.api.types.TypeDescriptor}s.
 *
 * @since 1.3.0
 */
public class TypesFactory {

    /**
     * Get the type descriptor for the given type.
     *
     * @param bType BType tp get the type descriptor
     * @param rawTypeOnly Whether convert the type descriptor to type reference or keep the raw type
     * @return {@link TypeDescriptor} generated
     */
    public static TypeDescriptor getTypeDescriptor(BType bType, boolean rawTypeOnly) {
        TypeDescriptor typeDescriptor;
        if (bType == null) {
            return null;
        }
        ModuleID moduleID = bType.tsymbol == null ? null : new ModuleID(bType.tsymbol.pkgID);
        switch (bType.getKind()) {
            case OBJECT:
                typeDescriptor = new ObjectTypeDescriptor(moduleID, (BObjectType) bType);
                break;
            case RECORD:
                typeDescriptor = new RecordTypeDescriptor(moduleID, (BRecordType) bType);
                break;
            case ERROR:
                typeDescriptor = new ErrorTypeDescriptor(moduleID, (BErrorType) bType);
                break;
            case UNION:
                typeDescriptor = new UnionTypeDescriptor(moduleID, (BUnionType) bType);
                break;
            case FUTURE:
                typeDescriptor = new FutureTypeDescriptor(moduleID, (BFutureType) bType);
                break;
            case MAP:
                typeDescriptor = new MapTypeDescriptor(moduleID, (BMapType) bType);
                break;
            case STREAM:
                typeDescriptor = new StreamTypeDescriptor(moduleID, (BStreamType) bType);
                break;
            case ARRAY:
                typeDescriptor = new ArrayTypeDescriptor(moduleID, (BArrayType) bType);
                break;
            case TUPLE:
                typeDescriptor = new TupleTypeDescriptor(moduleID, (BTupleType) bType);
                break;
            case TYPEDESC:
                typeDescriptor = new TypeDescTypeDescriptor(moduleID, (BTypedescType) bType);
                break;
            case NIL:
                return new NilTypeDescriptor(moduleID, (BNilType) bType);
            case OTHER:
                if (bType instanceof BInvokableType) {
                    typeDescriptor = new FunctionTypeDescriptor(moduleID, (BInvokableType) bType);
                } else {
                    String name = bType.getKind().typeName();
                    typeDescriptor = new BuiltinTypeDescriptor(moduleID, name, bType);
                }
                break;
            default:
                String name = bType.getKind().typeName();
                return new BuiltinTypeDescriptor(moduleID, name, bType);
        }

        if (!rawTypeOnly && bType.tsymbol != null && ((bType.tsymbol.flags & Flags.ANONYMOUS) != Flags.ANONYMOUS)
                && !bType.tsymbol.getName().getValue().isEmpty()) {
            typeDescriptor = new TypeReferenceTypeDescriptor(moduleID, bType, bType.tsymbol.getName().getValue());
        }
        return typeDescriptor;
    }

    public static TypeDescriptor getTypeDescriptor(BType bType) {
        return getTypeDescriptor(bType, false);
    }
}
