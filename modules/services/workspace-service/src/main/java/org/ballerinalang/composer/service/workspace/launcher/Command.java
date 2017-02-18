package org.ballerinalang.composer.service.workspace.launcher;


import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class Command {

    private String fileName;
    private String filePath;
    private boolean debug = false;
    private LauncherConstants.ProgramType type;
    private int port;

    public Command(LauncherConstants.ProgramType type, String fileName, String filePath, boolean debug) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.debug = debug;
        this.type = type;
        if(debug){
            this.port = getFreePort();
        }
    }

    private static String OS = System.getProperty("os.name").toLowerCase();

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public LauncherConstants.ProgramType getType() {
        return type;
    }

    public void setType(LauncherConstants.ProgramType type) {
        this.type = type;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString(){
        String ballerinaBin, ballerinaCommand, programType, scriptLocation, debugSwitch = "";
        int port = -1;

        // path to bi directory
        ballerinaBin = System.getProperty("ballerina.home") + File.separator + "bin" + File.separator;

        if (Command.isWindows()) {
            ballerinaCommand = "ballerina.bat run ";
        } else {
            ballerinaCommand = "ballerina run ";
        }

        if(type == LauncherConstants.ProgramType.RUN) {
            programType = "main ";
        }else{
            programType = "service ";
        }

        scriptLocation =   filePath + File.separator + fileName;


        if(debug) {
            port = getFreePort();
            debugSwitch = "  --ballerina.debug " + port;
        }
        return ballerinaBin + ballerinaCommand + programType + scriptLocation + debugSwitch;
    }

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
    }

    public static boolean isSolaris() {
        return (OS.indexOf("sunos") >= 0);
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
}
