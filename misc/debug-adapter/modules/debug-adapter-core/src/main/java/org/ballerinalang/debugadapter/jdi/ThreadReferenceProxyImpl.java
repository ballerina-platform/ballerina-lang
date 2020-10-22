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

package org.ballerinalang.debugadapter.jdi;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InternalException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Proxy impkementation for JDI thread reference.
 *
 * @since 2.0.0
 */
public final class ThreadReferenceProxyImpl extends ObjectReferenceProxyImpl implements ThreadReferenceProxy {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadReferenceProxyImpl.class);
    // cached data
    private String myName;
    private int myFrameCount = -1;
    // stack frames, 0 - bottom
    private final LinkedList<StackFrameProxyImpl> myFramesFromBottom = new LinkedList<>();
    //cache build on the base of myFramesFromBottom 0 - top, initially nothing is cached
    private List<StackFrameProxyImpl> myFrames = null;

    private ThreadGroupReferenceProxyImpl myThreadGroupProxy;

    private ThreeState myResumeOnHotSwap = ThreeState.UNSURE;

    public static final Comparator<ThreadReferenceProxyImpl> COMPARATOR = (th1, th2) -> {
        int res = Boolean.compare(th2.isSuspended(), th1.isSuspended());
        if (res == 0) {
            return th1.name().compareToIgnoreCase(th2.name());
        }
        return res;
    };

    public ThreadReferenceProxyImpl(VirtualMachineProxyImpl virtualMachineProxy, ThreadReference threadReference) {
        super(virtualMachineProxy, threadReference);
    }

    @Override
    public ThreadReference getThreadReference() {
        return (ThreadReference) getObjectReference();
    }

    @Override
    public VirtualMachineProxyImpl getVirtualMachine() {
        return (VirtualMachineProxyImpl) myTimer;
    }

    public String name() {
        checkValid();
        if (myName == null) {
            try {
                myName = getThreadReference().name();
            } catch (ObjectCollectedException ignored) {
                myName = "";
            } catch (IllegalThreadStateException ignored) {
                myName = "zombie";
            }
        }
        return myName;
    }

    public int getSuspendCount() {
        //LOG.assertTrue((mySuspendCount > 0) == suspends());
        try {
            return getThreadReference().suspendCount();
        } catch (ObjectCollectedException ignored) {
            return 0;
        }
    }

    public void suspend() {
        try {
            getThreadReference().suspend();
        } catch (ObjectCollectedException ignored) {
        }
        clearCaches();
    }

    public void resume() {
        //JDI clears all caches on thread resume !!
        final ThreadReference threadRef = getThreadReference();
        if (LOG.isDebugEnabled()) {
            LOG.debug("before resume" + threadRef);
        }
        getVirtualMachineProxy().clearCaches();
        try {
            threadRef.resume();
        } catch (ObjectCollectedException ignored) {
        }
    }

    @Override
    protected void clearCaches() {
        myName = null;
        myFrames = null;
        myFrameCount = -1;
        super.clearCaches();
    }

    public int status() {
        try {
            return getThreadReference().status();
        } catch (IllegalThreadStateException | ObjectCollectedException e) {
            return ThreadReference.THREAD_STATUS_ZOMBIE;
        }
    }

    public ThreadGroupReferenceProxyImpl threadGroupProxy() {
        checkValid();
        if (myThreadGroupProxy == null) {
            ThreadGroupReference threadGroupRef;
            try {
                threadGroupRef = getThreadReference().threadGroup();
            } catch (ObjectCollectedException ignored) {
                threadGroupRef = null;
            }
            myThreadGroupProxy = getVirtualMachineProxy().getThreadGroupReferenceProxy(threadGroupRef);
        }
        return myThreadGroupProxy;
    }

    @Override
    public int frameCount() throws JdiProxyException {
        checkValid();
        if (myFrameCount == -1) {
            final ThreadReference threadReference = getThreadReference();
            try {
                myFrameCount = threadReference.frameCount();
            } catch (ObjectCollectedException ignored) {
                myFrameCount = 0;
            } catch (IncompatibleThreadStateException e) {
                final boolean isSuspended;
                try {
                    isSuspended = threadReference.isSuspended();
                } catch (Throwable ignored) {
                    // unable to determine whether the thread is actually suspended, so propagating original exception
                    throw new JdiProxyException(e.getMessage(), e);
                }
                if (!isSuspended) {
                    // give up because it seems to be really resumed
                    throw new JdiProxyException(e.getMessage(), e);
                } else {
                    // JDI bug: although isSuspended() == true, frameCount() may throw IncompatibleThreadStateException
                    // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4783403
                    // unfortunately, impossible to get this information at the moment, so assume frame count is null
                    myFrameCount = 0;
                }
            } catch (InternalException e) {
                LOG.info(e.getMessage());
                myFrameCount = 0;
            } catch (Exception e) {
                if (!getVirtualMachine().canBeModified()) { // do not care in read only vms
                    LOG.debug(e.getMessage());
                    myFrameCount = 0;
                } else {
                    throw e;
                }
            }
        }
        return myFrameCount;
    }

    /**
     * Same as frames(), but always force full frames refresh if not cached,
     * this is useful when you need all frames but do not plan to invoke anything
     * as only one request is sent.
     */
    public List<StackFrameProxyImpl> forceFrames() throws JdiProxyException {
        final ThreadReference threadRef = getThreadReference();
        try {
            //LOG.assertTrue(threadRef.isSuspended());
            checkValid();
            if (myFrames == null) {
                try {
                    List<StackFrame> frames = threadRef.frames();
                    myFrameCount = frames.size();
                    myFrames = new ArrayList<>(myFrameCount);
                    myFramesFromBottom.clear();
                    int idx = 0;
                    for (StackFrame frame : frames) {
                        StackFrameProxyImpl frameProxy = new StackFrameProxyImpl(this, frame, myFrameCount - idx);
                        myFrames.add(frameProxy);
                        myFramesFromBottom.addFirst(frameProxy);
                        idx++;
                    }
                } catch (IncompatibleThreadStateException | InternalException e) {
                    throw new JdiProxyException(e.getMessage(), e);
                }
            }
        } catch (ObjectCollectedException ignored) {
            return Collections.emptyList();
        }
        return myFrames;
    }

    public List<StackFrameProxyImpl> frames() throws JdiProxyException {
        final ThreadReference threadRef = getThreadReference();
        try {
            //LOG.assertTrue(threadRef.isSuspended());
            checkValid();
            if (myFrames == null) {
                checkFrames(threadRef);
                myFrames = new ArrayList<>(myFramesFromBottom.subList(0, frameCount()));
                Collections.reverse(myFrames);
            }
        } catch (ObjectCollectedException ignored) {
            return Collections.emptyList();
        }
        return myFrames;
    }

    private void checkFrames(final ThreadReference threadRef) throws JdiProxyException {
        int frameCount = frameCount();
        if (myFramesFromBottom.size() < frameCount) {
            List<StackFrame> frames;
            try {
                frames = threadRef.frames(0, frameCount - myFramesFromBottom.size());
            } catch (IncompatibleThreadStateException | InternalException e) {
                throw new JdiProxyException(e.getMessage(), e);
            }

            int index = myFramesFromBottom.size() + 1;
            for (ListIterator<StackFrame> iterator = frames.listIterator(frameCount - myFramesFromBottom.size());
                 iterator.hasPrevious(); ) {
                myFramesFromBottom.add(new StackFrameProxyImpl(this, iterator.previous(), index));
                index++;
            }
        } else { // avoid leaking frames
            while (myFramesFromBottom.size() > frameCount) {
                myFramesFromBottom.removeLast();
            }
        }
    }

    @Override
    public StackFrameProxyImpl frame(int i) throws JdiProxyException {
        final ThreadReference threadReference = getThreadReference();
        try {
            if (!threadReference.isSuspended()) {
                return null;
            }
            checkFrames(threadReference);
            final int frameCount = frameCount();
            if (frameCount == 0) {
                return null;
            }
            return myFramesFromBottom.get(frameCount - i - 1);
        } catch (ObjectCollectedException | IllegalThreadStateException ignored) {
            return null;
        }
    }

    public void popFrames(StackFrameProxyImpl stackFrame) throws JdiProxyException {
        try {
            getThreadReference().popFrames(stackFrame.getStackFrame());
        } catch (InvalidStackFrameException | ObjectCollectedException ignored) {
        } catch (InternalException | IncompatibleThreadStateException e) {
            throw new JdiProxyException(e.getMessage(), e.getCause());
        } finally {
            clearCaches();
            getVirtualMachineProxy().clearCaches();
        }
    }

    public void forceEarlyReturn(Value value) throws ClassNotLoadedException, IncompatibleThreadStateException,
            InvalidTypeException {
        try {
            getThreadReference().forceEarlyReturn(value);
        } finally {
            clearCaches();
            getVirtualMachineProxy().clearCaches();
        }
    }

    public void stop(ObjectReference exception) throws InvalidTypeException {
        try {
            getThreadReference().stop(exception);
        } finally {
            clearCaches();
            getVirtualMachineProxy().clearCaches();
        }
    }

    public boolean isSuspended() throws ObjectCollectedException {
        try {
            return getThreadReference().isSuspended();
        } catch (IllegalThreadStateException e) {
            // must be zombie thread
            LOG.info(String.valueOf(e));
        } catch (ObjectCollectedException ignored) {
        }

        return false;
    }

    public boolean isAtBreakpoint() {
        try {
            return getThreadReference().isAtBreakpoint();
        } catch (InternalException e) {
            LOG.info(String.valueOf(e));
        } catch (ObjectCollectedException ignored) {
        }
        return false;
    }

    public boolean isResumeOnHotSwap() {
        if (myResumeOnHotSwap == ThreeState.UNSURE) {
            myResumeOnHotSwap = ThreeState.fromBoolean(name().startsWith("YJPAgent-"));
        }
        return myResumeOnHotSwap.toBoolean();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
