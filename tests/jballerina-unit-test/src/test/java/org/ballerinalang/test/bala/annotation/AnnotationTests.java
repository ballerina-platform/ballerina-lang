/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.bala.annotation;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.AnnotationAttachmentSymbol;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationAttachmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClassSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeDefinitionSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleMember;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangConstantValue;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Lists;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Test cases for reading annotations.
 */
public class AnnotationTests {

    private CompileResult result;
    private CompileResult birTestResult;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        result = BCompileUtil.compile("test-src/bala/test_bala/annotations/annotation.bal");

        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_annotation_project");
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_annotation_usage_project");
        birTestResult = BCompileUtil.compile("test-src/bala/test_bala/annotations/annot_attachments_bala_test.bal");
    }

    @Test
    public void testAnnotationsOnRecordFields() {
        BRunUtil.invoke(result, "testAnnotOnRecordFields");
    }

    @Test
    public void testAnnotationsOnTupleFields() {
        BRunUtil.invoke(result, "testAnnotOnTupleFields");
    }

    @Test(description = "Test the deprecated construct from external module")
    public void testDeprecation() {
        CompileResult result = BCompileUtil.compile("test-src/bala/test_bala/annotations/deprecation_annotation.bal");
        Assert.assertEquals(result.getWarnCount(), 14);

        int i = 0;
        BAssertUtil.validateWarning(result, i++, 
                "usage of construct 'testorg/foo:1.0.0:DummyObject1' is deprecated", 3, 23);
        BAssertUtil.validateWarning(result, i++, 
                "usage of construct 'testorg/foo:1.0.0:Bar' is deprecated", 3, 45);
        BAssertUtil.validateWarning(result, i++, 
                "usage of construct 'testorg/foo:1.0.0:C1' is deprecated", 3, 69);
        BAssertUtil.validateWarning(result, i++, 
                "usage of construct 'testorg/foo:1.0.0:DummyObject2.doThatOnObject' is deprecated", 8, 5);
        BAssertUtil.validateWarning(result, i++, 
                "usage of construct 'testorg/foo:1.0.0:DummyObject2.id' is deprecated", 9, 13);
        BAssertUtil.validateWarning(result, i++, 
                "usage of construct 'testorg/foo:1.0.0:deprecated_func' is deprecated", 11, 16);
        BAssertUtil.validateWarning(result, i++, 
                "usage of construct 'testorg/foo:1.0.0:deprecated_func' is deprecated", 12, 24);
        BAssertUtil.validateWarning(result, i++, 
                "usage of construct 'testorg/foo:1.0.0:DummyObject1' is deprecated", 14, 5);
        BAssertUtil.validateWarning(result, i++, 
                "usage of construct 'testorg/foo:1.0.0:DummyObject1' is deprecated", 15, 5);
        BAssertUtil.validateWarning(result, i++, 
                "usage of construct 'testorg/foo:1.0.0:deprecatedAnnotation' is deprecated", 18, 1);
        BAssertUtil.validateWarning(result, i++, 
                "usage of construct 'testorg/foo:1.0.0:deprecatedAnnotation' is deprecated", 24, 9);
        BAssertUtil.validateWarning(result, i++, 
                "usage of construct 'testorg/foo:1.0.0:MyClientObject' is deprecated", 28, 5);
        BAssertUtil.validateWarning(result, i++, 
                "usage of construct 'testorg/foo:1.0.0:MyClientObject.remoteFunction' is deprecated", 34, 5);
        BAssertUtil.validateWarning(result, i, "usage of construct 'a' is deprecated", 38, 9);
    }

    @Test
    public void testNonBallerinaAnnotations() {
        Object returns = BRunUtil.invoke(result, "testNonBallerinaAnnotations");
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"numVal\":10,\"textVal\":\"text\",\"conditionVal\":false," +
                "\"recordVal\":{\"nestNumVal\":20,\"nextTextVal\":\"nestText\"}}");
    }

    @Test
    public void testAnnotOnBoundMethod() {
        BRunUtil.invoke(result, "testAnnotOnBoundMethod");
    }

    @Test
    public void testParamAnnotAttachmentsViaBir() {
        BLangPackage bLangPackage = (BLangPackage) birTestResult.getAST();
        Map<Name, Scope.ScopeEntry> importedModuleEntries = bLangPackage.getImports().get(0).symbol.scope.entries;

        List<? extends AnnotationAttachmentSymbol> annotationAttachmentSymbols =
                ((BInvokableSymbol) importedModuleEntries.get(
                        Names.fromString("func")).symbol).params.get(0).getAnnotations();
        Assert.assertEquals(annotationAttachmentSymbols.size(), 1);
        BAnnotationAttachmentSymbol attachmentSymbol = (BAnnotationAttachmentSymbol) annotationAttachmentSymbols.get(0);
        PackageID pkgID = attachmentSymbol.annotPkgID;
        Assert.assertEquals(pkgID.orgName.value, "annots");
        Assert.assertEquals(pkgID.pkgName.value, "usage");
        Assert.assertEquals(pkgID.version.value, "0.2.0");
        Assert.assertEquals(attachmentSymbol.annotTag.value, "Allow");
        Assert.assertTrue(attachmentSymbol.isConstAnnotation());
        assertTrueAnnot(attachmentSymbol);
        BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol constAttachmentSymbol;

        BInvokableSymbol otherFunc = (BInvokableSymbol) importedModuleEntries.get(
                Names.fromString("otherFunc")).symbol;
        List<BVarSymbol> params = otherFunc.params;

        annotationAttachmentSymbols = params.get(0).getAnnotations();
        Assert.assertEquals(annotationAttachmentSymbols.size(), 0);

        annotationAttachmentSymbols = params.get(1).getAnnotations();
        Assert.assertEquals(annotationAttachmentSymbols.size(), 2);

        attachmentSymbol = (BAnnotationAttachmentSymbol) annotationAttachmentSymbols.get(0);
        pkgID = attachmentSymbol.annotPkgID;
        Assert.assertEquals(pkgID.orgName.value, "annots");
        Assert.assertEquals(pkgID.pkgName.value, "defn");
        Assert.assertEquals(pkgID.version.value, "0.0.1");
        Assert.assertEquals(attachmentSymbol.annotTag.value, "Annot");
        Assert.assertTrue(attachmentSymbol.isConstAnnotation());
        Object value = ((BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) attachmentSymbol)
                .attachmentValueSymbol.value.value;
        Assert.assertTrue(value instanceof Map);
        Map<String, BLangConstantValue> mapValue = (Map<String, BLangConstantValue>) value;
        Assert.assertEquals(mapValue.size(), 1);
        Assert.assertEquals(mapValue.get("i").value, 456L);

        attachmentSymbol = (BAnnotationAttachmentSymbol) annotationAttachmentSymbols.get(1);
        pkgID = attachmentSymbol.annotPkgID;
        Assert.assertEquals(pkgID.orgName.value, "annots");
        Assert.assertEquals(pkgID.pkgName.value, "defn");
        Assert.assertEquals(pkgID.version.value, "0.0.1");
        Assert.assertEquals(attachmentSymbol.annotTag.value, "NonConstAnnot");
        Assert.assertFalse(attachmentSymbol.isConstAnnotation());

        annotationAttachmentSymbols = otherFunc.restParam.getAnnotations();
        Assert.assertEquals(annotationAttachmentSymbols.size(), 1);
        attachmentSymbol = (BAnnotationAttachmentSymbol) annotationAttachmentSymbols.get(0);
        pkgID = attachmentSymbol.annotPkgID;
        Assert.assertEquals(pkgID.orgName.value, "annots");
        Assert.assertEquals(pkgID.pkgName.value, "usage");
        Assert.assertEquals(pkgID.version.value, "0.2.0");
        Assert.assertEquals(attachmentSymbol.annotTag.value, "Allow");
        Assert.assertTrue(attachmentSymbol.isConstAnnotation());
        constAttachmentSymbol = (BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) attachmentSymbol;
        Assert.assertEquals(constAttachmentSymbol.attachmentValueSymbol.type.tag, TypeTags.BOOLEAN);
        Assert.assertEquals(constAttachmentSymbol.attachmentValueSymbol.value.value, Boolean.TRUE);

        BInvokableSymbol anotherFunc = (BInvokableSymbol) importedModuleEntries.get(
                Names.fromString("anotherFunc")).symbol;
        params = anotherFunc.params;
        annotationAttachmentSymbols = params.get(0).getAnnotations();
        Assert.assertEquals(annotationAttachmentSymbols.size(), 1);
        attachmentSymbol = (BAnnotationAttachmentSymbol) annotationAttachmentSymbols.get(0);
        pkgID = attachmentSymbol.annotPkgID;
        Assert.assertEquals(pkgID.orgName.value, "annots");
        Assert.assertEquals(pkgID.pkgName.value, "usage");
        Assert.assertEquals(pkgID.version.value, "0.2.0");
        Assert.assertEquals(attachmentSymbol.annotTag.value, "NonConstAllow");
        Assert.assertFalse(attachmentSymbol.isConstAnnotation());

        BClassSymbol testListener =
                (BClassSymbol) importedModuleEntries.get(Names.fromString("TestListener")).symbol;

        params = testListener.initializerFunc.symbol.params;
        annotationAttachmentSymbols = params.get(0).getAnnotations();
        Assert.assertEquals(annotationAttachmentSymbols.size(), 0);
        annotationAttachmentSymbols = params.get(1).getAnnotations();
        Assert.assertEquals(annotationAttachmentSymbols.size(), 2);

        for (AnnotationAttachmentSymbol annotationAttachmentSymbol : annotationAttachmentSymbols) {
            attachmentSymbol = (BAnnotationAttachmentSymbol) annotationAttachmentSymbol;
            pkgID = attachmentSymbol.annotPkgID;
            Assert.assertEquals(pkgID.orgName.value, "annots");

            Assert.assertTrue(attachmentSymbol.isConstAnnotation());
            constAttachmentSymbol = (BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) attachmentSymbol;
            Assert.assertEquals(constAttachmentSymbol.attachmentValueSymbol.type.tag, TypeTags.BOOLEAN);
            Assert.assertEquals(constAttachmentSymbol.attachmentValueSymbol.value.value, Boolean.TRUE);

            String pkgName = pkgID.pkgName.value;

            if ("defn".equals(pkgName)) {
                Assert.assertEquals(pkgID.version.value, "0.0.1");
                Assert.assertEquals(attachmentSymbol.annotTag.value, "Expose");
                continue;
            }

            Assert.assertEquals(pkgName, "usage");
            Assert.assertEquals(pkgID.version.value, "0.2.0");
            Assert.assertEquals(attachmentSymbol.annotTag.value, "Allow");
        }

        BAttachedFunction attachMethod = null;
        BAttachedFunction detachMethod = null;

        for (BAttachedFunction attachedFunc : testListener.attachedFuncs) {
            String funcName = attachedFunc.funcName.value;

            if ("attach".equals(funcName)) {
                attachMethod = attachedFunc;
                continue;
            }

            if ("detach".equals(funcName)) {
                detachMethod = attachedFunc;
            }
        }

        params = attachMethod.symbol.params;
        annotationAttachmentSymbols = params.get(1).getAnnotations();
        Assert.assertEquals(annotationAttachmentSymbols.size(), 0);
        annotationAttachmentSymbols = params.get(0).getAnnotations();
        Assert.assertEquals(annotationAttachmentSymbols.size(), 1);
        attachmentSymbol = (BAnnotationAttachmentSymbol) annotationAttachmentSymbols.get(0);
        pkgID = attachmentSymbol.annotPkgID;
        Assert.assertEquals(pkgID.orgName.value, "annots");
        Assert.assertEquals(pkgID.pkgName.value, "defn");
        Assert.assertEquals(pkgID.version.value, "0.0.1");
        Assert.assertEquals(attachmentSymbol.annotTag.value, "Annot");
        constAttachmentSymbol = (BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) attachmentSymbol;
        value = constAttachmentSymbol.attachmentValueSymbol.value.value;
        Assert.assertTrue(value instanceof Map);
        mapValue = (Map<String, BLangConstantValue>) value;
        Assert.assertEquals(mapValue.size(), 1);
        Assert.assertEquals(mapValue.get("i").value, 1L);
        BType type = constAttachmentSymbol.attachmentValueSymbol.type;
        Assert.assertEquals(type.tag, TypeTags.INTERSECTION);
        BIntersectionType intersectionType = (BIntersectionType) type;
        BType effectiveType = intersectionType.effectiveType;
        Assert.assertEquals(effectiveType.tag, TypeTags.RECORD);

        params = detachMethod.symbol.params;
        annotationAttachmentSymbols = params.get(0).getAnnotations();
        Assert.assertEquals(annotationAttachmentSymbols.size(), 2);

        List<Long> members = Lists.of(2L, 3L);

        for (AnnotationAttachmentSymbol annotationAttachmentSymbol : annotationAttachmentSymbols) {
            attachmentSymbol = (BAnnotationAttachmentSymbol) annotationAttachmentSymbol;
            pkgID = attachmentSymbol.annotPkgID;
            Assert.assertEquals(pkgID.orgName.value, "annots");
            Assert.assertEquals(pkgID.pkgName.value, "defn");
            Assert.assertEquals(pkgID.version.value, "0.0.1");
            Assert.assertEquals(attachmentSymbol.annotTag.value, "Annots");
            constAttachmentSymbol = (BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) attachmentSymbol;
            value = constAttachmentSymbol.attachmentValueSymbol.value.value;
            Assert.assertTrue(value instanceof Map);
            mapValue = (Map<String, BLangConstantValue>) value;
            Assert.assertEquals(mapValue.size(), 1);
            Assert.assertTrue(members.remove(mapValue.get("i").value));
            type = constAttachmentSymbol.attachmentValueSymbol.type;
            Assert.assertEquals(type.tag, TypeTags.INTERSECTION);
            type = ((BIntersectionType) type).effectiveType;
            Assert.assertEquals(type.tag, TypeTags.RECORD);
        }
    }

    @Test
    public void testModuleLevelVariableAnnotAttachmentsViaBir() {
        BLangPackage bLangPackage = (BLangPackage) birTestResult.getAST();
        Map<Name, Scope.ScopeEntry> importedModuleEntries = bLangPackage.getImports().get(0).symbol.scope.entries;

        List<? extends AnnotationAttachmentSymbol> annotationAttachmentSymbols =
                ((BVarSymbol) importedModuleEntries.get(Names.fromString("iVal")).symbol).getAnnotations();
        Assert.assertEquals(annotationAttachmentSymbols.size(), 0);

        annotationAttachmentSymbols =
                ((BVarSymbol) importedModuleEntries.get(Names.fromString("jVal")).symbol).getAnnotations();
        Assert.assertEquals(annotationAttachmentSymbols.size(), 2);
        BAnnotationAttachmentSymbol attachmentSymbol = (BAnnotationAttachmentSymbol) annotationAttachmentSymbols.get(0);
        PackageID pkgID = attachmentSymbol.annotPkgID;
        Assert.assertEquals(pkgID.orgName.value, "annots");
        Assert.assertEquals(pkgID.pkgName.value, "usage");
        Assert.assertEquals(pkgID.version.value, "0.2.0");
        Assert.assertEquals(attachmentSymbol.annotTag.value, "Allow");
        Assert.assertTrue(attachmentSymbol.isConstAnnotation());
        assertTrueAnnot(attachmentSymbol);
        BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol constAttachmentSymbol;

        attachmentSymbol = (BAnnotationAttachmentSymbol) annotationAttachmentSymbols.get(1);
        pkgID = attachmentSymbol.annotPkgID;
        Assert.assertEquals(pkgID.orgName.value, "annots");
        Assert.assertEquals(pkgID.pkgName.value, "defn");
        Assert.assertEquals(pkgID.version.value, "0.0.1");
        Assert.assertEquals(attachmentSymbol.annotTag.value, "Annot");
        Assert.assertTrue(attachmentSymbol.isConstAnnotation());
        constAttachmentSymbol = (BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) attachmentSymbol;
        Object value = constAttachmentSymbol.attachmentValueSymbol.value.value;
        Assert.assertTrue(value instanceof Map);
        Map<String, BLangConstantValue> mapValue = (Map<String, BLangConstantValue>) value;
        Assert.assertEquals(mapValue.size(), 1);
        Assert.assertEquals(mapValue.get("i").value, 321L);
    }

    @Test
    public void testFieldAnnotAttachmentsViaBir() {
        BLangPackage bLangPackage = (BLangPackage) birTestResult.getAST();
        Map<Name, Scope.ScopeEntry> importedModuleEntries = bLangPackage.getImports().get(0).symbol.scope.entries;

        BTypeDefinitionSymbol symbol =
                ((BTypeDefinitionSymbol) importedModuleEntries.get(Names.fromString("Recx")).symbol);
        Assert.assertEquals(symbol.getAnnotations().size(), 1);
        LinkedHashMap<String, BField> fields = ((BRecordType) symbol.type).fields;
        List<? extends AnnotationAttachmentSymbol> f1 = fields.get("x1").symbol.getAnnotations();

        BAnnotationAttachmentSymbol f1a1 = ((BAnnotationAttachmentSymbol) f1.get(0));
        PackageID pkgID = f1a1.annotPkgID;
        Assert.assertEquals(pkgID.orgName.value, "annots");
        Assert.assertEquals(pkgID.pkgName.value, "usage");
        Assert.assertEquals(pkgID.version.value, "0.2.0");
        Assert.assertEquals(f1a1.annotTag.value, "Member");
        Assert.assertTrue(f1a1.isConstAnnotation());
        BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol constAttachmentSymbol =
                (BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) f1a1;
        Assert.assertEquals(constAttachmentSymbol.attachmentValueSymbol.type.tag, TypeTags.BOOLEAN);
        Assert.assertEquals(constAttachmentSymbol.attachmentValueSymbol.value.value, Boolean.TRUE);
    }

    @Test
    public void testTupleMemberAnnotAttachmentsViaBir() {
        BLangPackage bLangPackage = (BLangPackage) birTestResult.getAST();
        Map<Name, Scope.ScopeEntry> importedModuleEntries = bLangPackage.getImports().get(0).symbol.scope.entries;

        BTypeDefinitionSymbol symbol =
                ((BTypeDefinitionSymbol) importedModuleEntries.get(Names.fromString("Tup")).symbol);
        Assert.assertEquals(symbol.getAnnotations().size(), 1);
        List<BTupleMember> members = ((BTupleType) symbol.type).getMembers();
        List<? extends AnnotationAttachmentSymbol> m1 = members.get(0).symbol.getAnnotations();

        BAnnotationAttachmentSymbol m1a1 = ((BAnnotationAttachmentSymbol) m1.get(0));
        PackageID pkgID = m1a1.annotPkgID;
        Assert.assertEquals(pkgID.orgName.value, "annots");
        Assert.assertEquals(pkgID.pkgName.value, "usage");
        Assert.assertEquals(pkgID.version.value, "0.2.0");
        Assert.assertEquals(m1a1.annotTag.value, "Member");
        Assert.assertTrue(m1a1.isConstAnnotation());
        BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol constAttachmentSymbol =
                (BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) m1a1;
        Assert.assertEquals(constAttachmentSymbol.attachmentValueSymbol.type.tag, TypeTags.BOOLEAN);
        Assert.assertEquals(constAttachmentSymbol.attachmentValueSymbol.value.value, Boolean.TRUE);

        symbol = ((BTypeDefinitionSymbol) importedModuleEntries.get(Names.fromString("T1")).symbol);
        Assert.assertEquals(symbol.getAnnotations().size(), 0);
        members = ((BTupleType) symbol.type).getMembers();
        m1 = members.get(1).symbol.getAnnotations();

        m1a1 = ((BAnnotationAttachmentSymbol) m1.get(0));
        Assert.assertEquals(m1a1.annotTag.value, "Member");
        Assert.assertTrue(m1a1.isConstAnnotation());
        constAttachmentSymbol = (BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) m1a1;
        Assert.assertEquals(constAttachmentSymbol.attachmentValueSymbol.type.tag, TypeTags.BOOLEAN);
        Assert.assertEquals(constAttachmentSymbol.attachmentValueSymbol.value.value, Boolean.TRUE);

        symbol = ((BTypeDefinitionSymbol) importedModuleEntries.get(Names.fromString("T2")).symbol);
        Assert.assertEquals(symbol.getAnnotations().size(), 0);
        members = ((BTupleType) symbol.type).getMembers();
        m1 = members.get(1).symbol.getAnnotations();

        m1a1 = ((BAnnotationAttachmentSymbol) m1.get(0));
        Assert.assertEquals(m1a1.annotTag.value, "Member");
        Assert.assertTrue(m1a1.isConstAnnotation());
        constAttachmentSymbol = (BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) m1a1;
        Assert.assertEquals(constAttachmentSymbol.attachmentValueSymbol.type.tag, TypeTags.BOOLEAN);
        Assert.assertEquals(constAttachmentSymbol.attachmentValueSymbol.value.value, Boolean.TRUE);
    }

    @Test
    public void testAnnotDeclarationAnnotAttachmentsViaBir() {
        BLangPackage bLangPackage = (BLangPackage) birTestResult.getAST();
        Map<Name, Scope.ScopeEntry> importedModuleEntries = bLangPackage.getImports().get(0).symbol.scope.entries;

        List<? extends AnnotationAttachmentSymbol> annotationAttachmentSymbols =
                ((BAnnotationSymbol) importedModuleEntries.get(Names.fromString("Allow")).symbol).getAnnotations();
        Assert.assertEquals(annotationAttachmentSymbols.size(), 0);

        annotationAttachmentSymbols =
                ((BAnnotationSymbol) importedModuleEntries.get(Names.fromString("Custom")).symbol).getAnnotations();
        Assert.assertEquals(annotationAttachmentSymbols.size(), 0);

        annotationAttachmentSymbols =
                ((BAnnotationSymbol) importedModuleEntries.get(
                        Names.fromString("NonConstAllow")).symbol).getAnnotations();
        Assert.assertEquals(annotationAttachmentSymbols.size(), 1);
        BAnnotationAttachmentSymbol attachmentSymbol = (BAnnotationAttachmentSymbol) annotationAttachmentSymbols.get(0);
        PackageID pkgID = attachmentSymbol.annotPkgID;
        Assert.assertEquals(pkgID.orgName.value, "annots");
        Assert.assertEquals(pkgID.pkgName.value, "usage");
        Assert.assertEquals(pkgID.version.value, "0.2.0");
        Assert.assertEquals(attachmentSymbol.annotTag.value, "Custom");
        Assert.assertTrue(attachmentSymbol.isConstAnnotation());
        assertTrueAnnot(attachmentSymbol);
    }

    @Test
    public void testConstDeclarationAnnotAttachmentsViaBir() {
        BLangPackage bLangPackage = (BLangPackage) birTestResult.getAST();
        Map<Name, Scope.ScopeEntry> importedModuleEntries = bLangPackage.getImports().get(0).symbol.scope.entries;

        List<? extends AnnotationAttachmentSymbol> annotationAttachmentSymbols =
                ((BConstantSymbol) importedModuleEntries.get(Names.fromString("C")).symbol).getAnnotations();
        Assert.assertEquals(annotationAttachmentSymbols.size(), 1);
        BAnnotationAttachmentSymbol attachmentSymbol = (BAnnotationAttachmentSymbol) annotationAttachmentSymbols.get(0);
        PackageID pkgID = attachmentSymbol.annotPkgID;
        Assert.assertEquals(pkgID.orgName.value, "annots");
        Assert.assertEquals(pkgID.pkgName.value, "defn");
        Assert.assertEquals(pkgID.version.value, "0.0.1");
        Assert.assertEquals(attachmentSymbol.annotTag.value, "KnownConst");
        Assert.assertTrue(attachmentSymbol.isConstAnnotation());
        assertTrueAnnot(attachmentSymbol);

        annotationAttachmentSymbols =
                ((BConstantSymbol) importedModuleEntries.get(Names.fromString("D")).symbol).getAnnotations();
        Assert.assertEquals(annotationAttachmentSymbols.size(), 0);
    }

    @Test
    public void testSourceAnnotsWithConstLists() {
        BLangPackage bLangPackage = (BLangPackage) birTestResult.getAST();
        Map<Name, Scope.ScopeEntry> importedModuleEntries = bLangPackage.getImports().get(0).symbol.scope.entries;
        BClassSymbol classSymbol =
                (BClassSymbol) importedModuleEntries.get(Names.fromString("Cl")).symbol;
        List<? extends AnnotationAttachmentSymbol> attachments = classSymbol.getAnnotations();
        Assert.assertEquals(attachments.size(), 1);

        BAnnotationAttachmentSymbol annotationAttachmentSymbol = (BAnnotationAttachmentSymbol) attachments.get(0);
        PackageID pkgID = annotationAttachmentSymbol.annotPkgID;
        Assert.assertEquals(pkgID.orgName.value, "annots");
        Assert.assertEquals(pkgID.pkgName.value, "usage");
        Assert.assertEquals(pkgID.version.value, "0.2.0");
        Assert.assertEquals(annotationAttachmentSymbol.annotTag.value, "ClassAnnot");
        Assert.assertTrue(annotationAttachmentSymbol.isConstAnnotation());

        Object constValue =
                ((BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) annotationAttachmentSymbol)
                        .attachmentValueSymbol.value.value;
        Assert.assertTrue(constValue instanceof Map);

        Map<String, BLangConstantValue> annotMapValue = (Map<String, BLangConstantValue>) constValue;
        Assert.assertEquals(annotMapValue.size(), 2);

        Assert.assertTrue(annotMapValue.containsKey("f1"));
        Object f1ConstValue = annotMapValue.get("f1").value;
        Assert.assertTrue(f1ConstValue instanceof List);
        List<BLangConstantValue> f1 = (List<BLangConstantValue>) f1ConstValue;
        Assert.assertEquals(f1.size(), 2);

        Object f1Member1 = f1.get(0).value;
        Assert.assertTrue(f1Member1 instanceof Map);
        Map<String, BLangConstantValue> f1Member1Map = (Map<String, BLangConstantValue>) f1Member1;
        Assert.assertEquals(f1Member1Map.size(), 2);
        Assert.assertEquals(f1Member1Map.get("s").value, "s");
        Assert.assertEquals(f1Member1Map.get("t").value, "t");

        Object f1Member2 = f1.get(1).value;
        Assert.assertTrue(f1Member2 instanceof Map);
        Map<String, BLangConstantValue> f1Member2Map = (Map<String, BLangConstantValue>) f1Member2;
        Assert.assertEquals(f1Member2Map.size(), 2);
        Assert.assertEquals(f1Member2Map.get("s").value, "s2");
        Assert.assertEquals(f1Member2Map.get("t").value, "t2");

        Assert.assertTrue(annotMapValue.containsKey("f2"));
        Object f2ConstValue = annotMapValue.get("f2").value;
        Assert.assertTrue(f2ConstValue instanceof List);
        List<BLangConstantValue> f2 = (List<BLangConstantValue>) f2ConstValue;
        Assert.assertEquals(f2.size(), 2);

        Object f2Member1 = f2.get(0).value;
        Assert.assertTrue(f2Member1 instanceof Map);
        Map<String, BLangConstantValue> f2Member1Map = (Map<String, BLangConstantValue>) f2Member1;
        Assert.assertEquals(f2Member1Map.size(), 2);
        Assert.assertEquals(f2Member1Map.get("s").value, "s3");
        Assert.assertEquals(f2Member1Map.get("t").value, "t3");

        Assert.assertEquals(f2.get(1).value, "test");
    }

    @Test
    public void testNonSourceAnnotsWithConstLists() {
        BLangPackage bLangPackage = (BLangPackage) birTestResult.getAST();
        Map<Name, Scope.ScopeEntry> importedModuleEntries = bLangPackage.getImports().get(0).symbol.scope.entries;
        BTypeDefinitionSymbol classSymbol =
                (BTypeDefinitionSymbol) importedModuleEntries.get(Names.fromString("TypeWithListInAnnots")).symbol;
        List<? extends AnnotationAttachmentSymbol> attachments = classSymbol.getAnnotations();
        Assert.assertEquals(attachments.size(), 1);

        BAnnotationAttachmentSymbol annotationAttachmentSymbol = (BAnnotationAttachmentSymbol) attachments.get(0);
        PackageID pkgID = annotationAttachmentSymbol.annotPkgID;
        Assert.assertEquals(pkgID.orgName.value, "annots");
        Assert.assertEquals(pkgID.pkgName.value, "usage");
        Assert.assertEquals(pkgID.version.value, "0.2.0");
        Assert.assertEquals(annotationAttachmentSymbol.annotTag.value, "AnnotWithList");
        Assert.assertTrue(annotationAttachmentSymbol.isConstAnnotation());

        Object constValue =
                ((BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) annotationAttachmentSymbol)
                        .attachmentValueSymbol.value.value;
        Assert.assertTrue(constValue instanceof Map);

        Map<String, BLangConstantValue> annotMapValue = (Map<String, BLangConstantValue>) constValue;
        Assert.assertEquals(annotMapValue.size(), 1);

        Assert.assertTrue(annotMapValue.containsKey("arr"));
        Object arr = annotMapValue.get("arr").value;
        Assert.assertTrue(arr instanceof List);
        List<BLangConstantValue> arrConst = (List<BLangConstantValue>) arr;
        Assert.assertEquals(arrConst.get(0).value, 1L);
        Assert.assertEquals(arrConst.get(1).value, 2L);
        Assert.assertEquals(arrConst.get(2).value, 3L);
    }

    @Test
    public void testFunctionAnnotAttachmentsViaBir() {
        BLangPackage bLangPackage = (BLangPackage) birTestResult.getAST();
        Map<Name, Scope.ScopeEntry> importedModuleEntries = bLangPackage.getImports().get(0).symbol.scope.entries;

        BInvokableSymbol invokableSymbol =
                (BInvokableSymbol) importedModuleEntries.get(Names.fromString("fn1")).symbol;
        List<? extends AnnotationAttachmentSymbol> attachments = invokableSymbol.getAnnotations();
        Assert.assertEquals(attachments.size(), 1);

        BAnnotationAttachmentSymbol annotationAttachmentSymbol = (BAnnotationAttachmentSymbol) attachments.get(0);
        PackageID pkgID = annotationAttachmentSymbol.annotPkgID;
        Assert.assertEquals(pkgID.orgName.value, "annots");
        Assert.assertEquals(pkgID.pkgName.value, "usage");
        Assert.assertEquals(pkgID.version.value, "0.2.0");
        Assert.assertEquals(annotationAttachmentSymbol.annotTag.value, "FunctionAnnot");
        Assert.assertTrue(annotationAttachmentSymbol.isConstAnnotation());
        assertTrueAnnot(annotationAttachmentSymbol);

        attachments = ((BInvokableTypeSymbol) invokableSymbol.type.tsymbol).returnTypeAnnots;
        Assert.assertEquals(attachments.size(), 1);
        annotationAttachmentSymbol = (BAnnotationAttachmentSymbol) attachments.get(0);
        pkgID = annotationAttachmentSymbol.annotPkgID;
        Assert.assertEquals(pkgID.orgName.value, "annots");
        Assert.assertEquals(pkgID.pkgName.value, "usage");
        Assert.assertEquals(pkgID.version.value, "0.2.0");
        Assert.assertEquals(annotationAttachmentSymbol.annotTag.value, "ReturnAnnot");
        Assert.assertTrue(annotationAttachmentSymbol.isConstAnnotation());
        assertTrueAnnot(annotationAttachmentSymbol);

        invokableSymbol = (BInvokableSymbol) importedModuleEntries.get(Names.fromString("fn2")).symbol;
        Assert.assertEquals(invokableSymbol.getAnnotations().size(), 0);
        Assert.assertEquals(((BInvokableTypeSymbol) invokableSymbol.type.tsymbol).returnTypeAnnots.size(), 0);
    }

    @Test
    public void testMethodAnnotAttachmentsViaBir() {
        BLangPackage bLangPackage = (BLangPackage) birTestResult.getAST();
        Map<Name, Scope.ScopeEntry> importedModuleEntries = bLangPackage.getImports().get(0).symbol.scope.entries;

        BClassSymbol classSymbol = (BClassSymbol) importedModuleEntries.get(Names.fromString("Cl2")).symbol;
        BInvokableSymbol invokableSymbol = classSymbol.attachedFuncs.stream()
                .filter(method -> method.funcName.value.equals("cfn1"))
                .findFirst().get().symbol;
        Assert.assertEquals(invokableSymbol.getAnnotations().size(), 0);

        List<? extends AnnotationAttachmentSymbol> attachments =
                ((BInvokableTypeSymbol) invokableSymbol.type.tsymbol).returnTypeAnnots;
        Assert.assertEquals(attachments.size(), 1);
        BAnnotationAttachmentSymbol annotationAttachmentSymbol = (BAnnotationAttachmentSymbol) attachments.get(0);
        PackageID pkgID = annotationAttachmentSymbol.annotPkgID;
        Assert.assertEquals(pkgID.orgName.value, "annots");
        Assert.assertEquals(pkgID.pkgName.value, "usage");
        Assert.assertEquals(pkgID.version.value, "0.2.0");
        Assert.assertEquals(annotationAttachmentSymbol.annotTag.value, "ReturnAnnot");
        Assert.assertTrue(annotationAttachmentSymbol.isConstAnnotation());
        assertTrueAnnot(annotationAttachmentSymbol);

        invokableSymbol = classSymbol.attachedFuncs.stream().filter(method -> method.funcName.value.equals("cfn2"))
                .findFirst().get().symbol;
        attachments = invokableSymbol.getAnnotations();
        Assert.assertEquals(attachments.size(), 1);
        annotationAttachmentSymbol = (BAnnotationAttachmentSymbol) attachments.get(0);
        pkgID = annotationAttachmentSymbol.annotPkgID;
        Assert.assertEquals(pkgID.orgName.value, "annots");
        Assert.assertEquals(pkgID.pkgName.value, "usage");
        Assert.assertEquals(pkgID.version.value, "0.2.0");
        Assert.assertEquals(annotationAttachmentSymbol.annotTag.value, "FunctionAnnot");
        Assert.assertTrue(annotationAttachmentSymbol.isConstAnnotation());
        assertTrueAnnot(annotationAttachmentSymbol);

        Assert.assertEquals(((BInvokableTypeSymbol) invokableSymbol.type.tsymbol).returnTypeAnnots.size(), 0);
    }

    private void assertTrueAnnot(BAnnotationAttachmentSymbol annotAttachmentSymbol) {
        BConstantSymbol attachmentValueSymbol =
                ((BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) annotAttachmentSymbol)
                        .attachmentValueSymbol;
        Assert.assertEquals(attachmentValueSymbol.type.tag, TypeTags.BOOLEAN);
        Assert.assertEquals(attachmentValueSymbol.value.value, Boolean.TRUE);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
