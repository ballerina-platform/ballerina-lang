/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.bir;

import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.UsedState;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnnotationType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BHandleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNeverType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BPackageType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BParameterizedType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Analyzes the BIRTypeDefinition nodes and generate the dependency graph for type definitions
 * TODO optimize the algorithm to only traverse one type dependency chain once. Right now it traverses the same chain multiple times
 *
 */
public class TypeDefAnalyzer implements TypeVisitor {

    private static final CompilerContext.Key<TypeDefAnalyzer> BIR_TYPE_DEF_ANALYZER_KEY = new CompilerContext.Key<>();
    public final HashMap<BType, BIRNode.BIRTypeDefinition> typeDefPool = new HashMap<>();
    // The parent node could be either a function or a typeDef
    private BIRNode.BIRDocumentableNode currentParentNode;
    private static boolean currentTypeIsNotVisited = true;
    public final HashSet<BType> visitedTypes = new HashSet<>();

    private TypeDefAnalyzer(CompilerContext context) {
        context.put(BIR_TYPE_DEF_ANALYZER_KEY, this);
    }

    public static TypeDefAnalyzer getInstance(CompilerContext context) {
        TypeDefAnalyzer typeDefAnalyzer = context.get(BIR_TYPE_DEF_ANALYZER_KEY);
        if (typeDefAnalyzer == null) {
            typeDefAnalyzer = new TypeDefAnalyzer(context);
        }
        return typeDefAnalyzer;
    }

//    //TODO make this analyze only one typeDef at a time
//    public void analyze(BIRNode.BIRPackage birPackage) {
//        populateTypeDefPool(birPackage);
//        birPackage.typeDefs.forEach(typeDef -> {
//            currentParentNode = typeDef;
//            typeDef.type.accept(this);
//            if (typeDef.usedState == UsedState.UNEXPOLORED) {
//                typeDef.usedState = UsedState.UNUSED;
//            }
//        });
//    }

    public void populateTypeDefPool(BIRNode.BIRPackage birPackage) {
        birPackage.typeDefs.forEach(typeDef -> typeDefPool.putIfAbsent(typeDef.type, typeDef));
    }

    public void analyzeTypeDef(BIRNode.BIRTypeDefinition typeDef) {
        typeDefPool.putIfAbsent(typeDef.type, typeDef);
        currentParentNode = typeDef;
        typeDef.type.accept(this);
        if (typeDef.referencedTypes != null) {
            typeDef.referencedTypes.forEach(this::accept);
        }

        if (typeDef.usedState == UsedState.UNEXPOLORED) {
            typeDef.usedState = UsedState.UNUSED;
        }
    }

    public void analyzeFunctionLevelTypeDef(BType bType, BIRNode.BIRFunction parentFunction) {
        currentParentNode = parentFunction;
        bType.accept(this);
    }

    public void addParamTypeDefsAsChildren(BIRNode.BIRFunction externalFunction) {
        currentParentNode = externalFunction;
        externalFunction.parameters.forEach(birFunctionParameter -> {
            birFunctionParameter.type.accept(this);
        });
    }

    public void analyzeServiceDecl(BIRNode.BIRServiceDeclaration serviceDecl) {
        currentParentNode = serviceDecl;
        serviceDecl.listenerTypes.forEach(type -> type.accept(this));
    }

    private void addDependency(BType bType) {
        currentTypeIsNotVisited = visitedTypes.add(bType);
        BIRNode.BIRTypeDefinition childNode = getBIRTypeDef(bType);
        // TODO Check recursive self calls in typeDefs
        if (childNode == null) {
            return;
        }
        if (childNode.usedState == UsedState.UNEXPOLORED) {
            childNode.usedState = UsedState.UNUSED;
        }
        if (currentParentNode == childNode) {
            return;
        }
        currentParentNode.addChildNode(childNode);
        currentParentNode = childNode;
    }

    private BIRNode.BIRTypeDefinition getBIRTypeDef(BType bType) {
        if (typeDefPool.containsKey(bType)) {
            return typeDefPool.get(bType);
        }
        return null;
    }

    private boolean isAlreadyTraversed(BType bType) {
        BIRNode.BIRTypeDefinition birTypeDef = getBIRTypeDef(bType);
        if (birTypeDef == null) {
            return false;
        }
        return birTypeDef.usedState != UsedState.UNEXPOLORED;
    }

    @Override
    public void visit(BAnnotationType bAnnotationType) {
        addDependency(bAnnotationType);
    }

    @Override
    public void visit(BBuiltInRefType bBuiltInRefType) {
        addDependency(bBuiltInRefType);
    }

    @Override
    public void visit(BAnyType bAnyType) {
        addDependency(bAnyType);
    }

    @Override
    public void visit(BAnydataType bAnydataType) {
        addDependency(bAnydataType);
    }

    @Override
    public void visit(BErrorType bErrorType) {
        bErrorType.detailType.accept(this);
        addDependency(bErrorType);
    }

    @Override
    public void visit(BFiniteType bFiniteType) {
        addDependency(bFiniteType);
    }

    @Override
    public void visit(BInvokableType bInvokableType) {
        addDependency(bInvokableType);
    }

    @Override
    public void visit(BJSONType bjsonType) {
        addDependency(bjsonType);
    }

    @Override
    public void visit(BMapType bMapType) {
        if (bMapType.mutableType != null) {
            bMapType.mutableType.accept(this);
        }
        if (bMapType.constraint != null) {
            bMapType.constraint.accept(this);
        }
        addDependency(bMapType);
    }

    @Override
    public void visit(BStreamType bStreamType) {
        addDependency(bStreamType);
    }

    @Override
    public void visit(BTypedescType bTypedescType) {
        addDependency(bTypedescType);
    }

    @Override
    public void visit(BParameterizedType bTypedescType) {
        addDependency(bTypedescType);
    }

    @Override
    public void visit(BNeverType bNeverType) {
        addDependency(bNeverType);
    }

    @Override
    public void visit(BNilType bNilType) {
        addDependency(bNilType);
    }

    @Override
    public void visit(BNoType bNoType) {
        addDependency(bNoType);
    }

    @Override
    public void visit(BPackageType bPackageType) {
        addDependency(bPackageType);
    }

    @Override
    public void visit(BXMLType bxmlType) {
        addDependency(bxmlType);
    }

    @Override
    public void visit(BTableType bTableType) {
        addDependency(bTableType);
    }

    @Override
    public void visit(BType bType) {
        addDependency(bType);
    }

    @Override
    public void visit(BFutureType bFutureType) {
        addDependency(bFutureType);
    }

    @Override
    public void visit(BHandleType bHandleType) {
        addDependency(bHandleType);
    }

    @Override
    public void visit(BTypeReferenceType bTypeReferenceType) {
        addDependency(bTypeReferenceType);
        // Eliminating infinite loops caused by cyclic types
        BIRNode.BIRTypeDefinition referredTypeDef = getBIRTypeDef(bTypeReferenceType.referredType);
        if (referredTypeDef != null) {
            currentParentNode.addChildNode(referredTypeDef);
            if (referredTypeDef.usedState != UsedState.UNEXPOLORED) {
                return;
            }
        }
        bTypeReferenceType.referredType.accept(this);
    }

    @Override
    public void visit(BStructureType bStructureType) {
        addDependency(bStructureType);
        if (currentTypeIsNotVisited) {
            bStructureType.fields.values().forEach(bType -> bType.type.accept(this));
        }
    }

    @Override
    public void visit(BTupleType bTupleType) {
        addDependency(bTupleType);
        if (currentTypeIsNotVisited) {
            bTupleType.getMembers().forEach(bTupleMember -> bTupleMember.type.accept(this));
        }
    }

    @Override
    public void visit(BUnionType bUnionType) {
        addDependency(bUnionType);
        if (currentTypeIsNotVisited) {
            bUnionType.getOriginalMemberTypes().forEach(member -> member.accept(this));
        }
//        bUnionType.getMemberTypes().forEach(member -> member.accept(this));
    }

    @Override
    public void visit(BIntersectionType bIntersectionType) {
        addDependency(bIntersectionType);
        bIntersectionType.getEffectiveType().accept(this);
        bIntersectionType.getConstituentTypes().forEach(member -> member.accept(this));
//        if (currentTypeIsNotVisited) {
//        }

    }

    @Override
    public void visit(BArrayType bArrayType) {
        addDependency(bArrayType);
        if (currentTypeIsNotVisited) {
            bArrayType.eType.accept(this);
        }
    }

    @Override
    public void visit(BRecordType bRecordType) {
        addDependency(bRecordType);
        // TODO check whether the following foreach is correct
        if (currentTypeIsNotVisited) {
            if (bRecordType.mutableType != null) {
                bRecordType.mutableType.accept(this);
            }
            if (bRecordType.restFieldType != null) {
                bRecordType.restFieldType.accept(this);
            }
            bRecordType.fields.values().forEach(bType -> bType.type.accept(this));
        }
    }

    @Override
    public void visit(BObjectType bObjectType) {
        addDependency(bObjectType);
        if (currentTypeIsNotVisited) {
            if (bObjectType.mutableType != null) {
                bObjectType.mutableType.accept(this);
            }
            bObjectType.fields.values().forEach(bType -> bType.type.accept(this));
        }
    }

    private void accept(BType type) {
        type.accept(this);
    }
}
