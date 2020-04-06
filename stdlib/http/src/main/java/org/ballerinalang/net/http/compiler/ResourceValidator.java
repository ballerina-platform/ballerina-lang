package org.ballerinalang.net.http.compiler;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_WEBSOCKET_UPGRADE;
import static org.ballerinalang.net.http.HttpConstants.ANN_NAME_BODY_PARAM;
import static org.ballerinalang.net.http.HttpConstants.ANN_NAME_PATH_PARAM;
import static org.ballerinalang.net.http.HttpConstants.ANN_NAME_QUERY_PARAM;
import static org.ballerinalang.net.http.HttpConstants.ANN_NAME_RESOURCE_CONFIG;
import static org.ballerinalang.net.http.HttpConstants.ANN_RESOURCE_ATTR_PATH;
import static org.ballerinalang.net.http.HttpConstants.ANN_WEBSOCKET_ATTR_UPGRADE_PATH;
import static org.ballerinalang.net.http.HttpConstants.CALLER;
import static org.ballerinalang.net.http.HttpConstants.HTTP_LISTENER_ENDPOINT;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_HTTP;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.HttpConstants.REQUEST;

/**
 * A utility class for validating an HTTP resource signature, c at compile time.
 *
 * @since 0.965.0
 */
public class ResourceValidator {

    public static final int COMPULSORY_PARAM_COUNT = 2;

    private static final String ENDPOINT_TYPE = PROTOCOL_PACKAGE_HTTP + ":" + HTTP_LISTENER_ENDPOINT;
    private static final String CALLER_TYPE = PROTOCOL_PACKAGE_HTTP + ":" + CALLER;
    private static final String HTTP_REQUEST_TYPE = PROTOCOL_PACKAGE_HTTP + ":" + REQUEST;
    private static final String HTTP_ANNOTATION = "@" + PROTOCOL_HTTP + ":";

    static void validateSignature(List<BLangSimpleVariable> signatureParams, DiagnosticLog dlog, DiagnosticPos pos,
                                  List<String> pathSegments) {
        final int nParams = signatureParams.size();

        if (nParams < COMPULSORY_PARAM_COUNT) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "resource signature parameter count should be >= 2");
            return;
        }

        BLangSimpleVariable caller = signatureParams.get(0);
        if (isInvalidResourceParam(caller, CALLER_TYPE)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, caller.pos, "first parameter should be of type '" +
                    CALLER_TYPE + "'");
            return;
        }

        BLangSimpleVariable request = signatureParams.get(1);
        if (isInvalidResourceParam(request, HTTP_REQUEST_TYPE)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, request.pos, "second parameter should be of type '" +
                    HTTP_REQUEST_TYPE + "'");
            return;
        }

        if (nParams == COMPULSORY_PARAM_COUNT) {
            return;
        }

        int bodyParamCount = 0;
        for (int index = 2; index < nParams; index++) {

            BLangSimpleVariable param = signatureParams.get(index);
            String annotationName = getCompatibleAnnotation(param);
            if (annotationName == null) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, param.pos, "missing annotation of parameter '" +
                        param.name.value + "': expected '" + HTTP_ANNOTATION + ANN_NAME_PATH_PARAM + "', '" +
                        HTTP_ANNOTATION + ANN_NAME_QUERY_PARAM + "', '" + HTTP_ANNOTATION + ANN_NAME_BODY_PARAM + "'");
                continue;
            }
            switch (annotationName) {
                case ANN_NAME_PATH_PARAM:
                    validatePathParam(param, pathSegments, dlog);
                    break;
                case ANN_NAME_QUERY_PARAM:
                    validateQueryParam(param, dlog);
                    break;
                case ANN_NAME_BODY_PARAM:
                    if (bodyParamCount++ == 0) {
                        validateBodyParam(param, dlog);
                        continue;
                    }
                    dlog.logDiagnostic(Diagnostic.Kind.ERROR, param.pos, "invalid multiple '" + HTTP_ANNOTATION +
                            ANN_NAME_BODY_PARAM + "' annotations: cannot specify > 1 entity-body params");
                    break;
                default:
                    // do not execute
            }
        }
        if (!pathSegments.isEmpty()) {
            List<String> expressions = new ArrayList<>();
            for (String segment : pathSegments) {
                expressions.add("{" + segment + "}");
            }
            dlog.logDiagnostic(Diagnostic.Kind.WARNING, pos, "unused path segment(s) '" +
                    String.join(", ", expressions) + "' in the 'path' field of the 'ResourceConfig'");
        }
    }

    private static boolean isInvalidResourceParam(BLangSimpleVariable param, String expectedType) {
        return !expectedType.equals(param.type.toString());
    }

    private static String getCompatibleAnnotation(BLangSimpleVariable param) {
        for (BLangAnnotationAttachment annotationAttachment : param.annAttachments) {
            BAnnotationSymbol annotationSymbol = annotationAttachment.annotationSymbol;
            if (!PROTOCOL_HTTP.equals(annotationSymbol.pkgID.name.value)) {
                continue;
            }
            String annotationName = annotationSymbol.name.value;
            if (ANN_NAME_PATH_PARAM.equals(annotationName) || ANN_NAME_QUERY_PARAM.equals(annotationName) ||
                    ANN_NAME_BODY_PARAM.equals(annotationName)) {
                return annotationName;
            }
        }
        return null;
    }

    private static void validatePathParam(BLangSimpleVariable param, List<String> pathSegments, DiagnosticLog dlog) {
        BType paramType = param.type;
        int varTag = paramType.tag;

        if (varTag != TypeTags.STRING && varTag != TypeTags.INT && varTag != TypeTags.BOOLEAN &&
                varTag != TypeTags.FLOAT) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, param.pos, "incompatible path parameter type: expected " +
                    "'string', 'int', 'boolean', 'float', found '" + param.type + "'");
            return;
        }
        String paramName = param.name.value;
        if (!pathSegments.contains(paramName)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, param.pos, "invalid path parameter: '" + param.toString() +
                    "', missing segment '{" + paramName + "}' in the 'path' field of the 'ResourceConfig'");
            return;
        }
        pathSegments.remove(paramName);
    }

    private static void validateQueryParam(BLangSimpleVariable param, DiagnosticLog dlog) {
        BType paramType = param.type;
        int varTag = paramType.tag;

        if (varTag != TypeTags.STRING && (varTag != TypeTags.ARRAY ||
                ((BArrayType) paramType).getElementType().tag != TypeTags.STRING)) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, param.pos, "incompatible query parameter type: expected " +
                    "'string', 'string[]', found '" + param.type + "'");
        }
    }

    private static void validateBodyParam(BLangSimpleVariable param, DiagnosticLog dlog) {
        BType paramType = param.type;
        int type = paramType.tag;

        if (type != TypeTags.RECORD && type != TypeTags.JSON && type != TypeTags.XML &&
                type != TypeTags.STRING && (type != TypeTags.ARRAY || !validArrayType(paramType))) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, param.pos, "incompatible entity-body parameter type: expected " +
                    "'string', 'json', 'xml', 'byte[]', '{}', '{}[]', found '" + param.type + "'");
        }
    }

    private static boolean validArrayType(BType entityBodyParamType) {
        return ((BArrayType) entityBodyParamType).getElementType().tag == TypeTags.BYTE ||
                ((BArrayType) entityBodyParamType).getElementType().tag == TypeTags.RECORD;
    }

    @SuppressWarnings("unchecked")
    static List<String> validateResourceAnnotation(FunctionNode resourceNode, DiagnosticLog dlog) {
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
            return paramSegments;
        }

        for (BLangRecordLiteral.BLangRecordKeyValueField keyValue : annVals) {
            switch (getAnnotationFieldKey(keyValue)) {
                case ANN_CONFIG_ATTR_WEBSOCKET_UPGRADE:
                    validateWebSocketUpgrade(resourceNode, dlog, annVals, paramSegments, keyValue);
                    break;
                case ANN_RESOURCE_ATTR_PATH:
                    validateResourcePath(dlog, paramSegments, keyValue);
                    break;
                default:
                    break;
            }
        }
        return paramSegments;
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
            if (getAnnotationFieldKey(upgradeField).equals(ANN_WEBSOCKET_ATTR_UPGRADE_PATH)) {
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
                                           "Invalid parameter expression in resource path config");
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
                            dlog.logDiagnostic(Diagnostic.Kind.ERROR, pos, "Incomplete path parameter expression");
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

    private ResourceValidator() {
    }

    private static String getAnnotationFieldKey(BLangRecordLiteral.BLangRecordKeyValueField keyValue) {
        return ((BLangSimpleVarRef) (keyValue.key).expr).variableName.getValue();
    }
}
