package io.ballerina.shell.cli.handlers.help;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Helper class to provide code completions related to /help topics.
 *
 */
public class BbeCompletionProvider {

    private static final String BALLERINA_HOME =
            System.getProperty("ballerina.home");
    private static final String BBE_PATH = "/examples/";
    private static final String INDEX_FILE = "index.json";

    private final List<String> topicList;

    public BbeCompletionProvider() {
        topicList = new ArrayList<>();
    }

    public List<String> getTopicList() {
        Gson gson = new Gson();

        String file = BALLERINA_HOME + BBE_PATH + INDEX_FILE;
        String jsonString = readFileAsString(file).trim();

        BbeTitle[] bbeTitles = gson.fromJson(jsonString, BbeTitle[].class);
        Stream<BbeTitle> streamList = Arrays.stream(bbeTitles);

        streamList.forEach((bbeTitle) -> {
            BbeRecord[] samples = bbeTitle.getSamples();
            Stream<BbeRecord> sampleList = Arrays.stream(samples);
            sampleList.forEach((bbeRecordElement) -> {
                topicList.add(bbeRecordElement.getName());
            });
        });
        return topicList;
    }

    private static String readFileAsString(String file) {
        String content;
        try {
            content = Files.readString(Paths.get(file));
        } catch (IOException e) {
            return null;
        }
        return content;
    }
}
