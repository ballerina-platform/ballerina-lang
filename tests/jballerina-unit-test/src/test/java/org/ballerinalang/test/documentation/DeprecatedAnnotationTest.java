/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.documentation;

import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.docgen.generator.model.Annotation;
import org.ballerinalang.docgen.generator.model.BClass;
import org.ballerinalang.docgen.generator.model.Constant;
import org.ballerinalang.docgen.generator.model.Construct;
import org.ballerinalang.docgen.generator.model.DefaultableVariable;
import org.ballerinalang.docgen.generator.model.Error;
import org.ballerinalang.docgen.generator.model.FiniteType;
import org.ballerinalang.docgen.generator.model.Function;
import org.ballerinalang.docgen.generator.model.Module;
import org.ballerinalang.docgen.generator.model.Project;
import org.ballerinalang.docgen.generator.model.Record;
import org.ballerinalang.docgen.generator.model.UnionType;
import org.ballerinalang.docgen.generator.model.Variable;
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
 * Test cases to check @deprecated annotation is showing in the docs.
 */
public class DeprecatedAnnotationTest {

    private Module testModule;

    @BeforeClass
    public void setup() throws IOException {
        String sourceRoot =
                "test-src" + File.separator + "documentation" + File.separator + "deprecated_annotation_project";
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

    @Test(description = "Test @deprecated annotation for module-level union type definitions")
    public void testDeprecatedUnionTypeDef() {
        List<UnionType> unionTypes = testModule.unionTypes;
        UnionType depUnionType = null;
        UnionType nonDepUnionType = null;

        for (UnionType type : unionTypes) {
            String typeName = type.name;
            if ("CountryCode".equals(typeName)) {
                depUnionType = type;
            } else if ("NewCountryCode".equals(typeName)) {
                nonDepUnionType = type;
            }
        }

        Assert.assertNotNull(depUnionType);
        Assert.assertTrue(depUnionType.isDeprecated);

        Assert.assertNotNull(nonDepUnionType);
        Assert.assertFalse(nonDepUnionType.isDeprecated);
    }

    @Test(description = "Test @deprecated annotation for module-level finite type definitions")
    public void testDeprecatedFiniteTypeDef() {
        List<FiniteType> finiteTypes = testModule.finiteTypes;
        FiniteType depFiniteType = null;
        FiniteType nonDepFiniteType = null;

        for (FiniteType finiteType : finiteTypes) {
            String fTypeName = finiteType.name;
            if ("State".equals(fTypeName)) {
                depFiniteType = finiteType;
            } else if ("NewState".equals(fTypeName)) {
                nonDepFiniteType = finiteType;
            }
        }

        Assert.assertNotNull(depFiniteType);
        Assert.assertTrue(depFiniteType.isDeprecated);

        Assert.assertNotNull(nonDepFiniteType);
        Assert.assertFalse(nonDepFiniteType.isDeprecated);
    }

    @Test(description = "Test @deprecated annotation for module-level bClass type definitions")
    public void testDeprecatedObjectTypeDef() {
        List<BClass> classes = testModule.classes;
        BClass deprecatedClass = null;
        BClass nonDeprecatedClass = null;

        for (BClass bClass : classes) {
            String clsName = bClass.name;
            if ("Person".equals(clsName)) {
                deprecatedClass = bClass;
            } else if ("Student".equals(clsName)) {
                nonDeprecatedClass = bClass;
            }
        }

        testDeprecated(deprecatedClass);
        testNonDeprecated(nonDeprecatedClass);
    }

    @Test(description = "Test @deprecated annotation for module-level record type definitions")
    public void testDeprecatedRecordTypeDef() {
        List<Record> records = testModule.records;
        Record deprecatedRecord = null;
        Record nonDeprecatedRecord = null;

        for (Record record : records) {
            String recordName = record.name;
            if ("Address".equals(recordName)) {
                deprecatedRecord = record;
            } else if ("Bank".equals(recordName)) {
                nonDeprecatedRecord = record;
            }
        }

        testDeprecated(deprecatedRecord);
        testNonDeprecated(nonDeprecatedRecord);
    }

    @Test(description = "Test @deprecated annotation for module-level error type definitions")
    public void testDeprecatedErrorTypeDef() {
        List<Error> errTypes = testModule.errors;
        Error deprecatedErr = null;
        Error nonDeprecatedErr = null;

        for (Error error : errTypes) {
            String errName = error.name;
            if ("InvalidAccountTypeError".equals(errName)) {
                deprecatedErr = error;
            } else if ("InvalidBankTypeError".equals(errName)) {
                nonDeprecatedErr = error;
            }
        }

        testDeprecated(deprecatedErr);
        testNonDeprecated(nonDeprecatedErr);
    }

    @Test(description = "Test @deprecated annotation for module-level constant declarations")
    public void testDeprecatedConstantDecl() {
        List<Constant> constants = testModule.constants;
        Constant deprecatedLKA = null;
        Constant nonDeprecatedLK = null;
        Constant nonDeprecatedUSA = null;

        for (Constant constant : constants) {
            String constName = constant.name;
            if ("LKA".equals(constName)) {
                deprecatedLKA = constant;
            } else if ("LK".equals(constName)) {
                nonDeprecatedLK = constant;
            } else if ("USA".equals(constName)) {
                nonDeprecatedUSA = constant;
            }
        }

        testDeprecated(deprecatedLKA);
        testNonDeprecated(nonDeprecatedLK);
        testNonDeprecated(nonDeprecatedUSA);
    }

    @Test(description = "Test @deprecated annotation for module-level annotation declaration")
    public void testDeprecatedAnnotationDef() {
        List<Annotation> annotations = testModule.annotations;
        Annotation deprecatedAnnotation = null;
        Annotation nonDeprecatedAnnotation = null;

        for (Annotation annotation : annotations) {
            String annotationName = annotation.name;
            if ("Bye".equals(annotationName)) {
                deprecatedAnnotation = annotation;
            } else if ("Greeting".equals(annotationName)) {
                nonDeprecatedAnnotation = annotation;
            }
        }

        testDeprecated(deprecatedAnnotation);
        testNonDeprecated(nonDeprecatedAnnotation);
    }

    @Test(description = "Test @deprecated annotation for module-level function definition")
    public void testDeprecatedFunctionDef() {
        List<Function> functions = testModule.functions;
        Function deprecatedFunc = null;
        Function nonDeprecatedFunc = null;

        for (Function function : functions) {
            String funcName = function.name;
            if ("createPerson".equals(funcName)) {
                deprecatedFunc = function;
            } else if ("sayHello".equals(funcName)) {
                nonDeprecatedFunc = function;
            }
        }

        testDeprecated(deprecatedFunc);
        testNonDeprecated(nonDeprecatedFunc);
    }

    @Test(description = "Test @deprecated annotation for required and defaultable params")
    public void testFunctionParams() {
        List<Function> functions = testModule.functions;
        Function getFullNameFunc = null;

        for (Function function : functions) {
            if ("getFullName".equals(function.name)) {
                getFullNameFunc = function;
            }
        }

        // test function parameters
        Assert.assertNotNull(getFullNameFunc);
        for (Variable param : getFullNameFunc.parameters) {
            String paramName = param.name;
            if ("title".equals(paramName)) {
                testNonDeprecated(param);
            } else if ("fName".equals(paramName)) {
                testDeprecated(param);
            } else if ("mName".equals(paramName)) {
                testNonDeprecated(param);
            } else if ("lName".equals(paramName)) {
                testDeprecated(param);
            }
        }
    }

    @Test(description = "Test @deprecated annotation for bClass member method")
    public void testDeprecatedObjectMemberMethod() {
        List<BClass> bClasses = testModule.classes;
        BClass personClass = null;
        BClass studentClass = null;

        for (BClass cls : bClasses) {
            String clsName = cls.name;
            if ("Person".equals(clsName)) {
                personClass = cls;
            } else if ("Student".equals(clsName)) {
                studentClass = cls;
            }
        }

        // test deprecated bClass member methods
        Assert.assertNotNull(personClass);
        for (Function method : personClass.methods) {
            String methodName = method.name;
            if ("getFullName".equals(methodName)) {
                testDeprecated(method);
            } else if ("getCity".equals(methodName)) {
                testNonDeprecated(method);
            }
        }

        // test non-deprecated bClass member methods
        Assert.assertNotNull(studentClass);
        for (Function method : personClass.methods) {
            String methodName = method.name;
            if ("getName".equals(methodName)) {
                testDeprecated(method);
            } else if ("getAge".equals(methodName)) {
                testNonDeprecated(method);
            }
        }
    }

    @Test(description = "Test @deprecated annotation for bClass member field")
    public void testDeprecatedObjectMemberField() {
        List<BClass> bClasses = testModule.classes;
        BClass playerClass = null;

        for (BClass cls : bClasses) {
            String clsName = cls.name;
            if ("Player".equals(clsName)) {
                playerClass = cls;
            }
        }

        Assert.assertNotNull(playerClass);
        for (DefaultableVariable field : playerClass.fields) {
            String fieldName = field.name;
            if ("name".equals(fieldName)) {
                testDeprecated(field);
            } else if ("age".equals(fieldName)) {
                testNonDeprecated(field);
            }
        }
    }

    private void testDeprecated(Construct element) {
        Assert.assertNotNull(element);
        Assert.assertTrue(element.isDeprecated);
    }

    private void testNonDeprecated(Construct element) {
        Assert.assertNotNull(element);
        Assert.assertFalse(element.isDeprecated);
    }
}
