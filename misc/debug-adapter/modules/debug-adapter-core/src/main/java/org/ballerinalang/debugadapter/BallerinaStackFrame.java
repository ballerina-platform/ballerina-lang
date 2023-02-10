/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.debugadapter;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.config.ClientConfigHolder;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.jdi.LocalVariableProxyImpl;
import org.ballerinalang.debugadapter.jdi.StackFrameProxyImpl;
import org.ballerinalang.debugadapter.utils.PackageUtils;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.StackFrame;

import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.debugadapter.JBallerinaDebugServer.isBalStackFrame;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.STRAND_VAR_NAME;
import static org.ballerinalang.debugadapter.variable.VariableUtils.isService;
import static org.ballerinalang.debugadapter.variable.VariableUtils.removeRedundantQuotes;
import static org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper.LAMBDA;

/**
 * Represents a ballerina stack frame instance.
 *
 * @since 2.0.0
 */
public class BallerinaStackFrame {

    private final ExecutionContext context;
    private final Integer frameId;
    private final StackFrameProxyImpl jStackFrame;
    private StackFrame dapStackFrame;

    private static final String STRAND_FIELD_NAME = "name";
    private static final String FRAME_TYPE_START = "start";
    private static final String FRAME_TYPE_WORKER = "worker";
    private static final String FRAME_TYPE_ANONYMOUS = "anonymous";
    private static final String FRAME_SEPARATOR = ":";
    private static final String ACCESSOR_DEFAULT = "default";
    private static final String METHOD_INIT = "$init$";
    private static final String SELF_VAR_NAME = "self";
    private static final String WORKER_LAMBDA_REGEX = "(\\$lambda\\$)\\b(.*)\\b(\\$lambda)(.*)";
    private static final String BALA_URI_SCHEME_NAME = "bala:";

    public BallerinaStackFrame(ExecutionContext context, Integer frameId, StackFrameProxyImpl stackFrameProxy) {
        this.context = context;
        this.frameId = frameId;
        this.jStackFrame = stackFrameProxy;
    }

    public StackFrameProxyImpl getJStackFrame() {
        return jStackFrame;
    }

    /**
     * Returns a debugger adapter protocol compatible instance of this breakpoint.
     *
     * @return as an instance of {@link org.eclipse.lsp4j.debug.StackFrame}
     */
    public Optional<StackFrame> getAsDAPStackFrame() {
        if (dapStackFrame != null) {
            return Optional.of(dapStackFrame);
        }

        dapStackFrame = computeDapStackFrame();
        return Optional.ofNullable(dapStackFrame);
    }

    private StackFrame computeDapStackFrame() {
        try {
            if (!isBalStackFrame(jStackFrame.getStackFrame())) {
                return null;
            }
            StackFrame dapStackFrame = new StackFrame();
            dapStackFrame.setId(frameId);
            dapStackFrame.setName(getStackFrameName(jStackFrame));
            dapStackFrame.setLine(jStackFrame.location().lineNumber());
            dapStackFrame.setColumn(0);

            Optional<Map.Entry<Path, DebugSourceType>> sourcePathAndType =
                    PackageUtils.getStackFrameSourcePath(jStackFrame.location(), context.getSourceProject());
            if (sourcePathAndType.isEmpty()) {
                return null;
            }
            Path frameLocationPath = sourcePathAndType.get().getKey();
            DebugSourceType sourceType = sourcePathAndType.get().getValue();
            Source source = new Source();
            source.setName(jStackFrame.location().sourceName());
            URI uri = frameLocationPath.toAbsolutePath().toUri();

            // If the corresponding source file of the stack frame is a bala source, the source should be readonly in
            // the editor side. Therefore adds a custom URI scheme to the file path, after verifying that the connected
            // debug client support custom URI schemes.
            Optional<ClientConfigHolder.ExtendedClientCapabilities> capabilities =
                    context.getAdapter().getClientConfigHolder().getExtendedCapabilities();
            boolean supportsReadOnlyEditors = capabilities.isPresent() && capabilities.get().supportsReadOnlyEditors();

            if (supportsReadOnlyEditors && sourceType == DebugSourceType.DEPENDENCY) {
                // Note: Since we are using a Ballerina-specific custom URI scheme, the future DAP client
                // implementations may have to implement custom editor providers in order to support URIs coming
                // from the debug server.
                source.setPath(PackageUtils.covertToBalaUri(uri).toString());
            } else {
                source.setPath(uri.toString());
            }
            dapStackFrame.setSource(source);
            return dapStackFrame;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Derives ballerina stack frame name from the given java stack frame instance.
     *
     * @param stackFrame JDI stack frame instance
     * @return Ballerina stack frame name
     */
    private static String getStackFrameName(StackFrameProxyImpl stackFrame) {
        try {
            String frameName;
            ObjectReference strand = getStrand(stackFrame);
            if (strand != null) {
                Value frameNameValue = strand.getValue(strand.referenceType().fieldByName(STRAND_FIELD_NAME));
                if (frameNameValue != null) {
                    frameName = removeRedundantQuotes(String.valueOf(frameNameValue));
                } else {
                    frameName = FRAME_TYPE_ANONYMOUS;
                }
            } else {
                frameName = FRAME_TYPE_ANONYMOUS;
            }

            if (stackFrame.location().method().name().matches(WORKER_LAMBDA_REGEX)) {
                return FRAME_TYPE_WORKER + FRAME_SEPARATOR + frameName;
            } else if (stackFrame.location().method().name().contains(LAMBDA)) {
                return stackFrame.visibleVariableByName(STRAND_VAR_NAME) != null ? frameName :
                        FRAME_TYPE_START + FRAME_SEPARATOR + frameName;
            } else {
                return getFilteredStackFrame(stackFrame);
            }
        } catch (Exception e) {
            return FRAME_TYPE_ANONYMOUS;
        }
    }

    /**
     * Derives filtered ballerina stack frame name from the given java stack frame instance.
     *
     * @param stackFrame JDI stack frame instance
     * @return filtered ballerina stack frame name
     */
    private static String getFilteredStackFrame(StackFrameProxyImpl stackFrame) throws JdiProxyException {
        String stackFrameName = stackFrame.location().method().name();
        LocalVariableProxyImpl selfVariable = stackFrame.visibleVariableByName(SELF_VAR_NAME);
        if (selfVariable == null) {
            return stackFrameName;
        }
        Value selfValue = stackFrame.getValue(selfVariable);
        if (isService(selfValue)) {
            if (stackFrameName.equals(METHOD_INIT)) {
                return BVariableType.SERVICE.getString();
            } else {
                // Here stackFrameName format will be `$resourceAccessor$resourceName$otherResourceParts`
                String[] stackFrameNameParts = stackFrameName.split("\\$");
                if (stackFrameNameParts.length > 1 && stackFrameNameParts[1].equals(ACCESSOR_DEFAULT)) {
                    return stackFrameNameParts[1];
                }
                return stackFrameNameParts.length > 2 ? stackFrameNameParts[2] : stackFrameName;
            }
        }
        return stackFrameName;
    }

    /**
     * Retrieves ballerina strand instance of the given stack frame.
     */
    private static ObjectReference getStrand(StackFrameProxyImpl frame) {
        try {
            if (frame.visibleVariableByName(STRAND_VAR_NAME) == null) {
                return (ObjectReference) ((ArrayReference) frame.getStackFrame().getArgumentValues().get(0))
                        .getValue(0);
            }
            return (ObjectReference) frame.getValue(frame.visibleVariableByName(STRAND_VAR_NAME));
        } catch (Exception e) {
            return null;
        }
    }
}
