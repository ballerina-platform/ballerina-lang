package io.ballerina.semantic.api.test.typeof;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.semantic.api.test.util.SemanticAPITestUtils;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.TypeDescKind.BOOLEAN;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests cases for testing typeOf() with conditional expressions.
 *
 * @since 2201.9.0
 */
public class TypeOfConditionalExprTest {

    private SemanticModel model;

    @BeforeClass
    public void setup() {
        model = SemanticAPITestUtils.getDefaultModulesSemanticModel(
                "test-src/symbols/symbols_in_conditional_exprs_test.bal");
    }

    @Test(dataProvider = "TernaryExprsPos")
    public void testTernaryExprs(int sLine, int sCol, int eLine, int eCol, List<TypeDescKind> kinds) {
        assertType(sLine, sCol, eLine, eCol, kinds);
    }

    @DataProvider(name = "TernaryExprsPos")
    public Object[][] getTernaryExprPos() {
        return new Object[][]{
                {3, 10, 3, 36, List.of(BOOLEAN)},
                {4, 11, 4, 39, List.of(STRING, BOOLEAN)},
                {5, 15, 5, 43, List.of(STRING, BOOLEAN)},
                {6, 14, 6, 60, List.of(STRING, BOOLEAN, INT)},
                {9, 11, 9, 30, List.of(BOOLEAN)},
                {13, 11, 13, 32, List.of(STRING, BOOLEAN)},
                {17, 11, 17, 46, List.of(STRING, BOOLEAN, INT)},
                {21, 11, 21, 45, List.of(STRING, BOOLEAN, INT)},
                {25, 11, 25, 85, List.of(STRING, INT)},
                {29, 11, 29, 79, List.of(BOOLEAN, STRING, INT)},
        };
    }

    @Test(dataProvider = "ElvisExprsPos")
    public void testElvisExprs(int sLine, int sCol, int eLine, int eCol, List<TypeDescKind> kinds) {
        assertType(sLine, sCol, eLine, eCol, kinds);
    }

    @DataProvider(name = "ElvisExprsPos")
    public Object[][] getElvisExprPos() {
        return new Object[][]{
                {35, 10, 35, 38, List.of(STRING)},
                {36, 10, 36, 36, List.of(STRING, BOOLEAN)},
                {37, 11, 37, 58, List.of(STRING, INT, BOOLEAN)},
                {40, 11, 40, 33, List.of(STRING)},
                {44, 11, 44, 31, List.of(STRING, BOOLEAN)},
                {52, 11, 52, 72, List.of(STRING, INT, BOOLEAN, FLOAT)},
                {56, 11, 57, 33, List.of(BOOLEAN, INT, STRING)},
        };
    }

    private TypeSymbol assertType(int sLine, int sCol, int eLine, int eCol, List<TypeDescKind> kinds) {
        Optional<TypeSymbol> type = model.typeOf(
                LineRange.from("symbols_in_conditional_exprs_test.bal", LinePosition.from(sLine, sCol),
                        LinePosition.from(eLine, eCol)));

        if (kinds == null) {
            assertTrue(type.isEmpty());
            return null;
        }
        assertFalse(type.isEmpty());

        TypeSymbol typeSymbol = type.get();
        if (Objects.requireNonNull(type.get().typeKind()) == TypeDescKind.UNION) {
            UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) typeSymbol;
            assertEquals(unionTypeSymbol.memberTypeDescriptors().size(), kinds.size());
            for (int i = 0; i < kinds.size(); i++) {
                assertEquals(unionTypeSymbol.memberTypeDescriptors().get(i).typeKind(), kinds.get(i));
            }
        } else {
            assertEquals(type.get().typeKind(), kinds.get(0));
        }

        return type.get();
    }

}
