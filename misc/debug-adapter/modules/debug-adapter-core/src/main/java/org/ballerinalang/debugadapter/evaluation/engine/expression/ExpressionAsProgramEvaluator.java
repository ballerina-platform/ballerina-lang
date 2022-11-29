/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.evaluation.engine.expression;

import com.sun.jdi.Value;
import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.BImport;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationImportResolver;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.ExternalVariableReferenceFinder;
import org.ballerinalang.debugadapter.evaluation.engine.ModuleLevelDefinitionFinder;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;
import org.ballerinalang.debugadapter.evaluation.utils.FileUtils;
import org.ballerinalang.debugadapter.evaluation.utils.VariableUtils;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.VariableFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.INTERNAL_ERROR;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.VARIABLE_NOT_FOUND;
import static org.ballerinalang.debugadapter.evaluation.IdentifierModifier.QUOTED_IDENTIFIER_PREFIX;
import static org.ballerinalang.debugadapter.evaluation.IdentifierModifier.decodeAndEscapeIdentifier;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_DEBUGGER_RUNTIME_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.CLASSLOAD_AND_INVOKE_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_OBJECT_ARRAY_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_STRING_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.MODULE_NAME_SEPARATOR;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.MODULE_NAME_SEPARATOR_REGEX;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.MODULE_VERSION_SEPARATOR_REGEX;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getAsJString;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getValueAsObject;
import static org.ballerinalang.debugadapter.evaluation.utils.LangLibUtils.LANG_LIB_ORG;
import static org.ballerinalang.debugadapter.evaluation.utils.LangLibUtils.LANG_LIB_PACKAGE_PREFIX;
import static org.ballerinalang.debugadapter.utils.PackageUtils.BAL_FILE_EXT;
import static org.ballerinalang.debugadapter.utils.PackageUtils.BAL_TOML_FILE_NAME;
import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;
import static org.ballerinalang.debugadapter.variable.VariableUtils.getPackageOrgAndName;

/**
 * A base evaluator implementation which provides a more generic evaluation approach to be used for some complex
 * Ballerina expressions (queries, let expressions, etc.).
 * <p>
 * This evaluator transforms the user expression into a standalone ballerina program, which returns the user
 * expression result as the program output and can be executed in the same debuggee JVM.
 * <p>
 * The common execution flow for the expressions based on this evaluator will be as follows.
 * <ol>
 * <li> Generates a valid Ballerina program code snippet, which can output the result of the given expression.
 * <li> Creates a 'BuildProject' instance which contains the given source snippet as its main file.
 * <li> If the user expression includes same package module usages, extracts all the module-level declarations from the
 * non-default modules and add them as separate modules into the evaluation package created in above step.
 * <li> Creates a Ballerina executable jar based on the generated code snippet, in a temp directory.
 * <li> Invokes 'classloadAndInvokeFunction' in the remote VM to classload the created executable jar and
 * invoke its '__getEvaluationResult' method. It will return the result of the expression as its return value.
 * </ol>
 *
 * @since 2.0.0
 */
public class ExpressionAsProgramEvaluator extends Evaluator {

    private Path tempProjectDir;

    protected final ExpressionNode syntaxNode;
    private final List<String> externalVariableNames = new ArrayList<>();
    private final List<Value> externalVariableValues = new ArrayList<>();
    private final List<BImport> capturedImports = new ArrayList<>();

    private static final String TEMP_DIR_PREFIX = "evaluation-executable-dir-";
    private static final String MAIN_FILE_PREFIX = "main-";
    private static final String EVALUATION_PACKAGE_ORG = "jballerina_debugger";
    private static final String EVALUATION_PACKAGE_NAME = "evaluation_executor";
    private static final String EVALUATION_PACKAGE_VERSION = "1.0.0";
    public static final String EVALUATION_FUNCTION_NAME = "__getEvaluationResult";

    private static final String EVALUATION_IMPORT_TEMPLATE = "import %s/%s as %s;";
    // Note: the function definition snippet cannot be public, since compiler does not allow public functions to
    // return non-public types.
    private static final String EVALUATION_FUNCTION_TEMPLATE = "function %s(%s) returns any|error { return %s; }";
    // Format: ${imports} ${declarations} ${evaluation_function}
    private static final String EVALUATION_SNIPPET_TEMPLATE = String.format("%%s %1$s %%s %1$s %%s",
            System.lineSeparator());

    public ExpressionAsProgramEvaluator(EvaluationContext evaluationContext, ExpressionNode syntaxNode) {
        super(evaluationContext);
        this.syntaxNode = syntaxNode;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            String evaluationSnippet = generateEvaluationSnippet();
            BuildProject project = createProject(evaluationSnippet);
            Path executablePath = createExecutables(project);
            String mainClassName = constructMainClassName(project);
            return classAndInvokeExecutable(executablePath, mainClassName);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException(INTERNAL_ERROR, syntaxNode.toSourceCode().trim());
        } finally {
            this.dispose();
        }
    }

    /**
     * Generates a valid Ballerina program code snippet, which can output the result of the given expression.
     *
     * @return Ballerina program snippet
     */
    private String generateEvaluationSnippet() throws EvaluationException {
        // Generates top-level declarations snippet.
        String moduleDeclarations = extractModuleDefinitions(context.getModule(), false);

        // Generates function signature (parameter definitions) for the snippet function template.
        processSnippetFunctionParameters();
        StringJoiner parameters = new StringJoiner(",");
        externalVariableNames.forEach(parameters::add);

        // Generates required import declarations based on the expression.
        String functionSnippet = String.format(EVALUATION_FUNCTION_TEMPLATE, EVALUATION_FUNCTION_NAME,
                parameters, evaluationContext.getExpression());
        String importDeclarations = generateImportDeclarations(functionSnippet);

        return String.format(EVALUATION_SNIPPET_TEMPLATE, importDeclarations, moduleDeclarations, functionSnippet);
    }

    /**
     * Creates and returns a 'BuildProject' instance which contains the given source snippet in the main file.
     *
     * @param mainBalContent Source file content to be used for generating the project
     * @return Created Ballerina project
     */
    private BuildProject createProject(String mainBalContent) throws EvaluationException {
        try {
            // Creates a new directory in the default temporary file directory.
            this.tempProjectDir = Files.createTempDirectory(TEMP_DIR_PREFIX + System.currentTimeMillis());
            this.tempProjectDir.toFile().deleteOnExit();

            // Creates a main file and writes the generated code snippet.
            createMainBalFile(mainBalContent);
            // Creates the Ballerina.toml file and writes the package meta information.
            createBallerinaToml();

            // If the user expression contains any import usages of other modules in the same package, we need
            // to include all the definitions from those modules, in our temporary evaluation project. (Here we cannot
            // selectively include only the imported modules as the package modules can have inter dependencies.)
            if (containsOtherModuleImports()) {
                fillOtherModuleDefinitions();
            }

            BuildOptions buildOptions = BuildOptions.builder()
                    .setOffline(true)
                    .targetDir(ProjectUtils.getTemporaryTargetPath())
                    .build();
            return BuildProject.load(this.tempProjectDir, buildOptions);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException(String.format("error occurred while creating the temporary evaluation " +
                    "project due to: %s", e.getMessage()));
        }
    }

    /**
     * Indicates whether detected imports within the user expression contains modules within the same package.
     */
    private boolean containsOtherModuleImports() {
        return capturedImports.stream().anyMatch(bImport -> context.getPackageOrg().isPresent()
                && bImport.orgName().equals(context.getPackageOrg().get())
                && bImport.packageName().equals(context.getModule().packageInstance().packageName().value()));
    }

    /**
     * Creates a Ballerina executable jar on the generated code snippet, in a temp directory.
     *
     * @param project build project instance
     * @return path of the created executable JAR
     */
    private Path createExecutables(BuildProject project) throws EvaluationException {
        Target target;
        try {
            target = new Target(project.sourceRoot());
        } catch (IOException | ProjectException e) {
            throw createEvaluationException("failed to resolve target path while evaluating expression: "
                    + e.getMessage());
        }

        Path executablePath;
        try {
            executablePath = target.getExecutablePath(project.currentPackage()).toAbsolutePath().normalize();
        } catch (IOException e) {
            throw createEvaluationException("failed to create executables while evaluating expression: "
                    + e.getMessage());
        }

        try {
            PackageCompilation pkgCompilation = project.currentPackage().getCompilation();
            validateForCompilationErrors(pkgCompilation);
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(pkgCompilation, JvmTarget.JAVA_11);
            jBallerinaBackend.emit(JBallerinaBackend.OutputType.EXEC, executablePath);
        } catch (ProjectException e) {
            throw createEvaluationException("failed to create executables while evaluating expression: "
                    + e.getMessage());
        }
        return executablePath;
    }

    private String constructMainClassName(BuildProject project) {
        Optional<DocumentId> docId = project.currentPackage().getDefaultModule().documentIds().stream().findFirst();
        Document document = project.currentPackage().getDefaultModule().document(docId.get());

        StringJoiner classNameJoiner = new StringJoiner(".");
        classNameJoiner.add(EVALUATION_PACKAGE_ORG);
        classNameJoiner.add(EVALUATION_PACKAGE_NAME);
        // Generated class name will only contain the major version.
        classNameJoiner.add(EVALUATION_PACKAGE_VERSION.split(MODULE_VERSION_SEPARATOR_REGEX)[0]);
        classNameJoiner.add(getFileNameWithoutExtension(document.name()));

        return classNameJoiner.toString();
    }

    private BExpressionValue classAndInvokeExecutable(Path executablePath, String mainClassName)
            throws EvaluationException {

        List<String> argTypes = new ArrayList<>();
        argTypes.add(JAVA_STRING_CLASS);
        argTypes.add(JAVA_STRING_CLASS);
        argTypes.add(JAVA_STRING_CLASS);
        argTypes.add(JAVA_OBJECT_ARRAY_CLASS);
        RuntimeStaticMethod classLoadAndInvokeMethod = getRuntimeMethod(context, B_DEBUGGER_RUNTIME_CLASS,
                CLASSLOAD_AND_INVOKE_METHOD, argTypes);

        List<Value> argList = new ArrayList<>();
        argList.add(getAsJString(context, executablePath.toAbsolutePath().toString()));
        argList.add(getAsJString(context, mainClassName));
        argList.add(getAsJString(context, EVALUATION_FUNCTION_NAME));

        // adds all the captured variable values as rest arguments.
        argList.addAll(externalVariableValues);
        classLoadAndInvokeMethod.setArgValues(argList);
        Value expressionResult = classLoadAndInvokeMethod.invokeSafely();
        return new BExpressionValue(context, expressionResult);
    }

    /**
     * Returns the file name without extension.
     *
     * @param fileName file name
     * @return File name without extension
     */
    private static String getFileNameWithoutExtension(String fileName) {
        return fileName.endsWith(BAL_FILE_EXT) ? fileName.replaceAll(BAL_FILE_EXT + "$", "") : fileName;
    }

    /**
     * Helper method to create a main bal file with the given content.
     *
     * @param content Content to write to the file.
     * @throws IOException If writing was unsuccessful.
     */
    private void createMainBalFile(String content) throws Exception {
        File mainBalFile = File.createTempFile(MAIN_FILE_PREFIX, BAL_FILE_EXT, tempProjectDir.toFile());
        mainBalFile.deleteOnExit();

        FileUtils.writeToFile(mainBalFile, content);
    }

    /**
     * Helper method to create a Ballerina.toml file with predefined content.
     *
     * @throws IOException If writing was unsuccessful.
     */
    private void createBallerinaToml() throws Exception {
        Path ballerinaTomlPath = tempProjectDir.resolve(BAL_TOML_FILE_NAME);
        File balTomlFile = Files.createFile(ballerinaTomlPath).toFile();
        balTomlFile.deleteOnExit();

        StringJoiner balTomlContent = new StringJoiner(System.lineSeparator());
        balTomlContent.add("[package]");
        balTomlContent.add(String.format("org = \"%s\"", EVALUATION_PACKAGE_ORG));
        balTomlContent.add(String.format("name = \"%s\"", EVALUATION_PACKAGE_NAME));
        balTomlContent.add(String.format("version = \"%s\"", EVALUATION_PACKAGE_VERSION));

        FileUtils.writeToFile(balTomlFile, balTomlContent.toString());
    }

    /**
     * Detects all the import usages within the evaluation snippet and generates required import statements for
     * the detected import usages.
     */
    private String generateImportDeclarations(String functionSnippet) throws EvaluationException {
        ModuleMemberDeclarationNode functionNode = NodeParser.parseModuleMemberDeclaration(functionSnippet);
        if (functionNode.kind() != SyntaxKind.FUNCTION_DEFINITION) {
            return null;
        }
        // Detects all the import usages within the evaluation snippet and generates required import statements for
        // the detected import usages.
        EvaluationImportResolver importResolver = new EvaluationImportResolver(context);
        Map<String, BImport> usedImports = importResolver.detectUsedImports(functionNode, resolvedImports);
        this.capturedImports.addAll(usedImports.values());

        StringBuilder importBuilder = new StringBuilder(System.lineSeparator());
        for (Map.Entry<String, BImport> importEntry : usedImports.entrySet()) {
            importBuilder.append(constructImportStatement(importEntry));
        }

        return importBuilder.append(System.lineSeparator()).toString();
    }

    private String constructImportStatement(Map.Entry<String, BImport> importEntry) {
        String orgName = importEntry.getValue().getResolvedSymbol().id().orgName();
        // If the import belongs to a module inside the current package, replace the original org name name with
        // temporary evaluation project org name.
        if (context.getPackageOrg().isPresent() && orgName.equals(context.getPackageOrg().get())) {
            orgName = EVALUATION_PACKAGE_ORG;
        }

        String moduleName = getModuleName(importEntry.getValue().getResolvedSymbol().id());
        String[] moduleNameParts = moduleName.split(MODULE_NAME_SEPARATOR_REGEX);
        // If the import belongs to a module inside the current package, replace the original package name with
        // temporary evaluation project package name.
        if (context.getPackageName().isPresent() && moduleNameParts[0].equals(context.getPackageName().get())) {
            StringJoiner newModuleName = new StringJoiner(MODULE_NAME_SEPARATOR);
            newModuleName.add(EVALUATION_PACKAGE_NAME);
            for (int i = 1, copyOfRangeLength = moduleNameParts.length; i < copyOfRangeLength; i++) {
                newModuleName.add(moduleNameParts[i]);
            }
            moduleName = newModuleName.toString();
        }

        String alias = importEntry.getKey();
        return String.format(EVALUATION_IMPORT_TEMPLATE, orgName, moduleName, alias);
    }

    private String getModuleName(ModuleID moduleId) {
        if (moduleId.orgName().equals(LANG_LIB_ORG) && moduleId.moduleName().startsWith(LANG_LIB_PACKAGE_PREFIX)) {
            // Need to inject the quoted identifier prefix for lang-lib module imports.
            String[] moduleNameParts = moduleId.moduleName().split("\\.");
            moduleNameParts = Arrays.copyOfRange(moduleNameParts, 1, moduleNameParts.length);
            return LANG_LIB_PACKAGE_PREFIX + QUOTED_IDENTIFIER_PREFIX + String.join(".", moduleNameParts);
        } else {
            return moduleId.moduleName();
        }
    }

    /**
     * Updates the evaluation project with all the definitions from the modules which were used within the
     * user expression.
     */
    private void fillOtherModuleDefinitions() throws Exception {
        ModuleId currentModuleId = context.getModule().moduleId();
        ModuleId defaultModuleId = context.getModule().packageInstance().getDefaultModule().moduleId();
        for (Module module : context.getModule().packageInstance().modules()) {
            if (module.moduleId() != defaultModuleId && module.moduleId() != currentModuleId) {
                generateModuleDefinitions(module);
            }
        }
    }

    private void generateModuleDefinitions(Module module) throws Exception {
        // Todo: [IMPORTANT] Refactor to use in-memory project update APIs instead of using the FileWriter, once the
        //  project update API performance issues are fixed.

        String[] moduleNameParts = module.moduleId().moduleName().split("\\.");
        // We don't need the 0th element (as its the default module name).
        moduleNameParts = Arrays.copyOfRange(moduleNameParts, 1, moduleNameParts.length);

        // If moduleNameParts is 0, detected module should the default module (therefore need to be skipped)
        if (moduleNameParts.length == 0) {
            return;
        }
        Path filePath = tempProjectDir.resolve(ProjectConstants.MODULES_ROOT);
        for (String moduleNamePart : moduleNameParts) {
            filePath = filePath.resolve(moduleNamePart);
        }
        if (!Files.exists(filePath)) {
            Files.createDirectories(filePath);
        }

        File moduleMainFile = Files.createTempFile(filePath, MAIN_FILE_PREFIX, BAL_FILE_EXT).toFile();
        moduleMainFile.deleteOnExit();
        String moduleDefinitions = extractModuleDefinitions(module, true);
        FileUtils.writeToFile(moduleMainFile, moduleDefinitions);
    }

    private String extractModuleDefinitions(Module module, boolean includeImports) {
        ModuleLevelDefinitionFinder moduleDefinitionFinder = new ModuleLevelDefinitionFinder(context);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.FUNCTION_DEFINITION);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.TYPE_DEFINITION);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.LISTENER_DECLARATION);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.ANNOTATION_DECLARATION);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.MODULE_XML_NAMESPACE_DECLARATION);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.ENUM_DECLARATION);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.CLASS_DEFINITION);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.CONST_DECLARATION);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.MODULE_VAR_DECL);

        if (includeImports) {
            moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.IMPORT_DECLARATION);
        }

        List<NonTerminalNode> declarationList = moduleDefinitionFinder.getModuleDeclarations(module);
        if (includeImports) {
            declarationList = convertImports(declarationList);
        }

        return declarationList.stream()
                .map(Node::toSourceCode)
                .reduce((s, s2) -> s + System.lineSeparator() + s2)
                .map(s -> s + System.lineSeparator())
                .orElse("");
    }

    /**
     * Modify all the import statements within the given module level declarations, to be compatible with the evaluation
     * package structure.
     */
    private List<NonTerminalNode> convertImports(List<NonTerminalNode> declarationList) {

        return declarationList.stream().map(nonTerminalNode -> {
            if (nonTerminalNode.kind() != SyntaxKind.IMPORT_DECLARATION) {
                return nonTerminalNode;
            }

            ImportDeclarationNode importDeclarationNode = (ImportDeclarationNode) nonTerminalNode;
            if (importDeclarationNode.orgName().isPresent() && (context.getPackageOrg().isEmpty() ||
                    !context.getPackageOrg().get().equals(importDeclarationNode.orgName().get().orgName().text()))) {
                return nonTerminalNode;
            }
            if (context.getPackageName().isEmpty() || !context.getPackageName().get().equals(importDeclarationNode
                    .moduleName().get(0).text())) {
                return nonTerminalNode;
            }
            ImportDeclarationNode.ImportDeclarationNodeModifier modifier = importDeclarationNode.modify();

            // Replaces original org name with the evaluation org name.
            if (importDeclarationNode.orgName().isPresent()) {
                IdentifierToken orgToken = NodeFactory.createIdentifierToken(EVALUATION_PACKAGE_ORG);
                ImportOrgNameNode newOrgName = NodeFactory.createImportOrgNameNode(orgToken, importDeclarationNode
                        .orgName().get().slashToken());
                modifier.withOrgName(newOrgName);
            }

            // Replaces original package name with the evaluation package name.
            List<Node> moduleParts = importDeclarationNode.moduleName().stream().collect(Collectors.toList());
            IdentifierToken packageToken = NodeFactory.createIdentifierToken(EVALUATION_PACKAGE_NAME);
            moduleParts.remove(0);
            moduleParts.add(0, packageToken);
            IdentifierToken moduleNameSeparatorToken = NodeFactory.createIdentifierToken(MODULE_NAME_SEPARATOR);
            for (int i = 0, size = moduleParts.size(); i < size - 1; i++) {
                moduleParts.add(2 * i + 1, moduleNameSeparatorToken);
            }
            SeparatedNodeList<IdentifierToken> newModuleName = NodeFactory.createSeparatedNodeList(moduleParts);
            modifier = modifier.withModuleName(newModuleName);

            return modifier.apply();
        }).collect(Collectors.toList());
    }

    private void processSnippetFunctionParameters() throws EvaluationException {
        // Retrieves all the external (local + global) variables which are being used in the user expression.
        List<String> capturedVarNames = new ArrayList<>(new ExternalVariableReferenceFinder(syntaxNode)
                .getCapturedVariables());
        List<String> capturedTypes = new ArrayList<>();
        for (String name : capturedVarNames) {
            Optional<BExpressionValue> variableValue = EvaluationUtils.fetchVariableReferenceValue(evaluationContext,
                    name);
            if (variableValue.isEmpty()) {
                throw createEvaluationException(VARIABLE_NOT_FOUND, name);
            }

            BVariable bVar = VariableFactory.getVariable(context, variableValue.get().getJdiValue());
            capturedTypes.add(getTypeNameString(bVar));
            externalVariableValues.add(getValueAsObject(context, variableValue.get().getJdiValue()));
        }

        for (int index = 0; index < capturedVarNames.size(); index++) {
            externalVariableNames.add(capturedTypes.get(index) + " " + capturedVarNames.get(index));
        }
    }

    private void validateForCompilationErrors(PackageCompilation packageCompilation) throws EvaluationException {
        if (packageCompilation.diagnosticResult().hasErrors()) {
            StringJoiner errors = new StringJoiner(System.lineSeparator());
            errors.add("compilation error(s) found while creating executables for evaluation: ");
            packageCompilation.diagnosticResult().errors().forEach(error -> {
                if (error.diagnosticInfo().severity() == DiagnosticSeverity.ERROR) {
                    errors.add(error.message());
                }
            });
            throw createEvaluationException(errors.toString());
        }
    }

    /**
     * Returns the type name of the given Ballerina variable instance.
     *
     * @param bVar ballerina variable instance
     * @return type name
     */
    private String getTypeNameString(BVariable bVar) {
        switch (bVar.getBType()) {
            case BOOLEAN:
            case INT:
            case FLOAT:
            case DECIMAL:
            case STRING:
            case XML:
            case TABLE:
            case ERROR:
            case FUNCTION:
            case FUTURE:
            case TYPE_DESC:
            case HANDLE:
            case STREAM:
            case SINGLETON:
            case ANY:
            case ANYDATA:
            case NEVER:
            case BYTE:
            case SERVICE:
                return bVar.getBType().getString();
            case JSON:
                return "map<json>";
            case MAP:
                return VariableUtils.getMapType(context, bVar.getJvmValue());
            case NIL:
                return "()";
            case ARRAY:
                return bVar.computeValue().substring(0, bVar.computeValue().indexOf("[")) + "[]";
            case TUPLE:
                return bVar.computeValue();
            case RECORD:
            case OBJECT:
                return resolveObjectType(bVar);
            default:
                return UNKNOWN_VALUE;
        }
    }

    private String resolveObjectType(BVariable bVar) {
        Map.Entry<String, String> packageOrgAndName = getPackageOrgAndName(bVar.getJvmValue());
        if (packageOrgAndName == null) {
            return bVar.computeValue();
        }

        for (Map.Entry<String, BImport> entry : resolvedImports.entrySet()) {
            String alias = entry.getKey();
            BImport bImport = entry.getValue();
            if (bImport.orgName().equals(packageOrgAndName.getKey()) &&
                    bImport.moduleName().equals(packageOrgAndName.getValue())) {
                return alias + ":" + decodeAndEscapeIdentifier(bVar.computeValue());
            }
        }

        // since the variable.computeValue returns the encoded type name, need to decode it before injecting to the
        // source code snippet.
        return decodeAndEscapeIdentifier(bVar.computeValue());
    }

    private void dispose() {
        // Todo - anything else to be disposed?
        FileUtils.deleteDirectory(this.tempProjectDir);
    }
}
