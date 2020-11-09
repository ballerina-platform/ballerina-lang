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

package io.ballerina.cli.utils;

import org.ballerinalang.central.client.model.Package;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * {@code PrintUtils} has utilities to print search results.
 *
 * @since 2.0.0
 */
public class PrintUtils {

    private static PrintStream outStream = System.out;

    private PrintUtils() {
    }

    /**
     * Print package information as a table in the CLI.
     *
     * @param packages       packages array
     * @param terminalWidth terminal width of the CLI
     */
    public static void printPackages(List<Package> packages, String terminalWidth) {
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

        for (Package aPackage : packages) {
            printPackage(aPackage, dateColWidth, versionColWidth, authorsColWidth, nameColWidth, descColWidth,
                    minDescColWidth);
            outStream.println();
        }
        outStream.println();
        outStream.println(packages.size() + " packages found");
    }

    /**
     * Print package row.
     *
     * @param aPackage        package information
     * @param dateColWidth    date column width
     * @param versionColWidth version column width
     * @param nameColWidth    name column width
     * @param descColWidth    description column width
     * @param minDescColWidth minimum description column width
     * @param authorsColWidth authors column width
     */
    private static void printPackage(Package aPackage, int dateColWidth, int versionColWidth, int authorsColWidth,
            int nameColWidth, int descColWidth, int minDescColWidth) {
        printInCLI("|" + aPackage.getOrganization() + "/" + aPackage.getName(), nameColWidth);

        String summary = getSummary(aPackage);

        if (descColWidth >= minDescColWidth) {
            printInCLI(summary, descColWidth - authorsColWidth);
            String authors = "";
            List<String> authorsArr = aPackage.getAuthors();

            if (!authorsArr.isEmpty()) {
                for (int j = 0; j < authorsArr.size(); j++) {
                    if (j == 0) {
                        authors = authorsArr.get(j);
                    } else if (j == authorsArr.size() - 1) {
                        authors = authorsArr.get(j);
                    } else {
                        authors = ", " + authorsArr.get(j);
                    }
                }
            }
            printInCLI(authors, authorsColWidth);
        } else {
            printInCLI(summary, descColWidth);
        }

        printInCLI(getDateCreated(aPackage.getCreatedDate()), dateColWidth);
        printInCLI(aPackage.getVersion(), versionColWidth);
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

    private static String getSummary(Package aPackage) {
        String summary = aPackage.getSummary();
        if (summary == null) {
            String readme = aPackage.getReadme();
            if (readme == null) {
                return "";
            }
            summary = readme.substring(0, readme.indexOf('\n'));
        }
        return summary;
    }
}
