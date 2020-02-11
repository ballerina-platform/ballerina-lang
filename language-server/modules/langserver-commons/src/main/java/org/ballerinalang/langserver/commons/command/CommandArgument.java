package org.ballerinalang.langserver.commons.command;

/**
 *  Class for the command argument holding argument key and argument value.
 */
public class CommandArgument {
    private String argumentK;

    private String argumentV;

    public CommandArgument(String argumentK, String argumentV) {
        this.argumentK = argumentK;
        this.argumentV = argumentV;
    }

    public String getArgumentK() {
        return argumentK;
    }

    public String getArgumentV() {
        return argumentV;
    }

}
