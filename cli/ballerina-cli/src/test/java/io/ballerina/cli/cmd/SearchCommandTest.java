/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.cli.cmd;

import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;

/**
 * Search command tests.
 *
 * @since 2.0.
 */
public class SearchCommandTest extends BaseCommandTest {

    @Test(description = "Search without keyword")
    public void testSearchWithoutKeyword() throws IOException {
        SearchCommand searchCommand = new SearchCommand(printStream, printStream);
        new CommandLine(searchCommand).parse();
        searchCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains("ballerina: no keyword given"));
        Assert.assertTrue(actual.contains("bal search [<org>|<package>|<text>]"));
    }

    @Test(description = "Search with too many args")
    public void testPullWithTooManyArgs() throws IOException {
        SearchCommand searchCommand = new SearchCommand(printStream, printStream);
        new CommandLine(searchCommand).parse("wso2", "tests");
        searchCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains("ballerina: too many arguments"));
        Assert.assertTrue(actual.contains("bal search [<org>|<package>|<text>]"));
    }

    @Test(description = "Test search command with argument and a help")
    public void testSearchCommandArgAndHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = { "sample2", "--help" };
        SearchCommand searchCommand = new SearchCommand(printStream, printStream);
        new CommandLine(searchCommand).parse(args);
        searchCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-search - Search Ballerina Central for packages"));
    }

    @Test(description = "Test search command with help flag")
    public void testSearchCommandWithHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = { "-h" };
        SearchCommand searchCommand = new SearchCommand(printStream, printStream);
        new CommandLine(searchCommand).parse(args);
        searchCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-search - Search Ballerina Central for packages"));
    }
}
