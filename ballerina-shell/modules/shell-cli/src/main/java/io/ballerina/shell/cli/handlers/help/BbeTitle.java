package io.ballerina.shell.cli.handlers.help;

import java.util.Arrays;

/**
 * Helper class to Read BBE Records.
 */
public class BBETitle {

    private String title;
    private String column;
    private String category;
    private BBERecord[] samples;


    public BBETitle(String title, String column, String category, BBERecord[] samples) {

        this.title = title;
        this.column = column;
        this.category = category;
        this.samples = Arrays.copyOf(samples, samples.length);
    }

    public String getTitle() {
        return title;
    }

    public String getColumn() {
        return column;
    }

    public String getCategory() {
        return category;
    }

    public BBERecord[] getSamples() {
        return Arrays.copyOf(samples, samples.length);
    }
}
