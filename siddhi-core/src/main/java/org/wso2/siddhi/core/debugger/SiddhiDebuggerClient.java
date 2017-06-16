/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.debugger;

import com.google.gson.Gson;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SiddhiDebuggerClient is a commandline tool to feed siddhi app and input from text files and debug the query at
 * the runtime. It accepts the following commands:
 * - add breakpoint query:TERMINAL - Adding a new breakpoint. Eg:add breakpoint query1:IN
 * - remove breakpoint query:TERMINAL - Adding a new breakpoint. Eg:remove breakpoint query1:IN
 * - start - Start sending input
 * - stop - Stop the debugger
 * - next - Move to the next checkpoint
 * - play - Skip the current checkpoint
 */
public class SiddhiDebuggerClient {

    /**
     * Method name used to specify delay in input.
     * Example: delay(100)
     */
    private static final String DELAY = "delay";

    /**
     * Delimiter separating stream name and the input data in the input.
     */
    private static final String INPUT_DELIMITER = "=";

    /**
     * Command to start sending events to Siddhi.
     */
    private static final String START = "start";

    /**
     * Command to stop the debugger and Siddhi.
     */
    private static final String STOP = "stop";

    /**
     * Command to move the debugger to the next checkpoint.
     */
    private static final String NEXT = "next";

    /**
     * Command to skip the current breakpoint check.
     */
    private static final String PLAY = "play";

    /**
     * Command to get state of a query.
     */
    private static final String STATE = "state";

    /**
     * Query in terminal.
     */
    private static final String IN = "in";

    /**
     * Query out terminal.
     */
    private static final String OUT = "out";

    /**
     * Command to add a new break point.
     * Example: <pre>{@code add breakpoint  <query name>:<in/out>}</pre>
     */
    private static final String ADD_BREAKPOINT = "add breakpoint ";

    /**
     * Command to remove an existing break point.
     * Example: <pre>{@code remove breakpoint  <query name>:<in/out>}</pre>
     */
    private static final String REMOVE_BREAKPOINT = "remove breakpoint ";

    /**
     * Delimiter separating the query name and the terminal in add/remove breakpoint commands.
     *
     * @see SiddhiDebuggerClient#ADD_BREAKPOINT
     * @see SiddhiDebuggerClient#REMOVE_BREAKPOINT
     */
    private static final String QUERY_DELIMITER = ":";

    /**
     * The start of a new line expecting user input.
     */
    private static final String DEBUGGER_TERMINAL_PREFIX = ": ";


    /**
     * Main method of the SiddhiDebuggerClient.
     *
     * @param args two arguments are expected: siddhi app path and input file path
     */
    public static void main(String[] args) {
        // Validate the number of arguments
        if (args.length != 2) {
            error("Expected two arguments but found " + args.length + "\n. Please try again with two arguments: " +
                    "<siddhi app file> <input file path>");
            return;
        }

        String siddhiAppPath = args[0];
        String inputPath = args[1];

        // Validate file
        File siddhiAppFile = new File(siddhiAppPath);
        File inputFile = new File(inputPath);
        if (!siddhiAppFile.exists() || !siddhiAppFile.isFile()) {
            error("Invalid siddhi app file: " + siddhiAppPath);
        }
        if (!inputFile.exists() || !inputFile.isFile()) {
            error("Invalid input file: " + inputPath);
        }

        // Read the files
        String query;
        String input;
        try {
            query = readText(siddhiAppPath);
            input = readText(inputPath);
        } catch (IOException e) {
            error("Failed to read " + siddhiAppPath);
            return;
        }
        try {
            input = readText(inputPath);
        } catch (IOException e) {
            error("Failed to read " + inputPath);
            return;
        }
        // Start the SiddhiDebuggerClient
        SiddhiDebuggerClient client = new SiddhiDebuggerClient();
        client.start(query, input);
    }

    /**
     * Print information message to the console.
     *
     * @param msg the message
     */
    private static void info(String msg) {
        System.out.println("INFO: " + msg);
    }

    /**
     * Print error message to the console.
     *
     * @param msg the message
     */
    private static void error(String msg) {
        System.out.println("ERROR: " + msg);
    }

    /**
     * Read the text content of the given file.
     *
     * @param path the path of the file
     * @return the textr content of the file
     * @throws IOException if there are any issues in reading the file
     */
    private static String readText(String path) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            return builder.toString();
        } catch (FileNotFoundException e) {
            error("The file " + path + " does not exist");
            throw e;
        } catch (IOException e) {
            error("Error in reading the file " + path);
            throw e;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    error("Error when closing the input reader of " + path);
                }
            }
        }
    }

    /**
     * Start the {@link SiddhiDebuggerClient} and configure the breakpoints.
     *
     * @param siddhiApp the Siddhi query
     * @param input         the user input as a whole text
     */
    public void start(final String siddhiApp, String input) {
        SiddhiManager siddhiManager = new SiddhiManager();

        info("Deploying the siddhi app");
        final SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        // Add callbacks for all the streams
        final Set<String> streamNames = SiddhiCompiler.parse(siddhiApp).getStreamDefinitionMap().keySet();
        for (String streamName : streamNames) {
            final String stream = streamName;
            siddhiAppRuntime.addCallback(stream, new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    info("@Receive: Stream: " + stream + ", Event: " + Arrays.deepToString(events));
                }
            });
        }

        SiddhiDebugger siddhiDebugger = siddhiAppRuntime.debug();

        final InputFeeder inputFeeder = new InputFeeder(siddhiAppRuntime, input);

        System.out.println("Configure the breakpoints.\nYou can use the following commands:\n - " +
                ADD_BREAKPOINT + "<query name>:<IN/OUT>\n - " +
                REMOVE_BREAKPOINT + "<query name>:<IN/OUT>\n - " +
                START + "\n - " +
                STOP);
        printNextLine();
        final Scanner scanner = new Scanner(System.in, "UTF-8");
        while (scanner.hasNextLine()) {
            String userInput = scanner.nextLine().trim();
            String command = userInput.toLowerCase();

            if (command.startsWith(ADD_BREAKPOINT)) {
                if (!command.contains(QUERY_DELIMITER)) {
                    error("Invalid add query. The query must be " + ADD_BREAKPOINT + "<query " +
                            "name>:<IN/OUT>. Please try again");
                    printNextLine();
                    continue;
                }
                String[] components = userInput.substring(ADD_BREAKPOINT.length(), userInput.length()).split
                        (QUERY_DELIMITER);
                String queryName = components[0];
                String terminal = components[1].toLowerCase();

                if (IN.equals(terminal)) {
                    siddhiDebugger.acquireBreakPoint(queryName, SiddhiDebugger.QueryTerminal.IN);
                    info("Added a breakpoint at the IN terminal of " + queryName);
                    printNextLine();
                } else if (OUT.equals(terminal)) {
                    siddhiDebugger.acquireBreakPoint(queryName, SiddhiDebugger.QueryTerminal.OUT);
                    info("Added a breakpoint at the OUT terminal of " + queryName);
                    printNextLine();
                } else {
                    error("The terminal must be either IN or OUT but found: " + terminal.toUpperCase() +
                            ". Please try again");
                    printNextLine();
                }
            } else if (command.startsWith(REMOVE_BREAKPOINT)) {
                if (!command.contains(QUERY_DELIMITER)) {
                    error("Invalid add query. The query must be " + REMOVE_BREAKPOINT + "<query " +
                            "name>:<IN/OUT>. Please try again");
                    printNextLine();
                    continue;
                }
                String[] components = command.substring(ADD_BREAKPOINT.length(), command.length()).split
                        (QUERY_DELIMITER);
                String queryName = components[0];
                String terminal = components[1];

                if (IN.equals(terminal)) {
                    siddhiDebugger.releaseBreakPoint(queryName, SiddhiDebugger.QueryTerminal.IN);
                    info("Removed the breakpoint at the IN terminal of " + queryName);
                    printNextLine();
                } else if (OUT.equals(terminal)) {
                    siddhiDebugger.releaseBreakPoint(queryName, SiddhiDebugger.QueryTerminal.OUT);
                    info("Removed the breakpoint at the OUT terminal of " + queryName);
                    printNextLine();
                } else {
                    error("The terminal must be either IN or OUT but found: " + terminal.toUpperCase());
                    printNextLine();
                }

            } else if (STOP.equals(command)) {
                inputFeeder.stop();
                siddhiAppRuntime.shutdown();
                break;
            } else if (START.equals(command)) {
                inputFeeder.start();
                info("Siddhi Debugger starts sending input to Siddhi");
                System.out.println("You can use the following commands:\n - " +
                        NEXT + "\n - " +
                        PLAY + "\n - " +
                        STATE + ":<query name>\n - " +
                        STOP);
                break;
            } else {
                error("Invalid command: " + command);
                printNextLine();
            }
        }

        siddhiDebugger.setDebuggerCallback(new SiddhiDebuggerCallback() {
            @Override
            public void debugEvent(ComplexEvent event, String queryName, SiddhiDebugger.QueryTerminal queryTerminal,
                                   SiddhiDebugger debugger) {
                info("@Debug: Query: " + queryName + ", Terminal: " + queryTerminal + ", Event: " + event);
                printNextLine();
                while (scanner.hasNextLine()) {
                    String command = scanner.nextLine().trim().toLowerCase();
                    if (STOP.equals(command)) {
                        debugger.releaseAllBreakPoints();
                        debugger.play();
                        inputFeeder.stop();
                        siddhiAppRuntime.shutdown();
                        break;
                    } else if (NEXT.equals(command)) {
                        debugger.next();
                        break;
                    } else if (PLAY.equals(command)) {
                        debugger.play();
                        break;
                    } else if (command.startsWith(STATE)) {
                        if (!command.contains(QUERY_DELIMITER)) {
                            error("Invalid get state request. The query must be " + STATE + ":<query " +
                                    "name>. Please try again");
                            printNextLine();
                            continue;
                        }
                        String[] components = command.split(QUERY_DELIMITER);
                        String requestQueryName = components[1];

                        Map<String, Object> state = debugger.getQueryState(requestQueryName.trim());
                        System.out.println("Query '" + requestQueryName + "' state : ");
                        for (Map.Entry<String, Object> entry : state.entrySet()) {
                            System.out.println("    '" + entry.getKey() + "' : " + entry.getValue());
                        }
                        printNextLine();
                        continue;
                    } else {
                        error("Invalid command: " + command);
                        printNextLine();
                    }
                }
            }
        });

        inputFeeder.join();

        if (inputFeeder.isRunning()) {
            info("Input feeder has sopped sending all inputs. If you want to stop the execution, use " +
                    "the STOP command");
            printNextLine();
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine().trim().toLowerCase();
                if (STOP.equals(command)) {
                    inputFeeder.stop();
                    siddhiAppRuntime.shutdown();
                    break;
                } else {
                    error("Invalid command: " + command);
                    printNextLine();
                }
            }
        }

        scanner.close();

        info("Siddhi Debugger is stopped successfully");
    }

    /**
     * Prepare the termminal for the user input. It just prints the starting special character or line.
     */
    private void printNextLine() {
        System.out.print(DEBUGGER_TERMINAL_PREFIX);
    }

    /**
     * A runnable class to feed the input to the Siddhi runtime.
     */
    private static class InputFeeder implements Runnable {
        private final SiddhiAppRuntime siddhiAppRuntime;
        private String input;
        private volatile AtomicBoolean running = new AtomicBoolean(false);
        private Thread thread;

        private InputFeeder(SiddhiAppRuntime siddhiAppRuntime, String input) {
            this.siddhiAppRuntime = siddhiAppRuntime;
            this.input = input;
        }

        @Override
        public void run() {
            // Scanner to read the user input line by line
            Scanner scanner = new Scanner(input);
            Gson gson = new Gson();
            while (scanner.hasNext()) {
                if (!running.get()) {
                    break;
                }
                String line = scanner.nextLine().trim();
                if (line.startsWith(DELAY)) {
                    // The delay(<time in milliseconds>) is used to delay the input
                    line = line.substring(6, line.length() - 1);
                    try {
                        Thread.sleep(Integer.parseInt(line));
                    } catch (InterruptedException e) {
                        error("Error in waiting for " + line + " milliseconds");
                    }
                } else {
                    // The inout format is: <stream name>=<data in json object[] format>
                    String[] components = line.split(INPUT_DELIMITER);
                    String streamName = components[0];
                    String event = components[1];
                    Object[] data = gson.fromJson(event, Object[].class);
                    info("@Send: Stream: " + streamName + ", Event: " + event);
                    try {
                        siddhiAppRuntime.getInputHandler(streamName).send(data);
                    } catch (InterruptedException e) {
                        error("Error in sending event " + event + " to Siddhi");
                    }
                }
            }
            scanner.close();
        }

        /**
         * Check whether the input feeder is running or not.
         *
         * @return tru if the input feeder is running or not stopped manually, otherwise false.
         */
        boolean isRunning() {
            return running.get();

        }

        /**
         * Stop the input feeder.
         */
        void stop() {
            this.running.set(false);
        }

        /**
         * Start the input feeder.
         */
        public void start() {
            if (!this.running.get()) {
                this.running.set(true);
                thread = new Thread(this);
                thread.start();
            }
        }

        /**
         * Join the current thread behind the thread used to execute the input feeder.
         */
        public void join() {
            try {
                this.thread.join();
            } catch (InterruptedException e) {
                error("Error in joining the main thread behind the input feeder");
            }
        }
    }
}
