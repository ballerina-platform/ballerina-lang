package io.ballerinalang.compiler.parser.test.tree.nodeparser;

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test {@code parseRegexpPattern} method.
 *
 * @since 5.0.0
 */
public class ParseRegexpPatternTest {

    // Valid syntax tests

    @Test
    public void testSimpleCharset() {
        ExpressionNode regexpPattern = NodeParser.parseRegexpPattern("abc");
        Assert.assertEquals(regexpPattern.kind(), SyntaxKind.REGEX_TEMPLATE_EXPRESSION);
        Assert.assertFalse(regexpPattern.hasDiagnostics());
    }

    @Test
    public void testCharClass() {
        ExpressionNode regexpPattern = NodeParser.parseRegexpPattern("[a-z|A-Z]");
        Assert.assertEquals(regexpPattern.kind(), SyntaxKind.REGEX_TEMPLATE_EXPRESSION);
        Assert.assertFalse(regexpPattern.hasDiagnostics());
    }

    @Test
    public void testEmptyString() {
        ExpressionNode regexpPattern = NodeParser.parseRegexpPattern("");
        Assert.assertEquals(regexpPattern.kind(), SyntaxKind.REGEX_TEMPLATE_EXPRESSION);
        Assert.assertFalse(regexpPattern.hasDiagnostics());

        Assert.assertEquals(regexpPattern.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(regexpPattern.trailingInvalidTokens().size(), 0);
        Assert.assertTrue(((TemplateExpressionNode) NodeParser.parseRegexpPattern("")).content().isEmpty());
    }

    // Invalid syntax tests

    @Test
    public void testInvalidCharRegexp() {
        ExpressionNode regexpPattern = NodeParser.parseRegexpPattern("+");
        Assert.assertEquals(regexpPattern.kind(), SyntaxKind.REGEX_TEMPLATE_EXPRESSION);
        Assert.assertTrue(regexpPattern.hasDiagnostics());

        Assert.assertEquals(regexpPattern.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(regexpPattern.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(regexpPattern.toString(), "re` MISSING[\\ INVALID[+]]`");
    }

    @Test
    public void testInvalidEscapeCharRegexp() {
        ExpressionNode regexpPattern = NodeParser.parseRegexpPattern("\\");
        Assert.assertEquals(regexpPattern.kind(), SyntaxKind.REGEX_TEMPLATE_EXPRESSION);
        Assert.assertTrue(regexpPattern.hasDiagnostics());

        Assert.assertEquals(regexpPattern.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(regexpPattern.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(regexpPattern.toString(), "re`\\ MISSING[^ INVALID[]]`");
    }

    @Test
    public void testWithInvalidTokens() {
        ExpressionNode regexpPattern = NodeParser.parseRegexpPattern("[a-z] `");
        Assert.assertEquals(regexpPattern.kind(), SyntaxKind.REGEX_TEMPLATE_EXPRESSION);
        Assert.assertTrue(regexpPattern.hasDiagnostics());

        Assert.assertEquals(regexpPattern.leadingInvalidTokens().size(), 0);

        List<Token> tokens = regexpPattern.trailingInvalidTokens();
        Assert.assertEquals(tokens.size(), 1);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.BACKTICK_TOKEN);

        Assert.assertEquals(regexpPattern.toString(), "re`[a-z] ` INVALID[`]");
    }

    @Test
    public void testWithExprRecovery() {
        ExpressionNode regexpPattern = NodeParser.parseRegexpPattern("[a-z");
        Assert.assertEquals(regexpPattern.kind(), SyntaxKind.REGEX_TEMPLATE_EXPRESSION);
        Assert.assertTrue(regexpPattern.hasDiagnostics());

        Assert.assertEquals(regexpPattern.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(regexpPattern.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(regexpPattern.toString(), "re`[a-z MISSING[]]`");
    }
}
