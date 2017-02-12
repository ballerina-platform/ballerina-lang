package org.wso2.ballerina.tooling.service.workspace.launcher;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LaunchSession {

    private Channel channel = null;

    public LaunchSession(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    private Process process;


    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process p) {
        this.process = p;
    }
}
