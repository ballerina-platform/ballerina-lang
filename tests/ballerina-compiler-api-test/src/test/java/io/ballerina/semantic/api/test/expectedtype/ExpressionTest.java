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
 * Test cases for the find expected types in expressions.
 *
 * @since 2.3.0
 */
public class ExpressionTest {
    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/expected-type/expression_test.bal");
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
                {23, 19, TypeDescKind.INT},
                {29, 14, TypeDescKind.INT},
                {35, 32, TypeDescKind.INT},
                {51, 20, TypeDescKind.TYPE_REFERENCE},
                {52, 23, TypeDescKind.UNION},
                {57, 14, TypeDescKind.INT},
                {62, 8, TypeDescKind.BOOLEAN},
//                {68, 10, TypeDescKind.BOOLEAN},
                {89, 32, TypeDescKind.STRING},
                {90, 40, TypeDescKind.INT},
                {91, 44, TypeDescKind.INT},
                {96, 19, TypeDescKind.ANY},
                {111, 31, TypeDescKind.STRING},
                {111, 31, TypeDescKind.STRING},
                {112, 33, TypeDescKind.TYPE_REFERENCE},
////                {113, 35, TypeDescKind.TYPE_REFERENCE} this is a failing case
                {117, 28, TypeDescKind.INT},
                {118, 19, TypeDescKind.INT},
                {119, 18, TypeDescKind.ARRAY},

        };
    }
}
