package io.ballerinalang.compiler.parser.test.syntax.types;

import org.testng.annotations.Test;

/**
 * Test parsing parenthesised type.
 */
public class ParenthesisedTypeTest extends AbstractTypesTest {

    // Valid source tests

    @Test
    public void testParenthesisedType() {
        testFile("parenthesised-type/parenthesised-type_source_01.bal",
                "parenthesised-type/parenthesised-type_assert_01.json");
    }

    // Recovery tests

    @Test
    public void testParenthesisedTypeRecovery() {
        testFile("parenthesised-type/parenthesised-type_source_02.bal",
                "parenthesised-type/parenthesised-type_assert_02.json");
    }
}
