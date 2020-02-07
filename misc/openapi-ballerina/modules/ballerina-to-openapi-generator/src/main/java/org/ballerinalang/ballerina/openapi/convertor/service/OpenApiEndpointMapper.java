package org.ballerinalang.ballerina.openapi.convertor.service;

import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import org.ballerinalang.ballerina.openapi.convertor.Constants;
import org.ballerinalang.ballerina.openapi.convertor.ConverterUtils;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.SimpleVariableReferenceNode;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Extract OpenApi server information from and Ballerina endpoint.
 */
public class OpenApiEndpointMapper {

    /**
     * Convert endpoints bound to {@code service} openapi server information.
     * Currently this method only selects last endpoint bound to {@code service}
     * as the Server for openapi definition. This is due to OAS2 not supporting
     * multiple Server definitions.
     *
     * @param endpoints all endpoints defined in ballerina source
     * @param service   service node with bound endpoints
     * @param openapi   openapi definition to attach extracted information
     * @return openapi definition with Server information
     */
    public Swagger convertBoundEndpointsToOpenApi(List<BLangSimpleVariable> endpoints, ServiceNode service,
                                                  Swagger openapi) {
        if (endpoints == null || service == null || service.getAttachedExprs().isEmpty()
                || (service.getAttachedExprs().get(0).getKind() != NodeKind.SIMPLE_VARIABLE_REF
                && service.getAttachedExprs().get(0).getKind() != NodeKind.TYPE_INIT_EXPR)) {
            return openapi;
        }
        if (openapi == null) {
            return new Swagger();
        }

        if (service.getAttachedExprs().get(0).getKind() == NodeKind.TYPE_INIT_EXPR) {
            BLangTypeInit type = (BLangTypeInit) service.getAttachedExprs().get(0);
            List<BLangExpression> list = type.argsExpr;
            List<Scheme> schemes = new ArrayList<>();
            String port = null;
            String host = null;

            if (list.size() == 1) {
                port = ConverterUtils.getStringLiteralValue(list.get(0));
            }

            if (list.size() > 1
                    && list.get(1) instanceof BLangNamedArgsExpression
                    && ((BLangNamedArgsExpression) list.get(1)).getExpression() instanceof BLangRecordLiteral) {
                BLangRecordLiteral bLangRecordLiteral = (BLangRecordLiteral) ((BLangNamedArgsExpression) list.get(1))
                        .getExpression();
                Map<String, BLangExpression> configs = ConverterUtils.listToMap(bLangRecordLiteral.fields);

                host = configs.get(Constants.ATTR_HOST) != null ?
                        ConverterUtils.getStringLiteralValue(configs.get(Constants.ATTR_HOST)) :
                        null;
                Scheme scheme = configs.get(Constants.SECURE_SOCKETS) == null ? Scheme.HTTP : Scheme.HTTPS;
                schemes.add(scheme);
            }

            // Set default values to host and port if values are not defined
            if (host == null) {
                host = Constants.ATTR_DEF_HOST;
            }
            if (port != null) {
                host += ':' + port;
            }

            openapi.setHost(host);
            openapi.setSchemes(schemes);
        } else if (service.getAttachedExprs().get(0).getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            SimpleVariableReferenceNode node = (SimpleVariableReferenceNode) service.getAttachedExprs().get(0);
            for (BLangSimpleVariable ep : endpoints) {
                // At the moment only the last bound endpoint will be populated in openapi
                // we need to move to OAS3 models to support multiple server support
                if (node.getVariableName().equals(ep.getName())) {
                    extractServer(ep, openapi);
                    break;
                }
            }
        }

        return openapi;
    }

    private void extractServer(BLangSimpleVariable ep, Swagger openapi) {
        BLangTypeInit bTypeInit = (BLangTypeInit) ep.getInitialExpression();
        List<BLangExpression> list = bTypeInit.argsExpr;
        List<Scheme> schemes = new ArrayList<>();
        String port = null;
        String host = null;

        if (list.size() == 1) {
            port = ConverterUtils.getStringLiteralValue(list.get(0));
        }

        if (list.size() > 1
                && list.get(1) instanceof BLangNamedArgsExpression
                && ((BLangNamedArgsExpression) list.get(1)).getExpression() instanceof BLangRecordLiteral) {
            BLangRecordLiteral bLangRecordLiteral = (BLangRecordLiteral) ((BLangNamedArgsExpression) list.get(1))
                    .getExpression();
            Map<String, BLangExpression> configs = ConverterUtils.listToMap(bLangRecordLiteral.fields);

            host = configs.get(Constants.ATTR_HOST) != null ?
                    ConverterUtils.getStringLiteralValue(configs.get(Constants.ATTR_HOST)) :
                    null;
            Scheme scheme = configs.get(Constants.SECURE_SOCKETS) == null ? Scheme.HTTP : Scheme.HTTPS;
            schemes.add(scheme);
        }

        // Set default values to host and port if values are not defined
        if (host == null) {
            host = Constants.ATTR_DEF_HOST;
        }
        if (port != null) {
            host += ':' + port;
        }

        openapi.setHost(host);
        openapi.setSchemes(schemes);
    }
}
