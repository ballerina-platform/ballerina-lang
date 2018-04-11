package org.ballerinalang.net.http.compiler;

import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

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

    public static void validate(List<BLangVariable> signatureParams, DiagnosticLog dlog, DiagnosticPos pos) {
        final int nParams = signatureParams.size();

        if (nParams < COMPULSORY_PARAM_COUNT) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "resource signature parameter count should be >= 2");
        }

        if (!isValidResourceParam(signatureParams.get(0), ENDPOINT_TYPE)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "first parameter should be of type " + ENDPOINT_TYPE);
        }

        if (!isValidResourceParam(signatureParams.get(1), HTTP_REQUEST_TYPE)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "second parameter should be of type " + HTTP_REQUEST_TYPE);
        }

        if (nParams == COMPULSORY_PARAM_COUNT) {
            return;
        }
    }

    private static boolean isValidResourceParam(BLangVariable param, String expectedType) {
        return expectedType.equals(param.type.toString());
    }
}
