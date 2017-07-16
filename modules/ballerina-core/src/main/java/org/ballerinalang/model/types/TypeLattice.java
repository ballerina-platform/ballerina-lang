/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model.types;

import org.ballerinalang.bre.StructVarLocation;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.util.codegen.InstructionCodes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * Class to hold the types and their connections within ballerina.
 */
public class TypeLattice {

    private static final boolean SAFE = true;
    private static final boolean UNSAFE = false;

    protected final HashMap<String, TypeVertex> vertices = new HashMap<>();
    protected final HashMap<Integer, TypeEdge> edges = new HashMap<>();
    private static TypeLattice explicitCastLattice = new TypeLattice();
    private static TypeLattice implicitCastLattice = new TypeLattice();
    private static TypeLattice conversionLattice = new TypeLattice();

    public static TypeLattice getExplicitCastLattice() {
        return explicitCastLattice;
    }

    public static TypeLattice getImplicitCastLattice() {
        return implicitCastLattice;
    }

    public static TypeLattice getTransformLattice() {
        return conversionLattice;
    }

    public static void loadImplicitCastLattice(SymbolScope scope) {

        TypeVertex intV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.INT_TNAME)));
        TypeVertex floatV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.FLOAT_TNAME)));
        TypeVertex stringV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.STRING_TNAME)));
        TypeVertex booleanV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.BOOLEAN_TNAME)));
        TypeVertex blobV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.BLOB_TNAME)));
        TypeVertex jsonV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.JSON_TNAME)));
        TypeVertex anyV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.ANY_TNAME)));
        TypeVertex nullV = new TypeVertex(BTypes.typeNull);

        implicitCastLattice.addVertex(intV, false);
        implicitCastLattice.addVertex(floatV, false);
        implicitCastLattice.addVertex(stringV, false);

        implicitCastLattice.addEdge(intV, jsonV, SAFE, InstructionCodes.I2JSON);
        implicitCastLattice.addEdge(floatV, jsonV, SAFE, InstructionCodes.F2JSON);
        implicitCastLattice.addEdge(stringV, jsonV, SAFE, InstructionCodes.S2JSON);
        implicitCastLattice.addEdge(booleanV, jsonV, SAFE, InstructionCodes.B2JSON);
        implicitCastLattice.addEdge(nullV, jsonV, SAFE, InstructionCodes.NULL2JSON);

        implicitCastLattice.addEdge(intV, anyV, SAFE, InstructionCodes.I2ANY);
        implicitCastLattice.addEdge(floatV, anyV, SAFE, InstructionCodes.F2ANY);
        implicitCastLattice.addEdge(stringV, anyV, SAFE, InstructionCodes.S2ANY);
        implicitCastLattice.addEdge(booleanV, anyV, SAFE, InstructionCodes.B2ANY);
        implicitCastLattice.addEdge(blobV, anyV, SAFE, InstructionCodes.L2ANY);
    }

    public static void loadExplicitCastLattice(SymbolScope scope) {

        TypeVertex intV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.INT_TNAME)));
        TypeVertex floatV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.FLOAT_TNAME)));
        TypeVertex stringV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.STRING_TNAME)));
        TypeVertex booleanV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.BOOLEAN_TNAME)));
        TypeVertex blobV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.BLOB_TNAME)));
        TypeVertex xmlV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.XML_TNAME)));
        TypeVertex jsonV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.JSON_TNAME)));
        TypeVertex anyV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.ANY_TNAME)));
        TypeVertex connectorV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.CONNECTOR_TNAME)));
        TypeVertex mapV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.MAP_TNAME)));
        TypeVertex messageV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.MESSAGE_TNAME)));
        TypeVertex datatableV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.DATATABLE_TNAME)));


        explicitCastLattice.addVertex(intV, false);
        explicitCastLattice.addVertex(floatV, false);
        explicitCastLattice.addVertex(stringV, false);
        explicitCastLattice.addVertex(booleanV, false);
        explicitCastLattice.addVertex(blobV, false);
        explicitCastLattice.addVertex(xmlV, false);
        explicitCastLattice.addVertex(jsonV, false);
        explicitCastLattice.addVertex(anyV, false);
        explicitCastLattice.addVertex(connectorV, false);
        explicitCastLattice.addVertex(messageV, false);
        explicitCastLattice.addVertex(datatableV, false);

        explicitCastLattice.addEdge(intV, anyV, SAFE, InstructionCodes.I2ANY);
        explicitCastLattice.addEdge(intV, jsonV, SAFE, InstructionCodes.I2JSON);

        explicitCastLattice.addEdge(floatV, anyV, SAFE, InstructionCodes.F2ANY);
        explicitCastLattice.addEdge(floatV, jsonV, SAFE, InstructionCodes.F2JSON);

        explicitCastLattice.addEdge(stringV, anyV, SAFE, InstructionCodes.S2ANY);
        explicitCastLattice.addEdge(stringV, jsonV, SAFE, InstructionCodes.S2JSON);

        explicitCastLattice.addEdge(booleanV, anyV, SAFE, InstructionCodes.B2ANY);
        explicitCastLattice.addEdge(booleanV, jsonV, SAFE, InstructionCodes.B2JSON);

        explicitCastLattice.addEdge(blobV, anyV, SAFE, InstructionCodes.L2ANY);

        explicitCastLattice.addEdge(connectorV, anyV, SAFE, InstructionCodes.NOP);

        explicitCastLattice.addEdge(anyV, floatV, UNSAFE, InstructionCodes.ANY2F);
        explicitCastLattice.addEdge(anyV, stringV, UNSAFE, InstructionCodes.ANY2S);
        explicitCastLattice.addEdge(anyV, booleanV, UNSAFE, InstructionCodes.ANY2B);
        explicitCastLattice.addEdge(anyV, blobV, UNSAFE, InstructionCodes.ANY2L);
        explicitCastLattice.addEdge(anyV, intV, UNSAFE, InstructionCodes.ANY2I);
        explicitCastLattice.addEdge(anyV, jsonV, UNSAFE, InstructionCodes.ANY2JSON);
        explicitCastLattice.addEdge(anyV, xmlV, UNSAFE, InstructionCodes.ANY2XML);
        explicitCastLattice.addEdge(anyV, connectorV, UNSAFE, InstructionCodes.ANY2C);
        explicitCastLattice.addEdge(anyV, anyV, SAFE, InstructionCodes.NOP);
        explicitCastLattice.addEdge(anyV, mapV, UNSAFE, InstructionCodes.ANY2MAP);
        explicitCastLattice.addEdge(anyV, messageV, UNSAFE, InstructionCodes.ANY2MSG);
        explicitCastLattice.addEdge(anyV, datatableV, UNSAFE, InstructionCodes.ANY2DT);

        explicitCastLattice.addEdge(jsonV, anyV, SAFE, InstructionCodes.NOP);
        explicitCastLattice.addEdge(anyV, messageV, SAFE, InstructionCodes.ANY2MSG);

        explicitCastLattice.addEdge(jsonV, stringV, UNSAFE, InstructionCodes.JSON2S);
        explicitCastLattice.addEdge(jsonV, intV, UNSAFE, InstructionCodes.JSON2I);
        explicitCastLattice.addEdge(jsonV, floatV, UNSAFE, InstructionCodes.JSON2F);
        explicitCastLattice.addEdge(jsonV, booleanV, UNSAFE, InstructionCodes.JSON2B);

        explicitCastLattice.addEdge(xmlV, anyV, SAFE, InstructionCodes.NOP);

        explicitCastLattice.addEdge(mapV, anyV, SAFE, InstructionCodes.NOP);

        explicitCastLattice.addEdge(datatableV, anyV, SAFE, InstructionCodes.NOP);
    }

    public static void loadConversionLattice(SymbolScope scope) {

        TypeVertex intV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.INT_TNAME)));
        TypeVertex floatV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.FLOAT_TNAME)));
        TypeVertex stringV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.STRING_TNAME)));
        TypeVertex booleanV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.BOOLEAN_TNAME)));
        TypeVertex xmlV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.XML_TNAME)));
        TypeVertex jsonV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.JSON_TNAME)));
        TypeVertex connectorV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.CONNECTOR_TNAME)));
        TypeVertex datatableV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.DATATABLE_TNAME)));
        TypeVertex xmlAttributesV = new TypeVertex(BTypes.typeXMLAttributes);
        TypeVertex mapV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.MAP_TNAME)));
        
        conversionLattice.addVertex(intV, false);
        conversionLattice.addVertex(floatV, false);
        conversionLattice.addVertex(booleanV, false);
        conversionLattice.addVertex(stringV, false);
        conversionLattice.addVertex(xmlV, false);
        conversionLattice.addVertex(jsonV, false);
        conversionLattice.addVertex(connectorV, false);
        conversionLattice.addVertex(datatableV, false);

        conversionLattice.addEdge(intV, floatV, SAFE, InstructionCodes.I2F);
        conversionLattice.addEdge(intV, stringV, SAFE, InstructionCodes.I2S);
        conversionLattice.addEdge(intV, booleanV, SAFE, InstructionCodes.I2B);

        conversionLattice.addEdge(floatV, stringV, SAFE, InstructionCodes.F2S);
        conversionLattice.addEdge(floatV, booleanV, SAFE, InstructionCodes.F2B);
        conversionLattice.addEdge(floatV, intV, SAFE, InstructionCodes.F2I);

        conversionLattice.addEdge(stringV, floatV, UNSAFE, InstructionCodes.S2F);
        conversionLattice.addEdge(stringV, intV, UNSAFE, InstructionCodes.S2I);
        conversionLattice.addEdge(stringV, booleanV, UNSAFE, InstructionCodes.S2B);

        conversionLattice.addEdge(booleanV, stringV, SAFE, InstructionCodes.B2S);
        conversionLattice.addEdge(booleanV, intV, SAFE, InstructionCodes.B2I);
        conversionLattice.addEdge(booleanV, floatV, SAFE, InstructionCodes.B2F);

        conversionLattice.addEdge(jsonV, xmlV, UNSAFE, InstructionCodes.JSON2XML);

        conversionLattice.addEdge(xmlV, jsonV, UNSAFE, InstructionCodes.XML2JSON);
        conversionLattice.addEdge(datatableV, xmlV, UNSAFE, InstructionCodes.DT2XML);
        conversionLattice.addEdge(datatableV, jsonV, UNSAFE, InstructionCodes.DT2JSON);
        
        conversionLattice.addEdge(xmlAttributesV, mapV, SAFE, InstructionCodes.XMLATTRS2MAP);
    }

    /**
     * Accepts two vertices and a weight, and adds the edge
     * ({one, two}, weight) iff no TypeEdge relating one and two
     * exists in the Graph.
     *
     * @param one             The first TypeVertex of the TypeEdge
     * @param two             The second TypeVertex of the TypeEdge
     * @param safe            There will be runtime errors or not
     * @param instructionCode Instruction code to be used in VM
     * @return true iff no TypeEdge already exists in the Graph
     */
    public boolean addEdge(TypeVertex one, TypeVertex two, boolean safe, int instructionCode) {

        //ensures the TypeEdge is not in the Graph
        TypeEdge e = new TypeEdge(one, two, safe, instructionCode);
        if (this.edges.containsKey(e.hashCode())) {
            return false;
        } else if (one.containsNeighbor(e) || two.containsNeighbor(e)) {
            return false;
        }

        this.edges.put(e.hashCode(), e);
        one.addNeighbor(e);
        two.addNeighbor(e);
        return true;
    }

    public TypeEdge getEdgeFromTypes(BLangSymbol source, BLangSymbol target, String packageName) {
        TypeEdge result;
        // First check within the package
        result = this.edges.get(Objects.hash(source.toString(), target.toString()));
//        if (result == null) {
//            result = this.edges.get((packageName + ":" + source.toString() + packageName + ":" +
//                    target.toString() + packageName).hashCode());
//        }
        return result;
    }

    /**
     * This method removes the specified TypeEdge from the Graph,
     * including as each vertex's incidence neighborhood.
     *
     * @param e The TypeEdge to remove from the Graph
     * @return TypeEdge The TypeEdge removed from the Graph
     */
    public TypeEdge removeEdge(TypeEdge e) {
        e.getSource().removeNeighbor(e);
        e.getTarget().removeNeighbor(e);
        return this.edges.remove(e.hashCode());
    }

    /**
     * This method adds a TypeVertex to the graph. If a TypeVertex with the same label
     * as the parameter exists in the Graph, the existing TypeVertex is overwritten
     * only if overwriteExisting is true. If the existing TypeVertex is overwritten,
     * the Edges incident to it are all removed from the Graph.
     *
     * @param vertex            {@link TypeVertex} to add
     * @param overwriteExisting flag indicating whetehr to overide the vertex, if already exists with the same name
     * @return true iff vertex was added to the Graph
     */
    public boolean addVertex(TypeVertex vertex, boolean overwriteExisting) {
        TypeVertex current = this.vertices.get(vertex.toString());
        if (current != null) {
            if (!overwriteExisting) {
                return false;
            }

            while (current.getNeighborCount() > 0) {
                removeEdge(current.getNeighbor(0));
            }
        }

        this.vertices.put(vertex.toString(), vertex);
        return true;
    }

    /**
     * @return Set &lt;TypeEdge&gt; The Edges of this graph
     */
    public Set<TypeEdge> getEdges() {
        return new HashSet<TypeEdge>(this.edges.values());
    }

    /**
     * @return Set &lt;TypeVertex&gt; The Vertices of this graph
     */
    public Set<TypeVertex> getVertices() {
        return new HashSet<TypeVertex>(this.vertices.values());
    }

    /**
     * Add edges to type cast lattice for dynamically defined types structs.
     * This method will add edges from the current struct to map-type, json-type,
     * and all other struct-types, and the vice-versa.
     *
     * @param structDef {@link StructDef} of the dynamically defined struct type
     * @param scope     scope of the defined type
     */
    public static void addStructEdges(StructDef structDef, SymbolScope scope) {
        addExplicitCastingLatticeEdges(structDef, scope);
        addConversionLatticeEdges(structDef, scope);
    }

    private static void addExplicitCastingLatticeEdges(StructDef structDef, SymbolScope scope) {
        TypeVertex structV = new TypeVertex(structDef);
        TypeVertex anyV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.ANY_TNAME)));

        explicitCastLattice.addVertex(structV, false);
        explicitCastLattice.addEdge(anyV, structV, UNSAFE, InstructionCodes.ANY2T);
        explicitCastLattice.addEdge(structV, anyV, SAFE, InstructionCodes.NOP);

        // For all the structs in all the packages imported, check for possibility of casting.
        // Add an edge to the lattice, if casting is possible.
        for (Entry<SymbolName, BLangSymbol> pkg : scope.getEnclosingScope().getSymbolMap().entrySet()) {
            BLangSymbol pkgSymbol = pkg.getValue();
            if (!(pkgSymbol instanceof BLangPackage)) {
                continue;
            }
            for (Entry<SymbolName, BLangSymbol> entry : ((BLangPackage) pkgSymbol).getSymbolMap().entrySet()) {
                BLangSymbol symbol = entry.getValue();
                if (symbol instanceof StructDef && symbol != structDef) {
                    TypeVertex otherStructV = new TypeVertex(symbol);

                    if (isAssignCompatible(structDef, (StructDef) symbol)) {
                        explicitCastLattice.addEdge(otherStructV, structV, SAFE, InstructionCodes.NOP);
                    }

                    if (isAssignCompatible((StructDef) symbol, structDef)) {
                        explicitCastLattice.addEdge(structV, otherStructV, SAFE, InstructionCodes.NOP);
                    }
                }
            }
        }
    }

    /**
     * Add conversion edges for structs.
     *
     * @param structDef Struct definition
     * @param scope     scope of the struct
     */
    private static void addConversionLatticeEdges(StructDef structDef, SymbolScope scope) {
        TypeVertex structV = new TypeVertex(structDef);
        TypeVertex mapV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.MAP_TNAME)));
        TypeVertex jsonV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.JSON_TNAME)));

        conversionLattice.addVertex(structV, false);

        conversionLattice.addEdge(structV, mapV, SAFE, InstructionCodes.T2MAP);
        conversionLattice.addEdge(structV, jsonV, UNSAFE, InstructionCodes.T2JSON);
        conversionLattice.addEdge(jsonV, structV, UNSAFE, InstructionCodes.JSON2T);
        conversionLattice.addEdge(mapV, structV, UNSAFE, InstructionCodes.MAP2T);
    }

    public static boolean isAssignCompatible(StructDef targetStructDef, StructDef sourceStructDef) {
        if (targetStructDef == sourceStructDef) {
            return true;
        }

        for (VariableDefStmt fieldDef : targetStructDef.getFieldDefStmts()) {
            VariableDef targetFieldDef = fieldDef.getVariableDef();
            BType targetFieldType = targetFieldDef.getType();
            SymbolName fieldSymbolName = targetFieldDef.getSymbolName();
            VariableDef sourceFieldDef = (VariableDef) sourceStructDef
                    .resolveMembers(new SymbolName(fieldSymbolName.getName(), sourceStructDef.getPackagePath()));

            // field has to exists
            if (sourceFieldDef == null) {
                return false;
            }

            // struct memory index of both the fields has to be same. i.e: order of the fields
            // must be same in the target and the source structs
            if (((StructVarLocation) targetFieldDef.getMemoryLocation()).getStructMemAddrOffset() !=
                    ((StructVarLocation) sourceFieldDef.getMemoryLocation()).getStructMemAddrOffset()) {
                return false;
            }

            BType sourceFieldType = sourceFieldDef.getType();
            if (!isCompatible(targetFieldType, sourceFieldType)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check whether a given source type can be assigned to a destination type.
     *
     * @param lType Destination type
     * @param rType Source Type
     * @return Flag indicating whether the given source type can be assigned to the destination type.
     */
    public static boolean isCompatible(BType lType, BType rType) {
        if (lType == rType) {
            return true;
        }

        if (lType == BTypes.typeAny) {
            return true;
        }

        if (!BTypes.isValueType(lType) && (rType == BTypes.typeNull)) {
            return true;
        }

        return false;
    }
}
