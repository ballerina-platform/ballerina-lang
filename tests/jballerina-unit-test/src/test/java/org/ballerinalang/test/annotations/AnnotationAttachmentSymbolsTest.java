/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.annotations;

import org.ballerinalang.model.symbols.AnnotationAttachmentSymbol;
import org.ballerinalang.model.tree.ClassDefinition;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.TypeDefinition;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.desugar.AnnotationDesugar;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationAttachmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClassSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeDefinitionSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangConstantValue;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Class to test annotation attachments.
 *
 * @since 2.1.0
 */
public class AnnotationAttachmentSymbolsTest {

    private CompileResult compileResult;
    private CompileResult listAnnotFieldResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/annotations/annot_attachments.bal");
        listAnnotFieldResult = BCompileUtil.compile("test-src/annotations/annots_with_list_consts.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
    }

    @Test
    public void testAnnotOnType() {
        List<? extends AnnotationAttachmentSymbol> attachments =
                ((BTypeDefinitionSymbol) getTypeDefinition(compileResult.getAST().getTypeDefinitions(), "T1").symbol)
                        .getAnnotations();
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v1");
    }

    @Test
    public void testAnnotOnObjectType() {
        List<? extends AnnotationAttachmentSymbol> attachments =
                ((BClassSymbol) getClassDefinition(((BLangPackage) compileResult.getAST()).topLevelNodes, "T2").symbol)
                        .getAnnotations();
        Assert.assertEquals(attachments.size(), 2);
        assertAttachmentSymbol(attachments.get(0), "v1");
        assertAttachmentSymbol(attachments.get(1), "v2");
    }

    @Test
    public void testAnnotOnObjectFunction() {
        BLangFunction function = getFunction("T2.setName");
        List<? extends AnnotationAttachmentSymbol> attachments = function.symbol.getAnnotations();
        Assert.assertEquals(attachments.size(), 2);
        assertAttachmentSymbol(attachments.get(0), "v3");
        assertAttachmentSymbol(attachments.get(1), "v4");

        attachments = function.symbol.params.get(0).getAnnotations();
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v6");

        attachments = function.symbol.params.get(1).getAnnotations();
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v6");

        attachments = function.symbol.restParam.getAnnotations();
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v6");

        attachments = ((BInvokableTypeSymbol) function.symbol.type.tsymbol).returnTypeAnnots;
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v7");
    }

    @Test
    public void testAnnotOnFunction() {
        BLangFunction function = getFunction("func");
        List<? extends AnnotationAttachmentSymbol> attachments = function.symbol.getAnnotations();
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v3");

        attachments = function.symbol.params.get(0).getAnnotations();
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v6");

        attachments = function.symbol.params.get(1).getAnnotations();
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v6");

        attachments = function.symbol.restParam.getAnnotations();
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v6");

        attachments = ((BInvokableTypeSymbol) function.symbol.type.tsymbol).returnTypeAnnots;
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v7");
    }

    @Test
    public void testAnnotOnListener() {
        List<AnnotationAttachmentSymbol> attachments = new ArrayList<>();
        for (BLangVariable globalVar : compileResult.getAST().getGlobalVariables()) {
            if (((BLangSimpleVariable) globalVar).getName().getValue().equals("lis")) {
                attachments.addAll(globalVar.symbol.getAnnotations());
                break;
            }
        }
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v9", true, "val", "v91");
    }

    @Test
    public void testAnnotOnServiceOne() {
        BClassSymbol ser = (BClassSymbol) ((BLangClassDefinition) compileResult.getAST().getServices().stream()
                .filter(serviceNode ->
                                serviceNode.getAbsolutePath().stream().anyMatch(p -> p.getValue().contains("ser")))
                .findFirst()
                .get().getServiceClass()).symbol;
        List<BAnnotationAttachmentSymbol> attachments = getServiceAnnotsWithoutIntrospectionAnnot(ser.getAnnotations());
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v8");
    }

    @Test
    public void testIntrospectionInfoAnnot() {
        BServiceSymbol symbol = (BServiceSymbol) ((BLangService) compileResult.getAST()
                .getServices().stream()
                .filter(serviceNode ->
                        serviceNode.getAbsolutePath().stream().anyMatch(p -> p.getValue().contains("introspection")))
                .findFirst().get()).symbol;

        String serviceId = String.format("%d",
                                         Objects.hash(symbol.getOriginalName().getValue(), symbol.pkgID,
                                                      symbol.getPosition().lineRange()));

        List<? extends AnnotationAttachmentSymbol> attachments = symbol.getAssociatedClassSymbol().getAnnotations();
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), AnnotationDesugar.SERVICE_INTROSPECTION_INFO_ANN, true, "name",
                               serviceId);
    }

    @Test
    public void testAnnotOnResourceOne() {
        BLangFunction function = getFunction("$anonType$_0.$get$res");
        List<? extends AnnotationAttachmentSymbol> attachments = function.symbol.getAnnotations();
        Assert.assertEquals(attachments.size(), 2);
        assertAttachmentSymbol(attachments.get(0), "v3");
        assertAttachmentSymbol(attachments.get(1), "v5");

        attachments = function.symbol.params.get(0).getAnnotations();
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v6");

        attachments = ((BInvokableTypeSymbol) function.symbol.type.tsymbol).returnTypeAnnots;
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v7");
    }

    @Test
    public void testAnnotOnServiceTwo() {
        ClassDefinition ser = compileResult.getAST().getClassDefinitions().stream()
                .filter(classNode -> classNode.getName().getValue().equals("$anonType$_1"))
                .findFirst()
                .get();
        List<BAnnotationAttachmentSymbol> attachments =
                getServiceAnnotsWithoutIntrospectionAnnot(
                        ((BClassSymbol) ((BLangClassDefinition) ser).symbol).getAnnotations());
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v8");
    }

    @Test
    public void testAnnotOnResourceTwo() {
        BLangFunction function = getFunction("$anonType$_1.$get$res");
        List<? extends AnnotationAttachmentSymbol> attachments = function.symbol.getAnnotations();
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v5");

        attachments = function.symbol.params.get(0).getAnnotations();
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v6");

        attachments = ((BInvokableTypeSymbol) function.symbol.type.tsymbol).returnTypeAnnots;
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v7");
    }

    @Test
    public void testAnnotOnAnnot() {
        List<? extends AnnotationAttachmentSymbol> attachments =
                ((BAnnotationSymbol) ((BLangAnnotation) compileResult.getAST().getAnnotations().stream()
                        .filter(annotationNode -> annotationNode.getName().toString().equals("v14"))
                        .findFirst()
                        .get()).symbol)
                        .getAnnotations();
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v10", true, "str", "v10 value");
    }

    @Test
    public void testAnnotOnConst() {
        List<? extends AnnotationAttachmentSymbol> attachments =
                ((BLangConstant) compileResult.getAST().getConstants().stream()
                        .filter(constant -> ((BLangConstant) constant).name.toString().equals("F"))
                        .findFirst()
                        .get()).symbol.getAnnotations();
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v12", true, "str", "v12 value");
    }

    @Test
    public void testAnnotWithEmptyMapConstructorOnType() {
        List<? extends AnnotationAttachmentSymbol> attachments = ((BTypeDefinitionSymbol)
                getTypeDefinition(compileResult.getAST().getTypeDefinitions(), "MyType2").symbol).getAnnotations();
        Assert.assertEquals(attachments.size(), 2);

        // Should come from the default value of the field, doesn't atm.
        // https://github.com/ballerina-platform/ballerina-lang/issues/35348
        // assertAttachmentSymbol(attachments.get(0), "v25", true, "val", "ABC");
        // Replace the assertion for v25 with the line above once fixed.
        BAnnotationAttachmentSymbol annotationAttachmentSymbol = (BAnnotationAttachmentSymbol) attachments.get(0);
        Assert.assertEquals(annotationAttachmentSymbol.annotTag.value, "v25");
        Assert.assertTrue(annotationAttachmentSymbol.isConstAnnotation());
        Object constValue =
                ((BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) annotationAttachmentSymbol)
                        .attachmentValueSymbol.value.value;
        Assert.assertTrue(constValue instanceof Map);
        Map<String, BLangConstantValue> mapConst = (Map<String, BLangConstantValue>) constValue;
        Assert.assertEquals(mapConst.size(), 0);

        annotationAttachmentSymbol = (BAnnotationAttachmentSymbol) attachments.get(1);
        Assert.assertEquals(annotationAttachmentSymbol.annotTag.value, "v26");
        Assert.assertTrue(annotationAttachmentSymbol.isConstAnnotation());
        constValue =
                ((BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) annotationAttachmentSymbol)
                        .attachmentValueSymbol.value.value;
        Assert.assertTrue(constValue instanceof Map);
        mapConst = (Map<String, BLangConstantValue>) constValue;
        Assert.assertEquals(mapConst.size(), 0);
    }

    @Test
    public void testSourceAnnotsWithConstLists() {
        List<BAnnotationAttachmentSymbol> attachments = getServiceAnnotsWithoutIntrospectionAnnot(
                ((BClassSymbol) ((BLangClassDefinition)
                        listAnnotFieldResult.getAST().getClassDefinitions().get(0)).symbol).getAnnotations());
        Assert.assertEquals(attachments.size(), 1);

        BAnnotationAttachmentSymbol annotationAttachmentSymbol = attachments.get(0);
        Assert.assertEquals(annotationAttachmentSymbol.annotTag.value, "v1");
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
    public void testConstAnnotWithUnaryExpr() {
        List<? extends AnnotationAttachmentSymbol> attachments =
                ((BTypeDefinitionSymbol) getTypeDefinition(compileResult.getAST().getTypeDefinitions(), "Qux").symbol)
                        .getAnnotations();
        Assert.assertEquals(attachments.size(), 1);
        assertAttachmentSymbol(attachments.get(0), "v29", true, "increment", -1L);
    }

    @Test
    public void testNonSourceAnnotsWithConstLists() {
        List<? extends AnnotationAttachmentSymbol> attachments =
                ((BTypeDefinitionSymbol) getTypeDefinition(
                        listAnnotFieldResult.getAST().getTypeDefinitions(), "Baz").symbol).getAnnotations();
        Assert.assertEquals(attachments.size(), 1);
        BAnnotationAttachmentSymbol annotationAttachmentSymbol = (BAnnotationAttachmentSymbol) attachments.get(0);
        Assert.assertEquals(annotationAttachmentSymbol.annotTag.value, "v2");
        Assert.assertTrue(annotationAttachmentSymbol.isConstAnnotation());
        Object constValue =
                ((BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) annotationAttachmentSymbol)
                        .attachmentValueSymbol.value.value;
        Assert.assertTrue(constValue instanceof Map);

        Map<String, BLangConstantValue> annotMapValue = (Map<String, BLangConstantValue>) constValue;
        Assert.assertEquals(annotMapValue.size(), 1);

        Assert.assertTrue(annotMapValue.containsKey("arr"));
        Object arrValue = annotMapValue.get("arr").value;
        Assert.assertTrue(arrValue instanceof List);
        List<BLangConstantValue> arrList = (List<BLangConstantValue>) arrValue;
        Assert.assertEquals(arrList.size(), 2);

        Assert.assertEquals(arrList.get(0).value, 1L);
        Assert.assertEquals(arrList.get(1).value, 2L);
    }

    private BLangTypeDefinition getTypeDefinition(List<? extends TypeDefinition> typeDefinitions, String name) {
        for (TypeDefinition typeDefinition : typeDefinitions) {
            BLangTypeDefinition bLangTypeDefinition = (BLangTypeDefinition) typeDefinition;
            if (name.equals(bLangTypeDefinition.symbol.name.value)) {
                return bLangTypeDefinition;
            }
        }
        throw new RuntimeException("Type Definition '" + name + "' not found.");
    }

    private BLangClassDefinition getClassDefinition(List<? extends TopLevelNode> typeDefinitions, String name) {
        for (TopLevelNode topLevelNode : typeDefinitions) {
            if (topLevelNode.getKind() != NodeKind.CLASS_DEFN) {
                continue;
            }

            BLangClassDefinition classDefinition = (BLangClassDefinition) topLevelNode;
            if (name.equals(classDefinition.symbol.name.value)) {
                return classDefinition;
            }
        }
        throw new RuntimeException("Class Definition '" + name + "' not found.");
    }

    private void assertAttachmentSymbol(AnnotationAttachmentSymbol attachmentSymbol, String annotationTag) {
        assertAttachmentSymbol(attachmentSymbol, annotationTag, false, null, null);
    }

    private void assertAttachmentSymbol(AnnotationAttachmentSymbol attachmentSymbol, String annotationTag,
                                        boolean constAnnot, String key, Object value) {
        BAnnotationAttachmentSymbol annotationAttachmentSymbol = (BAnnotationAttachmentSymbol) attachmentSymbol;
        Assert.assertEquals(annotationAttachmentSymbol.annotTag.value, annotationTag);
        Assert.assertEquals(annotationAttachmentSymbol.isConstAnnotation(), constAnnot);

        if (!constAnnot) {
            return;
        }

        Object constValue =
                ((BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol) annotationAttachmentSymbol)
                        .attachmentValueSymbol.value.value;

        if (key == null) {
            Assert.assertEquals(constValue, Boolean.TRUE);
            return;
        }

        Map<String, BLangConstantValue> mapConst = (Map<String, BLangConstantValue>) constValue;
        Assert.assertEquals(mapConst.size(), 1);
        Assert.assertEquals(mapConst.get(key).value, value);
    }

    private BLangFunction getFunction(String functionName) {
        return (BLangFunction) compileResult.getAST().getFunctions().stream()
                .filter(function ->  functionName.equals(((BLangFunction) function).symbol.getName().toString()))
                .findFirst()
                .get();
    }

    @NotNull
    private List<BAnnotationAttachmentSymbol> getServiceAnnotsWithoutIntrospectionAnnot(
            List<? extends AnnotationAttachmentSymbol> annotations) {
        List<BAnnotationAttachmentSymbol> attachments = new ArrayList<>();
        for (AnnotationAttachmentSymbol attachmentSymbol : annotations) {
            BAnnotationAttachmentSymbol bAnnotationAttachmentSymbol = (BAnnotationAttachmentSymbol) attachmentSymbol;
            if (!isServiceIntropAnnot(bAnnotationAttachmentSymbol)) {
                attachments.add(bAnnotationAttachmentSymbol);
            }
        }
        return attachments;
    }

    private boolean isServiceIntropAnnot(BAnnotationAttachmentSymbol annot) {
        return AnnotationDesugar.SERVICE_INTROSPECTION_INFO_ANN.equals(annot.annotTag.value);
    }

    @AfterClass
    private void cleanUp() {
        this.compileResult = null;
        this.listAnnotFieldResult = null;
    }
}
