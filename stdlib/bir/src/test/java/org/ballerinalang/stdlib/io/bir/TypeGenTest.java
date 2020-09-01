package org.ballerinalang.stdlib.io.bir;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.tool.util.BCompileUtil;
import org.ballerinalang.tool.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Test cases for bir type gen.
 */
//TODO rewrite this test (manu)
public class TypeGenTest {

    public static final String EXPECTED_PREFIX = "// expected: ";
    public static final int EXPECTED_PREFIX_LEN = EXPECTED_PREFIX.length();

    private static Path resourceDir = Paths.get("src/test/resources").toAbsolutePath();

    @DataProvider()
    public static Iterator<Object[]> bTypes() throws IOException {
        String sourceFilePath = "test-src/bir/types.bal";
        List<String> source = Files.readAllLines(resourceDir.resolve(sourceFilePath));
        CompileResult result = BCompileUtil.compile(sourceFilePath, CompilerPhase.TYPE_CHECK);
        Assert.assertEquals(result.getErrorCount(), 0, Arrays.toString(result.getDiagnostics()));
        PackageNode ast = result.getAST();
        List<TopLevelNode> topLevelNodes = ((BLangPackage) ast).topLevelNodes;
        List<Object[]> testCases = new ArrayList<>();
        for (TopLevelNode topLevelNode : topLevelNodes) {
            if (topLevelNode.getKind() == NodeKind.VARIABLE) {
                int typeStartLine = ((BLangVariable) topLevelNode).pos.sLine - 1;
                String comment = source.get(typeStartLine - 1);

                Assert.assertTrue(comment.startsWith(EXPECTED_PREFIX),
                                  "missing the expected type string for " + source.get(typeStartLine));

                testCases.add(new Object[]{((BLangVariable) topLevelNode).type,
                                           comment.substring(EXPECTED_PREFIX_LEN)});
            }
        }
        return testCases.iterator();
    }
}
