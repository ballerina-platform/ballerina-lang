package org.ballerinalang.ballerina.swagger.convertor.service;

import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import org.ballerinalang.ballerina.swagger.convertor.Constants;
import org.ballerinalang.ballerina.swagger.convertor.ConverterUtils;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.SimpleVariableReferenceNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Extract Swagger server information from and Ballerina endpoint.
 */
public class SwaggerEndpointMapper {

    /**
     * Convert endpoints bound to {@code service} swagger server information.
     * <p>Currently this method only selects last endpoint bound to {@code service}
     * as the Server for swagger definition. This is due to OAS2 not supporting
     * multiple Server definitions.</p>
     *
     * @param endpoints all endpoints defined in ballerina source
     * @param service service node with bound endpoints
     * @param swagger swagger definition to attach extracted information
     * @return swagger definition with Server information
     */
    public Swagger convertBoundEndpointsToSwagger(List<EndpointNode> endpoints, ServiceNode service, Swagger swagger) {
        if (endpoints == null || service == null) {
            return swagger;
        }
        if (swagger == null) {
            return new Swagger();
        }

        for (SimpleVariableReferenceNode node : service.getBoundEndpoints()) {
            for (EndpointNode ep : endpoints) {
                // At the moment only the last bound endpoint will be populated in swagger
                // we need to move to OAS3 models to support multiple server support
                if (node.getVariableName().equals(ep.getName())) {
                    extractServer(ep, swagger);
                    break;
                }
            }
        }
        return swagger;
    }

    private void extractServer(EndpointNode ep, Swagger swagger) {
        BLangRecordLiteral bLiteral = (BLangRecordLiteral) ep.getConfigurationExpression();
        List<BLangRecordLiteral.BLangRecordKeyValue> list = bLiteral.getKeyValuePairs();
        Map<String, BLangExpression> configs = ConverterUtils.listToMap(list);

        String port = configs.get(Constants.ATTR_HTTP_PORT) != null ?
                ConverterUtils.getStringLiteralValue(configs.get(Constants.ATTR_HTTP_PORT)) :
                null;
        String host = configs.get(Constants.ATTR_HOST) != null ?
                ConverterUtils.getStringLiteralValue(configs.get(Constants.ATTR_HOST)) :
                null;

        List<Scheme> schemes = new ArrayList<>();
        Scheme scheme = configs.get(Constants.SECURE_SOCKETS) == null ? Scheme.HTTP : Scheme.HTTPS;
        schemes.add(scheme);

        // Set default values to host and port if values are not defined
        if (host == null) {
            host = Constants.ATTR_DEF_HOST;
        }
        if (port != null) {
            host += ':' + port;
        }

        swagger.setHost(host);
        swagger.setSchemes(schemes);
    }
}
