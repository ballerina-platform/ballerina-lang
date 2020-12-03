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
package org.ballerinalang.diagramutil;

import io.ballerina.compiler.syntax.tree.SyntaxTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {

    /**
     * Get the Modified JSON ST with type info.
     *
     * @param syntaxTree       SyntaxTree to be modified and in need to convert to JSON.
     * @param node             Ballerina Node for the syntax tree to extract type from.
     * @param visibleEPsByNode Visible endpoints.
     * @return {@link JsonObject}   ST as a Json Object
     */
    public static JsonElement getSyntaxTreeJSON(SyntaxTree syntaxTree, Map<BLangNode,
            List<SymbolMetaInfo>> visibleEPsByNode) {
        // Extract type information from the BLangNode and store them with the line range as key.
        Map<String, JsonObject> typeInfo = new HashMap<>();
//        TypeInfoExtractingVisitor typeInfoExtractingVisitor = new TypeInfoExtractingVisitor(typeInfo,
//                visibleEPsByNode);
//        node.accept(typeInfoExtractingVisitor);
//
//        // Attach all visible endpoints to FunctionBodyBlock
//        for (Map.Entry<BLangNode, List<SymbolMetaInfo>> entry : visibleEPsByNode.entrySet()) {
//            JsonArray eps = new JsonArray();
//            for (SymbolMetaInfo symbolMetaInfo : entry.getValue()) {
//                eps.add(symbolMetaInfo.getJson());
//            }
//            JsonObject endpoints = new JsonObject();
//            endpoints.add("VisibleEndpoints", eps);
//            typeInfo.put("FunctionBodyBlock", endpoints);
//        }

        // Map each type data by looking at the line ranges and prepare the SyntaxTree JSON.
        SyntaxTreeMapGenerator mapGenerator = new SyntaxTreeMapGenerator(typeInfo);
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        return mapGenerator.transform(modulePartNode);
    }

}
