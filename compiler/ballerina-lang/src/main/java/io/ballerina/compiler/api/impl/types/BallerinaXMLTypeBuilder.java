/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api.impl.types;

import io.ballerina.compiler.api.impl.symbols.AbstractTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The implementation of the methods used to build the XML type descriptor.
 *
 * @since 2201.2.0
 */
public class BallerinaXMLTypeBuilder implements TypeBuilder.XML {

    private final TypesFactory typesFactory;
    private final SymbolTable symTable;
    private TypeSymbol typeParam;

    private final Map<String, Boolean> checkedTypeRefs = new HashMap<>();
    private final Set<String> visitedTypeRefs = new HashSet<>();
    private final Map<String, Boolean> checkedUnionTypes = new HashMap<>();
    private final Set<String> visitedUnionTypes = new HashSet<>();

    public BallerinaXMLTypeBuilder(CompilerContext context) {
        typesFactory = TypesFactory.getInstance(context);
        symTable = SymbolTable.getInstance(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TypeBuilder.XML withTypeParam(TypeSymbol typeParam) {
        this.typeParam = typeParam;

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLTypeSymbol build() {
        BXMLType xmlType = new BXMLType(getBType(typeParam), symTable.xmlType.tsymbol);
        XMLTypeSymbol xmlTypeSymbol = (XMLTypeSymbol) typesFactory.getTypeDescriptor(xmlType);
        this.typeParam = null;

        return xmlTypeSymbol;
    }

    private BType getBType(TypeSymbol typeSymbol) {
        if (typeSymbol != null) {
            if (isValidTypeParam(typeSymbol)) {
                return ((AbstractTypeSymbol) typeSymbol).getBType();
            }

            throw new IllegalArgumentException("Valid XML type param should be provided");
        }

        return null;
    }

    private boolean isValidTypeParam(TypeSymbol typeSymbol) {
        return isValidXMLSubType(typeSymbol) ||
                (typeSymbol.typeKind() == TypeDescKind.TYPE_REFERENCE
                        && isValidTypeReference((TypeReferenceTypeSymbol) typeSymbol)) ||
                (typeSymbol.typeKind() == TypeDescKind.UNION && isValidUnionType((UnionTypeSymbol) typeSymbol));
    }

    private boolean isValidTypeReference(TypeReferenceTypeSymbol typeSymbol) {
        if (visitedTypeRefs.contains(typeSymbol.signature())) {
            throw new IllegalArgumentException("Type parameter " + typeSymbol.signature()
                    + " contains cyclic dependencies");
        }


        Boolean checkedTypeRef = checkedTypeRefs.get(typeSymbol.signature());
        if (checkedTypeRef != null) {
            return checkedTypeRef;
        }

        visitedTypeRefs.add(typeSymbol.signature());
        boolean isValidTypeParam = isValidTypeParam(typeSymbol.typeDescriptor());
        visitedTypeRefs.remove(typeSymbol.signature());
        checkedTypeRefs.put(typeSymbol.signature(), isValidTypeParam);

        return isValidTypeParam;
    }

    private boolean isValidUnionType(UnionTypeSymbol typeSymbol) {
        if (visitedUnionTypes.contains(typeSymbol.signature())) {
            throw new IllegalArgumentException("Type parameter " + typeSymbol.signature()
                    + " contains cyclic dependencies");
        }

        Boolean checkedUnionType = checkedUnionTypes.get(typeSymbol.signature());
        if (checkedUnionType != null) {
            return checkedUnionType;
        }

        visitedUnionTypes.add(typeSymbol.signature());
        boolean hasValidMembers = true;

        for (TypeSymbol memberTypeDescriptor : typeSymbol.memberTypeDescriptors()) {
            if (!isValidTypeParam(memberTypeDescriptor)) {
                hasValidMembers = false;
            }
        }

        visitedUnionTypes.remove(typeSymbol.signature());
        checkedUnionTypes.put(typeSymbol.signature(), hasValidMembers);

        return hasValidMembers;
    }

    private boolean isValidXMLSubType(TypeSymbol typeSymbol) {
        TypeDescKind typeKind = typeSymbol.typeKind();

        return typeKind == TypeDescKind.XML || typeKind == TypeDescKind.XML_ELEMENT || typeKind == TypeDescKind.XML_TEXT
                || typeKind == TypeDescKind.XML_PROCESSING_INSTRUCTION || typeKind == TypeDescKind.XML_COMMENT;
    }
}
