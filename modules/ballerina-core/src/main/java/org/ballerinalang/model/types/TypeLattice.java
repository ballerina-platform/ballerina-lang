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

import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.TypeMapper;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.natives.typemappers.NativeCastMapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Class to hold the types and their connections within ballerina
 */
public class TypeLattice {

    protected final HashMap<String, TypeVertex> vertices = new HashMap<>();
    protected final HashMap<Integer, TypeEdge> edges = new HashMap<>();
    private static TypeLattice explicitCastLattice = new TypeLattice();
    private static TypeLattice implicitCastLattice = new TypeLattice();

    public static TypeLattice getExplicitCastLattice() {
        return explicitCastLattice;
    }

    public static TypeLattice getImplicitCastLattice() {
        return implicitCastLattice;
    }


    public static void loadImplicitCastLattice(SymbolScope scope) {

        TypeVertex intV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.INT_TNAME)));
        TypeVertex longV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.LONG_TNAME)));
        TypeVertex floatV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.FLOAT_TNAME)));
        TypeVertex doubleV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.DOUBLE_TNAME)));
        TypeVertex stringV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.STRING_TNAME)));
        TypeVertex booleanV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.BOOLEAN_TNAME)));

        implicitCastLattice.addVertex(intV, false);
        implicitCastLattice.addVertex(longV, false);
        implicitCastLattice.addVertex(floatV, false);
        implicitCastLattice.addVertex(doubleV, false);
        implicitCastLattice.addVertex(stringV, false);

        implicitCastLattice.addEdge(intV, longV, NativeCastMapper.INT_TO_LONG_FUNC);
        implicitCastLattice.addEdge(intV, floatV, NativeCastMapper.INT_TO_FLOAT_FUNC);
        implicitCastLattice.addEdge(intV, doubleV, NativeCastMapper.INT_TO_DOUBLE_FUNC);
        implicitCastLattice.addEdge(intV, stringV, NativeCastMapper.INT_TO_STRING_FUNC);

        implicitCastLattice.addEdge(longV, floatV, NativeCastMapper.LONG_TO_FLOAT_FUNC);
        implicitCastLattice.addEdge(longV, doubleV, NativeCastMapper.LONG_TO_DOUBLE_FUNC);
        implicitCastLattice.addEdge(longV, stringV, NativeCastMapper.LONG_TO_STRING_FUNC);

        implicitCastLattice.addEdge(floatV, doubleV, NativeCastMapper.FLOAT_TO_DOUBLE_FUNC);
        implicitCastLattice.addEdge(floatV, stringV, NativeCastMapper.FLOAT_TO_STRING_FUNC);

        implicitCastLattice.addEdge(doubleV, stringV, NativeCastMapper.DOUBLE_TO_STRING_FUNC);

        implicitCastLattice.addEdge(booleanV, stringV, NativeCastMapper.BOOLEAN_TO_STRING_FUNC);
    }

    public static void loadExplicitCastLattice(SymbolScope scope) {


        TypeVertex intV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.INT_TNAME)));
        TypeVertex longV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.LONG_TNAME)));
        TypeVertex floatV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.FLOAT_TNAME)));
        TypeVertex doubleV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.DOUBLE_TNAME)));
        TypeVertex stringV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.STRING_TNAME)));
        TypeVertex booleanV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.BOOLEAN_TNAME)));
        TypeVertex xmlV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.XML_TNAME)));
        TypeVertex jsonV = new TypeVertex(scope.resolve(new SymbolName(TypeConstants.JSON_TNAME)));

        explicitCastLattice.addVertex(intV, false);
        explicitCastLattice.addVertex(longV, false);
        explicitCastLattice.addVertex(floatV, false);
        explicitCastLattice.addVertex(doubleV, false);
        explicitCastLattice.addVertex(booleanV, false);
        explicitCastLattice.addVertex(stringV, false);
        explicitCastLattice.addVertex(xmlV, false);
        explicitCastLattice.addVertex(jsonV, false);

        explicitCastLattice.addEdge(intV, longV, NativeCastMapper.INT_TO_LONG_FUNC);
        explicitCastLattice.addEdge(intV, floatV, NativeCastMapper.INT_TO_FLOAT_FUNC);
        explicitCastLattice.addEdge(intV, doubleV, NativeCastMapper.INT_TO_DOUBLE_FUNC);
        explicitCastLattice.addEdge(intV, stringV, NativeCastMapper.INT_TO_STRING_FUNC);
        explicitCastLattice.addEdge(intV, intV, NativeCastMapper.INT_TO_INT_FUNC);

        explicitCastLattice.addEdge(longV, intV, NativeCastMapper.LONG_TO_INT_FUNC);
        explicitCastLattice.addEdge(longV, floatV, NativeCastMapper.LONG_TO_FLOAT_FUNC);
        explicitCastLattice.addEdge(longV, doubleV, NativeCastMapper.LONG_TO_DOUBLE_FUNC);
        explicitCastLattice.addEdge(longV, stringV, NativeCastMapper.LONG_TO_STRING_FUNC);
        explicitCastLattice.addEdge(longV, longV, NativeCastMapper.LONG_TO_LONG_FUNC);

        explicitCastLattice.addEdge(doubleV, longV, NativeCastMapper.DOUBLE_TO_LONG_FUNC);
        explicitCastLattice.addEdge(doubleV, floatV, NativeCastMapper.DOUBLE_TO_FLOAT_FUNC);
        explicitCastLattice.addEdge(doubleV, doubleV, NativeCastMapper.DOUBLE_TO_DOUBLE_FUNC);
        explicitCastLattice.addEdge(doubleV, stringV, NativeCastMapper.DOUBLE_TO_STRING_FUNC);
        explicitCastLattice.addEdge(doubleV, intV, NativeCastMapper.DOUBLE_TO_INT_FUNC);

        explicitCastLattice.addEdge(floatV, longV, NativeCastMapper.FLOAT_TO_LONG_FUNC);
        explicitCastLattice.addEdge(floatV, floatV, NativeCastMapper.FLOAT_TO_FLOAT_FUNC);
        explicitCastLattice.addEdge(floatV, doubleV, NativeCastMapper.FLOAT_TO_DOUBLE_FUNC);
        explicitCastLattice.addEdge(floatV, stringV, NativeCastMapper.FLOAT_TO_STRING_FUNC);
        explicitCastLattice.addEdge(floatV, intV, NativeCastMapper.FLOAT_TO_INT_FUNC);

        explicitCastLattice.addEdge(stringV, longV, NativeCastMapper.STRING_TO_LONG_FUNC);
        explicitCastLattice.addEdge(stringV, floatV, NativeCastMapper.STRING_TO_FLOAT_FUNC);
        explicitCastLattice.addEdge(stringV, doubleV, NativeCastMapper.STRING_TO_DOUBLE_FUNC);
        explicitCastLattice.addEdge(stringV, stringV, NativeCastMapper.STRING_TO_STRING_FUNC);
        explicitCastLattice.addEdge(stringV, intV, NativeCastMapper.STRING_TO_INT_FUNC);

        explicitCastLattice.addEdge(booleanV, stringV, NativeCastMapper.BOOLEAN_TO_STRING_FUNC);
        explicitCastLattice.addEdge(booleanV, booleanV, NativeCastMapper.BOOLEAN_TO_BOOLEAN_FUNC);

//        explicitCastLattice.addEdge(jsonV, xmlV, new JSONToXML(), TypeConstants.NATIVE_PACKAGE);
//        explicitCastLattice.addEdge(xmlV, jsonV, new XMLToJSON(), TypeConstants.NATIVE_PACKAGE);
//        explicitCastLattice.addEdge(stringV, jsonV, new StringToJSON(), TypeConstants.NATIVE_PACKAGE);
//        explicitCastLattice.addEdge(stringV, xmlV, new StringToXML(), TypeConstants.NATIVE_PACKAGE);
//        explicitCastLattice.addEdge(xmlV, stringV, new XMLToString(), TypeConstants.NATIVE_PACKAGE);
//        explicitCastLattice.addEdge(jsonV, stringV, new JSONToString(), TypeConstants.NATIVE_PACKAGE);
    }

    /**
     * Accepts two vertices and a weight, and adds the edge
     * ({one, two}, weight) iff no TypeEdge relating one and two
     * exists in the Graph.
     *
     * @param one           The first TypeVertex of the TypeEdge
     * @param two           The second TypeVertex of the TypeEdge
     * @param typeMapper The weight of the TypeEdge
     * @return true iff no TypeEdge already exists in the Graph
     */
    public boolean addEdge(TypeVertex one, TypeVertex two, Function typeMapper) {

        //ensures the TypeEdge is not in the Graph
        TypeEdge e = new TypeEdge(one, two, typeMapper);
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
     * @param one           The first TypeVertex of the TypeEdge
     * @param two           The second TypeVertex of the TypeEdge
     * @param typeMapper The weight of the TypeEdge
     * @return true iff no TypeEdge already exists in the Graph
     */
    public boolean addEdge(TypeVertex one, TypeVertex two, TypeMapper typeMapper, String packageName) {

        //ensures the TypeEdge is not in the Graph
        TypeEdge e = new TypeEdge(one, two, typeMapper, packageName);
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
            // If not found, check in native type typemappers
            packageName = TypeConstants.NATIVE_PACKAGE;
            result = this.edges.get((source.toString() + target.toString() + packageName).hashCode());
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
     * @param vertex
     * @param overwriteExisting
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
     * @return Set<TypeEdge> The Edges of this graph
     */
    public Set<TypeEdge> getEdges() {
        return new HashSet<TypeEdge>(this.edges.values());
    }
}
