/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.birspec;

import io.kaitai.struct.ByteBufferKaitaiStream;
import io.kaitai.struct.KaitaiStruct;
import org.ballerinalang.build.kaitai.Bir;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.wso2.ballerinalang.compiler.bir.model.BIRAbstractInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BirScope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile.BIRPackageFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility methods to help with testing BIR model.
 */
class BIRTestUtils {

    private static final String TEST_RESOURCES_ROOT = "src/test/resources/test-src";
    private static final Path TEST_RESOURCES_ROOT_PATH = Paths.get(TEST_RESOURCES_ROOT);

    private static final String LANG_LIB_TEST_SRC_ROOT = "../../langlib/langlib-test/src/test/resources/test-src";
    private static final Path LANG_LIB_TEST_ROOT_PATH = Paths.get(LANG_LIB_TEST_SRC_ROOT);

    @DataProvider(name = "createTestSources")
    public static Object[][] createTestDataProvider() throws IOException {
        assert LANG_LIB_TEST_ROOT_PATH.toFile().exists();
        List<String> testSources = findBallerinaSourceFiles(LANG_LIB_TEST_ROOT_PATH);

        assert TEST_RESOURCES_ROOT_PATH.toFile().exists();
        testSources.addAll(findBallerinaSourceFiles(TEST_RESOURCES_ROOT_PATH));

        Object[][] data = new Object[testSources.size()][];
        for (int i = 0; i < data.length; i++) {
            data[i] = new Object[]{testSources.get(i)};
        }
        return data;
    }

    private static List<String> findBallerinaSourceFiles(Path testSourcesPath) throws IOException {
        return Files.walk(testSourcesPath)
                .filter(file -> Files.isRegularFile(file))
                .map(file -> file.toAbsolutePath().normalize().toString())
                .filter(file -> file.endsWith(".bal") && !file.contains("negative") && !file.contains("subtype"))
                .collect(Collectors.toList());
    }

    static void validateBIRSpec(String testSource) {
        BIRCompileResult compileResult = compile(testSource);
        BIRNode.BIRPackage expectedBIRModule = compileResult.getExpectedBIR();
        Bir actualBIR = compileResult.getActualBIR();

        ArrayList<Bir.ConstantPoolEntry> constantPoolEntries = actualBIR.constantPool().constantPoolEntries();
        Bir.Module actualBIRModule = actualBIR.module();

        // assert constants
        assertValues(expectedBIRModule, actualBIRModule, constantPoolEntries);

        // assert type definitions
        assertTypeDefs(expectedBIRModule, actualBIRModule, constantPoolEntries);

        // assert annotations
        assertAnnotations(expectedBIRModule, actualBIRModule, constantPoolEntries);

        // assert functions
        assertFunctions(expectedBIRModule, actualBIRModule, constantPoolEntries);
    }

    private static BIRCompileResult compile(String testSource) {
        CompileResult result = BCompileUtil.compileAndGetBIR(testSource);
        Assert.assertEquals(result.getErrorCount(), 0);

        BPackageSymbol packageSymbol = ((BLangPackage) result.getAST()).symbol;

        BIRPackageFile birPackageFile = packageSymbol.birPackageFile;
        Assert.assertNotNull(birPackageFile);

        byte[] birBinaryContent = birPackageFile.pkgBirBinaryContent;
        Assert.assertNotNull(birBinaryContent);

        Bir kaitaiBir = new Bir(new ByteBufferKaitaiStream(birBinaryContent));
        return new BIRCompileResult(packageSymbol.bir, kaitaiBir);
    }

    private static void assertFunctions(BIRNode.BIRPackage expectedBIR, Bir.Module birModule,
                                        ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        List<BIRNode.BIRFunction> expectedFunctions = expectedBIR.functions;
        ArrayList<Bir.Function> actualFunctions = birModule.functions();
        Assert.assertEquals(birModule.functionCount(), expectedFunctions.size());

        for (int i = 0; i < expectedFunctions.size(); i++) {
            Bir.Function actualFunction = actualFunctions.get(i);
            BIRNode.BIRFunction expectedFunction = expectedFunctions.get(i);

            // assert name
            Bir.ConstantPoolEntry constantPoolEntry = constantPoolEntries.get(actualFunction.nameCpIndex());
            assertConstantPoolEntry(constantPoolEntry, expectedFunction.name.value);

            // assert markdown document
            assertMarkdownDocument(actualFunction.doc(), expectedFunction.markdownDocAttachment, constantPoolEntries);

            // assert required param count
            Assert.assertEquals(actualFunction.requiredParamCount(), expectedFunction.requiredParams.size());

            Bir.FunctionBody actualFunctionBody = actualFunction.functionBody();
            // assert arguments count
            Assert.assertEquals(actualFunctionBody.argsCount(), expectedFunction.argsCount);
            Assert.assertEquals(actualFunctionBody.defaultParameterCount(), expectedFunction.parameters.size());

            // assert dependent global variables
            assertDepGlobalVar(actualFunction, expectedFunction.dependentGlobalVars.toArray(), constantPoolEntries);

            // assert basic blocks
            assertBasicBlocks(actualFunctionBody.functionBasicBlocksInfo(), expectedFunction.basicBlocks,
                    constantPoolEntries);

            assertScopes(actualFunction.scopeEntries(), expectedFunction);
        }
    }

    private static void assertScopes(ArrayList<Bir.ScopeEntry> scopeEntries, BIRNode.BIRFunction function) {
        int instructionOffset = 0;
        Map<Integer, ExpectedScopeEntry> scopes = new HashMap<>();
        Set<BirScope> visitedScopes = new HashSet<>();

        // Collect scope vs starting instruction offset
        Collection<List<BIRNode.BIRBasicBlock>> basicBlocksCollection = function.parameters.values();
        for (List<BIRNode.BIRBasicBlock> basicBlocks : basicBlocksCollection) {
            instructionOffset = generateExpectedScopeEntries(basicBlocks, instructionOffset, scopes, visitedScopes);
        }

        generateExpectedScopeEntries(function.basicBlocks, instructionOffset, scopes, visitedScopes);

        for (Bir.ScopeEntry actualScopeEntry : scopeEntries) {
            ExpectedScopeEntry expectedScopeEntry = scopes.get(actualScopeEntry.currentScopeIndex());
            Assert.assertNotNull(expectedScopeEntry);

            Assert.assertEquals(actualScopeEntry.instructionOffset(), expectedScopeEntry.instructionOffset);
            Assert.assertEquals(actualScopeEntry.hasParent(), expectedScopeEntry.hasParent);
            Assert.assertEquals(actualScopeEntry.parentScopeIndex(), expectedScopeEntry.parentId);
        }
    }

    private static int generateExpectedScopeEntries(List<BIRNode.BIRBasicBlock> bbList, int instructionOffset,
            Map<Integer, ExpectedScopeEntry> scopes, Set<BirScope> visitedScopes) {
        for (BIRNode.BIRBasicBlock bb : bbList) {
            for (BIRAbstractInstruction instruction : bb.instructions) {
                instructionOffset++;
                BirScope instructionScope = instruction.scope;

                if (visitedScopes.contains(instructionScope)) {
                    continue;
                }

                visitedScopes.add(instructionScope);
                boolean hasParent = instructionScope.parent != null;

                ExpectedScopeEntry expectedScopeEntry = new ExpectedScopeEntry(instructionScope.id,
                        instructionOffset,  hasParent ? 1 : 0, hasParent ? instructionScope.parent.id : null);
                scopes.put(instructionScope.id, expectedScopeEntry);
                putParentScopesAsWell(scopes, instructionScope.parent, instructionOffset);

            }
        }
        return instructionOffset;
    }

    private static void putParentScopesAsWell(Map<Integer, ExpectedScopeEntry> scopes, BirScope parent,
            int instructionOffset) {

        if (parent == null) {
            return;
        }

        if (scopes.containsKey(parent.id)) {
            return;
        }

        boolean hasParent = parent.parent != null;
        ExpectedScopeEntry expectedParentScopeEntry = new ExpectedScopeEntry(parent.id,
                instructionOffset,  hasParent ? 1 : 0, hasParent ? parent.parent.id : null);
        scopes.put(parent.id, expectedParentScopeEntry);

        if (hasParent) {
            putParentScopesAsWell(scopes, parent.parent, instructionOffset);
        }
    }

    private static void assertDepGlobalVar(Bir.Function actualFunction, Object[] expectedGlobalVars,
            ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {

        ArrayList<Integer> actualGlobalVarCpEntries = actualFunction.dependentGlobalVarCpEntry();
        Assert.assertEquals(actualGlobalVarCpEntries.size(), expectedGlobalVars.length);

        for (int i = 0; i < actualGlobalVarCpEntries.size(); i++) {
            Bir.ConstantPoolEntry constantPoolEntry = constantPoolEntries.get(actualGlobalVarCpEntries.get(i));
            String expectedName = ((BIRNode.BIRGlobalVariableDcl) expectedGlobalVars[i]).name.value;
            assertConstantPoolEntry(constantPoolEntry, expectedName);
        }
    }

    private static void assertMarkdownDocument(Bir.Markdown actualDocument, MarkdownDocAttachment expectedDocument,
                                               ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        if (actualDocument.hasDoc() == 0) {
            Assert.assertNull(expectedDocument);
            return;
        }

        Bir.MarkdownContent actualDocContent = actualDocument.markdownContent();
        assertMarkdownEntry(constantPoolEntries, actualDocContent.descriptionCpIndex(), expectedDocument.description);

        assertMarkdownEntry(constantPoolEntries, actualDocContent.returnValueDescriptionCpIndex(),
                expectedDocument.returnValueDescription);

        List<MarkdownDocAttachment.Parameter> expectedParameters = expectedDocument.parameters;
        Assert.assertEquals(actualDocContent.parametersCount(), expectedParameters.size());
        ArrayList<Bir.MarkdownParameter> actualParameters = actualDocContent.parameters();
        for (int i = 0; i < actualParameters.size(); i++) {
            Bir.MarkdownParameter actualParameter = actualParameters.get(i);
            MarkdownDocAttachment.Parameter expectedParameter = expectedParameters.get(i);

            assertMarkdownEntry(constantPoolEntries, actualParameter.nameCpIndex(), expectedParameter.name);

            assertMarkdownEntry(constantPoolEntries, actualParameter.descriptionCpIndex(),
                    expectedParameter.description);
        }
    }

    private static void assertMarkdownEntry(ArrayList<Bir.ConstantPoolEntry> constantPoolEntries, int cpIndex,
                                            String description) {
        if (cpIndex < 0) {
            return;
        }

        Bir.ConstantPoolEntry constantPoolEntry = constantPoolEntries.get(cpIndex);
        assertConstantPoolEntry(constantPoolEntry, description);
    }

    private static void assertBasicBlocks(Bir.BasicBlocksInfo actualBasicBlocksInfo,
                                          List<BIRNode.BIRBasicBlock> expectedBasicBlocks,
                                          ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        Assert.assertEquals(actualBasicBlocksInfo.basicBlocksCount(), expectedBasicBlocks.size());

        ArrayList<Bir.BasicBlock> actualBasicBlocks = actualBasicBlocksInfo.basicBlocks();
        for (int i = 0; i < actualBasicBlocks.size(); i++) {
            Bir.BasicBlock actualBasicBlock = actualBasicBlocks.get(i);
            BIRNode.BIRBasicBlock expectedBasicBlock = expectedBasicBlocks.get(i);

            Bir.ConstantPoolEntry constantPoolEntry = constantPoolEntries.get(actualBasicBlock.nameCpIndex());
            assertConstantPoolEntry(constantPoolEntry, expectedBasicBlock.id.value);

            ArrayList<Bir.Instruction> actualInstructions = actualBasicBlock.instructions();
            List<BIRNonTerminator> expectedInstructions = expectedBasicBlock.instructions;

            Assert.assertEquals(actualBasicBlock.instructionsCount(), expectedInstructions.size() + 1);

            for (int j = 0; j < actualInstructions.size() - 1; j++) {
                assertInstruction(actualInstructions.get(j), expectedInstructions.get(j), constantPoolEntries);
            }

            Bir.Instruction actualTerminator = actualInstructions.get(actualBasicBlock.instructionsCount() - 1);
            assertInstruction(actualTerminator, expectedBasicBlock.terminator, constantPoolEntries);
        }
    }

    private static void assertInstruction(Bir.Instruction actualInstruction, BIRInstruction expectedInstruction,
                                          ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        Bir.Instruction.InstructionKindEnum instructionKindEnum = actualInstruction.instructionKind();
        KaitaiStruct instructionStructure = actualInstruction.instructionStructure();
        switch (instructionKindEnum) {
            case INSTRUCTION_KIND_MOVE:
                assertMoveInstruction((Bir.InstructionMove) instructionStructure,
                        (BIRNonTerminator.Move) expectedInstruction,
                        constantPoolEntries);
                break;
            case INSTRUCTION_KIND_GOTO:
                assertGotoInstruction((Bir.InstructionGoto) instructionStructure,
                        (BIRTerminator.GOTO) expectedInstruction, constantPoolEntries);
                break;
            case INSTRUCTION_KIND_CALL:
                assertCallInstruction((Bir.InstructionCall) instructionStructure,
                        (BIRTerminator.Call) expectedInstruction, constantPoolEntries);
                break;
            case INSTRUCTION_KIND_BRANCH:
                assertBranchInstruction((Bir.InstructionBranch) instructionStructure,
                        (BIRTerminator.Branch) expectedInstruction, constantPoolEntries);
                break;
            default:
                Assert.assertEquals(instructionKindEnum.id(), expectedInstruction.getKind().getValue());
        }
    }

    private static void assertBranchInstruction(Bir.InstructionBranch actualBranch,
                                                BIRTerminator.Branch expectedBranch,
                                                ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        assertOperand(actualBranch.branchOperand(), expectedBranch.op, constantPoolEntries);

        // assert true bb name
        Bir.ConstantPoolEntry constantPoolEntry = constantPoolEntries.get(actualBranch.trueBbIdNameCpIndex());
        assertConstantPoolEntry(constantPoolEntry, expectedBranch.trueBB.id.value);

        // assert false bb name
        constantPoolEntry = constantPoolEntries.get(actualBranch.falseBbIdNameCpIndex());
        assertConstantPoolEntry(constantPoolEntry, expectedBranch.falseBB.id.value);
    }

    private static void assertCallInstruction(Bir.InstructionCall actualCall, BIRTerminator.Call expectedCall,
                                              ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        Bir.CallInstructionInfo callInstructionInfo = actualCall.callInstructionInfo();

        Assert.assertEquals(callInstructionInfo.argumentsCount(), expectedCall.args.size());
        Assert.assertEquals(callInstructionInfo.hasLhsOperand() == 1, expectedCall.getLhsOperand() != null);
        Assert.assertEquals(callInstructionInfo.isVirtual() == 1, expectedCall.isVirtual);

        // assert pkg name
        Bir.ConstantPoolEntry constantPoolEntry = constantPoolEntries.get(callInstructionInfo.packageIndex());
        assertConstantPoolEntry(constantPoolEntry, expectedCall.calleePkg);

        // assert call name
        constantPoolEntry = constantPoolEntries.get(callInstructionInfo.callNameCpIndex());
        assertConstantPoolEntry(constantPoolEntry, expectedCall.name.value);

        // assert then bb id
        constantPoolEntry = constantPoolEntries.get(actualCall.thenBbIdNameCpIndex());
        assertConstantPoolEntry(constantPoolEntry, expectedCall.thenBB.id.value);
    }

    private static void assertGotoInstruction(Bir.InstructionGoto actualGoto, BIRTerminator.GOTO expectedInstruction,
                                              ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        // assert goto bb id
        Bir.ConstantPoolEntry constantPoolEntry = constantPoolEntries.get(actualGoto.targetBbIdNameCpIndex());
        assertConstantPoolEntry(constantPoolEntry, expectedInstruction.targetBB.id.value);
    }

    private static void assertMoveInstruction(Bir.InstructionMove actual, BIRNonTerminator.Move expected,
                                              ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        Bir.Operand actualLhsOperand = actual.lhsOperand();
        BIROperand expectedLhsOp = expected.lhsOp;
        assertOperand(actualLhsOperand, expectedLhsOp, constantPoolEntries);

        Bir.Operand actualRhsOperand = actual.rhsOperand();
        BIROperand expectedRhsOp = expected.rhsOp;
        assertOperand(actualRhsOperand, expectedRhsOp, constantPoolEntries);
    }

    private static void assertOperand(Bir.Operand actual, BIROperand expected,
                                      ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        BIRNode.BIRVariableDcl expectedVar = expected.variableDcl;
        Assert.assertEquals(actual.ignoredVariable() == 1, expectedVar.ignoreVariable);

        if (actual.ignoredVariable() == 0) {
            Bir.Variable actualVar = actual.variable();
            Assert.assertEquals(actualVar.kind(), expectedVar.kind.getValue());
            Assert.assertEquals(actualVar.scope(), expectedVar.scope.getValue());

            // assert var name
            Bir.ConstantPoolEntry constantPoolEntry = constantPoolEntries.get(actualVar.variableDclNameCpIndex());
            assertConstantPoolEntry(constantPoolEntry, expectedVar.name.value);
        }
    }

    private static void assertValues(BIRNode.BIRPackage expectedBIR, Bir.Module birModule,
                                     ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        List<BIRNode.BIRConstant> expectedConstants = expectedBIR.constants;
        ArrayList<Bir.Constant> actualConstants = birModule.constants();
        Assert.assertEquals(birModule.constCount(), expectedConstants.size());

        for (int i = 0; i < expectedConstants.size(); i++) {
            Bir.Constant actualConstant = actualConstants.get(i);
            BIRNode.BIRConstant expectedConstant = expectedConstants.get(i);

            // assert name
            assertConstantPoolEntry(constantPoolEntries.get(actualConstant.nameCpIndex()), expectedConstant.name.value);

            // assert flags
            assertFlags(actualConstant.flags(), expectedConstant.flags);

            // assert type
            assertConstantPoolEntry(constantPoolEntries.get(actualConstant.typeCpIndex()), expectedConstant.type);

            // assert value
            assertConstantValue(actualConstant.constantValue(), expectedConstant.constValue.value, constantPoolEntries);
        }
    }

    private static void assertTypeDefs(BIRNode.BIRPackage expectedBIR, Bir.Module birModule,
                                       ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        List<BIRNode.BIRTypeDefinition> expectedTypeDefs = expectedBIR.typeDefs;
        ArrayList<Bir.TypeDefinition> actualTypeDefinitions = birModule.typeDefinitions();

        Assert.assertEquals(actualTypeDefinitions.size(), expectedTypeDefs.size());

        for (int i = 0; i < expectedTypeDefs.size(); i++) {
            BIRNode.BIRTypeDefinition expectedTypeDefinition = expectedTypeDefs.get(i);
            Bir.TypeDefinition actualTypeDefinition = actualTypeDefinitions.get(i);

            // assert name
            assertConstantPoolEntry(constantPoolEntries.get(actualTypeDefinition.nameCpIndex()),
                    expectedTypeDefinition.name.value);

            // assert flags
            assertFlags(actualTypeDefinition.flags(), expectedTypeDefinition.flags);

            // assert type
            assertConstantPoolEntry(constantPoolEntries.get(actualTypeDefinition.typeCpIndex()),
                    expectedTypeDefinition.type);

            // assert position
            if (expectedTypeDefinition.pos != null) {
                assertPosition(actualTypeDefinition.position(), expectedTypeDefinition.pos);
            }

            // assert markdown document
            assertMarkdownDocument(actualTypeDefinition.doc(), expectedTypeDefinition.markdownDocAttachment,
                    constantPoolEntries);
        }
    }

    private static void assertPosition(Bir.Position actualPosition, DiagnosticPos expectedPosition) {
        Assert.assertEquals(actualPosition.sLine(), expectedPosition.sLine);
        Assert.assertEquals(actualPosition.eLine(), expectedPosition.eLine);
        Assert.assertEquals(actualPosition.sCol(), expectedPosition.sCol);
        Assert.assertEquals(actualPosition.eCol(), expectedPosition.eCol);
    }

    private static void assertAnnotations(BIRNode.BIRPackage expectedBIR, Bir.Module birModule,
                                          ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        List<BIRNode.BIRAnnotation> expectedAnnotations = expectedBIR.annotations;
        ArrayList<Bir.Annotation> actualAnnotations = birModule.annotations();

        Assert.assertEquals(actualAnnotations.size(), expectedAnnotations.size());

        for (int i = 0; i < expectedAnnotations.size(); i++) {
            BIRNode.BIRAnnotation expectedAnnotation = expectedAnnotations.get(i);
            Bir.Annotation actualAnnotation = actualAnnotations.get(i);

            // assert name
            assertConstantPoolEntry(constantPoolEntries.get(actualAnnotation.nameCpIndex()),
                    expectedAnnotation.name.value);

            // assert type
            assertConstantPoolEntry(constantPoolEntries.get(actualAnnotation.annotationTypeCpIndex()),
                    expectedAnnotation.annotationType);

            // assert attach points
            assertAttachPoints(actualAnnotation.attachPoints(), expectedAnnotation.attachPoints, constantPoolEntries);
        }
    }

    private static void assertAttachPoints(ArrayList<Bir.AttachPoint> actualAttachPoints,
                                           Set<AttachPoint> expectedAttachPoints,
                                           ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        Assert.assertEquals(actualAttachPoints.size(), expectedAttachPoints.size());
        AttachPoint[] expected = expectedAttachPoints.toArray(new AttachPoint[0]);
        for (int i = 0; i < expected.length; i++) {
            AttachPoint expectedAttachPoint = expected[i];
            Bir.AttachPoint actualAttachPoint = actualAttachPoints.get(i);

            assertConstantPoolEntry(constantPoolEntries.get(actualAttachPoint.pointNameCpIndex()),
                    expectedAttachPoint.point.getValue());
        }
    }

    private static void assertConstantPoolEntry(Bir.ConstantPoolEntry constantPoolEntry, Object expectedValue) {
        switch (constantPoolEntry.tag()) {
            case CP_ENTRY_INTEGER:
                Bir.IntCpInfo intCpInfo = (Bir.IntCpInfo) constantPoolEntry.cpInfo();
                Assert.assertEquals(intCpInfo.value(), (long) expectedValue);
                break;
            case CP_ENTRY_STRING:
                Bir.StringCpInfo stringCpInfo = (Bir.StringCpInfo) constantPoolEntry.cpInfo();
                Assert.assertEquals(stringCpInfo.value(), (String) expectedValue);
                break;
            case CP_ENTRY_SHAPE:
                assertType(constantPoolEntry, (BType) expectedValue);
                break;
            case CP_ENTRY_FLOAT:
                Bir.FloatCpInfo floatCpInfo = (Bir.FloatCpInfo) constantPoolEntry.cpInfo();
                double expectedFloatVal = expectedValue instanceof String ? Double.parseDouble((String) expectedValue)
                        : (Double) expectedValue;
                Assert.assertEquals(floatCpInfo.value(), expectedFloatVal);
                break;
            case CP_ENTRY_PACKAGE:
                Bir.PackageCpInfo packageCpInfo = (Bir.PackageCpInfo) constantPoolEntry.cpInfo();
                PackageID packageID = (PackageID) expectedValue;
                ArrayList<Bir.ConstantPoolEntry> constantPoolEntries = constantPoolEntry._parent().
                        constantPoolEntries();
                assertConstantPoolEntry(constantPoolEntries.get(packageCpInfo.orgIndex()), packageID.orgName.value);
                assertConstantPoolEntry(constantPoolEntries.get(packageCpInfo.nameIndex()), packageID.name.value);
                assertConstantPoolEntry(constantPoolEntries.get(packageCpInfo.versionIndex()), packageID.version.value);
                break;
            case CP_ENTRY_BYTE:
                Bir.ByteCpInfo byteCpInfo = (Bir.ByteCpInfo) constantPoolEntry.cpInfo();
                Assert.assertEquals(byteCpInfo.value(), (int) expectedValue);
                break;
            case CP_ENTRY_BOOLEAN:
                Bir.BooleanCpInfo booleanCpInfo = (Bir.BooleanCpInfo) constantPoolEntry.cpInfo();
                Assert.assertEquals(booleanCpInfo.value(), expectedValue);
                break;
            default:
                Assert.fail(String.format("Unknown constant pool entry: %s", constantPoolEntry.tag().name()));
        }
    }

    private static void assertType(Bir.ConstantPoolEntry constantPoolEntry, BType expectedValue) {
        Bir.TypeInfo typeInfo = ((Bir.ShapeCpInfo) constantPoolEntry.cpInfo()).shape();
        Assert.assertEquals(typeInfo.typeTag().id(), expectedValue.tag);
        Assert.assertEquals(typeInfo.nameAsStr(), expectedValue.name.getValue());
        assertFlags(typeInfo.typeFlag(), expectedValue.flags);
        KaitaiStruct typeStructure = typeInfo.typeStructure();

        if (typeStructure instanceof Bir.TypeObjectOrService) {
            Bir.TypeObjectOrService objectOrService = (Bir.TypeObjectOrService) typeStructure;
            BTypeIdSet expTypeIdSet = ((BObjectType) expectedValue.tsymbol.type).typeIdSet;
            Bir.TypeId actualTypeIdSet = objectOrService.typeIds();
            assertDistinctTypeIds(expTypeIdSet, actualTypeIdSet, constantPoolEntry._parent());
        } else if (typeStructure instanceof Bir.TypeError) {
            Bir.TypeError errorType = (Bir.TypeError) typeStructure;
            BTypeIdSet expTypeIdSet = ((BErrorType) expectedValue.tsymbol.type).typeIdSet;
            Bir.TypeId actualTypeIdSet = errorType.typeIds();
            assertDistinctTypeIds(expTypeIdSet, actualTypeIdSet, constantPoolEntry._parent());
        }
    }

    private static void assertDistinctTypeIds(BTypeIdSet expTypeIdSet, Bir.TypeId actualTypeIdSet,
                                              Bir.ConstantPoolSet constantPoolSet) {
        Assert.assertEquals(actualTypeIdSet.primaryTypeIdCount(), expTypeIdSet.primary.size());
        Assert.assertEquals(actualTypeIdSet.secondaryTypeIdCount(), expTypeIdSet.secondary.size());

        ArrayList<Bir.TypeIdSet> primaryTypeId = actualTypeIdSet.primaryTypeId();
        for (int i = 0; i < primaryTypeId.size(); i++) {
            Bir.TypeIdSet typeId = primaryTypeId.get(i);
            Assert.assertTrue(containsTypeId(typeId, expTypeIdSet.primary, constantPoolSet));
        }

        ArrayList<Bir.TypeIdSet> secondaryTypeId = actualTypeIdSet.secondaryTypeId();
        for (int i = 0; i < secondaryTypeId.size(); i++) {
            Bir.TypeIdSet typeId = secondaryTypeId.get(i);
            Assert.assertTrue(containsTypeId(typeId, expTypeIdSet.secondary, constantPoolSet));
        }
    }

    private static boolean containsTypeId(Bir.TypeIdSet aTypeIdSet, Set<BTypeIdSet.BTypeId> set,
                                          Bir.ConstantPoolSet constantPoolSet) {
        for (BTypeIdSet.BTypeId expId : set) {
            Bir.ConstantPoolEntry typeNameEntry =
                    constantPoolSet.constantPoolEntries().get(aTypeIdSet.typeIdNameCpIndex());
            Bir.ConstantPoolEntry pkgIdEntry = constantPoolSet.constantPoolEntries().get(aTypeIdSet.pkgIdCpIndex());
            boolean samePublicness = expId.publicId == (aTypeIdSet.isPublicId() != 0);

            Bir.StringCpInfo stringCpInfo = (Bir.StringCpInfo) typeNameEntry.cpInfo();
            boolean sameName = stringCpInfo.value().equals(expId.name);

            if (samePublicness && sameName) {
                assertConstantPoolEntry(pkgIdEntry, expId.packageID);
                return true;
            }
        }
        return false;
    }

    private static void assertFlags(int actualFlags, int expectedFlags) {

        Assert.assertEquals(actualFlags, expectedFlags, "Invalid flags");
    }

    private static void assertConstantValue(Bir.ConstantValue actualConstantValue, Object expectedValue,
                                            ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        KaitaiStruct constantValueInfo = actualConstantValue.constantValueInfo();
        Bir.TypeTagEnum typeTag = actualConstantValue.type().shape().typeTag();
        switch (typeTag) {
            case TYPE_TAG_INT:
                Bir.IntConstantInfo intConstantInfo = (Bir.IntConstantInfo) constantValueInfo;
                assertConstantPoolEntry(constantPoolEntries.get(intConstantInfo.valueCpIndex()), expectedValue);
                break;
            case TYPE_TAG_BYTE:
                Bir.ByteConstantInfo byteConstantInfo = (Bir.ByteConstantInfo) constantValueInfo;
                assertConstantPoolEntry(constantPoolEntries.get(byteConstantInfo.valueCpIndex()), expectedValue);
                break;
            case TYPE_TAG_STRING:
                Bir.StringConstantInfo stringConstantInfo = (Bir.StringConstantInfo) constantValueInfo;
                assertConstantPoolEntry(constantPoolEntries.get(stringConstantInfo.valueCpIndex()), expectedValue);
                break;
            case TYPE_TAG_FLOAT:
                Bir.FloatConstantInfo floatConstantInfo = (Bir.FloatConstantInfo) constantValueInfo;
                assertConstantPoolEntry(constantPoolEntries.get(floatConstantInfo.valueCpIndex()), expectedValue);
                break;
            case TYPE_TAG_DECIMAL:
                Bir.DecimalConstantInfo decimalConstantInfo = (Bir.DecimalConstantInfo) constantValueInfo;
                assertConstantPoolEntry(constantPoolEntries.get(decimalConstantInfo.valueCpIndex()), expectedValue);
                break;
            case TYPE_TAG_MAP:
                Bir.MapConstantInfo actualMapConst = (Bir.MapConstantInfo) constantValueInfo;
                Map<String, BIRNode.ConstValue> expectedMapConst = (Map<String, BIRNode.ConstValue>) expectedValue;
                Assert.assertEquals(actualMapConst.mapConstantSize(), expectedMapConst.size());
                break;
            default:
                Assert.fail(String.format("Unknown constant value type: %s", typeTag.name()));
        }
    }

    /**
     * Class to hold both expected and actual compile result of BIR.
     */
    static class BIRCompileResult {

        private BIRNode.BIRPackage expectedBIR;
        private Bir actualBIR;

        BIRCompileResult(BIRNode.BIRPackage expectedBIR, Bir actualBIR) {
            this.expectedBIR = expectedBIR;
            this.actualBIR = actualBIR;
        }

        BIRNode.BIRPackage getExpectedBIR() {
            return expectedBIR;
        }

        Bir getActualBIR() {
            return actualBIR;
        }
    }

    static class ExpectedScopeEntry {
        public final int id;
        public final int instructionOffset;
        public final int hasParent;
        public final Integer parentId;

        ExpectedScopeEntry(int id, int instructionOffset, int hasParent, Integer parentId) {
            this.id = id;
            this.instructionOffset = instructionOffset;
            this.hasParent = hasParent;
            this.parentId = parentId;
        }
    }
}
