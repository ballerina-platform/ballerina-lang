package org.ballerinalang.net.grpc.proto;

import org.ballerinalang.annotation.AbstractAnnotationProcessor;
import org.ballerinalang.annotation.AnnotationType;
import org.ballerinalang.annotation.SupportedAnnotations;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.proto.definition.File;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;

import java.io.PrintStream;
import java.util.List;

/**
 * This class validates annotations attached to Ballerina service and resource nodes.
 *
 * @since 1.0
 */
@SupportedAnnotations(
        value = {@AnnotationType(packageName = "ballerina.net.grpc", name = "serviceInfo"),
                @AnnotationType(packageName = "ballerina.net.grpc", name = "methodInfo")
        }
)
public class ServiceProtoBuilder extends AbstractAnnotationProcessor {

    private DiagnosticLog dlog;
    private PrintStream out = System.out;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dlog = diagnosticLog;
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        out.println("service node: " + serviceNode.getName().getValue());
        try {
            File fileDefinition =  ServiceProtoUtils.generateProtoDefinition(serviceNode);
            ServiceProtoUtils.writeServiceFiles(fileDefinition, serviceNode.getName().getValue());
        } catch (GrpcServerException e) {
            dlog.logDiagnostic(Diagnostic.Kind.WARNING, serviceNode.getPosition(), e.getMessage());
        }

        // This is how you can report compilation errors, warnings, and messages.
    }
}
