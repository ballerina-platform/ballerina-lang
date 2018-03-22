package org.ballerinalang.net.http.compiler;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;

import java.util.List;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.HttpConstants.REQUEST;
import static org.ballerinalang.net.http.HttpConstants.SERVICE_ENDPOINT;

/**
 * A utility class for validating an HTTP resource signature at compile time.
 *
 * @since 0.965.0
 */
public class ResourceSignatureValidator {

    public static final int COMPULSORY_PARAM_COUNT = 2;

    private static final String ENDPOINT_TYPE = PROTOCOL_PACKAGE_HTTP + ":" + SERVICE_ENDPOINT;
    private static final String HTTP_REQUEST_TYPE = PROTOCOL_PACKAGE_HTTP + ":" + REQUEST;

    public static void validate(List<BLangVariable> signatureParams) {
        final int nParams = signatureParams.size();

        if (nParams < COMPULSORY_PARAM_COUNT) {
            throw new BallerinaConnectorException("resource signature parameter count should be >= 2");
        }

        if (!isValidResourceParam(signatureParams.get(0), ENDPOINT_TYPE)) {
            throw new BallerinaConnectorException("first parameter should be of type " + ENDPOINT_TYPE);
        }

        if (!isValidResourceParam(signatureParams.get(1), HTTP_REQUEST_TYPE)) {
            throw new BallerinaConnectorException("second parameter should be of type " + HTTP_REQUEST_TYPE);
        }

        if (nParams == COMPULSORY_PARAM_COUNT) {
            return;
        }
    }

    private static boolean isValidResourceParam(BLangVariable param, String expectedType) {
        return expectedType.equals(param.type.toString());
    }
}
