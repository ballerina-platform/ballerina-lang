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
package org.ballerina.compiler.impl.semantic;

import org.ballerina.compiler.api.element.ModuleID;
import org.ballerina.compiler.api.type.BallerinaTypeDescriptor;
import org.ballerina.compiler.impl.types.ArrayTypeDescriptor;
import org.ballerina.compiler.impl.types.BuiltinTypeDescriptor;
import org.ballerina.compiler.impl.types.ErrorTypeDescriptor;
import org.ballerina.compiler.impl.types.FunctionTypeDescriptor;
import org.ballerina.compiler.impl.types.FutureTypeDescriptor;
import org.ballerina.compiler.impl.types.MapTypeDescriptor;
import org.ballerina.compiler.impl.types.NilTypeDescriptor;
import org.ballerina.compiler.impl.types.ObjectTypeDescriptor;
import org.ballerina.compiler.impl.types.RecordTypeDescriptor;
import org.ballerina.compiler.impl.types.StreamTypeDescriptor;
import org.ballerina.compiler.impl.types.TupleTypeDescriptor;
import org.ballerina.compiler.impl.types.TypeDescTypeDescriptor;
import org.ballerina.compiler.impl.types.TypeReferenceTypeDescriptor;
import org.ballerina.compiler.impl.types.UnionTypeDescriptor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
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
 * Represents a set of factory methods to generate the {@link BallerinaTypeDescriptor}s.
 *
 * @since 1.3.0
 */
public class TypesFactory {

    /**
     * Get the type descriptor for the given type.
     *
     * @param bType BType tp get the type descriptor
     * @param rawTypeOnly Whether convert the type descriptor to type reference or keep the raw type
     * @return {@link BallerinaTypeDescriptor} generated
     */
    public static BallerinaTypeDescriptor getTypeDescriptor(BType bType, boolean rawTypeOnly) {
        BallerinaTypeDescriptor typeDescriptorImpl;
        if (bType == null) {
            return null;
        }
        ModuleID moduleID = bType.tsymbol == null ? null : new ModuleID(bType.tsymbol.pkgID);
        switch (bType.getKind()) {
            case OBJECT:
                typeDescriptorImpl = new ObjectTypeDescriptor(moduleID, (BObjectType) bType);
                break;
            case RECORD:
                typeDescriptorImpl = new RecordTypeDescriptor(moduleID, (BRecordType) bType);
                break;
            case ERROR:
                typeDescriptorImpl = new ErrorTypeDescriptor(moduleID, (BErrorType) bType);
                break;
            case UNION:
                typeDescriptorImpl = new UnionTypeDescriptor(moduleID, (BUnionType) bType);
                break;
            case FUTURE:
                typeDescriptorImpl = new FutureTypeDescriptor(moduleID, (BFutureType) bType);
                break;
            case MAP:
                typeDescriptorImpl = new MapTypeDescriptor(moduleID, (BMapType) bType);
                break;
            case STREAM:
                typeDescriptorImpl = new StreamTypeDescriptor(moduleID, (BStreamType) bType);
                break;
            case ARRAY:
                typeDescriptorImpl = new ArrayTypeDescriptor(moduleID, (BArrayType) bType);
                break;
            case TUPLE:
                typeDescriptorImpl = new TupleTypeDescriptor(moduleID, (BTupleType) bType);
                break;
            case TYPEDESC:
                typeDescriptorImpl = new TypeDescTypeDescriptor(moduleID, (BTypedescType) bType);
                break;
            case NIL:
                return new NilTypeDescriptor(moduleID, (BNilType) bType);
            case OTHER:
                if (bType instanceof BInvokableType) {
                    typeDescriptorImpl = new FunctionTypeDescriptor(moduleID, (BInvokableTypeSymbol) bType.tsymbol);
                } else {
                    String name = bType.getKind().typeName();
                    typeDescriptorImpl = new BuiltinTypeDescriptor(moduleID, name, bType);
                }
                break;
            default:
                String name = bType.getKind().typeName();
                return new BuiltinTypeDescriptor(moduleID, name, bType);
        }

        if (!rawTypeOnly && bType.tsymbol != null && ((bType.tsymbol.flags & Flags.ANONYMOUS) != Flags.ANONYMOUS)
                && !bType.tsymbol.getName().getValue().isEmpty()) {
            typeDescriptorImpl = new TypeReferenceTypeDescriptor(moduleID, bType, bType.tsymbol.getName().getValue());
        }
        return typeDescriptorImpl;
    }

    public static BallerinaTypeDescriptor getTypeDescriptor(BType bType) {
        return getTypeDescriptor(bType, false);
    }
}
