package io.ballerina.semantic.api.test.expectedtype;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
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

/**
 * Test cases for the find expected types.
 *
 * @since 2.3.0
 */
public class FunctionCallExpressionTest {
    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.
                loadProject("test-src/expected-type/function_call_expression_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "LinePosProvider")
    public void testExpectedType(int line, int col, TypeDescKind typeDescKind) {
        Optional<TypeSymbol> typeSymbol = model.
                expectedType(srcFile, LinePosition.from(line, col));
        assertEquals(typeSymbol.get().typeKind(), typeDescKind);
    }

    @DataProvider(name = "LinePosProvider")
    public Object[][] getLinePos() {
        return new Object[][]{
                {25, 88, TypeDescKind.FUNCTION},
                {38, 15, TypeDescKind.TYPE_REFERENCE},
                {43, 13, TypeDescKind.INT},
                {44, 25, TypeDescKind.INT},
                {45, 34, TypeDescKind.INT},
                {53, 36, TypeDescKind.STRING},
                {63, 51, TypeDescKind.TYPE_REFERENCE},
                {79, 63, TypeDescKind.TYPE_REFERENCE}
        };
    }
}
