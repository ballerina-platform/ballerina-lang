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
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.BuildOptionsBuilder;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.internal.model.Target;
import org.ballerinalang.compiler.plugins.CompilerPlugin;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.utils.VariableUtils;
import org.ballerinalang.debugadapter.utils.PackageUtils;
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
import java.util.ServiceLoader;
import java.util.StringJoiner;

import static io.ballerina.projects.util.ProjectConstants.BLANG_COMPILED_JAR_EXT;
import static org.apache.commons.io.FilenameUtils.indexOfExtension;
import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;

/**
 * Ballerina query expression processor implementation.
 *
 * @since 2.0.0
 * <p>
 * Todo - try compiling as a ballerina module (to avoid namespace conflicts when using single file)
 */
public class QueryExpressionProcessor {

    private final SuspendedContext context;
    private final QueryExpressionNode syntaxNode;
    private final List<String> externalVariableNames = new ArrayList<>();
    private final List<Value> externalVariableValues = new ArrayList<>();

    private Path currentDir;
    private Path executablePath;
    private File bufferFile;

    private static final String TEMP_FILE_PREFIX = "main-";
    private static final String TEMP_FILE_SUFFIX = ".bal";
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
            File mainBal = writeToFile(querySnippet);
            Project project = getProject(mainBal, true);

            Path executablePath = createExecutables(project);
            String mainClassName = getFileNameWithoutExtension(mainBal.toPath());
            return new AbstractMap.SimpleEntry<>(executablePath, mainClassName);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException("error occurred while generating executables to invoke the query " +
                    "expression");
        }
    }

    private Path createExecutables(Project project) throws EvaluationException {
        // Todo - should we reuse `CreateExecutableTask` instead?
        Target target;
        try {
            this.currentDir = Files.createTempDirectory("query-executable-dir");
            if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
                target = new Target(project.sourceRoot());
            } else {
                target = new Target(Files.createTempDirectory("ballerina-cache" + System.nanoTime()));
                target.setOutputPath(getExecutablePath(project));
            }
        } catch (IOException e) {
            throw createEvaluationException("failed to resolve target path while evaluating query expression: "
                    + e.getMessage());
        } catch (ProjectException e) {
            throw createEvaluationException("failed to create executable while evaluating query expression: "
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
                throw createEvaluationException("compilation failed while creating executables for query evaluation");
            }
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(pkgCompilation, JvmTarget.JAVA_11);
            jBallerinaBackend.emit(JBallerinaBackend.OutputType.EXEC, executablePath);
        } catch (ProjectException e) {
            throw createEvaluationException("failed to create executable while evaluating query expression: "
                    + e.getMessage());
        }

        notifyPlugins(project, target);
        return executablePath;
    }

    private Path getExecutablePath(Project project) {
        if (executablePath == null) {
            Path fileName = project.sourceRoot().getFileName();
            executablePath = currentDir.resolve(getFileNameWithoutExtension(fileName) + BLANG_COMPILED_JAR_EXT);
        }
        return executablePath;
    }

    /**
     * Get the name of the without the extension.
     *
     * @param filePath Path of the file.
     * @return File name without extension.
     */
    private static String getFileNameWithoutExtension(Path filePath) {
        Path fileName = filePath.getFileName();
        if (fileName != null) {
            int index = indexOfExtension(fileName.toString());
            return index == -1 ? fileName.toString() : fileName.toString().substring(0, index);
        } else {
            return null;
        }
    }

    /**
     * Get the project with the context data.
     *
     * @param mainBal   Source file to use for generating project.
     * @param isOffline Whether to use offline flag for build options.
     * @return Created ballerina project.
     */
    private Project getProject(File mainBal, boolean isOffline) {
        BuildOptions buildOptions = new BuildOptionsBuilder().offline(isOffline).build();
        // Todo - create a build project instead
        return SingleFileProject.load(mainBal.toPath(), buildOptions);
    }

    /**
     * Helper method to write a string source to a file.
     *
     * @param source Content to write to the file.
     * @return The created temp file.
     * @throws IOException If writing was unsuccessful.
     */
    private File writeToFile(String source) throws IOException {
        File createdFile = getBufferFile();
        try (FileWriter fileWriter = new FileWriter(createdFile, Charset.defaultCharset())) {
            fileWriter.write(source);
        }
        return createdFile;
    }

    /**
     * Get the file that would be used as the buffer for loading project.
     *
     * @return File to use.
     * @throws IOException If file open failed.
     */
    private File getBufferFile() throws IOException {
        if (this.bufferFile == null) {
            this.bufferFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
            this.bufferFile.deleteOnExit();
        }
        return this.bufferFile;
    }

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

    private String getTypeString(BVariable bVar) {
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
            capturedTypes.add(getTypeString(bVar));
            externalVariableValues.add(jdiValue);
        }

        for (int index = 0; index < capturedVarNames.size(); index++) {
            externalVariableNames.add(capturedTypes.get(index) + " " + capturedVarNames.get(index));
        }
    }

    private void notifyPlugins(Project project, Target target) {
        ServiceLoader<CompilerPlugin> processorServiceLoader = ServiceLoader.load(CompilerPlugin.class);
        for (CompilerPlugin plugin : processorServiceLoader) {
            plugin.codeGenerated(project, target);
        }
    }

    public void dispose() {
        // Todo - anything else to be disposed?
        PackageUtils.deleteFile(this.executablePath);
    }
}
