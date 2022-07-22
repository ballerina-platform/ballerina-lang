package io.ballerina.multiservice;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.multiservice.model.ComponentModel;
import io.ballerina.multiservice.model.ComponentModel.PackageId;
import io.ballerina.multiservice.model.Service;
import io.ballerina.multiservice.nodevisitors.ServiceNodeVisitor;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Construct component model fpr project with multiple service.
 */
public class ComponentModelConstructor {
    private ComponentModel componentModel;

    public ComponentModel getComponentModel() {
        return componentModel;
    }

    public void setComponentModel(ComponentModel componentModel) {
        this.componentModel = componentModel;
    }

    public void constructComponentModel(Project project, SemanticModel semanticModel) {
        List<Service> availableServices = new ArrayList<>();

        // get project from the workspace

        Package currentPackage = project.currentPackage();

        String packageName = String.valueOf(currentPackage.packageName());
        String packageOrg = String.valueOf(currentPackage.packageOrg());
        String packageVersion = String.valueOf(currentPackage.packageVersion());

        PackageId packageId = new PackageId(packageName, packageOrg, packageVersion);

        currentPackage.modules().forEach(module -> {
            Collection<DocumentId> documentIds = module.documentIds();
            for (DocumentId documentId : documentIds) {
                SyntaxTree syntaxTree = module.document(documentId).syntaxTree();
                ServiceNodeVisitor serviceNodeVisitor = new ServiceNodeVisitor(semanticModel,
                        module.document(documentId));
                syntaxTree.rootNode().accept(serviceNodeVisitor);
                availableServices.addAll(serviceNodeVisitor.getServices());
            }
            this.componentModel = new ComponentModel(packageId, availableServices);
        });
    }

//    public static MultiServiceModel generateMultiServiceModel(Path path) {
//        MultiServiceModel multiServiceModel;
//        List<String> availableServices = new ArrayList<>();
//
//        Project project = ProjectLoader.loadProject(path);
//        Package currentPackage = project.currentPackage();
//
//        Collection<ModuleId> moduleIds = currentPackage.moduleIds();
//
//        for (ModuleId moduleId : moduleIds) {
//            Module module = currentPackage.module(moduleId);
//            Collection<DocumentId> documentIds = module.documentIds();
//            for (DocumentId documentId : documentIds) {
//                SyntaxTree syntaxTree = module.document(documentId).syntaxTree();
//                ModulePartNode modulePartNode = syntaxTree.rootNode();
//                for (Node node : modulePartNode.members()) {
//                    SyntaxKind syntaxKind = node.kind();
//                    if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
//                        StringBuilder serviceName = new StringBuilder();
//                        ServiceDeclarationNode serviceNode = (ServiceDeclarationNode) node;
//                        NodeList<Node> serviceNameNodes = serviceNode.absoluteResourcePath();
//                        for (Node serviceNameNode : serviceNameNodes) {
//                            serviceName.append(serviceNameNode.toString());
//                        }
//                    }
//                }
//            }
//        }
//        return null;
//    }

}
