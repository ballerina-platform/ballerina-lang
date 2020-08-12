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

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InternalException;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VoidValue;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.request.EventRequestManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Proxy implementation for JDI virtual machine.
 *
 * @since 2.0.0
 */
public class VirtualMachineProxyImpl implements JdiTimer, VirtualMachineProxy {
    private static final Logger LOG = LoggerFactory.getLogger(VirtualMachineProxyImpl.class);
    private final VirtualMachine myVirtualMachine;
    private int myTimeStamp = 0;
    private int myPausePressedCount = 0;

    // cached data
    private final Map<ObjectReference, ObjectReferenceProxyImpl> myObjectReferenceProxies = new HashMap<>();
    private final Map<String, StringReference> myStringLiteralCache = new HashMap<>();

    private final Map<ThreadReference, ThreadReferenceProxyImpl> myAllThreads = new ConcurrentHashMap<>();
    private final Map<ThreadGroupReference, ThreadGroupReferenceProxyImpl> myThreadGroups = new HashMap<>();
    private boolean myAllThreadsDirty = true;
    private List<ReferenceType> myAllClasses;
    private Map<ReferenceType, List<ReferenceType>> myNestedClassesCache = new HashMap<>();

    public final Throwable mySuspendLogger = new Throwable();
    private final boolean myVersionHigher15;
    private final boolean myVersionHigher14;

    public VirtualMachineProxyImpl(VirtualMachine virtualMachine) {

        myVirtualMachine = virtualMachine;
        // All versions of Dalvik/ART support at least the JDWP spec as of 1.6.
        myVersionHigher15 = versionHigher("1.5");
        myVersionHigher14 = myVersionHigher15 || versionHigher("1.4");
        // avoid lazy-init for some properties: the following will pre-calculate values
        canRedefineClasses();
        canWatchFieldModification();
        canPopFrames();

        if (canBeModified()) { // no need to spend time here for read only sessions
            try {
                // this will cache classes inside JDI and enable faster search of classes later
                virtualMachine.allClasses();
            } catch (VMDisconnectedException e) {
                throw e;
            } catch (Throwable e) {
                // catch all exceptions in order not to break vm attach process
                // Example:
                // java.lang.IllegalArgumentException: Invalid JNI signature character ';'
                //  caused by some bytecode "optimizers" which break type signatures as a side effect.
                //  solution if you are using JAX-WS: add -Dcom.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize=true to
                //  JVM args
                LOG.info(String.valueOf(e));
            }
        }
        virtualMachine.topLevelThreadGroups().forEach(this::threadGroupCreated);
    }

    public VirtualMachine getVirtualMachine() {
        return myVirtualMachine;
    }

    public ClassesByNameProvider getClassesByNameProvider() {
        return this::classesByName;
    }

    @Override
    public List<ReferenceType> classesByName(String s) {
        return myVirtualMachine.classesByName(s);
    }

    @Override
    public List<ReferenceType> allClasses() {
        List<ReferenceType> allClasses = myAllClasses;
        if (allClasses == null) {
            myAllClasses = allClasses = myVirtualMachine.allClasses();
        }
        return allClasses;
    }

    public String toString() {
        return myVirtualMachine.toString();
    }

    public void redefineClasses(Map<ReferenceType, byte[]> map) {
        try {
            myVirtualMachine.redefineClasses(map);
        } finally {
            clearCaches();
        }
    }

    /**
     * @return a list of all ThreadReferenceProxies
     */
    public Collection<ThreadReferenceProxyImpl> allThreads() {
        if (myAllThreadsDirty) {
            myAllThreadsDirty = false;

            for (ThreadReference threadReference : myVirtualMachine.allThreads()) {
                getThreadReferenceProxy(threadReference); // add a proxy
            }
        }
        return new ArrayList<>(myAllThreads.values());
    }

    public void threadStarted(ThreadReference thread) {
        getThreadReferenceProxy(thread); // add a proxy
    }

    public void threadStopped(ThreadReference thread) {
        myAllThreads.remove(thread);
    }

    public void suspend() {
        if (!canBeModified()) {
            return;
        }
        myPausePressedCount++;
        myVirtualMachine.suspend();
        clearCaches();
    }

    public void resume() {
        if (!canBeModified()) {
            return;
        }
        if (myPausePressedCount > 0) {
            myPausePressedCount--;
        }
        clearCaches();
        LOG.debug("before resume VM");
        try {
            myVirtualMachine.resume();
        } catch (InternalException e) {
            // ok to ignore. Although documentation says it is safe to invoke resume() on running VM,
            // sometimes this leads to com.sun.jdi.InternalException: Unexpected JDWP Error: 13 (THREAD_NOT_SUSPENDED)
            LOG.info(String.valueOf(e));
        }
        LOG.debug("VM resumed");
        //logThreads();
    }

    /**
     * @return a list of threadGroupProxies
     */
    public List<ThreadGroupReferenceProxyImpl> topLevelThreadGroups() {
        return getVirtualMachine().topLevelThreadGroups().stream().map(this::getThreadGroupReferenceProxy).collect(
                Collectors.toList());
    }

    public void threadGroupCreated(ThreadGroupReference threadGroupReference) {
        if (!isJ2ME()) {
            ThreadGroupReferenceProxyImpl proxy = new ThreadGroupReferenceProxyImpl(this, threadGroupReference);
            myThreadGroups.put(threadGroupReference, proxy);
        }
    }

    public boolean isJ2ME() {
        return isJ2ME(getVirtualMachine());
    }

    private static boolean isJ2ME(VirtualMachine virtualMachine) {
        return virtualMachine.version().startsWith("1.0");
    }

    public void threadGroupRemoved(ThreadGroupReference threadGroupReference) {
        myThreadGroups.remove(threadGroupReference);
    }

    public EventQueue eventQueue() {
        return myVirtualMachine.eventQueue();
    }

    public EventRequestManager eventRequestManager() {
        return myVirtualMachine.eventRequestManager();
    }

    /**
     * @deprecated use {@link #mirrorOfVoid()} instead
     */
    @Deprecated
    public VoidValue mirrorOf() throws JdiProxyException {
        return mirrorOfVoid();
    }

    public VoidValue mirrorOfVoid() {
        return myVirtualMachine.mirrorOfVoid();
    }

    public BooleanValue mirrorOf(boolean b) {
        return myVirtualMachine.mirrorOf(b);
    }

    public ByteValue mirrorOf(byte b) {
        return myVirtualMachine.mirrorOf(b);
    }

    public CharValue mirrorOf(char c) {
        return myVirtualMachine.mirrorOf(c);
    }

    public ShortValue mirrorOf(short i) {
        return myVirtualMachine.mirrorOf(i);
    }

    public IntegerValue mirrorOf(int i) {
        return myVirtualMachine.mirrorOf(i);
    }

    public LongValue mirrorOf(long l) {
        return myVirtualMachine.mirrorOf(l);
    }

    public FloatValue mirrorOf(float v) {
        return myVirtualMachine.mirrorOf(v);
    }

    public DoubleValue mirrorOf(double v) {
        return myVirtualMachine.mirrorOf(v);
    }

    public StringReference mirrorOf(String s) {
        return myVirtualMachine.mirrorOf(s);
    }

    public Process process() {
        return myVirtualMachine.process();
    }

    public void dispose() {
        try {
            myVirtualMachine.dispose();
        } catch (UnsupportedOperationException e) {
            LOG.info(String.valueOf(e));
        } finally {
            // Todo - Restore
            // Memory leak workaround, see IDEA-163334
//            Object target = getField(myVirtualMachine.getClass(), myVirtualMachine, null, "target");
//            if (target != null) {
//                Thread controller = getField(target.getClass(), target, Thread.class, "eventController");
//                if (controller != null) {
//                    controller.stop();
//                }
//            }
        }
    }

    public void exit(int i) {
        myVirtualMachine.exit(i);
    }

    private final Capability myWatchFielsModification = new Capability() {
        @Override
        protected boolean calcValue() {
            return myVirtualMachine.canWatchFieldModification();
        }
    };

    @Override
    public boolean canWatchFieldModification() {
        return myWatchFielsModification.isAvailable();
    }

    private final Capability myWatchFieldAccess = new Capability() {
        @Override
        protected boolean calcValue() {
            return myVirtualMachine.canWatchFieldAccess();
        }
    };

    @Override
    public boolean canWatchFieldAccess() {
        return myWatchFieldAccess.isAvailable();
    }

    private final Capability myIsJ2ME = new Capability() {
        @Override
        protected boolean calcValue() {
            return isJ2ME();
        }
    };

    @Override
    public boolean canInvokeMethods() {
        return !myIsJ2ME.isAvailable();
    }

    private final Capability myGetBytecodes = new Capability() {
        @Override
        protected boolean calcValue() {
            return myVirtualMachine.canGetBytecodes();
        }
    };

    @Override
    public boolean canGetBytecodes() {
        return myGetBytecodes.isAvailable();
    }

    private final Capability myGetConstantPool = new Capability() {
        @Override
        protected boolean calcValue() {
            return myVirtualMachine.canGetConstantPool();
        }
    };

    public boolean canGetConstantPool() {
        return myGetConstantPool.isAvailable();
    }

    private final Capability myGetSyntheticAttribute = new Capability() {
        @Override
        protected boolean calcValue() {
            return myVirtualMachine.canGetSyntheticAttribute();
        }
    };

    public boolean canGetSyntheticAttribute() {
        return myGetSyntheticAttribute.isAvailable();
    }

    private final Capability myGetOwnedMonitorInfo = new Capability() {
        @Override
        protected boolean calcValue() {
            return myVirtualMachine.canGetOwnedMonitorInfo();
        }
    };

    public boolean canGetOwnedMonitorInfo() {
        return myGetOwnedMonitorInfo.isAvailable();
    }

    private final Capability myGetMonitorFrameInfo = new Capability() {
        @Override
        protected boolean calcValue() {
            return myVirtualMachine.canGetMonitorFrameInfo();
        }
    };

    public boolean canGetMonitorFrameInfo() {
        return myGetMonitorFrameInfo.isAvailable();
    }

    private final Capability myGetCurrentContendedMonitor = new Capability() {
        @Override
        protected boolean calcValue() {
            return myVirtualMachine.canGetCurrentContendedMonitor();
        }
    };

    public boolean canGetCurrentContendedMonitor() {
        return myGetCurrentContendedMonitor.isAvailable();
    }

    private final Capability myGetMonitorInfo = new Capability() {
        @Override
        protected boolean calcValue() {
            return myVirtualMachine.canGetMonitorInfo();
        }
    };

    public boolean canGetMonitorInfo() {
        return myGetMonitorInfo.isAvailable();
    }

    private final Capability myUseInstanceFilters = new Capability() {
        @Override
        protected boolean calcValue() {
            return myVersionHigher14 && myVirtualMachine.canUseInstanceFilters();
        }
    };

    public boolean canUseInstanceFilters() {
        return myUseInstanceFilters.isAvailable();
    }

    private final Capability myRedefineClasses = new Capability() {
        @Override
        protected boolean calcValue() {
            return myVersionHigher14 && myVirtualMachine.canRedefineClasses();
        }
    };

    public boolean canRedefineClasses() {
        return myRedefineClasses.isAvailable();
    }

    private final Capability myAddMethod = new Capability() {
        @Override
        protected boolean calcValue() {
            return myVersionHigher14 && myVirtualMachine.canAddMethod();
        }
    };

    public boolean canAddMethod() {
        return myAddMethod.isAvailable();
    }

    private final Capability myUnrestrictedlyRedefineClasses = new Capability() {
        @Override
        protected boolean calcValue() {
            return myVersionHigher14 && myVirtualMachine.canUnrestrictedlyRedefineClasses();
        }
    };

    public boolean canUnrestrictedlyRedefineClasses() {
        return myUnrestrictedlyRedefineClasses.isAvailable();
    }

    private final Capability myPopFrames = new Capability() {
        @Override
        protected boolean calcValue() {
            return myVersionHigher14 && myVirtualMachine.canPopFrames();
        }
    };

    public boolean canPopFrames() {
        return myPopFrames.isAvailable();
    }

    private final Capability myForceEarlyReturn = new Capability() {
        @Override
        protected boolean calcValue() {
            return myVirtualMachine.canForceEarlyReturn();
        }
    };

    public boolean canForceEarlyReturn() {
        return myForceEarlyReturn.isAvailable();
    }

    private final Capability myCanGetInstanceInfo = new Capability() {
        @Override
        protected boolean calcValue() {
            if (!myVersionHigher15) {
                return false;
            }
            try {
                final Method method = VirtualMachine.class.getMethod("canGetInstanceInfo");
                return (Boolean) method.invoke(myVirtualMachine);
            } catch (NoSuchMethodException ignored) {
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOG.error(String.valueOf(e));
            }
            return false;
        }
    };

    public boolean canGetInstanceInfo() {
        return myCanGetInstanceInfo.isAvailable();
    }

    public boolean canBeModified() {
        return myVirtualMachine.canBeModified();
    }

    @Override
    public final boolean versionHigher(String version) {
        return myVirtualMachine.version().compareTo(version) >= 0;
    }

    private final Capability myGetSourceDebugExtension = new Capability() {
        @Override
        protected boolean calcValue() {
            return myVersionHigher14 && myVirtualMachine.canGetSourceDebugExtension();
        }
    };

    public boolean canGetSourceDebugExtension() {
        return myGetSourceDebugExtension.isAvailable();
    }

    private final Capability myRequestVMDeathEvent = new Capability() {
        @Override
        protected boolean calcValue() {
            return myVersionHigher14 && myVirtualMachine.canRequestVMDeathEvent();
        }
    };

    public boolean canRequestVMDeathEvent() {
        return myRequestVMDeathEvent.isAvailable();
    }

    private final Capability myGetMethodReturnValues = new Capability() {
        @Override
        protected boolean calcValue() {
            if (myVersionHigher15) {
                //return myVirtualMachine.canGetMethodReturnValues();
                try {
                    final Method method = VirtualMachine.class.getDeclaredMethod("canGetMethodReturnValues");
                    final Boolean rv = (Boolean) method.invoke(myVirtualMachine);
                    return rv.booleanValue();
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
                }
            }
            return false;
        }
    };

    public boolean canGetMethodReturnValues() {
        return myGetMethodReturnValues.isAvailable();
    }

    public String getDefaultStratum() {
        return myVersionHigher14 ? myVirtualMachine.getDefaultStratum() : null;
    }

    public String description() {
        return myVirtualMachine.description();
    }

    public String version() {
        return myVirtualMachine.version();
    }

    public String name() {
        return myVirtualMachine.name();
    }

    public void setDebugTraceMode(int i) {
        myVirtualMachine.setDebugTraceMode(i);
    }

    public ThreadReferenceProxyImpl getThreadReferenceProxy(ThreadReference thread) {
        if (thread == null) {
            return null;
        }
        return myAllThreads.computeIfAbsent(thread, t -> new ThreadReferenceProxyImpl(this, t));
    }

    public ThreadGroupReferenceProxyImpl getThreadGroupReferenceProxy(ThreadGroupReference group) {
        if (group == null) {
            return null;
        }
        ThreadGroupReferenceProxyImpl proxy = myThreadGroups.get(group);
        if (proxy == null) {
            if (!myIsJ2ME.isAvailable()) {
                proxy = new ThreadGroupReferenceProxyImpl(this, group);
                myThreadGroups.put(group, proxy);
            }
        }
        return proxy;
    }

    public ObjectReferenceProxyImpl getObjectReferenceProxy(ObjectReference objectReference) {
        if (objectReference != null) {
            if (objectReference instanceof ThreadReference) {
                return getThreadReferenceProxy((ThreadReference) objectReference);
            } else if (objectReference instanceof ThreadGroupReference) {
                return getThreadGroupReferenceProxy((ThreadGroupReference) objectReference);
            } else {
                ObjectReferenceProxyImpl proxy = myObjectReferenceProxies.get(objectReference);
                if (proxy == null) {
                    if (objectReference instanceof StringReference) {
                        proxy = new StringReferenceProxyImpl(this, (StringReference) objectReference);
                    } else {
                        proxy = new ObjectReferenceProxyImpl(this, objectReference);
                    }
                    myObjectReferenceProxies.put(objectReference, proxy);
                }
                return proxy;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VirtualMachineProxyImpl)) {
            return false;
        }
        return myVirtualMachine.equals(((VirtualMachineProxyImpl) obj).getVirtualMachine());
    }

    public int hashCode() {
        return myVirtualMachine.hashCode();
    }

    public void clearCaches() {
        LOG.debug("VM cleared");
        myAllClasses = null;
        if (!myNestedClassesCache.isEmpty()) {
            myNestedClassesCache = new HashMap<>(myNestedClassesCache.size());
        }
        //myAllThreadsDirty = true;
        myTimeStamp++;
    }

    @Override
    public int getCurrentTime() {
        return myTimeStamp;
    }

    public static boolean isCollected(ObjectReference reference) {
        try {
            return !isJ2ME(reference.virtualMachine()) && reference.isCollected();
        } catch (UnsupportedOperationException e) {
            LOG.info(String.valueOf(e));
        }
        return false;
    }

    public boolean isPausePressed() {
        return myPausePressedCount > 0;
    }

    public boolean isSuspended() {
        return allThreads().stream().anyMatch(thread -> thread.getSuspendCount() != 0);
    }

    public void logThreads() {
        if (LOG.isDebugEnabled()) {
            for (ThreadReferenceProxyImpl thread : allThreads()) {
                if (!thread.isCollected()) {
                    LOG.debug("suspends " + thread + " " + thread.getSuspendCount() + " " + thread.isSuspended());
                }
            }
        }
    }

    private abstract static class Capability {
        private ThreeState myValue = ThreeState.UNSURE;

        public final boolean isAvailable() {
            if (myValue == ThreeState.UNSURE) {
                try {
                    myValue = ThreeState.fromBoolean(calcValue());
                } catch (VMDisconnectedException e) {
                    LOG.info(String.valueOf(e));
                    myValue = ThreeState.NO;
                }
            }
            return myValue.toBoolean();
        }

        protected abstract boolean calcValue();
    }

    static final class JNITypeParserReflect {
        static Method typeNameToSignatureMethod;

        static {
            Method method = null;
            try {
                method = getDeclaredMethod(Class.forName("com.sun.tools.jdi.JNITypeParser"), "typeNameToSignature",
                        String.class);
            } catch (ClassNotFoundException e) {
                LOG.warn(String.valueOf(e));
            }
            typeNameToSignatureMethod = method;
            if (typeNameToSignatureMethod == null) {
                LOG.warn("Unable to find JNITypeParser.typeNameToSignature method");
            }
        }

        static String typeNameToSignature(String name) {
            if (typeNameToSignatureMethod != null) {
                try {
                    return (String) typeNameToSignatureMethod.invoke(null, name);
                } catch (Exception ignored) {
                }
            }
            return null;
        }

        private static Method getDeclaredMethod(Class<?> aClass, String name, Class... parameters) {
            try {
                Method declaredMethod = aClass.getDeclaredMethod(name, parameters);
                declaredMethod.setAccessible(true);
                return declaredMethod;
            } catch (NoSuchMethodException e) {
                return null;
            }
        }
    }
}
