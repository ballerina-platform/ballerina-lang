package org.ballerinalang.bindgen.model;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TreeModifier;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Class for traversing the Ballerina syntax tree used for creating the bindings.
 *
 * @since 2.0.0
 */
public class BindgenTreeModifier extends TreeModifier {

    public BindgenTreeModifier() {
    }

    @Override
    public ModulePartNode transform(ModulePartNode modulePartNode) {
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createNodeList(
                BindgenNodeFactory.createImportDeclarationNode("ballerina",
                        "jarrays", new LinkedList<>(Arrays.asList("java", ".", "arrays"))));
        NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
        Token eofToken = modulePartNode.eofToken();
        return modulePartNode.modify(imports, members, eofToken);
    }

    @Override
    public ImportDeclarationNode transform(ImportDeclarationNode importDeclarationNode) {
        Token importKeyword = AbstractNodeFactory.createIdentifierToken("lllll");

        return importDeclarationNode.modify()
                .withImportKeyword(importKeyword)
                .apply();
    }
}
