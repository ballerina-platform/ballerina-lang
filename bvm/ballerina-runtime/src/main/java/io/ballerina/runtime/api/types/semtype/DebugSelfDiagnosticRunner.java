package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.internal.types.semtype.MutableSemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class DebugSelfDiagnosticRunner implements TypeCheckSelfDiagnosticsRunner {

    private static final int MAX_TIMEOUT = 1000;
    private static final String LOG_FILE_PATH = "/tmp/type_check_diagnostics.log";
    private static final Logger log = LoggerFactory.getLogger(DebugSelfDiagnosticRunner.class);
    private final Queue<TypeResolutionData> pendingTypeResolutions = new ArrayDeque<>();
    private final Queue<TypeCheckData> pendingTypeChecks = new ArrayDeque<>();
    private final Map<Thread, String> uncaughtExceptions = new ConcurrentHashMap<>();
    private final Env env;

    DebugSelfDiagnosticRunner(Env env) {
        try {
            Files.write(Paths.get(LOG_FILE_PATH), "Type Check Diagnostics \n\n".getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
        this.env = env;
        Thread houseKeeperThread = new Thread(new HouseKeeper());
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

    @Override
    public void registerTypeCheckStart(Context cx, SemType t1, SemType t2) {
        synchronized (pendingTypeResolutions) {
            pendingTypeResolutions.removeIf(data -> data.cx == cx);
        }
        synchronized (pendingTypeChecks) {
            pendingTypeChecks.add(new TypeCheckData(cx, t1, t2));
        }
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
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Abrupt type resolution end:\n");
        logBuilder.append("Context: ").append(cx).append("\n");
        logBuilder.append("Exception: ").append(ex.getMessage()).append("\n");
        for (StackTraceElement element : ex.getStackTrace()) {
            logBuilder.append("\tat ").append(element).append("\n");
        }
        synchronized (pendingTypeChecks) {
            logBuilder.append(pendingTypeChecksToString(pendingTypeChecks));
        }
        synchronized (pendingTypeResolutions) {
            logBuilder.append(pendingTypeResolutionsToString(pendingTypeResolutions));
        }
        try {
            Files.write(Paths.get(LOG_FILE_PATH), logBuilder.toString().getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void registerAbruptTypeCheckEnd(Context cx, Exception ex) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Abrupt type check end:\n");
        logBuilder.append("Context: ").append(cx).append("\n");
        logBuilder.append("Exception: ").append(ex.getMessage()).append("\n");
        for (StackTraceElement element : ex.getStackTrace()) {
            logBuilder.append("\tat ").append(element).append("\n");
        }
        synchronized (pendingTypeChecks) {
            logBuilder.append(pendingTypeChecksToString(pendingTypeChecks));
        }
        synchronized (pendingTypeResolutions) {
            logBuilder.append(pendingTypeResolutionsToString(pendingTypeResolutions));
        }
        try {
            Files.write(Paths.get(LOG_FILE_PATH), logBuilder.toString().getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException ignored) {
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
        });
        return logBuilder.toString();
    }

    private record TypeResolutionData(Context cx, Thread t, MutableSemType type, long startTime) {

        TypeResolutionData(Context cx, MutableSemType type) {
            this(cx, Thread.currentThread(), type, System.nanoTime());
        }
    }

    private record TypeCheckData(Context cx, Thread t, SemType t1, SemType t2, long startTime) {

        TypeCheckData(Context cx, SemType t1, SemType t2) {
            this(cx, Thread.currentThread(), t1, t2, System.nanoTime());
        }
    }

    private class HouseKeeper implements Runnable {

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
                StringBuilder logBuilder = new StringBuilder();
                if (!hangedTypeResolutions.isEmpty()) {
                    logBuilder.append(pendingTypeResolutionsToString(hangedTypeResolutions));
                }
                if (!hangedTypeChecks.isEmpty()) {
                    logBuilder.append(pendingTypeChecksToString(hangedTypeChecks));
                }
                uncaughtExceptions.forEach((thread, message) -> {
                    logBuilder.append("Uncaught exception in thread: ").append(thread).append("\n");
                    logBuilder.append("Exception message: ").append(message).append("\n");
                    for (StackTraceElement element : thread.getStackTrace()) {
                        logBuilder.append("\tat ").append(element).append("\n");
                    }
                });
                uncaughtExceptions.clear();

                try {
                    Files.write(Paths.get(LOG_FILE_PATH), logBuilder.toString().getBytes(),
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
    }
}
