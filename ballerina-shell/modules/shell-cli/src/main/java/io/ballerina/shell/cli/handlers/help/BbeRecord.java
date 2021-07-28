package io.ballerina.shell.cli.handlers.help;

/**
 * Helper class to Read BBE Records.
 */
public class BBERecord {

    private String name;
    private String url;
    private String verifyBuild;
    private String verifyOutput;
    private String isLearnByExample;

    public BBERecord(String name, String url, String verifyBuild, String verifyOutput, String isLearnByExample) {
        this.name = name;
        this.url = url;
        this.verifyBuild = verifyBuild;
        this.verifyOutput = verifyOutput;
        this.isLearnByExample = isLearnByExample;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getVerifyBuild() {
        return verifyBuild;
    }

    public String getVerifyOutput() {
        return verifyOutput;
    }

    public String getIsLearnByExample() {
        return isLearnByExample;
    }
}
