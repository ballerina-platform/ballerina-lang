package org.wso2.ballerina.tooling.service.workspace.launcher;

import com.google.gson.Gson;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.wso2.ballerina.tooling.service.workspace.launcher.dto.CommandDTO;
import org.wso2.ballerina.tooling.service.workspace.launcher.dto.MessageDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class LaunchManager {

    private static LaunchManager launchManagerInstance;

    private LaunchServer launchServer;

    private LaunchSession launchSession;

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

    public void runProgram(){
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("ping localhost");
            launchSession.setProcess(p);

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                MessageDTO m = new MessageDTO();
                m.setCode(LauncherConstants.OUTPUT);
                m.setMessage(line);
                pushMessageToClient(launchSession,m);
            }
        } catch (IOException e) {
            //if any exception destroy the process
            p.destroy();
        }
    }

    public void runService(){

    }

    public void debugProgram(){

    }

    public void debugService(){

    }

    public void stopProcess(){
        Process p = this.launchSession.getProcess();
        if(p.isAlive()){
            p.destroy();
        }
    }


    public int getFreePort(){
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
                runProgram();
                break;
            case LauncherConstants.RUN_SERVICE:
                runService();
                break;
            case LauncherConstants.DEBUG_PROGRAM:
                debugProgram();
                break;
            case LauncherConstants.DEBUG_SERVICE:
                debugService();
                break;
            case LauncherConstants.STOP:
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
     * @param debugSession the debug session
     * @param status       the status
     */
    public void pushMessageToClient(LaunchSession debugSession, MessageDTO status) {
        Gson gson = new Gson();
        String json = gson.toJson(status);
        debugSession.getChannel().write(new TextWebSocketFrame(json));
        debugSession.getChannel().flush();
    }
}
