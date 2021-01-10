package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import org.ballerinalang.langserver.codeaction.toml.C2CVisitor;
import org.ballerinalang.langserver.codeaction.toml.ServiceInfo;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.toml.Probe;
import org.ballerinalang.langserver.toml.ProbeStore;
import org.ballerinalang.langserver.toml.TomlProbeManger;
import org.ballerinalang.langserver.toml.TomlSyntaxTreeUtil;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.nio.file.Path;
import java.util.ArrayList;
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
                CodeAction action = new CodeAction();
                switch (diagnostic.getMessage()) {
                    case "Invalid Readiness Probe Path":
                    case "Invalid Liveness Probe Path":
                        codeActions.addAll(handleInvalidPath(diagnostic, ctx));
                        break;
                    case "Invalid Port":
                        action.setTitle("Port");
                        action.setDiagnostics(cursorDiagnostics);
                        codeActions.add(action);
                        break;
                    default:
                        break;
                }
            }
        }
        return codeActions;
    }

    public static List<CodeAction> handleInvalidPath(Diagnostic diagnostic, CodeActionContext ctx) {
        SyntaxTree syntaxTree = TomlSyntaxTreeUtil.getTomlSyntaxTree(ctx.filePath()).orElseThrow();
        ProbeStore store = TomlProbeManger.getInstance(syntaxTree).getStore();
        if (diagnostic.getMessage().equals("Invalid Readiness Probe Path")) {
            Optional<Probe> readiness = store.getReadiness();
            if (readiness.isPresent()) {
                return addResourceToService(diagnostic, ctx, readiness.get());
            }
        } else {
            Optional<Probe> liveness = store.getLiveness();
            if (liveness.isPresent()) {
                return addResourceToService(diagnostic, ctx, liveness.get());
            }
        }
        return new ArrayList<>();
    }

    public static List<CodeAction> addResourceToService(Diagnostic diagnostic, CodeActionContext ctx,
                                                        Probe probe) {
        Optional<Project> project = ctx.workspace().project(ctx.filePath());
        Map<String, List<ServiceInfo>> serviceList = getServiceFromProject(project.orElseThrow());

        //TODO get the filename
        List<ServiceInfo> serviceInfos = serviceList.get("main.bal");
        List<CodeAction> codeActionList = new ArrayList<>();
        for (ServiceInfo service : serviceInfos) {
            int port = service.getListener().getPort();
            if (probe.getPort() == port) {
                Path balFilePath = ctx.workspace().projectRoot(ctx.filePath()).resolve("main.bal");
                NodeList<Node> members = service.getNode().members();
                Node lastResource = members.get(members.size() - 1);
                Position position = new Position(lastResource.lineRange().endLine().line() + 1, 0);

                CodeAction action = new CodeAction();
                action.setKind(CodeActionKind.QuickFix);
                String importText = generateProbeFunctionText(service, probe);
                List<TextEdit> edits = Collections.singletonList(
                        new TextEdit(new Range(position, position), importText));
                action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                        new TextDocumentEdit(new VersionedTextDocumentIdentifier(balFilePath.toUri().toString(), null),
                                edits)))));
                action.setTitle("Add Resource to Service");
                List<Diagnostic> cursorDiagnostics = new ArrayList<>();
                cursorDiagnostics.add(diagnostic);
                action.setDiagnostics(cursorDiagnostics);
                codeActionList.add(action);
            }
        }
        return codeActionList;
    }

    private static String generateProbeFunctionText(ServiceInfo service, Probe probe) {
        String tomlPath = TomlSyntaxTreeUtil.trimResourcePath(probe.getPath());
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

    public static Map<String, List<ServiceInfo>> getServiceFromProject(Project project) {
        if (project.kind() == ProjectKind.BUILD_PROJECT) {
            Package currentPackage = project.currentPackage();

            Iterable<Module> modules = currentPackage.modules();
            Map<String, List<ServiceInfo>> serviceMap = new HashMap<>();
            for (Module module : modules) {
                Collection<DocumentId> documentIds = module.documentIds();
                for (DocumentId doc : documentIds) {
                    Document document = module.document(doc);
                    String name = document.name();
                    Node node = document.syntaxTree().rootNode();

                    C2CVisitor visitor = new C2CVisitor();
                    node.accept(visitor);
                    serviceMap.put(name, new ArrayList<>(visitor.getServices().values()));
                }
            }
            return serviceMap;
        }
        return new HashMap<>();
    }
}
