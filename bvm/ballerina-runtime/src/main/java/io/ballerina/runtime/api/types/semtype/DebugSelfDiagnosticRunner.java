package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.internal.types.semtype.FunctionAtomicType;
import io.ballerina.runtime.internal.types.semtype.ListAtomicType;
import io.ballerina.runtime.internal.types.semtype.MappingAtomicType;
import io.ballerina.runtime.internal.types.semtype.MutableSemType;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class DebugSelfDiagnosticRunner implements TypeCheckSelfDiagnosticsRunner {

    private static final int MAX_TIMEOUT = 1000;
    private final String LOG_FILE_PATH;
    private final Queue<TypeResolutionData> pendingTypeResolutions = new ArrayDeque<>();
    private final Queue<TypeCheckData> pendingTypeChecks = new ArrayDeque<>();
    private final Map<Thread, String> uncaughtExceptions = new ConcurrentHashMap<>();
    private final Env env;
    private final HouseKeeper houseKeeper;

    DebugSelfDiagnosticRunner(Env env) {
        LOG_FILE_PATH = Objects.requireNonNullElse(System.getenv("BAL_TYPE_CHECK_DIAGNOSTIC_PATH"),
                "/tmp/type_check_diagnostics.log");
        try {
            Files.write(Paths.get(LOG_FILE_PATH), "Type Check Diagnostics \n=============================\n".getBytes(),
                    StandardOpenOption.APPEND);
        } catch (IOException err) {
            throw new RuntimeException("Error occurred while creating the log file for type check diagnostics", err);
        }
        this.env = env;
        this.houseKeeper = new HouseKeeper();
        Thread houseKeeperThread = new Thread(houseKeeper);
        houseKeeperThread.setDaemon(true);
        houseKeeperThread.start();
    }

    @Override
    public void registerTypeResolutionStart(Context cx, MutableSemType type) {
        synchronized (pendingTypeResolutions) {
            pendingTypeResolutions.add(new TypeResolutionData(cx, type));
        }
        Thread t = Thread.currentThread();
        t.setUncaughtExceptionHandler((thread, throwable) -> uncaughtExceptions.put(thread, throwable.getMessage()));
    }

    // This so validation will not block the main thread
    private void validateRecAtomState() {
        List<MappingAtomicType> recMappingAtomsCopy = env.getRecMappingAtomsCopy();
        List<FunctionAtomicType> recFunctionAtomsCopy = env.getRecFunctionAtomsCopy();
        List<ListAtomicType> recListAtomsCopy = env.getRecListAtomsCopy();
        Thread validateThread = new Thread(() -> {
            LogBuilder logBuilder = new LogBuilder(this.houseKeeper);
            for (int i = 0; i < recMappingAtomsCopy.size(); i++) {
                if (recMappingAtomsCopy.get(i) == null) {
                    logBuilder.append("Rec mapping atoms contain null values at ").append(i).append("\n");
                }
            }
            for (int i = 0; i < recFunctionAtomsCopy.size(); i++) {
                if (recFunctionAtomsCopy.get(i) == null) {
                    logBuilder.append("Rec function atoms contain null values at ").append(i).append("\n");
                }
            }
            for (int i = 0; i < recListAtomsCopy.size(); i++) {
                if (recListAtomsCopy.get(i) == null) {
                    logBuilder.append("Rec list atoms contain null values at ").append(i).append("\n");
                }
            }
            logBuilder.flush();
        });
        validateThread.setDaemon(true);
        validateThread.start();
    }

    @Override
    public void registerTypeCheckStart(Context cx, SemType t1, SemType t2) {
        synchronized (pendingTypeResolutions) {
            pendingTypeResolutions.removeIf(data -> data.cx == cx);
        }
        synchronized (pendingTypeChecks) {
            pendingTypeChecks.add(new TypeCheckData(cx, t1, t2));
        }
        validateRecAtomState();
        Thread t = Thread.currentThread();
        t.setUncaughtExceptionHandler((thread, throwable) -> uncaughtExceptions.put(thread, throwable.getMessage()));
    }

    @Override
    public void registerTypeCheckEnd(Context cx) {
        synchronized (pendingTypeChecks) {
            pendingTypeChecks.removeIf(data -> data.cx == cx);
        }
    }

    @Override
    public void registerAbruptTypeResolutionEnd(Context cx, Exception ex) {
        LogBuilder logBuilder = new LogBuilder(cx, this.houseKeeper);
        logBuilder.append("Abrupt type resolution end:\n");
        logBuilder.append("Exception: ").append(ex.getMessage()).append("\n");
        logBuilder.append(ex.getStackTrace());
        synchronized (pendingTypeChecks) {
            logBuilder.append(pendingTypeChecksToString(pendingTypeChecks));
        }
        synchronized (pendingTypeResolutions) {
            logBuilder.append(pendingTypeResolutionsToString(pendingTypeResolutions));
        }
        logBuilder.flush();
    }

    @Override
    public void registerAbruptTypeCheckEnd(Context cx, Exception ex) {
        LogBuilder logBuilder = new LogBuilder(cx, houseKeeper);
        logBuilder.append("Abrupt type check end:\n");
        logBuilder.append("Exception: ").append(ex.getMessage()).append("\n");
        logBuilder.append(ex.getStackTrace());
        synchronized (pendingTypeChecks) {
            logBuilder.append(pendingTypeChecksToString(pendingTypeChecks));
        }
        synchronized (pendingTypeResolutions) {
            logBuilder.append(pendingTypeResolutionsToString(pendingTypeResolutions));
        }
        logBuilder.flush();
    }

    @Override
    public void registerTypeResolutionExit(Context cx) {
        synchronized (pendingTypeResolutions) {
            pendingTypeResolutions.removeIf(data -> data.cx == cx);
        }
    }

    private static String withIdentity(Object o) {
        return o + "[" + System.identityHashCode(o) + "]";
    }

    private static String pendingTypeChecksToString(Collection<TypeCheckData> typeChecks) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Pending type checks:\n");
        typeChecks.forEach(data -> {
            logBuilder.append(data.cx).append(" - ").append(withIdentity(data.t1)).append(" - ")
                    .append(withIdentity(data.t2)).append("\n");
            logBuilder.append("Thread state: ").append(data.t.getState()).append("\n");
            for (StackTraceElement element : data.t.getStackTrace()) {
                logBuilder.append("\tat ").append(element).append("\n");
            }
            logBuilder.append("Entry point\n");
        });
        return logBuilder.toString();
    }

    private static String pendingTypeResolutionsToString(Collection<TypeResolutionData> typeResolutions) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Pending type resolutions:\n");
        typeResolutions.forEach(data -> {
            logBuilder.append(data.cx).append(" - ").append(withIdentity(data.type)).append("\n");
            logBuilder.append("Thread state: ").append(data.t.getState()).append("\n");
            for (StackTraceElement element : data.t.getStackTrace()) {
                logBuilder.append("\tat ").append(element).append("\n");
            }
            logBuilder.append("Entry point\n");
        });
        return logBuilder.toString();
    }

    private record EntryPointData(StackTraceElement[] stackTrace) {

        EntryPointData() {
            this(Thread.currentThread().getStackTrace());
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (StackTraceElement element : stackTrace) {
                builder.append("\tat ").append(element).append("\n");
            }
            return builder.toString();
        }
    }

    private record TypeResolutionData(Context cx, Thread t, MutableSemType type, long startTime,
                                      EntryPointData entryPoint) {

        TypeResolutionData(Context cx, MutableSemType type) {
            this(cx, Thread.currentThread(), type, System.nanoTime(), new EntryPointData());
        }
    }

    private record TypeCheckData(Context cx, Thread t, SemType t1, SemType t2, long startTime,
                                 EntryPointData entryPointData) {

        TypeCheckData(Context cx, SemType t1, SemType t2) {
            this(cx, Thread.currentThread(), t1, t2, System.nanoTime(), new EntryPointData());
        }
    }

    private class HouseKeeper implements Runnable {

        private Queue<String> pendingLogs = new ConcurrentLinkedDeque<>();

        @Override
        public void run() {
            while (true) {
                List<TypeResolutionData> hangedTypeResolutions;
                List<TypeCheckData> hangedTypeChecks;
                synchronized (pendingTypeResolutions) {
                    hangedTypeResolutions = pendingTypeResolutions.stream().filter(data -> {
                        long elapsedTime = System.nanoTime() - data.startTime();
                        return elapsedTime > MAX_TIMEOUT;
                    }).toList();
                }
                synchronized (pendingTypeChecks) {
                    hangedTypeChecks = pendingTypeChecks.stream().filter(data -> {
                        long elapsedTime = System.nanoTime() - data.startTime();
                        return elapsedTime > MAX_TIMEOUT;
                    }).toList();
                }
                StringBuilder stringBuilder = new StringBuilder();
                if (!hangedTypeResolutions.isEmpty()) {
                    stringBuilder.append(pendingTypeResolutionsToString(hangedTypeResolutions));
                }
                if (!hangedTypeChecks.isEmpty()) {
                    stringBuilder.append(pendingTypeChecksToString(hangedTypeChecks));
                }
                uncaughtExceptions.forEach((thread, message) -> {
                    stringBuilder.append("Uncaught exception in thread: ").append(thread).append("\n");
                    stringBuilder.append("Exception message: ").append(message).append("\n");
                    for (StackTraceElement element : thread.getStackTrace()) {
                        stringBuilder.append("\tat ").append(element).append("\n");
                    }
                });
                uncaughtExceptions.clear();
                ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
                long[] ids = tmx.findDeadlockedThreads();
                if (ids != null) {
                    ThreadInfo[] infos = tmx.getThreadInfo(ids, true, true);
                    stringBuilder.append("The following threads are deadlocked:");
                    for (ThreadInfo ti : infos) {
                        stringBuilder.append(ti);
                    }
                }

                while (!pendingLogs.isEmpty()) {
                    stringBuilder.append(pendingLogs.poll());
                }
                stringBuilder.append("\n\n");
                try {
                    Files.write(Paths.get(LOG_FILE_PATH), stringBuilder.toString().getBytes(),
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } catch (IOException ignored) {
                }
                try {
                    Thread.sleep(5000); // Sleep for 5 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        void addLog(String log) {
            pendingLogs.add(log);
        }
    }

    private class LogBuilder {

        private final StringBuilder stringBuilder = new StringBuilder();
        private final HouseKeeper houseKeeper;
        private final Context cx;

        LogBuilder(Context cx, HouseKeeper houseKeeper) {
            this.cx = cx;
            stringBuilder.append("[").append(System.identityHashCode(cx)).append("] ");
            this.houseKeeper = houseKeeper;
        }

        LogBuilder(HouseKeeper houseKeeper) {
            this.cx = null;
            stringBuilder.append("[null] ");
            this.houseKeeper = houseKeeper;
        }

        public LogBuilder append(String str) {
            stringBuilder.append(str);
            return this;
        }

        public LogBuilder append(Object obj) {
            stringBuilder.append(obj);
            return this;
        }

        public LogBuilder append(StackTraceElement[] stackTrace) {
            for (StackTraceElement element : stackTrace) {
                stringBuilder.append("\tat ").append(element).append("\n");
            }
            return this;
        }

        public void flush() {
            stringBuilder.append("\n\n");
            houseKeeper.addLog(stringBuilder.toString());
        }

        public String toString() {
            return stringBuilder.toString();
        }
    }
}
