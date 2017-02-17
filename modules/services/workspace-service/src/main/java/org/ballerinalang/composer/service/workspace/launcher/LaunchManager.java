/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.composer.service.workspace.launcher;

import com.google.gson.Gson;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.ballerinalang.composer.service.workspace.launcher.dto.CommandDTO;
import org.ballerinalang.composer.service.workspace.launcher.dto.MessageDTO;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.ServerSocket;

/**
 * Launch Manager which manage launch requests from the clients.
 */
public class LaunchManager {

    private static LaunchManager launchManagerInstance;

    private LaunchServer launchServer;

    private LaunchSession launchSession;

    private Process program = null;

    /**
     * Instantiates a new Debug manager.
     */
    protected LaunchManager() {

    }

    /**
     * Launch manager singleton
     *
     * @return LaunchManager instance
     */
    public static LaunchManager getInstance() {
        synchronized (LaunchManager.class) {
            if (launchManagerInstance == null) {
                launchManagerInstance = new LaunchManager();
            }
        }
        return launchManagerInstance;
    }


    public void init(){
        // start the debug server if it is not started yet.
        if (this.launchServer == null) {
            this.launchServer = new LaunchServer();
            this.launchServer.startServer();
        }
    }

    private void run(LauncherConstants.ProgramType type, String fileName, String filePath, boolean debug){
        String command = "";
        int port = -1;

        // kill a previously running program
        stopProcess();

        // send a message if ballerina home is not set
        if(null == System.getProperty("ballerina.home") || System.getProperty("ballerina.home").isEmpty()){
            pushMessageToClient(launchSession, LauncherConstants.ERROR, LauncherConstants.ERROR,
                    LauncherConstants.INVALID_BAL_PATH_MESSAGE);
            pushMessageToClient(launchSession, LauncherConstants.ERROR, LauncherConstants.ERROR,
                    LauncherConstants.SET_BAL_PATH_MESSAGE);
            return;
        }

        // construct the command
        command = System.getProperty("ballerina.home") + File.separator + "bin" + File.separator + "ballerina ";

        if(type == LauncherConstants.ProgramType.RUN) {
            command = command + " run main ";
        }else{
            command = command + " run service ";
        }

        command = command + filePath + File.separator + fileName;

        if(debug){
            port = getFreePort();
            command = command + "  --ballerina.debug " + port;
        }

        try {
            this.program = Runtime.getRuntime().exec(command);

            if(type == LauncherConstants.ProgramType.RUN ){
                pushMessageToClient(launchSession, LauncherConstants.EXECUTION_STARTED, LauncherConstants.INFO,
                        String.format(LauncherConstants.RUN_MESSAGE , fileName));
            } else {
                pushMessageToClient(launchSession, LauncherConstants.EXECUTION_STARTED, LauncherConstants.INFO,
                        String.format(LauncherConstants.SERVICE_MESSAGE , fileName));
            }

            if(debug){
                MessageDTO debugMessage = new MessageDTO();
                debugMessage.setCode(LauncherConstants.DEBUG);
                debugMessage.setPort(port);
                pushMessageToClient(launchSession, debugMessage);
            }

            // start a new thread to stream command output.
            Runnable output = new Runnable() {
                public void run() {
                    LaunchManager.this.streamOutput();
                }
            };
            (new Thread(output)).start();
            Runnable error = new Runnable() {
                public void run() { LaunchManager.this.streamError();  }
            };
            (new Thread(error)).start();

        } catch (IOException e) {
            pushMessageToClient(launchSession, LauncherConstants.EXIT, LauncherConstants.ERROR, e.getMessage());
            this.program.destroy();
        }
    }

    public void streamOutput() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.program.getInputStream()));
        String line = "";
        try {
            while ((line = reader.readLine())!= null) {
                pushMessageToClient(launchSession, LauncherConstants.OUTPUT, LauncherConstants.DATA, line);
            }
            pushMessageToClient(launchSession, LauncherConstants.EXECUTION_STOPED , LauncherConstants.INFO,
                    LauncherConstants.END_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void streamError() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.program.getErrorStream()));
        String line = "";
        try {
            while ((line = reader.readLine())!= null) {
                pushMessageToClient(launchSession, LauncherConstants.OUTPUT, LauncherConstants.ERROR, line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopProcess() {
        int pid = -1;
        if (this.program != null && this.program.isAlive()) {
            this.program.destroyForcibly();
            try {
                this.program.waitFor();
            } catch (InterruptedException e) {
                //do nothing.
            }
            pushMessageToClient(launchSession, LauncherConstants.EXECUTION_TERMINATED , LauncherConstants.INFO,
                    LauncherConstants.TERMINATE_MESSAGE);
        }
    }


    public int getFreePort() {
        for (int i = LauncherConstants.MIN_PORT_NUMBER; i < LauncherConstants.MAX_PORT_NUMBER; i++) {
            if (available(i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks to see if a specific port is available.
     *
     * @param port the port number to check for availability
     * @return <tt>true</tt> if the port is available, or <tt>false</tt> if not
     * @throws IllegalArgumentException is thrown if the port number is out of range
     */
    public static boolean available(int port) throws IllegalArgumentException {
        if (port < LauncherConstants.MIN_PORT_NUMBER || port > LauncherConstants.MAX_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid start currentMinPort: " + port);
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            // Do nothing
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }

    public void addLaunchSession(Channel channel) {
        this.launchSession = new LaunchSession(channel);
    }

    public void processCommand(String json) {
        Gson gson = new Gson();
        CommandDTO command = gson.fromJson(json, CommandDTO.class);
        switch (command.getCommand()) {
            case LauncherConstants.RUN_PROGRAM:
                run(LauncherConstants.ProgramType.RUN, command.getFileName(), command.getFilePath(), false);
                break;
            case LauncherConstants.RUN_SERVICE:
                run(LauncherConstants.ProgramType.SERVICE, command.getFileName(), command.getFilePath(), false);
                break;
            case LauncherConstants.DEBUG_PROGRAM:
                run(LauncherConstants.ProgramType.RUN, command.getFileName(), command.getFilePath(), true);
                break;
            case LauncherConstants.DEBUG_SERVICE:
                run(LauncherConstants.ProgramType.SERVICE, command.getFileName(), command.getFilePath(), true);
                break;
            case LauncherConstants.TERMINATE:
                stopProcess();
                break;
            default:
                MessageDTO message = new MessageDTO();
                message.setCode(LauncherConstants.INVALID_CMD);
                message.setMessage(LauncherConstants.MSG_INVALID);
                launchServer.pushMessageToClient(launchSession, message);
        }
    }

    /**
     * Push message to client.
     *
     * @param session the debug session
     * @param status       the status
     */
    public void pushMessageToClient(LaunchSession session, MessageDTO status) {
        Gson gson = new Gson();
        String json = gson.toJson(status);
        session.getChannel().write(new TextWebSocketFrame(json));
        session.getChannel().flush();
    }

    public void pushMessageToClient(LaunchSession session, String code, String type, String text) {
        MessageDTO message = new MessageDTO();
        message.setCode(code);
        message.setType(type);
        message.setMessage(text);
        pushMessageToClient(session, message);
    }
}
