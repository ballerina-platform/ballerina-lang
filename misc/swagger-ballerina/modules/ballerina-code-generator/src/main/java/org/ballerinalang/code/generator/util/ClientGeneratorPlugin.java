package org.ballerinalang.code.generator.util;

import org.ballerinalang.code.generator.CodeGenerator;
import org.ballerinalang.code.generator.GeneratorConstants;
import org.ballerinalang.code.generator.exception.CodeGeneratorException;
import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.AnnotationAttachmentAttributeNode;
import org.ballerinalang.util.diagnostic.DiagnosticLog;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

/**
 * Compiler plugin for ballerina client code generation.
 */
@SupportedAnnotationPackages(value = { "ballerina.net.http.swagger" })
public class ClientGeneratorPlugin extends AbstractCompilerPlugin {
    @Override
    public void init(DiagnosticLog diagnosticLog) {}

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        CodeGenerator codegen = new CodeGenerator();
        PrintStream err = System.err;
        AnnotationAttachmentNode config = GeneratorUtils.getAnnotationFromList("configuration", annotations);

        // Generate client only if requested by providing the client config annotation
        if (isClientGenerationEnabled(config)) {
            try {
                ClientContextHolder context = ClientContextHolder.buildContext(serviceNode);
                codegen.writeGeneratedSource(GeneratorConstants.GenType.CLIENT, context,
                        getOutputFilePath(serviceNode));
            } catch (CodeGeneratorException e) {
                err.println("Client code was not generated: " + e.getMessage());
            }
        }
    }

    private String getOutputFilePath(ServiceNode serviceNode) {
        String cUnit = serviceNode.getPosition().getSource().getCompilationUnitName();
        String dir = cUnit.substring(0, cUnit.lastIndexOf(File.separator) + 1);
        String file = serviceNode.getName().getValue() + "Client.bal";
        return dir + file;
    }

    private boolean isClientGenerationEnabled(AnnotationAttachmentNode annotation) {
        boolean isClientRequested = false;
        if (annotation == null) {
            return false;
        }

        for (AnnotationAttachmentAttributeNode attr : annotation.getAttributes()) {
            String attrVal = attr.getValue().getValue().toString();

            if ("client".equals(attr.getName().getValue())) {
                isClientRequested = Boolean.parseBoolean(attrVal);
                break;
            }
        }

        return isClientRequested;
    }
}
