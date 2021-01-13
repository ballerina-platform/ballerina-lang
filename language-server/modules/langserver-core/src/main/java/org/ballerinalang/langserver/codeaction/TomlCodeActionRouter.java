package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import org.ballerinalang.langserver.codeaction.toml.C2CVisitor;
import org.ballerinalang.langserver.codeaction.toml.ListenerInfo;
import org.ballerinalang.langserver.codeaction.toml.ProjectServiceInfo;
import org.ballerinalang.langserver.codeaction.toml.ServiceInfo;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.toml.Probe;
import org.ballerinalang.langserver.toml.ProbeStore;
import org.ballerinalang.langserver.toml.TomlProbesVisitor;
import org.ballerinalang.langserver.toml.TomlSyntaxTreeUtil;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CreateFile;
import org.eclipse.lsp4j.CreateFileOptions;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Code Action Router for C2C.
 *
 * @since 2.0.0
 */
public class TomlCodeActionRouter {

    /**
     * Returns a list of supported code actions.
     *
     * @param ctx {@link CodeActionContext}
     * @return list of code actions
     */
    public static List<CodeAction> getAvailableCodeActions(CodeActionContext ctx) {
        List<CodeAction> codeActions = new ArrayList<>();
        List<Diagnostic> cursorDiagnostics = ctx.cursorDiagnostics();
        if (cursorDiagnostics != null && !cursorDiagnostics.isEmpty()) {
            for (Diagnostic diagnostic : cursorDiagnostics) {
                codeActions.addAll(handleInvalidPath(diagnostic, ctx));
            }
        }
        return codeActions;
    }

    public static List<CodeAction> handleInvalidPath(Diagnostic diagnostic, CodeActionContext ctx) {
        SyntaxTree syntaxTree = TomlSyntaxTreeUtil.getTomlSyntaxTree(ctx.filePath()).orElseThrow();
        DocumentNode node = syntaxTree.rootNode();
        TomlProbesVisitor probesVisitor = new TomlProbesVisitor();
        node.accept(probesVisitor);
        ProbeStore store = probesVisitor.getStore();
        Optional<Probe> readinessProbe = store.getReadiness();
        Optional<Probe> livenessProbe = store.getLiveness();
        switch (diagnostic.getMessage()) {
            case "Invalid Readiness Probe Port":
                if (readinessProbe.isPresent()) {
                    return handleInvalidPort(diagnostic, ctx, readinessProbe.get());
                }
                break;
            case "Invalid Liveness Probe Port":
                if (livenessProbe.isPresent()) {
                    return handleInvalidPort(diagnostic, ctx, livenessProbe.get());
                }
                break;
            case "Invalid Readiness Probe Resource Path":
                if (readinessProbe.isPresent()) {
                    return addResourceToService(diagnostic, ctx, readinessProbe.get());
                }
                break;
            case "Invalid Liveness Probe Resource Path":
                if (livenessProbe.isPresent()) {
                    return addResourceToService(diagnostic, ctx, livenessProbe.get());
                }
                break;
            case "Invalid Readiness Probe Service Path":
                if (readinessProbe.isPresent()) {
                    return fixServicePath(diagnostic, ctx, readinessProbe.get());
                }
                break;
            case "Invalid Liveness Probe Service Path":
                if (livenessProbe.isPresent()) {
                    return fixServicePath(diagnostic, ctx, livenessProbe.get());
                }
                break;
            default:
                break;
        }
        return new ArrayList<>();
    }

    public static List<CodeAction> fixServicePath(Diagnostic diagnostic, CodeActionContext ctx, Probe probe) {
        Optional<Project> project = ctx.workspace().project(ctx.filePath());
        Map<String, List<ServiceInfo>> serviceList = getServiceFromProject(project.orElseThrow()).getServiceMap();
        List<CodeAction> codeActionList = new ArrayList<>();
        for (List<ServiceInfo> serviceInfos : serviceList.values()) {
            // TODO: Listener Exists No attatched service -> Generate a service using the listener
            for (ServiceInfo service : serviceInfos) {
                int port = service.getListener().getPort();
                if (probe.getPort().getValue() == port) {
                    String servicePath = "/" + TomlSyntaxTreeUtil.trimResourcePath(service.getServiceName());
                    io.ballerina.toml.syntax.tree.Node node = probe.getPath().getNode();
                    Position startingPos = new Position(node.lineRange().startLine().line(),
                            node.lineRange().startLine().offset());
                    Position endingPos = new Position(node.lineRange().endLine().line(),
                            node.lineRange().endLine().offset());

                    CodeAction action = new CodeAction();
                    action.setKind(CodeActionKind.QuickFix);

                    TextEdit removeContent = new TextEdit(new Range(startingPos, endingPos), "");
                    TextEdit addContent = new TextEdit(new Range(startingPos, startingPos), servicePath);
                    List<TextEdit> edits = new ArrayList<>();
                    edits.add(removeContent);
                    edits.add(addContent);

                    action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                            new TextDocumentEdit(new VersionedTextDocumentIdentifier(ctx.fileUri(), null),
                                    edits)))));
                    action.setTitle("Modify service path");
                    List<Diagnostic> cursorDiagnostics = new ArrayList<>();
                    cursorDiagnostics.add(diagnostic);
                    action.setDiagnostics(cursorDiagnostics);
                    codeActionList.add(action);
                    break;
                }
            }
        }

        return codeActionList;
    }

    public static List<CodeAction> addResourceToService(Diagnostic diagnostic, CodeActionContext ctx, Probe probe) {
        Optional<Project> project = ctx.workspace().project(ctx.filePath());
        Map<String, List<ServiceInfo>> serviceList = getServiceFromProject(project.orElseThrow()).getServiceMap();
        List<CodeAction> codeActionList = new ArrayList<>();
        for (Map.Entry<String, List<ServiceInfo>> entry : serviceList.entrySet()) {
            String filePath = entry.getKey();
            List<ServiceInfo> serviceInfos = entry.getValue();
            for (ServiceInfo service : serviceInfos) {
                int port = service.getListener().getPort();
                if (probe.getPort().getValue() == port) {
                    Path balFilePath = ctx.workspace().projectRoot(ctx.filePath()).resolve(filePath);
                    NodeList<Node> members = service.getNode().members();
                    Node lastResource = members.get(members.size() - 1);
                    Position position = new Position(lastResource.lineRange().endLine().line() + 1, 0);

                    CodeAction action = new CodeAction();
                    action.setKind(CodeActionKind.QuickFix);
                    String importText = generateProbeFunctionText(service, probe);
                    List<TextEdit> edits = Collections.singletonList(
                            new TextEdit(new Range(position, position), importText));
                    action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                            new TextDocumentEdit(
                                    new VersionedTextDocumentIdentifier(balFilePath.toUri().toString(), null),
                                    edits)))));
                    action.setTitle("Add Resource to Service");
                    List<Diagnostic> cursorDiagnostics = new ArrayList<>();
                    cursorDiagnostics.add(diagnostic);
                    action.setDiagnostics(cursorDiagnostics);
                    codeActionList.add(action);
                    break;
                }
            }
        }
        return codeActionList;
    }

    /**
     * Makes a CodeAction to create a file and add content to the file.
     *
     * @param title      The displayed name of the CodeAction
     * @param docURI     The file to create
     * @param content    The text to put into the newly created document.
     * @param diagnostic The diagnostic that this CodeAction will fix
     */
    public static CodeAction createFile(String title, String docURI, String content, Diagnostic diagnostic) {

        List<Either<TextDocumentEdit, ResourceOperation>> actionsToTake = new ArrayList<>(2);

        // 1. create an empty file
        actionsToTake.add(Either.forRight(new CreateFile(docURI, new CreateFileOptions(false, true))));

        // 2. update the created file with the given content
        VersionedTextDocumentIdentifier identifier = new VersionedTextDocumentIdentifier(docURI, 0);
        TextEdit te = new TextEdit(new Range(new Position(0, 0), new Position(0, 0)), content);
        actionsToTake.add(Either.forLeft(new TextDocumentEdit(identifier, Collections.singletonList(te))));

        WorkspaceEdit createAndAddContentEdit = new WorkspaceEdit(actionsToTake);

        CodeAction codeAction = new CodeAction(title);
        codeAction.setEdit(createAndAddContentEdit);
        codeAction.setDiagnostics(Collections.singletonList(diagnostic));
        codeAction.setKind(CodeActionKind.QuickFix);

        return codeAction;
    }

    private static String getProbeServiceWithImportString(int port, String servicePath, String resourcePath) {
        return String
                .format("import ballerina/http;%s%s",
                        CommonUtil.LINE_SEPARATOR, getProbeServiceString(port,servicePath,resourcePath));
    }

    private static String getProbeServiceString(int port, String servicePath, String resourcePath) {
        return String
                .format("%sservice http:Service %s on new http:Listener(%d) {%s    resource " +
                                "function get %s (http:Caller caller) returns error? {%s        check caller->ok" +
                                "(\"Resource is Ready\");%s    }%s}%s",
                        CommonUtil.LINE_SEPARATOR, servicePath, port,
                        CommonUtil.LINE_SEPARATOR, resourcePath,
                        CommonUtil.LINE_SEPARATOR, CommonUtil.LINE_SEPARATOR, CommonUtil.LINE_SEPARATOR,
                        CommonUtil.LINE_SEPARATOR);
    }

    public static List<CodeAction> handleInvalidPort(Diagnostic diagnostic, CodeActionContext ctx, Probe probe) {
        List<CodeAction> codeActionList = new ArrayList<>();

        String tomlPath = probe.getPath().getValue();
        int tomlPort = probe.getPort().getValue();
        String resourcePath = "readyz";
        String servicePath = "/probes";
        if (!(tomlPath == null || tomlPath.equals(""))) {
            String trimmedResourcePath = TomlSyntaxTreeUtil.trimResourcePath(tomlPath);
            String[] split = trimmedResourcePath.split("/");
            resourcePath = split[split.length - 1];
            servicePath = "/" + String.join("/", Arrays.copyOf(split, split.length - 1));
        }
        Path balFilePath = ctx.workspace().projectRoot(ctx.filePath()).resolve("probe.bal");
        if (Files.exists(balFilePath)) {
            io.ballerina.compiler.syntax.tree.SyntaxTree syntaxTree =
                    ctx.workspace().syntaxTree(balFilePath).orElseThrow();
            ModulePartNode modulePartNode = syntaxTree.rootNode();
            NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
            ModuleMemberDeclarationNode lastTopLevelNode = members.get(members.size() - 1);
            Position position = new Position(lastTopLevelNode.lineRange().endLine().line() + 1, 0);
            String content = getProbeServiceString(tomlPort, servicePath, resourcePath);
            List<TextEdit> edits = Collections.singletonList(
                    new TextEdit(new Range(position, position), content));

            CodeAction action = new CodeAction("Add to Probe file");
            action.setKind(CodeActionKind.QuickFix);
            action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                    new TextDocumentEdit(
                            new VersionedTextDocumentIdentifier(balFilePath.toUri().toString(), null),
                            edits)))));
            List<Diagnostic> cursorDiagnostics = new ArrayList<>();
            cursorDiagnostics.add(diagnostic);
            action.setDiagnostics(cursorDiagnostics);
            codeActionList.add(action);
        } else {
            String content = getProbeServiceWithImportString(tomlPort, servicePath, resourcePath);
            codeActionList.add(createFile("Create Probe file", balFilePath.toUri().toString(), content, diagnostic));
        }
        return codeActionList;
    }

    private static String generateProbeFunctionText(ServiceInfo service, Probe probe) {
        String tomlPath = TomlSyntaxTreeUtil.trimResourcePath(probe.getPath().getValue());
        String serviceName = TomlSyntaxTreeUtil.trimResourcePath(service.getServiceName());
        String serviceResourcePath;
        if (tomlPath.startsWith(serviceName)) {
            serviceResourcePath = tomlPath.substring(serviceName.length() + 1);
        } else {
            serviceResourcePath = tomlPath;
        }
        return "    resource function get " + serviceResourcePath +
                " (http:Caller caller) returns error? {" + CommonUtil.LINE_SEPARATOR +
                "        check caller->ok(\"Resource is Ready\");" + CommonUtil.LINE_SEPARATOR +
                "    }" + CommonUtil.LINE_SEPARATOR;
    }

    public static ProjectServiceInfo getServiceFromProject(Project project) {
        Package currentPackage = project.currentPackage();

        Iterable<Module> modules = currentPackage.modules();
        Map<String, List<ServiceInfo>> serviceMap = new HashMap<>();
        Map<String, List<ListenerInfo>> listenerMap = new HashMap<>();
        for (Module module : modules) {
            Collection<DocumentId> documentIds = module.documentIds();
            for (DocumentId doc : documentIds) {
                Document document = module.document(doc);
                String name = document.name();
                Node node = document.syntaxTree().rootNode();

                C2CVisitor visitor = new C2CVisitor();
                node.accept(visitor);
                serviceMap.put(name, visitor.getServices());
                listenerMap.put(name, visitor.getListeners());
            }
        }

        //When service use a listener in another bal file
        //TODO test this
        for (Map.Entry<String, List<ServiceInfo>> entry : serviceMap.entrySet()) {
            String fileName = entry.getKey();
            List<ServiceInfo> value = entry.getValue();
            for (ServiceInfo serviceInfo : value) {
                ListenerInfo listener = serviceInfo.getListener();
                if (listener.getPort() == 0) {
                    String name = listener.getName();
                    List<ListenerInfo> listenerInfos = listenerMap.get(fileName);
                    for (ListenerInfo listenerInfo : listenerInfos) {
                        if (name.equals(listenerInfo.getName())) {
                            listener.setPort(listenerInfo.getPort());
                        }
                    }
                }
            }
        }

        return new ProjectServiceInfo(serviceMap, listenerMap);
    }
}
