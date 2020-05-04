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

import org.ballerina.compiler.api.model.BallerinaField;
import org.ballerina.compiler.api.types.BuiltinTypeDescriptor;
import org.ballerina.compiler.api.types.ErrorTypeDescriptor;
import org.ballerina.compiler.api.types.FunctionTypeDescriptor;
import org.ballerina.compiler.api.types.NilTypeDescriptor;
import org.ballerina.compiler.api.types.ObjectTypeDescriptor;
import org.ballerina.compiler.api.types.RecordTypeDescriptor;
import org.ballerina.compiler.api.types.TypeDescKind;
import org.ballerina.compiler.api.types.TypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.util.Flags;

/** Represents a set of factory methods to generate the {@link org.ballerina.compiler.api.types.TypeDescriptor}s.
 *
 * @since 1.3.0
 */
public class TypesFactory {

    /**
     * Get the type descriptor for the given type.
     * 
     * @param bType BType tp get the type descriptor
     * @return {@link TypeDescriptor} generated
     */
    public static TypeDescriptor getTypeDescriptor(BType bType) {
        switch (bType.getKind()) {
            case OBJECT:
                return createObjectTypeDescriptor((BObjectType) bType);
            case RECORD:
                return createRecordTypeDescriptor((BRecordType) bType);
            case FUNCTION:
                return createFunctionTypeDescriptor((BInvokableType) bType);
            case ERROR:
                if (bType.tsymbol != null && "error".equals(bType.tsymbol.name.getValue())) {
                    return createBuiltinTypeDesc(bType);
                }
                return createErrorTypeDescriptor((BErrorType) bType);
            case ANNOTATION:
                return null;
            case NIL:
                return createNilTypeDesc(bType);
            default:
                return createBuiltinTypeDesc(bType);
        }
    }

    /**
     * Creates an Object Type Descriptor.
     * 
     * @param objectType BObjectType to convert
     * @return {@link ObjectTypeDescriptor} generated
     */
    public static ObjectTypeDescriptor createObjectTypeDescriptor(BObjectType objectType) {
        PackageID pkgID = objectType.tsymbol.pkgID;
        ObjectTypeDescriptor.ObjectTypeBuilder objectTypeBuilder
                = new ObjectTypeDescriptor.ObjectTypeBuilder(TypeDescKind.OBJECT, pkgID);
        
        objectType.fields.forEach(bField -> objectTypeBuilder.withObjectField(new BallerinaField(bField)));
        if ((objectType.flags & Flags.ABSTRACT) == Flags.ABSTRACT) {
            objectTypeBuilder.withTypeQualifier(ObjectTypeDescriptor.TypeQualifier.ABSTRACT);
        }
        if ((objectType.flags & Flags.CLIENT) == Flags.CLIENT) {
            objectTypeBuilder.withTypeQualifier(ObjectTypeDescriptor.TypeQualifier.CLIENT);
        }
        if ((objectType.flags & Flags.LISTENER) == Flags.LISTENER) {
            objectTypeBuilder.withTypeQualifier(ObjectTypeDescriptor.TypeQualifier.CLIENT);
        }
        objectTypeBuilder.withSymbol((BObjectTypeSymbol) objectType.tsymbol);
        
        return objectTypeBuilder.build();
    }
    
    /**
     * Creates an Object Type Descriptor.
     *
     * @param recordType BRecordType to convert
     * @return {@link ObjectTypeDescriptor} generated
     */
    public static RecordTypeDescriptor createRecordTypeDescriptor(BRecordType recordType) {
        PackageID pkgID = recordType.tsymbol.pkgID;
        RecordTypeDescriptor.RecordTypeBuilder recordTypeBuilder =
                new RecordTypeDescriptor.RecordTypeBuilder(TypeDescKind.RECORD, pkgID, !recordType.sealed);
        recordType.fields.forEach(bField -> recordTypeBuilder.withFieldTypeDesc(new BallerinaField(bField)));
        // TODO: Figure out the type reference
        recordTypeBuilder.withRestTypeDesc(getTypeDescriptor(recordType.restFieldType));
        
        return recordTypeBuilder.build();
    }
    
    public static FunctionTypeDescriptor createFunctionTypeDescriptor(BInvokableType invokableType) {
        PackageID pkgID = invokableType.tsymbol.pkgID;
        FunctionTypeDescriptor.FunctionTypeBuilder functionTypeBuilder
                = new FunctionTypeDescriptor.FunctionTypeBuilder(TypeDescKind.FUNCTION, pkgID);
        return functionTypeBuilder.build();
    }
    
    public static ErrorTypeDescriptor createErrorTypeDescriptor(BErrorType errorType) {
        PackageID pkgID = errorType.tsymbol.pkgID;
        ErrorTypeDescriptor.ErrorTypeBuilder errorTypeBuilder
                = new ErrorTypeDescriptor.ErrorTypeBuilder(pkgID, (BErrorTypeSymbol) errorType.tsymbol);
        return errorTypeBuilder.build();
    }

    /**
     * Create a builtin type descriptor.
     * 
     * @param bType {@link BType} to convert
     * @return {@link BuiltinTypeDescriptor} generated
     */
    public static BuiltinTypeDescriptor createBuiltinTypeDesc(BType bType) {
        if (bType.tsymbol == null || bType.tsymbol.name.getValue().isEmpty()) {
            return null;
        }
        return new BuiltinTypeDescriptor
                .BuiltinTypeBuilder(bType.tsymbol.pkgID, bType.tsymbol)
                .build();
    }

    /**
     * Create a builtin type descriptor.
     * 
     * @param bType {@link BType} to convert
     * @return {@link BuiltinTypeDescriptor} generated
     */
    public static NilTypeDescriptor createNilTypeDesc(BType bType) {
        return new NilTypeDescriptor.NilTypeBuilder(bType.tsymbol.pkgID).build();
    }
}
