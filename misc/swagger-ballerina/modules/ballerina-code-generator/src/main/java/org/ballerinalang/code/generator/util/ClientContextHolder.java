package org.ballerinalang.code.generator.util;

import org.ballerinalang.code.generator.model.OperationContext;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ResourceNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.AnnotationAttachmentAttributeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Context holder for ballerina client generation.
 * This class will hold the context information required for ballerina code generation
 */
public class ClientContextHolder {
    private static final String HTTP_CONFIG_ANNOTATION = "configuration";
    private static final String HTTP_PKG_ALIAS = "http";

    private String name;
    private String url;
    private List<OperationContext> operations;

    public static ClientContextHolder buildContext(ServiceNode service) {
        ClientContextHolder context = new ClientContextHolder();
        context.name = service.getName().getValue();
        context.operations = new ArrayList<>();

        // Iterate through all service level annotations and find out service hosting information
        for (AnnotationAttachmentNode ann: service.getAnnotationAttachments()) {
            if (HTTP_PKG_ALIAS.equals(ann.getPackageAlias().getValue()) && HTTP_CONFIG_ANNOTATION
                    .equals(ann.getAnnotationName().getValue())) {
                String host = "";
                String port = "";
                String basePath = "";
                String httpsPort = null;

                for (AnnotationAttachmentAttributeNode attr: ann.getAttributes()) {
                    String attrVal = attr.getValue().getValue().toString();
                    String attrName = attr.getName().getValue();

                    if ("host".equals(attrName)) {
                        host = attrVal;
                    } else if ("port".equals(attrName)) {
                        port = attrVal;
                    } else if ("httpsPort".equals(attrName)) {
                        httpsPort = attrVal;
                    } else if ("basePath".equals(attrName)) {
                        basePath = attrVal;
                    }
                }

                StringBuffer sb = new StringBuffer();
                // Select protocol according to given ports. HTTPS is given the priority
                if (httpsPort != null) {
                    sb.append("https://");
                    port = httpsPort;
                } else {
                    sb.append("http://");
                }

                context.url = sb.append(host).append(':').append(port).append(basePath).toString();

                break;
            }
        }

        // Extract ballerina resource nodes as parsable operations
        for (ResourceNode resource: service.getResources()) {
            OperationContext operation = OperationContext.buildOperation(resource);
            context.operations.add(operation);
        }

        return context;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public List<OperationContext> getOperations() {
        return operations;
    }
}
