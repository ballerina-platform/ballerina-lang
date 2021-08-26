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

package org.ballerinalang.debugadapter.evaluation.engine;

import com.sun.jdi.Value;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.BallerinaToml;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.BuildOptionsBuilder;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.utils.VariableUtils;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.VariableFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import static org.apache.commons.io.FilenameUtils.indexOfExtension;
import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.utils.PackageUtils.BAL_FILE_EXT;
import static org.ballerinalang.debugadapter.utils.PackageUtils.BAL_TOML_FILE_NAME;
import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;

/**
 * Ballerina query expression processor implementation.
 *
 * @since 2.0.0
 */
public class QueryExpressionProcessor {

    private final SuspendedContext context;
    private final QueryExpressionNode syntaxNode;
    private final List<String> externalVariableNames = new ArrayList<>();
    private final List<Value> externalVariableValues = new ArrayList<>();

    private Path tempProjectDir;

    private static final String TEMP_DIR_PREFIX = "query-executable-dir-";
    private static final String MAIN_FILE_PREFIX = "main-";
    private static final String TEMP_PACKAGE_ORG = "jballerina_debugger";
    private static final String TEMP_PACKAGE_NAME = "query_executor";
    private static final String TEMP_PACKAGE_VERSION = "1.0.0";
    public static final String QUERY_FUNCTION_NAME = "__getQueryResult";

    // Note: the function definition snippet cannot be public, since compiler does not allow public functions to
    // return non-public types.
    private static final String QUERY_FUNCTION_TEMPLATE = "%s function %s(%s) returns any|error { return %s; }";

    public QueryExpressionProcessor(SuspendedContext context, QueryExpressionNode syntaxNode) {
        this.context = context;
        this.syntaxNode = syntaxNode;
    }

    public List<Value> getExternalVariableValues() {
        return externalVariableValues;
    }

    /**
     * Generates Ballerina executable jar files for the code snippet, which encloses the query expression to be
     * evaluated.
     *
     * @return Path to the generated executable file and the name of the main class.
     */
    public Map.Entry<Path, String> generateExecutables() throws EvaluationException {
        try {
            String querySnippet = generateQuerySnippet();
            BuildProject project = createProject(querySnippet);
            Path executablePath = createExecutables(project);
            String mainClassName = constructMainClassName(project);
            return new AbstractMap.SimpleEntry<>(executablePath, mainClassName);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException("error occurred while generating executables to invoke the query " +
                    "expression");
        }
    }

    /**
     * Generates the Ballerina program code snippet, which can output the result of the given query expression.
     *
     * @return Ballerina program snippet
     */
    private String generateQuerySnippet() throws EvaluationException {
        // Generates top level declarations snippet
        String topLevelDeclarations = generateModuleLevelDeclarations();

        // Generates function signature (parameter definitions) for the snippet.
        processSnippetFunctionParameters();
        StringJoiner parameters = new StringJoiner(",");
        externalVariableNames.forEach(parameters::add);

        return String.format(QUERY_FUNCTION_TEMPLATE,
                topLevelDeclarations,
                QUERY_FUNCTION_NAME,
                parameters,
                syntaxNode.toSourceCode());
    }

    /**
     * Creates and returns a 'BuildProject' instance which contains the given source snippet in the main file.
     *
     * @param mainBalContent Source file content to be used for generating the project
     * @return Created Ballerina project
     */
    private BuildProject createProject(String mainBalContent) throws Exception {
        try {
            // Creates a new directory in the default temporary file directory.
            this.tempProjectDir = Files.createTempDirectory(TEMP_DIR_PREFIX + System.currentTimeMillis());
            this.tempProjectDir.toFile().deleteOnExit();

            // Creates an empty Ballerina.toml file
            Path ballerinaTomlPath = tempProjectDir.resolve(BAL_TOML_FILE_NAME);
            Path balToml = Files.createFile(ballerinaTomlPath);
            balToml.toFile().deleteOnExit();

            // Creates a main file and writes the generated code snippet.
            createMainBalFile(mainBalContent);
            BuildOptions buildOptions = new BuildOptionsBuilder().offline(true).build();
            BuildProject buildProject = BuildProject.load(this.tempProjectDir, buildOptions);

            // Updates the 'Ballerina.toml' content
            StringJoiner balTomlContent = new StringJoiner(System.lineSeparator());
            balTomlContent.add("[package]");
            balTomlContent.add(String.format("org = \"%s\"", TEMP_PACKAGE_ORG));
            balTomlContent.add(String.format("name = \"%s\"", TEMP_PACKAGE_NAME));
            balTomlContent.add(String.format("version = \"%s\"", TEMP_PACKAGE_VERSION));
            BallerinaToml newBallerinaToml = buildProject.currentPackage().ballerinaToml().get()
                    .modify()
                    .withContent(balTomlContent.toString())
                    .apply();

            buildProject = (BuildProject) newBallerinaToml.packageInstance().project();
            return buildProject;
        } catch (Exception e) {
            throw createEvaluationException("Error occurred while creating a temporary evaluation project at: " +
                    this.tempProjectDir + ", due to: " + e.getMessage());
        }
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
            throw createEvaluationException("failed to resolve target path while evaluating query expression: "
                    + e.getMessage());
        }

        Path executablePath;
        try {
            executablePath = target.getExecutablePath(project.currentPackage()).toAbsolutePath().normalize();
        } catch (IOException e) {
            throw createEvaluationException("failed to create executable while evaluating query expression: "
                    + e.getMessage());
        }

        try {
            PackageCompilation pkgCompilation = project.currentPackage().getCompilation();
            if (pkgCompilation.diagnosticResult().hasErrors()) {
                StringJoiner errors = new StringJoiner(System.lineSeparator());
                errors.add("compilation error(s) found while creating executables for the query evaluation: ");
                pkgCompilation.diagnosticResult().errors().forEach(error -> {
                    if (error.diagnosticInfo().severity() == DiagnosticSeverity.ERROR) {
                        errors.add(error.message());
                    }
                });
                throw createEvaluationException(errors.toString());
            }
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(pkgCompilation, JvmTarget.JAVA_11);
            jBallerinaBackend.emit(JBallerinaBackend.OutputType.EXEC, executablePath);
        } catch (ProjectException e) {
            throw createEvaluationException("failed to create executable while evaluating query expression: "
                    + e.getMessage());
        }
        return executablePath;
    }

    private String constructMainClassName(BuildProject project) {
        Optional<DocumentId> docId = project.currentPackage().getDefaultModule().documentIds().stream().findFirst();
        Document document = project.currentPackage().getDefaultModule().document(docId.get());

        StringJoiner classNameJoiner = new StringJoiner(".");
        classNameJoiner.add(TEMP_PACKAGE_ORG);
        classNameJoiner.add(TEMP_PACKAGE_NAME);
        // Generated class name will only contain the major version.
        classNameJoiner.add(TEMP_PACKAGE_VERSION.split("\\.")[0]);
        classNameJoiner.add(getFileNameWithoutExtension(document.name()));

        return classNameJoiner.toString();
    }

    /**
     * Returns the file name without extension.
     *
     * @param fileName file name
     * @return File name without extension
     */
    private static String getFileNameWithoutExtension(String fileName) {
        int index = indexOfExtension(fileName);
        return index == -1 ? fileName : fileName.substring(0, index);
    }

    /**
     * Helper method to write a string source to a file.
     *
     * @param content Content to write to the file.
     * @throws IOException If writing was unsuccessful.
     */
    private void createMainBalFile(String content) throws Exception {
        File mainBalFile = File.createTempFile(MAIN_FILE_PREFIX, BAL_FILE_EXT, tempProjectDir.toFile());
        mainBalFile.deleteOnExit();

        try (FileWriter fileWriter = new FileWriter(mainBalFile, Charset.defaultCharset())) {
            fileWriter.write(content);
        }
    }

    private String generateModuleLevelDeclarations() {
        ModuleLevelDefinitionFinder moduleDefinitionFinder = new ModuleLevelDefinitionFinder(context);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.IMPORT_DECLARATION);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.FUNCTION_DEFINITION);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.TYPE_DEFINITION);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.MODULE_VAR_DECL);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.LISTENER_DECLARATION);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.CONST_DECLARATION);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.ANNOTATION_DECLARATION);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.MODULE_XML_NAMESPACE_DECLARATION);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.ENUM_DECLARATION);
        moduleDefinitionFinder.addInclusiveFilter(SyntaxKind.CLASS_DEFINITION);

        List<ModuleMemberDeclarationNode> declarationList = moduleDefinitionFinder.getModuleDeclarations();
        return declarationList.stream()
                .map(Node::toSourceCode)
                .reduce((s, s2) -> s + System.lineSeparator() + s2)
                .orElse("");
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
            case MAP:
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
            case JSON:
            case SERVICE:
                return bVar.getBType().getString();
            case NIL:
                return "()";
            case ARRAY:
                return bVar.computeValue().substring(0, bVar.computeValue().indexOf("[")) + "[]";
            case RECORD:
            case OBJECT:
            case TUPLE:
                return bVar.computeValue();
            default:
                return UNKNOWN_VALUE;
        }
    }

    private void processSnippetFunctionParameters() throws EvaluationException {
        // Retrieves all the external (local + global) variables which are being used in the query expression.
        List<String> capturedVarNames = new ArrayList<>(new QueryReferenceFinder(syntaxNode).getCapturedVariables());
        List<String> capturedTypes = new ArrayList<>();
        for (String name : capturedVarNames) {
            Value jdiValue = VariableUtils.getVariableValue(context, name);
            BVariable bVar = VariableFactory.getVariable(context, jdiValue);
            capturedTypes.add(getTypeNameString(bVar));
            externalVariableValues.add(jdiValue);
        }

        for (int index = 0; index < capturedVarNames.size(); index++) {
            externalVariableNames.add(capturedTypes.get(index) + " " + capturedVarNames.get(index));
        }
    }

    /**
     * Delete the given directory along with all files and sub directories.
     *
     * @param directoryPath Directory to delete.
     */
    private boolean deleteDirectory(Path directoryPath) {
        File directory = new File(String.valueOf(directoryPath));
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File f : files) {
                    boolean success = deleteDirectory(f.toPath());
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return directory.delete();
    }

    public void dispose() {
        // Todo - anything else to be disposed?
        deleteDirectory(this.tempProjectDir);
    }
}
