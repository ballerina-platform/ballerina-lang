package io.ballerinalang.compiler.parser.test.syntax.misc;

import org.testng.annotations.Test;

/**
 * Test recovery in completion mode.
 * <p>
 * i.e. test recovery at `}` and EOF.
 *
 * @since 2.0.0
 */
public class CompletionTest extends AbstractMiscTest {

    // Recovery tests
    
    @Test
    public void testObjectFieldCompletion() {
        testFile("completion/completion_source_01.bal", "completion/completion_assert_01.json");
    }

    @Test
    public void testMatchStmtCompletion() {
        testFile("completion/completion_source_02.bal", "completion/completion_assert_02.json");
    }

    @Test
    public void testEnumMemberCompletion() {
        testFile("completion/completion_source_03.bal", "completion/completion_assert_03.json");
    }

    @Test
    public void testStatementBlockMemberCompletion() {
        testFile("completion/completion_source_04.bal", "completion/completion_assert_04.json");
    }

    @Test
    public void testMappingConstructorFieldCompletion() {
        testFile("completion/completion_source_05.bal", "completion/completion_assert_05.json");
    }

    @Test
    public void testForkStmtMemberCompletion() {
        testFile("completion/completion_source_06.bal", "completion/completion_assert_06.json");
    }

    @Test
    public void testStmtCompletion() {
        testFile("completion/completion_source_07.bal", "completion/completion_assert_07.json");
    }

    @Test
    public void testActionCompletion() {
        testFile("completion/completion_source_08.bal", "completion/completion_assert_08.json");
    }

    @Test
    public void testExprCompletion() {
        testFile("completion/completion_source_09.bal", "completion/completion_assert_09.json");
    }

    @Test
    public void testTypeCompletion() {
        testFile("completion/completion_source_10.bal", "completion/completion_assert_10.json");
    }

    @Test
    public void testAssignmentStmtWithQualifiersCompletion() {
        testFile("completion/completion_source_11.bal", "completion/completion_assert_11.json");
    }
}
