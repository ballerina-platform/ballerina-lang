package io.ballerina.multiservice;

import com.google.gson.Gson;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.multiservice.model.ComponentModel;
import io.ballerina.projects.Project;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The extended service for generation solution architecture model.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("multiServiceModelingService")
public class MultiServiceModelingService implements ExtendedLanguageServerService {
    private WorkspaceManager workspaceManager;

    @Override
    public void init(LanguageServer langServer, WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }

    @JsonNotification
    public CompletableFuture<String> getMultiServiceModel(MultiServiceModelRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = request.getDocumentIdentifier().getUri();
            Path path = Path.of(fileUri);

            Optional<Project> project = workspaceManager.project(path);

            SemanticModel semanticModel = workspaceManager.semanticModel(path).get();

            ComponentModelConstructor multiServiceModelConstrcutor = new ComponentModelConstructor();
            multiServiceModelConstrcutor.constructComponentModel(project.get(), semanticModel);
            ComponentModel multiServiceModel = multiServiceModelConstrcutor.getComponentModel();
            Gson gson = new Gson();
            String multiServiceModelJsonStr = gson.toJson(multiServiceModel);
            return multiServiceModelJsonStr;
        });
    }
}
