package org.ballerinalang.test.documentation;

import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.docgen.generator.model.Module;
import org.ballerinalang.docgen.generator.model.Project;
import org.ballerinalang.docgen.model.ModuleDoc;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Test cases related to error types in docs.
 */
public class ErrorsTest {
    private Module testModule;

    @BeforeClass
    public void setup() throws IOException {
        String sourceRoot =
                "test-src" + File.separator + "documentation" + File.separator + "errors_project";
        CompileResult result = BCompileUtil.compile(sourceRoot, "test_module");

        List<BLangPackage> modules = new LinkedList<>();
        modules.add((BLangPackage) result.getAST());
        Map<String, ModuleDoc> docsMap = BallerinaDocGenerator.generateModuleDocs(
                Paths.get("src/test/resources", sourceRoot).toAbsolutePath().toString(), modules);
        List<ModuleDoc> moduleDocList = new ArrayList<>(docsMap.values());
        moduleDocList.sort(Comparator.comparing(pkg -> pkg.bLangPackage.packageID.toString()));

        Project project = BallerinaDocGenerator.getDocsGenModel(moduleDocList);
        testModule = project.modules.get(0);
    }

    @Test(description = "Test error type")
    public void testErrorAsAType() {
        Assert.assertEquals(testModule.errors.size(), 2, "Two errors expected");
        Assert.assertEquals(testModule.errors.get(0).name, "CacheError", "The error should be " +
                "CacheError. But Found:" + testModule.errors.get(0).name);
        Assert.assertEquals(testModule.errors.get(1).name, "Error", "The error should be " +
                "Error. But Found:" + testModule.errors.get(1).name);
    }
}
