/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.evaluation.engine.invokable;

import com.sun.jdi.Method;
import com.sun.jdi.Value;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;

import java.util.List;
import java.util.Optional;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.STRAND_NOT_FOUND;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.STRAND_VAR_NAME;

/**
 * JDI based java method representation for a given ballerina function.
 *
 * @since 2.0.0
 */
public abstract class JvmMethod {

    protected final SuspendedContext context;
    protected final Method methodRef;
    protected List<Value> argValues;

    JvmMethod(SuspendedContext context, Method methodRef) {
        this.context = context;
        this.methodRef = methodRef;
        this.argValues = null;
    }

    /**
     * Invokes the underlying JVM method with given args. Each concrete class should have their own implementation for
     * this JDI-based invocation.
     *
     * @return invocation result
     */
    protected abstract Value invoke() throws EvaluationException;

    /**
     * Safely invokes the underlying JVM method with given args. This will prevent potential deadlocks which can
     * occur during single-threaded method executions in the remote JVM.
     *
     * @return invocation result
     */
    public Value invokeSafely() throws EvaluationException {
        disablePendingJDIRequests();
        return this.invoke();
    }

    /**
     * When invoking methods in the remote JVM, it can cause deadlocks if 'invokeMethod' is called from the
     * client's event handler thread. In that case, the thread will be waiting for the invokeMethod to complete
     * and won't read the EventSet that comes in for the new event. If this new EventSet is in 'SUSPEND_ALL'
     * mode, then a deadlock will occur because no one will resume the EventSet. Therefore to avoid this, we are
     * disabling possible event requests before doing any method invocations.
     */
    private void disablePendingJDIRequests() {
        EventRequestManager eventManager = context.getExecutionContext().getEventManager();
        eventManager.classPrepareRequests().forEach(EventRequest::disable);
        eventManager.breakpointRequests().forEach(EventRequest::disable);
    }

    /**
     * Returns the required set of argument values to invoke underlying JVM method.
     *
     * @return invocation result
     */
    protected abstract List<Value> getMethodArgs(JvmMethod method) throws EvaluationException;

    public Method getJDIMethodRef() {
        return methodRef;
    }

    public void setArgValues(List<Value> argValues) {
        this.argValues = argValues;
    }

    /**
     * Checks if the exception is an instance of {@link io.ballerina.runtime.api.values.BError} and if so,
     * returns its JDI value instance.
     */
    protected Value extractBErrors(Exception e) throws EvaluationException {
        Optional<Value> potentialBError = EvaluationUtils.getBError(e);
        if (potentialBError.isPresent()) {
            return potentialBError.get();
        }
        throw createEvaluationException(FUNCTION_EXECUTION_ERROR, methodRef.name());
    }
}
