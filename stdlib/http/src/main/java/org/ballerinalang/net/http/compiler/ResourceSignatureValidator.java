package org.ballerinalang.net.http.compiler;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.net.http.HttpConstants.ANN_NAME_RESOURCE_CONFIG;
import static org.ballerinalang.net.http.HttpConstants.ANN_RESOURCE_ATTR_BODY;
import static org.ballerinalang.net.http.HttpConstants.ANN_RESOURCE_ATTR_PATH;
import static org.ballerinalang.net.http.HttpConstants.ANN_WEBSOCKET_ATTR_UPGRADE_PATH;
import static org.ballerinalang.net.http.HttpConstants.CALLER;
import static org.ballerinalang.net.http.HttpConstants.HTTP_LISTENER_ENDPOINT;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.HttpConstants.REQUEST;

/**
 * A utility class for validating an HTTP resource signature at compile time.
 *
 * @since 0.965.0
 */
public class ResourceSignatureValidator {

    public static final int COMPULSORY_PARAM_COUNT = 2;

    private static final String ENDPOINT_TYPE = PROTOCOL_PACKAGE_HTTP + ":" + HTTP_LISTENER_ENDPOINT;
    private static final String CALLER_TYPE = PROTOCOL_PACKAGE_HTTP + ":" + CALLER;
    private static final String HTTP_REQUEST_TYPE = PROTOCOL_PACKAGE_HTTP + ":" + REQUEST;

    @SuppressWarnings("unchecked")
    public static void validate(FunctionNode resourceNode, DiagnosticLog dlog, DiagnosticPos pos) {
        List<BLangSimpleVariable> signatureParams = (List<BLangSimpleVariable>) resourceNode.getParameters();
        final int nParams = signatureParams.size();

        if (nParams < COMPULSORY_PARAM_COUNT) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "resource signature parameter count should be >= 2");
            return;
        }

        if (!isValidResourceParam(signatureParams.get(0), CALLER_TYPE)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "first parameter should be of type " + CALLER_TYPE);
            return;
        }

        if (!isValidResourceParam(signatureParams.get(1), HTTP_REQUEST_TYPE)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "second parameter should be of type " + HTTP_REQUEST_TYPE);
        }

        validateResourceAnnotation(resourceNode, dlog);
    }

    private static boolean isValidResourceParam(BLangSimpleVariable param, String expectedType) {
        return expectedType.equals(param.type.toString());
    }

    @SuppressWarnings("unchecked")
    static void validateResourceAnnotation(FunctionNode resourceNode, DiagnosticLog dlog) {
        List<AnnotationAttachmentNode> annotations =
                (List<AnnotationAttachmentNode>) resourceNode.getAnnotationAttachments();
        List<BLangRecordLiteral.BLangRecordKeyValueField> annVals = new ArrayList<>();
        List<String> paramSegments = new ArrayList<>();
        int count = 0;
        for (AnnotationAttachmentNode annotation : annotations) {
            if (annotation.getAnnotationName().getValue().equals(ANN_NAME_RESOURCE_CONFIG) &&
                    annotation.getExpression() != null) {
                for (RecordLiteralNode.RecordField field : ((BLangRecordLiteral) annotation.getExpression()).fields) {
                    annVals.add((BLangRecordLiteral.BLangRecordKeyValueField) field);
                }
                count++;
            }
        }

        if (count != 1) {
            return;
        }

        for (BLangRecordLiteral.BLangRecordKeyValueField keyValue : annVals) {
            switch (getAnnotationFieldKey(keyValue)) {
                case HttpConstants.CONFIG_ATTR_WEBSOCKET_UPGRADE:
                    validateWebSocketUpgrade(resourceNode, dlog, annVals, paramSegments, keyValue);
                    break;
                case ANN_RESOURCE_ATTR_PATH:
                    validateResourcePath(dlog, paramSegments, keyValue);
                    break;
                case ANN_RESOURCE_ATTR_BODY:
                    List<? extends SimpleVariableNode> parameters = resourceNode.getParameters();
                    String bodyFieldValue = "";
                    BLangExpression valueExpr = keyValue.getValue();
                    if (valueExpr instanceof LiteralNode) {
                        LiteralNode literal = (LiteralNode) valueExpr;
                        bodyFieldValue = literal.getValue().toString();
                    }
                    // Data binding param should be placed as the last signature param
                    String signatureBodyParam = parameters.get(parameters.size() - 1).getName().getValue();
                    if (bodyFieldValue.isEmpty()) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, keyValue.getValue().getPosition(),
                                           "Empty data binding param value");

                    } else if (!signatureBodyParam.equals(bodyFieldValue)) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, keyValue.getValue().getPosition(),
                                           "Invalid data binding param in the signature : expected '" +
                                                   bodyFieldValue + "', but found '" + signatureBodyParam + "'");
                    }
                    paramSegments.add(bodyFieldValue);
                    break;
                default:
                    break;
            }
        }
        //Signature params must be a subset of path and data binding  param.
        verifySignatureParamsWithAnnotatedParams(resourceNode, dlog, paramSegments);
    }

    private static void validateWebSocketUpgrade(FunctionNode resourceNode, DiagnosticLog dlog,
                                                 List<BLangRecordLiteral.BLangRecordKeyValueField> annVals,
                                                 List<String> paramSegments,
                                                 BLangRecordLiteral.BLangRecordKeyValueField keyValue) {
        if (annVals.size() > 1) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resourceNode.getPosition(),
                               "Invalid configurations for WebSocket upgrade resource");
            return;
        }
        List<BLangRecordLiteral.BLangRecordKeyValueField> upgradeFields = new ArrayList<>();
        for (RecordLiteralNode.RecordField field : ((BLangRecordLiteral) keyValue.valueExpr).fields) {
            upgradeFields.add((BLangRecordLiteral.BLangRecordKeyValueField) field);
        }

        if (upgradeFields.isEmpty()) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resourceNode.getPosition(),
                               "An upgradeService need to be specified for the WebSocket upgrade " +
                                       "resource");
            return;
        }
        if (upgradeFields.size() == 1 && !(getAnnotationFieldKey(upgradeFields.get(0)).equals("upgradeService"))) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resourceNode.getPosition(),
                               "An upgradeService need to be specified for the WebSocket upgrade " +
                                       "resource");
            return;
        }
        // WebSocket upgrade path validation
        for (BLangRecordLiteral.BLangRecordKeyValueField upgradeField : upgradeFields) {
            if (getAnnotationFieldKey(upgradeField).equals(ANN_WEBSOCKET_ATTR_UPGRADE_PATH.getValue())) {
                validateResourcePath(dlog, paramSegments, upgradeField);
            }
        }
    }

    private static void validateResourcePath(DiagnosticLog dlog, List<String> paramSegments,
                                             BLangRecordLiteral.BLangRecordKeyValueField keyValue) {
        DiagnosticPos position = keyValue.getValue().getPosition();
        List<String> segments = new ArrayList<>();
        BLangExpression valueExpr = keyValue.getValue();
        if (valueExpr instanceof LiteralNode) {
            LiteralNode literal = (LiteralNode) valueExpr;
            segments = Lists.of(literal.getValue().toString().split("/"));
        }
        for (String segment : segments) {
            validatePathSegment(segment, position, dlog, paramSegments);
        }
    }

    private static void validatePathSegment(String segment, DiagnosticPos pos, DiagnosticLog dlog,
                                            List<String> pathParamSegments) {
        boolean expression = false;
        int startIndex = 0;
        int maxIndex = segment.length() - 1;
        int pointerIndex = 0;
        while (pointerIndex < segment.length()) {
            char ch = segment.charAt(pointerIndex);
            switch (ch) {
                case '{':
                    if (expression) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos,
                                           "Illegal open brace character in resource path config");
                        break;
                    }
                    if (pointerIndex + 1 >= maxIndex) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos,
                                           "Invalid param expression in resource path config");
                    }
                    if (pointerIndex != startIndex) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "Illegal expression in resource path config");
                    }
                    expression = true;
                    startIndex++;
                    break;
                case '}':
                    if (!expression) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos,
                                           "Illegal closing brace detected in resource path config");
                        break;
                    }
                    if (pointerIndex <= startIndex) {
                        dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos,
                                           "Illegal empty expression in resource path config");
                    }
                    pathParamSegments.add(segment.substring(startIndex, pointerIndex));
                    expression = false;
                    startIndex = pointerIndex + 1;
                    break;
                default:
                    if (pointerIndex == maxIndex) {
                        if (expression) {
                            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "Incomplete path param expression");
                            break;
                        }
                        if (startIndex != 0 && pointerIndex == startIndex) {
                            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos,
                                               "Illegal expression in resource path config");
                        }
                    }
            }
            pointerIndex++;
        }
    }

    private static void verifySignatureParamsWithAnnotatedParams(FunctionNode resourceNode, DiagnosticLog dlog,
                                                                 List<String> paramSegments) {
        List<? extends SimpleVariableNode> signatureParams = resourceNode.getParameters().subList(
                COMPULSORY_PARAM_COUNT, resourceNode.getParameters().size());
        if (!signatureParams.stream().allMatch(signatureParam -> paramSegments.stream()
                .anyMatch(parameter -> signatureParam.getName().getValue().equals(parameter)))) {
            String errorMsg = "invalid resource parameter(s): cannot specify > 2 parameters without specifying " +
                    "path config and/or body config in the resource annotation";
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resourceNode.getPosition(),
                               errorMsg);
        }
    }

    private ResourceSignatureValidator() {
    }

    private static String getAnnotationFieldKey(BLangRecordLiteral.BLangRecordKeyValueField keyValue) {
        return ((BLangSimpleVarRef) (keyValue.key).expr).variableName.getValue();
    }
}

