package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.symbols.BallerinaTypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
/**
 * This class contains constructor's type reference type related test cases.
 */
public class ReferenceTypeTest {

    @Test
    public void testLookup() {
        Project project = BCompileUtil.loadProject("test-src/reference_type_test.bal");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        DocumentId docId = currentPackage.getDefaultModule().documentIds().iterator().next();
        SyntaxTree syntaxTree = currentPackage.getDefaultModule().document(docId).syntaxTree();
        SemanticModel model = currentPackage.getCompilation().getSemanticModel(defaultModuleId);
        syntaxTree.rootNode().accept(getNodeVisitor(model));
    }

    NodeVisitor getNodeVisitor(SemanticModel model) {
        return new NodeVisitor() {

            @Override
            public void visit(TableConstructorExpressionNode tableConstructorExpressionNode) {
                assertType(tableConstructorExpressionNode, model, "BarTable");
            }
        };
    }

    private void assertType(Node node, SemanticModel model, String referenceTypeName) {
        Optional<TypeSymbol> type = model.typeOf(node);
        assertTrue(type.isPresent());
        assertEquals((((BallerinaTypeReferenceTypeSymbol) type.get()).getBType().toString()), referenceTypeName);
    }
}
