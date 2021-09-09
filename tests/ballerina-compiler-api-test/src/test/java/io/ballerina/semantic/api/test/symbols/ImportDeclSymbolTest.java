package io.ballerina.semantic.api.test.symbols;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.symbols.BallerinaModule;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for module-level symbols.
 *
 * @since 2.0.0
 */
public class ImportDeclSymbolTest {
    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/module_level");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "ImportDeclarationPosProvider")
    public void testImports(int line, int offset, String name, String org, String prefix) {
        BallerinaModule symbol = (BallerinaModule) assertBasicsAndGetSymbol(line, offset, name, SymbolKind.MODULE);
//        assertEquals(symbol.id().modulePrefix(), prefix);
        assertEquals(symbol.id().orgName(), org);
        assertEquals(symbol.id().version(), "1.0.0");
    }

    @DataProvider(name = "ImportDeclarationPosProvider")
    public Object[][] getImportDeclPos() {
        return new Object[][]{
                {16, 20, "module_level.foo", "testorg", "foo"},
                {17, 27, "module_level.bar", "testorg", "barPrefix"},
                {18, 27, "module_level.baz", "testorg", "_"},
        };
    }

    // private utils
    private Symbol assertBasicsAndGetSymbol(int line, int col, String name, SymbolKind symbolKind) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), symbolKind);
        assertEquals(symbol.get().getName().get(), name);
        return symbol.get();
    }

}
