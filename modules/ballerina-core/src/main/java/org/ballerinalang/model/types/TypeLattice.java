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
import org.ballerinalang.natives.typemappers.NativeCastMapper;
import org.ballerinalang.natives.typemappers.NativeConversionMapper;
import org.ballerinalang.natives.typemappers.TriFunction;
import org.ballerinalang.natives.typemappers.TypeMappingUtils;
import org.ballerinalang.util.codegen.InstructionCodes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
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
        TypeVertex nullV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.NULL_TNAME)));

        implicitCastLattice.addVertex(intV, false);
        implicitCastLattice.addVertex(floatV, false);
        implicitCastLattice.addVertex(stringV, false);
        implicitCastLattice.addEdge(stringV, jsonV, NativeConversionMapper.STRING_TO_JSON_FUNC,
                SAFE, InstructionCodes.S2JSON);

        implicitCastLattice.addEdge(intV, floatV, NativeConversionMapper.INT_TO_FLOAT_FUNC,
                SAFE, InstructionCodes.I2F);
        implicitCastLattice.addEdge(intV, stringV, NativeConversionMapper.INT_TO_STRING_FUNC,
                SAFE, InstructionCodes.I2S);
        implicitCastLattice.addEdge(intV, jsonV, NativeConversionMapper.INT_TO_JSON_FUNC,
                SAFE, InstructionCodes.I2JSON);

        implicitCastLattice.addEdge(floatV, stringV, NativeConversionMapper.FLOAT_TO_STRING_FUNC,
                SAFE, InstructionCodes.F2S);
        implicitCastLattice.addEdge(floatV, jsonV, NativeConversionMapper.FLOAT_TO_JSON_FUNC,
                SAFE, InstructionCodes.F2JSON);

        implicitCastLattice.addEdge(intV, anyV, NativeCastMapper.INT_TO_ANY_FUNC,
                SAFE, InstructionCodes.I2ANY);
        implicitCastLattice.addEdge(floatV, anyV, NativeCastMapper.FLOAT_TO_ANY_FUNC,
                SAFE, InstructionCodes.F2ANY);
        implicitCastLattice.addEdge(stringV, anyV, NativeCastMapper.STRING_TO_ANY_FUNC,
                SAFE, InstructionCodes.S2ANY);
        implicitCastLattice.addEdge(booleanV, anyV, NativeCastMapper.BOOLEAN_TO_ANY_FUNC,
                SAFE, InstructionCodes.B2ANY);
        implicitCastLattice.addEdge(blobV, anyV, NativeCastMapper.BLOB_TO_ANY_FUNC,
                SAFE, InstructionCodes.L2ANY);

        implicitCastLattice.addEdge(booleanV, stringV, NativeConversionMapper.BOOLEAN_TO_STRING_FUNC,
                SAFE, InstructionCodes.B2S);
        implicitCastLattice.addEdge(booleanV, intV, NativeConversionMapper.BOOLEAN_TO_INT_FUNC,
                SAFE, InstructionCodes.B2I);
        implicitCastLattice.addEdge(booleanV, floatV, NativeConversionMapper.BOOLEAN_TO_FLOAT_FUNC,
                SAFE, InstructionCodes.B2F);
        implicitCastLattice.addEdge(booleanV, jsonV, NativeConversionMapper.BOOLEAN_TO_JSON_FUNC,
                SAFE, InstructionCodes.B2JSON);

        implicitCastLattice.addEdge(nullV, jsonV, NativeConversionMapper.NULL_TO_JSON_FUNC,
                SAFE, InstructionCodes.NULL2JSON);
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

        explicitCastLattice.addEdge(intV, intV, NativeConversionMapper.INT_TO_INT_FUNC,
                SAFE, InstructionCodes.NOP);
        explicitCastLattice.addEdge(intV, floatV, NativeConversionMapper.INT_TO_FLOAT_FUNC,
                SAFE, InstructionCodes.I2F);
        explicitCastLattice.addEdge(intV, stringV, NativeConversionMapper.INT_TO_STRING_FUNC,
                SAFE, InstructionCodes.I2S);
        explicitCastLattice.addEdge(intV, booleanV, NativeConversionMapper.INT_TO_BOOLEAN_FUNC,
                SAFE, InstructionCodes.I2B);
        explicitCastLattice.addEdge(intV, anyV, NativeCastMapper.INT_TO_ANY_FUNC,
                SAFE, InstructionCodes.I2ANY);
        explicitCastLattice.addEdge(intV, jsonV, NativeConversionMapper.INT_TO_JSON_FUNC,
                SAFE, InstructionCodes.I2JSON);

        explicitCastLattice.addEdge(floatV, floatV, NativeConversionMapper.FLOAT_TO_FLOAT_FUNC,
                SAFE, InstructionCodes.NOP);
        explicitCastLattice.addEdge(floatV, stringV, NativeConversionMapper.FLOAT_TO_STRING_FUNC,
                SAFE, InstructionCodes.F2S);
        explicitCastLattice.addEdge(floatV, booleanV, NativeConversionMapper.FLOAT_TO_BOOLEAN_FUNC,
                SAFE, InstructionCodes.F2B);
        explicitCastLattice.addEdge(floatV, intV, NativeConversionMapper.FLOAT_TO_INT_FUNC,
                UNSAFE, InstructionCodes.F2I);
        explicitCastLattice.addEdge(floatV, anyV, NativeCastMapper.FLOAT_TO_ANY_FUNC,
                SAFE, InstructionCodes.F2ANY);
        explicitCastLattice.addEdge(floatV, jsonV, NativeConversionMapper.FLOAT_TO_JSON_FUNC,
                SAFE, InstructionCodes.F2JSON);

        explicitCastLattice.addEdge(stringV, stringV, NativeConversionMapper.STRING_TO_STRING_FUNC,
                SAFE, InstructionCodes.NOP);
        explicitCastLattice.addEdge(stringV, floatV, NativeConversionMapper.STRING_TO_FLOAT_FUNC,
                UNSAFE, InstructionCodes.S2F);
        explicitCastLattice.addEdge(stringV, intV, NativeConversionMapper.STRING_TO_INT_FUNC,
                UNSAFE, InstructionCodes.S2I);
        explicitCastLattice.addEdge(stringV, anyV, NativeCastMapper.STRING_TO_ANY_FUNC,
                SAFE, InstructionCodes.S2ANY);
        explicitCastLattice.addEdge(stringV, jsonV, NativeConversionMapper.STRING_TO_JSON_FUNC,
                SAFE, InstructionCodes.S2JSON);

        // TODO Verify this
        explicitCastLattice.addEdge(stringV, xmlV, NativeConversionMapper.STRING_TO_XML_FUNC);

        explicitCastLattice.addEdge(booleanV, booleanV, NativeConversionMapper.BOOLEAN_TO_BOOLEAN_FUNC,
                SAFE, InstructionCodes.NOP);
        explicitCastLattice.addEdge(booleanV, stringV, NativeConversionMapper.BOOLEAN_TO_STRING_FUNC,
                SAFE, InstructionCodes.B2S);
        explicitCastLattice.addEdge(booleanV, intV, NativeConversionMapper.BOOLEAN_TO_INT_FUNC,
                SAFE, InstructionCodes.B2I);
        explicitCastLattice.addEdge(booleanV, floatV, NativeConversionMapper.BOOLEAN_TO_FLOAT_FUNC,
                SAFE, InstructionCodes.B2F);
        explicitCastLattice.addEdge(booleanV, anyV, NativeCastMapper.BOOLEAN_TO_ANY_FUNC,
                SAFE, InstructionCodes.B2ANY);
        explicitCastLattice.addEdge(booleanV, jsonV, NativeConversionMapper.BOOLEAN_TO_JSON_FUNC,
                SAFE, InstructionCodes.B2JSON);

        explicitCastLattice.addEdge(blobV, anyV, NativeCastMapper.BLOB_TO_ANY_FUNC,
                                    SAFE, InstructionCodes.L2ANY);

        explicitCastLattice.addEdge(connectorV, anyV, NativeCastMapper.CONNECTOR_TO_ANY_FUNC,
                SAFE, InstructionCodes.NOP);

        explicitCastLattice.addEdge(anyV, floatV, NativeCastMapper.ANY_TO_FLOAT_FUNC,
                UNSAFE, InstructionCodes.ANY2F);
        explicitCastLattice.addEdge(anyV, stringV, NativeCastMapper.ANY_TO_STRING_FUNC,
                UNSAFE, InstructionCodes.ANY2S);
        explicitCastLattice.addEdge(anyV, booleanV, NativeCastMapper.ANY_TO_BOOLEAN_FUNC,
                UNSAFE, InstructionCodes.ANY2B);
        explicitCastLattice.addEdge(anyV, blobV, NativeCastMapper.ANY_TO_BLOB_FUNC,
                UNSAFE, InstructionCodes.ANY2L);
        explicitCastLattice.addEdge(anyV, intV, NativeCastMapper.ANY_TO_INT_FUNC,
                UNSAFE, InstructionCodes.ANY2I);
        explicitCastLattice.addEdge(anyV, jsonV, NativeCastMapper.ANY_TO_JSON_FUNC,
                UNSAFE, InstructionCodes.ANY2JSON);
        explicitCastLattice.addEdge(anyV, xmlV, NativeCastMapper.ANY_TO_XML_FUNC);
        explicitCastLattice.addEdge(anyV, connectorV, NativeCastMapper.ANY_TO_CONNECTOR_FUNC);
        explicitCastLattice.addEdge(anyV, anyV, NativeCastMapper.ANY_TO_ANY_FUNC,
                SAFE, InstructionCodes.NOP);
        explicitCastLattice.addEdge(anyV, mapV, NativeCastMapper.ANY_TO_MAP_FUNC,
                UNSAFE, InstructionCodes.ANY2MAP);

        explicitCastLattice.addEdge(jsonV, jsonV, NativeCastMapper.JSON_TO_JSON_FUNC,
                SAFE, InstructionCodes.NOP);
        explicitCastLattice.addEdge(jsonV, anyV, NativeCastMapper.JSON_TO_ANY_FUNC,
                SAFE, InstructionCodes.NOP);
        explicitCastLattice.addEdge(anyV, messageV, NativeCastMapper.ANY_TO_MESSAGE_FUNC,
                SAFE, InstructionCodes.ANY2MSG);

        explicitCastLattice.addEdge(jsonV, stringV, NativeConversionMapper.JSON_TO_STRING_FUNC,
                UNSAFE, InstructionCodes.JSON2S);
        explicitCastLattice.addEdge(jsonV, intV, NativeConversionMapper.JSON_TO_INT_FUNC,
                UNSAFE, InstructionCodes.JSON2I);
        explicitCastLattice.addEdge(jsonV, floatV, NativeConversionMapper.JSON_TO_FLOAT_FUNC,
                UNSAFE, InstructionCodes.JSON2F);
        explicitCastLattice.addEdge(jsonV, booleanV, NativeConversionMapper.JSON_TO_BOOLEAN_FUNC,
                UNSAFE, InstructionCodes.JSON2B);

        explicitCastLattice.addEdge(xmlV, xmlV, NativeCastMapper.XML_TO_XML_FUNC,
                SAFE, InstructionCodes.NOP);
        explicitCastLattice.addEdge(xmlV, anyV, NativeCastMapper.XML_TO_ANY_FUNC,
                SAFE, InstructionCodes.NOP);
        explicitCastLattice.addEdge(xmlV, stringV, NativeConversionMapper.XML_TO_STRING_FUNC);

        explicitCastLattice.addEdge(mapV, mapV, NativeCastMapper.MAP_TO_MAP_FUNC,
                SAFE, InstructionCodes.NOP);
        explicitCastLattice.addEdge(mapV, anyV, NativeCastMapper.MAP_TO_ANY_FUNC,
                SAFE, InstructionCodes.NOP);
        // explicitCastLattice.addEdge(mapV, jsonV, NativeCastMapper.MAP_TO_JSON_FUNC);
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

        conversionLattice.addVertex(intV, false);
        conversionLattice.addVertex(floatV, false);
        conversionLattice.addVertex(booleanV, false);
        conversionLattice.addVertex(stringV, false);
        conversionLattice.addVertex(xmlV, false);
        conversionLattice.addVertex(jsonV, false);
        conversionLattice.addVertex(connectorV, false);
        conversionLattice.addVertex(datatableV, false);

        conversionLattice.addEdge(intV, intV, NativeConversionMapper.INT_TO_INT_FUNC,
                SAFE, InstructionCodes.NOP);
        conversionLattice.addEdge(intV, floatV, NativeConversionMapper.INT_TO_FLOAT_FUNC,
                SAFE, InstructionCodes.I2F);
        conversionLattice.addEdge(intV, stringV, NativeConversionMapper.INT_TO_STRING_FUNC,
                SAFE, InstructionCodes.I2S);
        conversionLattice.addEdge(intV, booleanV, NativeConversionMapper.INT_TO_BOOLEAN_FUNC,
                SAFE, InstructionCodes.I2B);
        conversionLattice.addEdge(intV, jsonV, NativeConversionMapper.INT_TO_JSON_FUNC,
                SAFE, InstructionCodes.I2JSON);

        conversionLattice.addEdge(floatV, floatV, NativeConversionMapper.FLOAT_TO_FLOAT_FUNC,
                SAFE, InstructionCodes.NOP);
        conversionLattice.addEdge(floatV, stringV, NativeConversionMapper.FLOAT_TO_STRING_FUNC,
                SAFE, InstructionCodes.F2S);
        conversionLattice.addEdge(floatV, booleanV, NativeConversionMapper.FLOAT_TO_BOOLEAN_FUNC,
                SAFE, InstructionCodes.F2B);
        conversionLattice.addEdge(floatV, intV, NativeConversionMapper.FLOAT_TO_INT_FUNC,
                UNSAFE, InstructionCodes.F2I);
        conversionLattice.addEdge(floatV, jsonV, NativeConversionMapper.FLOAT_TO_JSON_FUNC,
                SAFE, InstructionCodes.F2JSON);

        conversionLattice.addEdge(stringV, stringV, NativeConversionMapper.STRING_TO_STRING_FUNC,
                SAFE, InstructionCodes.NOP);
        conversionLattice.addEdge(stringV, floatV, NativeConversionMapper.STRING_TO_FLOAT_FUNC,
                UNSAFE, InstructionCodes.S2F);
        conversionLattice.addEdge(stringV, intV, NativeConversionMapper.STRING_TO_INT_FUNC,
                UNSAFE, InstructionCodes.S2I);
        conversionLattice.addEdge(stringV, booleanV, NativeConversionMapper.STRING_TO_BOOLEAN_FUNC,
                UNSAFE, InstructionCodes.S2B);
        conversionLattice.addEdge(stringV, jsonV, NativeConversionMapper.STRING_TO_JSON_FUNC,
                SAFE, InstructionCodes.S2JSON);
        conversionLattice.addEdge(stringV, xmlV, NativeConversionMapper.STRING_TO_XML_FUNC);

        conversionLattice.addEdge(booleanV, booleanV, NativeConversionMapper.BOOLEAN_TO_BOOLEAN_FUNC,
                SAFE, InstructionCodes.NOP);
        conversionLattice.addEdge(booleanV, stringV, NativeConversionMapper.BOOLEAN_TO_STRING_FUNC,
                SAFE, InstructionCodes.B2S);
        conversionLattice.addEdge(booleanV, intV, NativeConversionMapper.BOOLEAN_TO_INT_FUNC,
                SAFE, InstructionCodes.B2I);
        conversionLattice.addEdge(booleanV, floatV, NativeConversionMapper.BOOLEAN_TO_FLOAT_FUNC,
                SAFE, InstructionCodes.B2F);
        conversionLattice.addEdge(booleanV, jsonV, NativeConversionMapper.BOOLEAN_TO_JSON_FUNC,
                SAFE, InstructionCodes.B2JSON);

        conversionLattice.addEdge(jsonV, stringV, NativeConversionMapper.JSON_TO_STRING_FUNC,
                UNSAFE, InstructionCodes.JSON2S);
        conversionLattice.addEdge(jsonV, intV, NativeConversionMapper.JSON_TO_INT_FUNC,
                UNSAFE, InstructionCodes.JSON2I);
        conversionLattice.addEdge(jsonV, floatV, NativeConversionMapper.JSON_TO_FLOAT_FUNC,
                UNSAFE, InstructionCodes.JSON2F);
        conversionLattice.addEdge(jsonV, booleanV, NativeConversionMapper.JSON_TO_BOOLEAN_FUNC,
                UNSAFE, InstructionCodes.JSON2B);
        conversionLattice.addEdge(jsonV, xmlV, NativeConversionMapper.JSON_TO_XML_FUNC);

        conversionLattice.addEdge(xmlV, jsonV, NativeConversionMapper.XML_TO_JSON_FUNC);
        conversionLattice.addEdge(xmlV, stringV, NativeConversionMapper.XML_TO_STRING_FUNC);
        conversionLattice.addEdge(datatableV, xmlV, NativeConversionMapper.DATATABLE_TO_XML_FUNC, UNSAFE,
                InstructionCodes.DT2XML);
        conversionLattice.addEdge(datatableV, jsonV, NativeConversionMapper.DATATABLE_TO_JSON_FUNC, UNSAFE,
                InstructionCodes.DT2JSON);
    }

    /**
     * Merges a given type lattice with the current type lattice
     *
     * @param typeLattice given type lattice
     * @param packageName package name to be merged into
     */
    public void merge(TypeLattice typeLattice, String packageName) {
        for (TypeVertex typeVertex : typeLattice.getVertices()) {
            this.addVertex(typeVertex, false);
        }

        for (TypeEdge typeEdge : typeLattice.getEdges()) {
            this.addEdge(typeEdge.getSource(), typeEdge.getTarget(), typeEdge.getTypeMapperFunction());
        }
    }

    /**
     * Accepts two vertices and a weight, and adds the edge
     * ({one, two}, weight) iff no TypeEdge relating one and two
     * exists in the Graph.
     *
     * @param one             The first TypeVertex of the TypeEdge
     * @param two             The second TypeVertex of the TypeEdge
     * @param mappingFunction The weight of the TypeEdge
     * @return true iff no TypeEdge already exists in the Graph
     */
    public boolean addEdge(TypeVertex one, TypeVertex two, TriFunction mappingFunction,
                           boolean safe, int instructionCode) {

        //ensures the TypeEdge is not in the Graph
        TypeEdge e = new TypeEdge(one, two, mappingFunction, safe, instructionCode);
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

    /**
     * Accepts two vertices and a weight, and adds the edge
     * ({one, two}, weight) iff no TypeEdge relating one and two
     * exists in the Graph.
     *
     * @param one             The first TypeVertex of the TypeEdge
     * @param two             The second TypeVertex of the TypeEdge
     * @param mappingFunction The weight of the TypeEdge
     * @return true iff no TypeEdge already exists in the Graph
     */
    public boolean addEdge(TypeVertex one, TypeVertex two, TriFunction mappingFunction) {

        //ensures the TypeEdge is not in the Graph
        TypeEdge e = new TypeEdge(one, two, mappingFunction);
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
        result = this.edges.get((source.toString() + target.toString() + packageName).hashCode());
        if (result == null) {
            result = this.edges.get((packageName + ":" + source.toString() + packageName + ":" +
                    target.toString() + packageName).hashCode());
        }
        if (result == null) {
            // If not found, check in native type typemappers
            packageName = TypeConstants.NATIVE_PACKAGE;
            result = this.edges.get((source.toString() + target.toString() + packageName)
                    .hashCode());
        }
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
        explicitCastLattice.addEdge(anyV, structV, NativeCastMapper.ANY_TO_STRUCT_FUNC, UNSAFE, InstructionCodes.ANY2T);
        explicitCastLattice.addEdge(structV, anyV, NativeCastMapper.STRUCT_TO_ANY_FUNC, SAFE, InstructionCodes.NOP);

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
                        explicitCastLattice.addEdge(otherStructV, structV, NativeCastMapper.STRUCT_TO_STRUCT_SAFE_FUNC,
                                SAFE, InstructionCodes.NOP);
                    }

                    if (isAssignCompatible((StructDef) symbol, structDef)) {
                        explicitCastLattice.addEdge(structV, otherStructV, NativeCastMapper.STRUCT_TO_STRUCT_SAFE_FUNC,
                                SAFE, InstructionCodes.NOP);
                    }
                }
            }
        }
    }

    /**
     * Add conversion edges for structs
     *
     * @param structDef Struct definition
     * @param scope     scope of the struct
     */
    private static void addConversionLatticeEdges(StructDef structDef, SymbolScope scope) {
        TypeVertex structV = new TypeVertex(structDef);
        TypeVertex mapV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.MAP_TNAME)));
        TypeVertex jsonV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.JSON_TNAME)));

        conversionLattice.addVertex(structV, false);

        conversionLattice.addEdge(structV, mapV, NativeConversionMapper.STRUCT_TO_MAP_FUNC);
        conversionLattice.addEdge(structV, jsonV, NativeConversionMapper.STRUCT_TO_JSON_FUNC);
        conversionLattice.addEdge(jsonV, structV, NativeConversionMapper.JSON_TO_STRUCT_FUNC);
        conversionLattice.addEdge(mapV, structV, NativeConversionMapper.MAP_TO_STRUCT_FUNC);
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
            if (!TypeMappingUtils.isCompatible(targetFieldType, sourceFieldType)) {
                return false;
            }
        }
        return true;
    }
}
