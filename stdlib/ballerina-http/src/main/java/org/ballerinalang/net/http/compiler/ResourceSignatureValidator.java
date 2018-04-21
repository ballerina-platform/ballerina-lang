package org.ballerinalang.net.http.compiler;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ResourceNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.net.http.HttpConstants.ANN_NAME_RESOURCE_CONFIG;
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
            return;
        }

        if (!isValidResourceParam(signatureParams.get(0), ENDPOINT_TYPE)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "first parameter should be of type " + ENDPOINT_TYPE);
            return;
        }

        if (!isValidResourceParam(signatureParams.get(1), HTTP_REQUEST_TYPE)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "second parameter should be of type " + HTTP_REQUEST_TYPE);
            return;
        }

        if (nParams == COMPULSORY_PARAM_COUNT) {
            return;
        }
    }

    private static boolean isValidResourceParam(BLangVariable param, String expectedType) {
        return expectedType.equals(param.type.toString());
    }

    @SuppressWarnings("unchecked")

    public static void validateAnnotation(ResourceNode resourceNode, DiagnosticLog dlog) {
        List<AnnotationAttachmentNode> annotations =
                (List<AnnotationAttachmentNode>) resourceNode.getAnnotationAttachments();
        List<BLangRecordLiteral.BLangRecordKeyValue> annVals = new ArrayList<>();
        int count = 0;
        for (AnnotationAttachmentNode annotation : annotations) {
            if (annotation.getAnnotationName().getValue().equals(ANN_NAME_RESOURCE_CONFIG)) {
                annVals = ((BLangRecordLiteral) annotation.getExpression()).keyValuePairs;
                count++;
            }
        }
        if (count > 1) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resourceNode.getPosition(),
                               "There cannot be more than one resource annotations");
        } else if (count == 1) {
            for (BLangRecordLiteral.BLangRecordKeyValue keyValue : annVals) {
                if (((BLangSimpleVarRef) (keyValue.key).expr).variableName
                        .getValue().equals("webSocketUpgrade")) {
                    if (annVals.size() > 1) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, resourceNode.getPosition(),
                                           "Invalid configurations for WebSocket upgrade resource");
                    } else if (((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs.size() == 1) {
                        if (!((BLangSimpleVarRef) (((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs).get(
                                0).key.expr).variableName.getValue().equals("upgradeService")) {
                            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resourceNode.getPosition(),
                                               "An upgradeService need to be specified for the WebSocket upgrade " +
                                                       "resource");
                        }
                    } else if (((BLangRecordLiteral) keyValue.valueExpr).keyValuePairs.size() == 0) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, resourceNode.getPosition(),
                                           "An upgradeService need to be specified for the WebSocket upgrade " +
                                                   "resource");
                    }
                }
            }
        }
    }
}

