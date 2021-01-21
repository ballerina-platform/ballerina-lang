/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.cli.handlers.help;

import io.ballerina.shell.cli.PropertiesLoader;
import io.ballerina.shell.cli.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static io.ballerina.shell.cli.PropertiesLoader.DESCRIPTION_URL_TEMPLATE;
import static io.ballerina.shell.cli.PropertiesLoader.EXAMPLE_URL_TEMPLATE;
import static io.ballerina.shell.cli.PropertiesLoader.HELP_EXAMPLE_POSTFIX;
import static io.ballerina.shell.cli.PropertiesLoader.TOPICS_FILE;

/**
 * Help provider that will fetch data from the BBE.
 * Will fetch the topic and show the description associated with them.
 * TODO: Replace with a proper help provider for REPL.
 *
 * @since 2.0.0
 */
public class RemoteBbeHelpProvider implements HelpProvider {
    private static final char HYPHEN = '-';
    private static final char UNDERSCORE = '_';
    private static final String NEWLINE = "\n";
    private static final String TOPICS = "topics";

    @Override
    public void getTopic(String[] args, StringBuilder output) throws HelpProviderException {
        String helpExamplePostfix = PropertiesLoader.getProperty(HELP_EXAMPLE_POSTFIX);
        boolean isExample = (args.length > 1) && args[1].equals(helpExamplePostfix);
        String topic = args[0];

        if (!topic.matches("^[a-zA-Z-_]+$")) {
            throw new HelpProviderException("Not a valid topic name.");
        }

        if (topic.trim().equals(TOPICS)) {
            // If command is 'help topics' output all available topics.
            List<String> topicsKeywords = FileUtils.readKeywords(PropertiesLoader.getProperty(TOPICS_FILE));
            output.append("Following are all available topics.\n\n");
            for (String keyword : topicsKeywords) {
                output.append(keyword).append(NEWLINE);
            }
            return;
        }

        try {
            String descriptionUrl = PropertiesLoader.getProperty(DESCRIPTION_URL_TEMPLATE);
            String exampleUrl = PropertiesLoader.getProperty(EXAMPLE_URL_TEMPLATE);
            String fileName = topic.replace(HYPHEN, UNDERSCORE);

            String file = String.format(isExample ? exampleUrl : descriptionUrl, topic, fileName);
            String content = FileUtils.readFromUrl(file).trim();
            if (!isExample) {
                content = content
                        .replaceAll("^//\\s*", NEWLINE) // Comment sign at start of file
                        .replaceAll("\n//\\s*", NEWLINE) // Comment sign at start of new lines
                        .replaceAll("<br/>", NEWLINE) // <br/> tag
                        .trim();
            }
            output.append(content).append(NEWLINE);
        } catch (FileNotFoundException e) {
            throw new HelpProviderException("" +
                    "No ballerina documentation found for '" + topic + "'.\n" +
                    "Use '/help TOPIC' to get help on a specific topic.");
        } catch (IOException e) {
            throw new HelpProviderException("" +
                    "Help retrieval failed.\n" +
                    "Use '/help TOPIC' to get help on a specific topic.");
        }
    }
}
