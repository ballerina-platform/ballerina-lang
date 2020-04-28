package org.ballerinalang.test.documentation;

import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.docgen.generator.model.DefaultableVariable;
import org.ballerinalang.docgen.generator.model.Module;
import org.ballerinalang.docgen.generator.model.Object;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Test cases to check default value initialization for objects in docs.
 */
public class DefaultValueInitializationObjects {
    private Module testModule;

    @BeforeClass
    public void setup() throws IOException {
        String sourceRoot =
                "test-src" + File.separator + "documentation" + File.separator + "default_value_initialization";
        CompileResult result = BCompileUtil.compile(sourceRoot, "test_module");

        List<BLangPackage> modules = new LinkedList<>();
        modules.add((BLangPackage) result.getAST());
        Map<String, ModuleDoc> docsMap = BallerinaDocGenerator.generateModuleDocs(
                Paths.get("src/test/resources", sourceRoot).toAbsolutePath().toString(), modules);
        List<ModuleDoc> moduleDocList = new ArrayList<>(docsMap.values());
        moduleDocList.sort(Comparator.comparing(pkg -> pkg.bLangPackage.packageID.toString()));
        Map<String, List<Path>> resources = new HashMap<>();

        Project project = BallerinaDocGenerator.getDocsGenModel(moduleDocList, resources);
        testModule = project.modules.get(0);
    }

    @Test(description = "Test default value init")
    public void testDefValInit() {
        List<Object> objects = testModule.objects;
        Object foo, bar, student;
        for (Object object : objects) {
            for (DefaultableVariable variable : object.fields) {
                if (object.name.equals("Foo")) {
                    if (variable.name.equals("i")) {
                        Assert.assertEquals(variable.defaultValue, "1");
                    }
                } else if (object.name.equals("Bar")) {
                    if (variable.name.equals("i")) {
                        Assert.assertEquals(variable.defaultValue, "1");
                    } else if (variable.name.equals("s")) {
                        Assert.assertEquals(variable.defaultValue, "str");
                    } else {
                        Assert.assertEquals(variable.defaultValue, "Foo");
                    }
                } else {
                    Assert.assertEquals(variable.defaultValue, "Bar");
                }
            }
        }
    }

}
