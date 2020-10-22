// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.ballerinalang.cli.module;

import io.ballerina.runtime.JSONParser;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.cli.module.util.ErrorUtil;
import org.ballerinalang.cli.module.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.ballerinalang.cli.module.util.Utils.convertToUrl;
import static org.ballerinalang.cli.module.util.Utils.createHttpUrlConnection;
import static org.ballerinalang.cli.module.util.Utils.getStatusCode;
import static org.ballerinalang.cli.module.util.Utils.initializeSsl;
import static org.ballerinalang.cli.module.util.Utils.setRequestMethod;

/**
 * This class is searching modules from ballerina central.
 *
 * @since 1.2.0
 */
public class Search {

    private static PrintStream outStream = System.out;

    private Search() {
    }

    /**
     * Execute search command.
     *
     * @param url           remote uri of the central
     * @param proxyHost     proxy host
     * @param proxyPort     proxy port
     * @param proxyUsername proxy username
     * @param proxyPassword proxy password
     * @param terminalWidth terminal width of the CLI
     */
    public static void execute(String url, String proxyHost, int proxyPort, String proxyUsername, String proxyPassword,
            String terminalWidth) {
        initializeSsl();
        HttpURLConnection conn = createHttpUrlConnection(convertToUrl(url), proxyHost, proxyPort, proxyUsername,
                proxyPassword);
        conn.setInstanceFollowRedirects(false);
        setRequestMethod(conn, Utils.RequestMethod.GET);
        handleResponse(conn, getStatusCode(conn), terminalWidth);
        Authenticator.setDefault(null);
    }

    /**
     * Handle http response.
     *
     * @param conn          http connection
     * @param statusCode    status code of the response
     * @param terminalWidth terminal width of the CLI
     */
    private static void handleResponse(HttpURLConnection conn, int statusCode, String terminalWidth) {
        try {
            // 200 - modules found
            // Other - Error occurred, json returned with the error message
            BMap payload;
            if (statusCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), Charset.defaultCharset()))) {
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    payload = (BMap) JSONParser.parse(result.toString());
                } catch (IOException e) {
                    throw ErrorUtil.createCommandException(e.getMessage());
                }

                if (payload.getIntValue(StringUtils.fromString("count")) > 0) {
                    BArray modules = payload.getArrayValue(StringUtils.fromString("modules"));
                    printModules(modules, terminalWidth);
                } else {
                    outStream.println("no modules found");
                }
            } else {
                StringBuilder result = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), Charset.defaultCharset()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                } catch (IOException e) {
                    throw ErrorUtil.createCommandException(e.getMessage());
                }

                payload = (BMap) JSONParser.parse(result.toString());
                throw ErrorUtil.createCommandException(
                        payload.getStringValue(StringUtils.fromString("message")).getValue());
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Print module information as a table in the CLI.
     *
     * @param modules       modules array
     * @param terminalWidth terminal width of the CLI
     */
    public static void printModules(BArray modules, String terminalWidth) {
        int rightMargin = 3;
        int width = Integer.parseInt(terminalWidth) - rightMargin;
        int dateColWidth = 15;
        int versionColWidth = 8;
        int authorsColWidth = 15;
        double nameColFactor = 9.0;
        double descColFactor = 16.0;
        int additionalSpace = 7;
        double remainingWidth = (double) width - (dateColWidth + versionColWidth + additionalSpace);
        int nameColWidth = (int) Math.round(remainingWidth * (nameColFactor / (nameColFactor + descColFactor)));
        int descColWidth = (int) Math.round(remainingWidth * (descColFactor / (nameColFactor + descColFactor)));
        int minDescColWidth = 60;

        printTitle();
        printTableHeader(dateColWidth, versionColWidth, nameColWidth, descColWidth, minDescColWidth, authorsColWidth);

        int i = 0;
        while (i < modules.size()) {
            printModule((BMap) modules.get(i), dateColWidth, versionColWidth, authorsColWidth, nameColWidth,
                    descColWidth, minDescColWidth);
            i = i + 1;
            outStream.println();
        }
        outStream.println();
        outStream.println(modules.size() + " modules found");
    }

    /**
     * Print module row.
     *
     * @param module          module information
     * @param dateColWidth    date column width
     * @param versionColWidth version column width
     * @param nameColWidth    name column width
     * @param descColWidth    description column width
     * @param minDescColWidth minimum description column width
     * @param authorsColWidth authors column width
     */
    private static void printModule(BMap module, int dateColWidth, int versionColWidth, int authorsColWidth,
            int nameColWidth, int descColWidth, int minDescColWidth) {
        String orgName = module.getStringValue(StringUtils.fromString("orgName")).getValue();
        String packageName = module.getStringValue(StringUtils.fromString("name")).getValue();
        printInCLI("|" + orgName + "/" + packageName, nameColWidth);

        String summary = module.getStringValue(StringUtils.fromString("summary")).getValue();

        if (descColWidth >= minDescColWidth) {
            printInCLI(summary, descColWidth - authorsColWidth);
            String authors = "";
            BArray authorsArr = module.getArrayValue(StringUtils.fromString("authors"));

            if (authorsArr.size() > 0) {
                for (int j = 0; j < authorsArr.size(); j++) {
                    if (j == 0) {
                        authors = authorsArr.get(j).toString();
                    } else if (j == authorsArr.size() - 1) {
                        authors = authorsArr.get(j).toString();
                    } else {
                        authors = ", " + authorsArr.get(j);
                    }
                }
            }
            printInCLI(authors, authorsColWidth);
        } else {
            printInCLI(summary, descColWidth);
        }

        long createTimeJson = module.getIntValue(StringUtils.fromString("createdDate"));
        printInCLI(getDateCreated(createTimeJson), dateColWidth);

        String packageVersion = module.getStringValue(StringUtils.fromString("version")).getValue();
        printInCLI(packageVersion, versionColWidth);
    }

    /**
     * Print the tile.
     */
    private static void printTitle() {
        outStream.println();
        outStream.println("Ballerina Central");
        outStream.println("=================");
        outStream.println();
    }

    /**
     * Print in CLI.
     *
     * @param element           printing element
     * @param charactersAllowed number of characters allowed
     */
    private static void printInCLI(String element, int charactersAllowed) {
        int lengthOfElement = element.length();
        if (lengthOfElement > charactersAllowed || lengthOfElement == charactersAllowed) {
            int margin = 3;
            String trimmedElement = element.substring(0, charactersAllowed - margin) + "...";
            outStream.print(trimmedElement + " |");
        } else {
            printCharacter(element, charactersAllowed, " ", false);
        }
    }

    /**
     * Print characters.
     *
     * @param element           printing element
     * @param charactersAllowed number of characters allowed
     * @param separator         separating element
     * @param isDashElement     is a dash element
     */
    private static void printCharacter(String element, int charactersAllowed, String separator, boolean isDashElement) {
        int lengthOfElement = element.length();
        StringBuilder print = new StringBuilder(element);
        int i = 0;
        while (i < charactersAllowed - lengthOfElement) {
            print.append(separator);
            i = i + 1;
        }
        if (isDashElement) {
            outStream.print(print + "-|");
        } else {
            outStream.print(print + " |");
        }
    }

    /**
     * Get date in simple date format.
     *
     * @param timeInMillis date in millis
     * @return date in simple date format
     */
    private static String getDateCreated(long timeInMillis) {
        Date date = new Date(timeInMillis);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-E");
        return df.format(date);
    }

    /**
     * Print table header.
     *
     * @param dateColWidth    date column width
     * @param versionColWidth version column width
     * @param nameColWidth    name column width
     * @param descColWidth    description column width
     * @param minDescColWidth minimum description column width
     * @param authorsColWidth authors column width
     */
    private static void printTableHeader(int dateColWidth, int versionColWidth, int nameColWidth, int descColWidth,
            int minDescColWidth, int authorsColWidth) {
        printHeadingRow(dateColWidth, versionColWidth, nameColWidth, descColWidth, minDescColWidth, authorsColWidth);
        printDashRow(dateColWidth, versionColWidth, nameColWidth, descColWidth, minDescColWidth, authorsColWidth);
    }

    /**
     * Print heading row of the table header.
     *
     * @param dateColWidth    date column width
     * @param versionColWidth version column width
     * @param nameColWidth    name column width
     * @param descColWidth    description column width
     * @param minDescColWidth minimum description column width
     * @param authorsColWidth authors column width
     */
    private static void printHeadingRow(int dateColWidth, int versionColWidth, int nameColWidth, int descColWidth,
            int minDescColWidth, int authorsColWidth) {
        printInCLI("|NAME", nameColWidth);
        if (descColWidth >= minDescColWidth) {
            printInCLI("DESCRIPTION", descColWidth - authorsColWidth);
            printInCLI("AUTHOR", authorsColWidth);
        } else {
            printInCLI("DESCRIPTION", descColWidth);
        }
        printInCLI("DATE", dateColWidth);
        printInCLI("VERSION", versionColWidth);
        outStream.println();
    }

    /**
     * Print dash row of the table header.
     *
     * @param dateColWidth    date column width
     * @param versionColWidth version column width
     * @param nameColWidth    name column width
     * @param descColWidth    description column width
     * @param minDescColWidth minimum description column width
     * @param authorsColWidth authors column width
     */
    private static void printDashRow(int dateColWidth, int versionColWidth, int nameColWidth, int descColWidth,
            int minDescColWidth, int authorsColWidth) {
        printCharacter("|-", nameColWidth, "-", true);

        if (descColWidth >= minDescColWidth) {
            printCharacter("-", descColWidth - authorsColWidth, "-", true);
            printCharacter("-", authorsColWidth, "-", true);
        } else {
            printCharacter("-", descColWidth, "-", true);
        }

        printCharacter("-", dateColWidth, "-", true);
        printCharacter("-", versionColWidth, "-", true);

        outStream.println();
    }
}
