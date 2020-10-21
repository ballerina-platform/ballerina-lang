/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.implementation;

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Goto Implementation Utilities.
 *
 * @since 0.990.3
 */
public class GotoImplementationUtil {
    private GotoImplementationUtil() {
    }

    /**
     * Get the Implementations' locations.
     *
     * @param bLangPkg      Current BLangPackage
     * @param ctx           Service Operation Context
     * @param position      Cursor position
     * @param srcRoot       Source root
     * @return {@link List} List of found locations
     */
    public static List<Location> getImplementationLocation(BLangPackage bLangPkg, LSContext ctx, Position position,
                                                           String srcRoot) {
        int line = position.getLine();
        List<Location> implementationLocations = new ArrayList<>();
        String packageName = bLangPkg.packageID.name.toString();
        String funcName = ctx.get(GotoImplementationKeys.SYMBOL_TOKEN_KEY);
        boolean isTestSrc = CommonUtil.isTestSource(ctx.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY));
        BLangPackage evalPkg = isTestSrc ? bLangPkg.getTestablePkg() : bLangPkg;
        List<TopLevelNode> topLevelNodes = CommonUtil.getCurrentFileTopLevelNodes(evalPkg, ctx);
        Optional<BLangObjectTypeNode> bLangObjectTypeNode = getObjectTypeNode(topLevelNodes, line);

        if (!bLangObjectTypeNode.isPresent()) {
            return implementationLocations;
        }

        Optional<BLangFunction> interfaceFunction = getInterfaceFunction(bLangObjectTypeNode.get(), funcName);

        if (!interfaceFunction.isPresent()) {
            return implementationLocations;
        }

        evalPkg.topLevelNodes.stream()
                .filter(node -> node instanceof BLangFunction
                        && ((BLangFunction) node).flagSet.contains(Flag.ATTACHED)
                        && ((BLangFunction) node).symbol == interfaceFunction.get().symbol)
                .map(node -> (BLangFunction) node)
                .forEach(function -> implementationLocations.add(getLocation(srcRoot, packageName, function)));

        return implementationLocations;
    }

    private static Location getLocation(String sourceRoot, String pkgName, BLangFunction bLangFunction) {
        String cUnitName = bLangFunction.getPosition().src.cUnitName;
        Location location = new Location();
        DiagnosticPos implPosition = CommonUtil.toZeroBasedPosition(bLangFunction.getPosition());
        Range range = new Range(new Position(implPosition.sLine, implPosition.sCol),
                new Position(implPosition.eLine, implPosition.eCol));
        location.setRange(range);
        String uri = new File(sourceRoot).toPath().resolve(pkgName).resolve(cUnitName).toUri().toString();
        location.setUri(uri);
        return location;
    }

    private static Optional<BLangFunction> getInterfaceFunction(BLangObjectTypeNode node, String functionName) {
        return node.functions.stream()
                .filter(bLangFunction -> bLangFunction.flagSet.contains(Flag.INTERFACE)
                        && bLangFunction.symbol.getName().getValue() != null
                        && bLangFunction.symbol.getName().getValue().endsWith("." + functionName)
                )
                .findAny();
    }

    private static Optional<BLangObjectTypeNode> getObjectTypeNode(List<TopLevelNode> topLevelNodes, int line) {
        return topLevelNodes.stream()
                .filter(node -> {
                    DiagnosticPos nodePosition = (DiagnosticPos) node.getPosition();
                    DiagnosticPos zeroBasedPosition = CommonUtil.toZeroBasedPosition(nodePosition);
                    return NodeKind.TYPE_DEFINITION.equals(node.getKind())
                            && NodeKind.OBJECT_TYPE.equals(((BLangTypeDefinition) node).typeNode.getKind())
                            && zeroBasedPosition.sLine <= line && zeroBasedPosition.eLine >= line;
                })
                .map(topLevelNode -> ((BLangObjectTypeNode) ((BLangTypeDefinition) topLevelNode).typeNode))
                .findAny();
    }
}
