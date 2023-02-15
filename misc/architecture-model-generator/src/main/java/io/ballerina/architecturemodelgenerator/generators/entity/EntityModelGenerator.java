/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.architecturemodelgenerator.generators.entity;

import io.ballerina.architecturemodelgenerator.ComponentModel.PackageId;
import io.ballerina.architecturemodelgenerator.ProjectDesignConstants.CardinalityValue;
import io.ballerina.architecturemodelgenerator.generators.GeneratorUtils;
import io.ballerina.architecturemodelgenerator.generators.ModelGenerator;
import io.ballerina.architecturemodelgenerator.generators.entity.nodevisitors.TypeDefinitionNodeVisitor;
import io.ballerina.architecturemodelgenerator.model.ElementLocation;
import io.ballerina.architecturemodelgenerator.model.entity.Association;
import io.ballerina.architecturemodelgenerator.model.entity.Attribute;
import io.ballerina.architecturemodelgenerator.model.entity.Entity;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.NilTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.tools.text.LineRange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.architecturemodelgenerator.ProjectDesignConstants.ARRAY;
import static io.ballerina.architecturemodelgenerator.ProjectDesignConstants.COLON;
import static io.ballerina.architecturemodelgenerator.ProjectDesignConstants.CONSTRAINT_ARRAY;
import static io.ballerina.architecturemodelgenerator.ProjectDesignConstants.CONSTRAINT_KEYWORD;
import static io.ballerina.architecturemodelgenerator.ProjectDesignConstants.FORWARD_SLASH;
import static io.ballerina.architecturemodelgenerator.ProjectDesignConstants.MAX_LENGTH_FIELD;
import static io.ballerina.architecturemodelgenerator.ProjectDesignConstants.MIN_LENGTH_FIELD;

/**
 * Build entity model to represent relationship between records.
 *
 * @since 2201.2.2
 */
public class EntityModelGenerator extends ModelGenerator {

    private final Map<String, Entity> types = new HashMap<>();

    public EntityModelGenerator(PackageCompilation packageCompilation, Module module) {
        super(packageCompilation, module);
    }

    public Map<String, Entity> generate() {
        Map<String, RecordTypeDescriptorNode> recordTypeDescNodes = new HashMap<>();
        for (DocumentId documentId : getModule().documentIds()) {
            SyntaxTree syntaxTree = getModule().document(documentId).syntaxTree();
            TypeDefinitionNodeVisitor typeDefNodeVisitor = new TypeDefinitionNodeVisitor();
            syntaxTree.rootNode().accept(typeDefNodeVisitor);
            recordTypeDescNodes = typeDefNodeVisitor.getRecordTypeDescNodes();
        }

        List<Symbol> symbols = getSemanticModel().moduleSymbols();
        for (Symbol symbol : symbols) {
            if (symbol.kind().equals(SymbolKind.TYPE_DEFINITION)) {
                TypeDefinitionSymbol typeDefinitionSymbol = (TypeDefinitionSymbol) symbol;
                if (typeDefinitionSymbol.typeDescriptor() instanceof RecordTypeSymbol) {
                    String entityName = getEntityName(typeDefinitionSymbol.moduleQualifiedName());
                    RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) typeDefinitionSymbol.typeDescriptor();
                    RecordTypeDescriptorNode recordTypeDescNode = typeDefinitionSymbol.getName().isPresent() ?
                            recordTypeDescNodes.get(typeDefinitionSymbol.getName().get()) : null;
                    this.types.put(entityName, getType(recordTypeSymbol, recordTypeDescNode, entityName,
                            getElementLocation(typeDefinitionSymbol), false));
                }
            }
        }
        return types;
    }

    private Entity getType(RecordTypeSymbol recordTypeSymbol, RecordTypeDescriptorNode recordNode,
                           String entityName, ElementLocation elementLocation, boolean isAnonymous) {
        List<Attribute> attributeList = new ArrayList<>();
        List<String> inclusionList = new ArrayList<>();
        Map<String, RecordFieldSymbol> recordFieldSymbolMap =
                getOriginalFieldMap(recordTypeSymbol, inclusionList, entityName);
        for (RecordFieldSymbol recordFieldSymbol : recordFieldSymbolMap.values()) {
            RecordFieldNode recordFieldNode = null;
            if (recordFieldSymbol.getName().isPresent() && recordNode != null) {
                recordFieldNode = (RecordFieldNode) recordNode.fields().stream().filter(node ->
                        node.kind().equals(SyntaxKind.RECORD_FIELD) &&
                                ((RecordFieldNode) node).fieldName().text().equals(recordFieldSymbol.getName().get()))
                        .findFirst().orElse(null);
            }
            attributeList.add(getAttribute(recordFieldSymbol, recordFieldNode, entityName));
        }
        return new Entity(attributeList, inclusionList, elementLocation, isAnonymous);
    }

    private Attribute getAttribute(RecordFieldSymbol recordFieldSymbol, RecordFieldNode recordFieldNode,
                                   String entityName) {
        TypeDescKind fieldTypeDescKind = recordFieldSymbol.typeDescriptor().typeKind();
        TypeSymbol fieldTypeSymbol = recordFieldSymbol.typeDescriptor();

        String fieldName = recordFieldSymbol.getName().get(); // need to handle
        String fieldType = recordFieldSymbol.typeDescriptor().signature();
        List<Association> associations;
        boolean optional = recordFieldSymbol.isOptional();
        String defaultValue = ""; //need to address
        boolean nillable = isNillable(recordFieldSymbol.typeDescriptor());
        String inlineRecordName = entityName + fieldName.substring(0, 1).toUpperCase(Locale.ROOT) +
                fieldName.substring(1);

        if (fieldTypeDescKind.equals(TypeDescKind.RECORD)) {
            RecordTypeSymbol inlineRecordTypeSymbol = (RecordTypeSymbol) fieldTypeSymbol;
            fieldType = TypeDescKind.RECORD.getName();
            RecordTypeDescriptorNode recordTypeDescNode = (RecordTypeDescriptorNode) recordFieldNode.typeName();
            this.types.put(inlineRecordName, getType(inlineRecordTypeSymbol, recordTypeDescNode, inlineRecordName,
                    getElementLocation(recordFieldSymbol), true));
            String associateCardinality = optional ? CardinalityValue.ZERO_OR_ONE.getValue() :
                    CardinalityValue.ONE_AND_ONLY_ONE.getValue();
            Association association = new Association(inlineRecordName, new Association.Cardinality(
                    CardinalityValue.ONE_AND_ONLY_ONE.getValue(), associateCardinality));
            associations = new LinkedList<>(List.of(association));
        } else if (fieldTypeDescKind.equals(TypeDescKind.ARRAY) &&
                getInlineRecordTypeSymbol((ArrayTypeSymbol) fieldTypeSymbol).isPresent()) {
            RecordTypeSymbol inlineRecordTypeSymbol =
                    getInlineRecordTypeSymbol((ArrayTypeSymbol) fieldTypeSymbol).get();
            int arraySize = ((ArrayTypeDescriptorNode) recordFieldNode.typeName()).dimensions().size();
            fieldType = TypeDescKind.RECORD.getName() + ARRAY.repeat(arraySize);
            RecordTypeDescriptorNode recordTypeDescNode =
                    (RecordTypeDescriptorNode) ((ArrayTypeDescriptorNode) recordFieldNode.typeName()).memberTypeDesc();
            this.types.put(inlineRecordName, getType(inlineRecordTypeSymbol, recordTypeDescNode, inlineRecordName,
                    getElementLocation(recordFieldSymbol), true));
            List<String> associateCardinalities = getArrayAssociateCardinalities(recordFieldNode);
            List<Association> associationsTemp = new LinkedList<>();
            associateCardinalities.forEach(associationCardinality -> {
                Association association = new Association(inlineRecordName, new Association.Cardinality(
                        CardinalityValue.ONE_AND_ONLY_ONE.getValue(), associationCardinality));
                associationsTemp.add(association);
            });
            associations = associationsTemp;
        } else {
            associations = getAssociations(fieldTypeSymbol, recordFieldNode, entityName, optional, nillable);
        }
        // todo: address when union types has anonymous records
        return new Attribute(fieldName, fieldType, optional, nillable, defaultValue, associations,
                getElementLocation(recordFieldSymbol));
    }

    private Optional<RecordTypeSymbol> getInlineRecordTypeSymbol(ArrayTypeSymbol arrayTypeSymbol) {
        if (arrayTypeSymbol.memberTypeDescriptor().typeKind().equals(TypeDescKind.RECORD)) {
            return Optional.of((RecordTypeSymbol) arrayTypeSymbol.memberTypeDescriptor());
        } else if (arrayTypeSymbol.memberTypeDescriptor().typeKind().equals(TypeDescKind.ARRAY)) {
            return getInlineRecordTypeSymbol((ArrayTypeSymbol) arrayTypeSymbol.memberTypeDescriptor());
        }
        return Optional.empty();
    }

    private Map<String, RecordFieldSymbol> getOriginalFieldMap(
            RecordTypeSymbol recordTypeSymbol, List<String> inclusionList, String entityName) {

        Map<String, RecordFieldSymbol> childRecordFieldSymbolMap = recordTypeSymbol.fieldDescriptors();
        if (!recordTypeSymbol.typeInclusions().isEmpty()) {
            List<TypeSymbol> typeInclusions = recordTypeSymbol.typeInclusions();
            for (TypeSymbol includedType : typeInclusions) {
                if (includedType instanceof TypeReferenceTypeSymbol) {
                    TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) includedType;
                    inclusionList.add(getAssociateEntityName(typeReferenceTypeSymbol, entityName));
                    RecordTypeSymbol parentRecordTypeSymbol = (RecordTypeSymbol)
                            typeReferenceTypeSymbol.typeDescriptor();
                    Map<String, RecordFieldSymbol> parentRecordFieldSymbolMap =
                            parentRecordTypeSymbol.fieldDescriptors();
                    // is it enough to check only based on the key ?
                    childRecordFieldSymbolMap = childRecordFieldSymbolMap.entrySet().stream()
                            .filter(entry -> !parentRecordFieldSymbolMap.containsKey(entry.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                }
            }
        }
        return childRecordFieldSymbolMap;
    }

    private boolean isNillable(TypeSymbol fieldTypeDescriptor) {

        boolean isNillable = false;
        if (fieldTypeDescriptor instanceof UnionTypeSymbol) {
            UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) fieldTypeDescriptor;
            List<TypeSymbol> memberTypeDescriptors = unionTypeSymbol.memberTypeDescriptors();
            isNillable = memberTypeDescriptors.stream().anyMatch(m -> m instanceof NilTypeSymbol);
        }
        return isNillable;
    }

    private String getSelfCardinality(TypeSymbol typeSymbol, String entityName) {

        String selfCardinality = CardinalityValue.ONE_AND_ONLY_ONE.getValue();
        if (typeSymbol instanceof TypeReferenceTypeSymbol &&
                ((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor() instanceof RecordTypeSymbol) {
            RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol)
                    (((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor());
            Map<String, RecordFieldSymbol> recordFieldSymbolMap = recordTypeSymbol.fieldDescriptors();
            for (Map.Entry<String, RecordFieldSymbol> fieldEntry : recordFieldSymbolMap.entrySet()) {
                TypeSymbol fieldTypeDescriptor = fieldEntry.getValue().typeDescriptor();
                if (fieldTypeDescriptor instanceof TypeReferenceTypeSymbol) {
                    if (entityName.equals(getAssociateEntityName(
                            (TypeReferenceTypeSymbol) fieldTypeDescriptor, entityName))) {
                        selfCardinality = CardinalityValue.ONE_AND_ONLY_ONE.getValue();
                    }
                } else if (fieldTypeDescriptor instanceof UnionTypeSymbol) {
                    boolean isFound = false;
                    boolean isNull = false;
                    UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) fieldTypeDescriptor;
                    List<TypeSymbol> memberTypeDescriptors = unionTypeSymbol.memberTypeDescriptors();
                    for (TypeSymbol memberTypeSymbol : memberTypeDescriptors) {
                        if (memberTypeSymbol instanceof TypeReferenceTypeSymbol &&
                                entityName.equals(getAssociateEntityName(
                                        (TypeReferenceTypeSymbol) memberTypeSymbol, entityName))) {
                            isFound = true;
                        } else if (memberTypeSymbol instanceof NilTypeSymbol) {
                            isNull = true;
                        }
                    }
                    if (isFound && isNull) {
                        selfCardinality = CardinalityValue.ZERO_OR_ONE.getValue();
                    }
                } else if (fieldTypeDescriptor instanceof ArrayTypeSymbol) {
                    TypeSymbol memberTypeDescriptor = ((ArrayTypeSymbol) fieldTypeDescriptor).memberTypeDescriptor();
                    if (memberTypeDescriptor instanceof TypeReferenceTypeSymbol &&
                            getAssociateEntityName((TypeReferenceTypeSymbol) memberTypeDescriptor, entityName).
                                    replace(ARRAY, "").equals(entityName)) {
                        selfCardinality = CardinalityValue.ZERO_OR_MANY.getValue();
                    }
                }
            }
        }
        return selfCardinality;
    }

    private String getAssociateCardinality(boolean isArray, boolean isOptional, boolean isNillable) {
        // If the associate entity is an array regardless of its value being optional or null, the cardinality is 0-m
        if (isArray) {
            return CardinalityValue.ZERO_OR_MANY.getValue();
        } else {
            // If the associate entity is not an array, the cardinality is 0-1 if the value is optional or nillable as
            // in both cases the value of the associate entity can be empty or present once
            if (isOptional || isNillable) {
                return CardinalityValue.ZERO_OR_ONE.getValue();
            } else {
                return CardinalityValue.ONE_AND_ONLY_ONE.getValue();
            }
        }
    }

    private List<String> getArrayAssociateCardinalities(RecordFieldNode recordFieldNode) {
        List<String> associateCardinalities = new LinkedList<>();
        if (recordFieldNode != null && recordFieldNode.metadata().isPresent()) {
            for (AnnotationNode annotationNode : recordFieldNode.metadata().get().annotations()) {
                if (annotationNode.annotReference().kind().equals(SyntaxKind.QUALIFIED_NAME_REFERENCE)) {
                    QualifiedNameReferenceNode annotRef =  (QualifiedNameReferenceNode) annotationNode.annotReference();
                    if (annotRef.modulePrefix().text().equals(CONSTRAINT_KEYWORD) &&
                            annotRef.identifier().text().equals(CONSTRAINT_ARRAY) &&
                            annotationNode.annotValue().isPresent()) {
                        String minLength = CardinalityValue.ZERO.getValue();
                        String maxLength = CardinalityValue.MANY.getValue();
                        for (MappingFieldNode annotValField : annotationNode.annotValue().get().fields()) {
                            if (((SpecificFieldNode) annotValField).fieldName().kind()
                                    .equals(SyntaxKind.IDENTIFIER_TOKEN) &&
                                    ((SpecificFieldNode) annotValField).valueExpr().isPresent() &&
                                    ((SpecificFieldNode) annotValField).valueExpr().get().kind()
                                            .equals(SyntaxKind.NUMERIC_LITERAL)) {
                                if (((IdentifierToken) ((SpecificFieldNode) annotValField).fieldName()).text()
                                        .equals(MIN_LENGTH_FIELD)) {
                                    minLength = ((BasicLiteralNode) ((SpecificFieldNode) annotValField)
                                            .valueExpr().get()).literalToken().text();
                                } else if (((IdentifierToken) ((SpecificFieldNode) annotValField).fieldName()).text()
                                        .equals(MAX_LENGTH_FIELD)) {
                                    maxLength = ((BasicLiteralNode) ((SpecificFieldNode) annotValField)
                                            .valueExpr().get()).literalToken().text();
                                }
                            }
                        }
                        associateCardinalities.add(CardinalityValue.CUSTOM.getCustomValue(minLength, maxLength));
                    }
                }
            }
        }
        if (associateCardinalities.size() > 0) {
            return associateCardinalities;
        }
        return new LinkedList<>(List.of(CardinalityValue.ZERO_OR_MANY.getValue()));
    }

    /**
     * Build the FQN of the entity Ex: ballerina/reservation_api:0.1.0:Flight.
     *
     * @param moduleQualifiedName
     * @return
     */
    private String getEntityName(String moduleQualifiedName) {
        // moduleQualifiedName is not correct when there is a dot in package name
        PackageId packageId = new PackageId(getModule().packageInstance());
        String entityName;
        String[] nameSpits = moduleQualifiedName.split(COLON);
        if (packageId.getName().equals(nameSpits[0])) { // check whether the referenced type is from the same module
            entityName = packageId.getOrg() + FORWARD_SLASH + packageId.getName() + COLON + packageId.getVersion() +
                    COLON + nameSpits[1];
        } else {
            entityName = packageId.getOrg() + FORWARD_SLASH + packageId.getName() + COLON + nameSpits[0] + COLON +
                    packageId.getVersion() + COLON + nameSpits[1];
        }
        return entityName;
    }

    private List<Association> getAssociationsInUnionTypes(UnionTypeSymbol unionTypeSymbol, String entityName,
                                                          boolean isRequired) {

        List<Association> unionTypeAssociations = new ArrayList<>();
        List<TypeSymbol> memberTypeDescriptors = unionTypeSymbol.memberTypeDescriptors();
        boolean isNullableAssociate = memberTypeDescriptors.stream().anyMatch(m -> m instanceof NilTypeSymbol);
        for (TypeSymbol typeSymbol : memberTypeDescriptors) {
            if (!(typeSymbol instanceof NilTypeSymbol)) {
                List<Association> associations = getAssociations(typeSymbol, null, entityName,
                        isRequired, isNullableAssociate);
                unionTypeAssociations.addAll(associations);
            }
        }
        return unionTypeAssociations;
    }

    private String getAssociateEntityName(TypeReferenceTypeSymbol typeReferenceTypeSymbol,
                                          String referencedPackageName) {

        String referenceType = typeReferenceTypeSymbol.signature();
        if (typeReferenceTypeSymbol.getModule().isPresent() &&
                !referenceType.split(":")[0].equals(referencedPackageName.split(":")[0])) {
            String orgName = typeReferenceTypeSymbol.getModule().get().id().orgName();
            String packageName = typeReferenceTypeSymbol.getModule().get().id().packageName();
            String modulePrefix = typeReferenceTypeSymbol.getModule().get().id().modulePrefix();
            String recordName = typeReferenceTypeSymbol.getName().get();
            String version = typeReferenceTypeSymbol.getModule().get().id().version();
            // module name check
            if (packageName.equals(modulePrefix)) {
                referenceType = String.format("%s/%s:%s:%s", orgName, packageName, version, recordName);
            } else {
                referenceType = String.format(
                        "%s/%s:%s:%s:%s", orgName, packageName, modulePrefix, version, recordName);
            }
        }
        return referenceType;
    }

    private List<Association> getAssociations(TypeSymbol fieldTypeDescriptor, RecordFieldNode recordFieldNode,
                                              String entityName, boolean optional, boolean isNillable) {

        List<Association> associations = new ArrayList<>();
        if (fieldTypeDescriptor instanceof TypeReferenceTypeSymbol) {
            String associate = getAssociateEntityName((TypeReferenceTypeSymbol) fieldTypeDescriptor, entityName);
            Association.Cardinality cardinality = new Association.Cardinality(
                    getSelfCardinality(fieldTypeDescriptor, entityName),
                    getAssociateCardinality(false, optional, isNillable));

            associations.add(new Association(associate, cardinality));
        } else if (fieldTypeDescriptor instanceof UnionTypeSymbol) {
            UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) fieldTypeDescriptor;
            associations.addAll(getAssociationsInUnionTypes(unionTypeSymbol, entityName, optional));
        } else if (fieldTypeDescriptor instanceof ArrayTypeSymbol) {
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) fieldTypeDescriptor;
            if (arrayTypeSymbol.memberTypeDescriptor() instanceof TypeReferenceTypeSymbol) {
                String associate = getAssociateEntityName((TypeReferenceTypeSymbol)
                        arrayTypeSymbol.memberTypeDescriptor(), entityName).replace(ARRAY, "");
                List<String> associateCardinalities = getArrayAssociateCardinalities(recordFieldNode);
                associateCardinalities.forEach(associationCardinality -> {
                    Association.Cardinality cardinality = new Association.Cardinality(
                            getSelfCardinality(arrayTypeSymbol, entityName),
                            associationCardinality);
                    associations.add(new Association(associate, cardinality));
                });
            } else {
                associations.addAll(getAssociations(arrayTypeSymbol.memberTypeDescriptor(), recordFieldNode,
                        entityName, optional, isNillable));
            }
        }
        return associations;
    }

    private ElementLocation getElementLocation(Symbol symbol) {
        ElementLocation elementLocation = null;
        if (symbol.getLocation().isPresent()) {
            LineRange typeLineRange = symbol.getLocation().get().lineRange();
            String filePath = getModuleRootPath().resolve(typeLineRange.filePath()).toAbsolutePath().toString();
            elementLocation = GeneratorUtils.getElementLocation(filePath, typeLineRange);
        }
        return elementLocation;
    }
}
