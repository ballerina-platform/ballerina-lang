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

import io.ballerina.projects.BalToolsManifest;
import io.ballerina.projects.util.ProjectConstants;
import org.ballerinalang.central.client.model.Package;
import org.ballerinalang.central.client.model.Tool;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * {@code PrintUtils} has utilities to print search results.
 *
 * @since 2.0.0
 */
public class PrintUtils {

    private static final PrintStream outStream = System.out;

    private PrintUtils() {
    }

    /**
     * Print locally available tool information as a table in the CLI.
     *
     * @param tools       List of tools
     */
    public static void printLocalTools(List<BalToolsManifest.Tool> tools, String terminalWidth) {
        int rightMargin = 3;
        int width = Integer.parseInt(terminalWidth) - rightMargin;
        int maxToolIdNameLength = 0;
        for (BalToolsManifest.Tool tool: tools) {
            if (maxToolIdNameLength < tool.id().length()) {
                maxToolIdNameLength = tool.id().length();
            }
        }
        int versionColWidth = 15;
        int repositoryColWidth = 10;
        int padding = 2;
        int minimumToolIdColWidth = 20;
        int remainingWidth = Math.max(minimumToolIdColWidth, width - versionColWidth);
        int toolIdColWidth = Math.max(minimumToolIdColWidth, Math.min(maxToolIdNameLength, remainingWidth)) + padding;

        printListLocalTableHeader(toolIdColWidth, versionColWidth, repositoryColWidth);

        for (BalToolsManifest.Tool tool: tools) {
            String repository = ProjectConstants.LOCAL_REPOSITORY_NAME.equals(tool.repository()) ? "local" : "central";
            String activeIndicator = tool.active() ? "* " : "  ";
            printInCLI("|" + tool.id(), toolIdColWidth);
            printInCLI(activeIndicator + tool.version(), versionColWidth);
            printInCLI(repository, repositoryColWidth);
            outStream.println();
        }
        outStream.println();
        outStream.println(tools.size() + " tools found.");
    }

    private static void printListLocalTableHeader(int toolIdColWidth, int versionColWidth, int repositoryColWidth) {
        printInCLI("|TOOL ID", toolIdColWidth);
        printInCLI("VERSION", versionColWidth);
        printInCLI("REPO", repositoryColWidth);
        outStream.println();

        printCharacter("|-", toolIdColWidth, "-", true);
        printCharacter("-", versionColWidth, "-", true);
        printCharacter("-", repositoryColWidth, "-", true);
        outStream.println();
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
        int authorsColWidth = 15;
        double nameColFactor = 7.0;
        double descColFactor = 14.0;
        double versionColFactor = 4.0;
        int additionalSpace = 7;
        double remainingWidth = (double) width - (dateColWidth + additionalSpace);
        int nameColWidth = getColWidth(remainingWidth, nameColFactor, descColFactor, versionColFactor);
        int descColWidth = getColWidth(remainingWidth, descColFactor, nameColFactor, versionColFactor);
        int versionColWidth = getColWidth(remainingWidth, versionColFactor, nameColFactor, descColFactor);
        int minDescColWidth = 60;

        String[] columnNames;
        int[] columnWidths;
        if (descColWidth >= minDescColWidth) {
            columnNames = new String[]{"NAME", "DESCRIPTION", "AUTHOR", "DATE", "VERSION"};
            columnWidths = new int[]{
                    nameColWidth, descColWidth - authorsColWidth, authorsColWidth, dateColWidth, versionColWidth};
        } else {
            columnNames = new String[]{"NAME", "DESCRIPTION", "DATE", "VERSION"};
            columnWidths = new int[]{nameColWidth, descColWidth, dateColWidth, versionColWidth};
        }
        printBallerinaCentralTitle();
        printPackageSearchTableHeader(columnNames, columnWidths);

        for (Package aPackage : packages) {
            outStream.print("|");
            printPackage(aPackage, dateColWidth, versionColWidth, authorsColWidth, nameColWidth - 1, descColWidth,
                         minDescColWidth);
            outStream.println();
        }
        outStream.println();
        outStream.println(packages.size() + " packages found");
    }

    /**
     * Print package information as a table in the CLI.
     *
     * @param tools       packages array
     * @param terminalWidth terminal width of the CLI
     */
    public static void printTools(List<Tool> tools, String terminalWidth) {
        int rightMargin = 3;
        int width = Integer.parseInt(terminalWidth) - rightMargin;
        int dateColWidth = 15;
        int authorsColWidth = 15;
        double idColFactor = 7.0;
        double nameColFactor = 7.0;
        double descColFactor = 14.0;
        double versionColFactor = 4.0;
        int additionalSpace = 7;
        double remainingWidth = (double) width - (dateColWidth + additionalSpace);
        int idColWidth = getColWidth(remainingWidth, idColFactor, nameColFactor, descColFactor, versionColFactor);
        int nameColWidth = getColWidth(remainingWidth, idColFactor, nameColFactor, descColFactor, versionColFactor);
        int descColWidth = getColWidth(remainingWidth, idColFactor, descColFactor, nameColFactor, versionColFactor);
        int versionColWidth = getColWidth(remainingWidth, idColFactor, versionColFactor, nameColFactor, descColFactor);
        int minDescColWidth = 60;

        String[] columnNames;
        int[] columnWidths;
        if (descColWidth >= minDescColWidth) {
            columnNames = new String[]{"ID", "PACKAGE", "DESCRIPTION", "AUTHOR", "DATE", "VERSION"};
            columnWidths = new int[]{ idColWidth, nameColWidth, descColWidth - authorsColWidth, authorsColWidth,
                    dateColWidth, versionColWidth};
        } else {
            columnNames = new String[]{"ID", "PACKAGE", "DESCRIPTION", "DATE", "VERSION"};
            columnWidths = new int[]{idColWidth, nameColWidth, descColWidth, dateColWidth, versionColWidth};
        }
        printBallerinaCentralTitle();
        printPackageSearchTableHeader(columnNames, columnWidths);

        for (Tool tool : tools) {
            printInCLI("|" + tool.getBalToolId(), idColWidth);
            printTool(tool, dateColWidth, versionColWidth, authorsColWidth, nameColWidth, descColWidth,
                    minDescColWidth);
            outStream.println();
        }
        outStream.println();
        outStream.println(tools.size() + " tools found.");
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
        printInCLI(aPackage.getOrganization() + "/" + aPackage.getName(), nameColWidth);

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
     * Print tool row.
     *
     * @param tool        tool information
     * @param dateColWidth    date column width
     * @param versionColWidth version column width
     * @param nameColWidth    name column width
     * @param descColWidth    description column width
     * @param minDescColWidth minimum description column width
     * @param authorsColWidth authors column width
     */
    private static void printTool(Tool tool, int dateColWidth, int versionColWidth, int authorsColWidth,
                                     int nameColWidth, int descColWidth, int minDescColWidth) {
        printInCLI(tool.getOrganization() + "/" + tool.getName(), nameColWidth);

        String summary = getSummary(tool);

        if (descColWidth >= minDescColWidth) {
            printInCLI(summary, descColWidth - authorsColWidth);
            String authors = "";
            List<String> authorsArr = tool.getAuthors();

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

        printInCLI(getDateCreated(tool.getCreatedDate()), dateColWidth);
        printInCLI(tool.getVersion(), versionColWidth);
    }

    /**
     * Print the ballerina central tile.
     */
    private static void printBallerinaCentralTitle() {
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
     * Print table header for the package search.
     *
     * @param columnNames    array of column names
     * @param columnWidths    array of column widths
     */
    private static void printPackageSearchTableHeader(String[] columnNames, int[] columnWidths) {
        printHeadingRow(columnNames, columnWidths);
        printDashRow(columnWidths);
    }

    /**
     * Print heading row of the table header.
     *
     * @param columnNames    array of column names
     * @param columnWidths    array of column widths
     */
    private static void printHeadingRow(String[] columnNames, int[] columnWidths) {
        printInCLI("|" + columnNames[0], columnWidths[0]);
        for (int i = 1; i < columnNames.length; i++) {
            printInCLI(columnNames[i], columnWidths[i]);
        }
        outStream.println();
    }

    /**
     * Print dash row of the table header.
     *
     * @param columnWidths  array of column widths
     */
    private static void printDashRow(int[] columnWidths) {
        printCharacter("|-", columnWidths[0], "-", true);
        for (int i = 1; i < columnWidths.length; i++) {
            printCharacter("-", columnWidths[i], "-", true);
        }
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

    private static String getSummary(Tool tool) {
        String summary = tool.getSummary();
        if (summary == null) {
            String readme = tool.getReadme();
            if (readme == null) {
                return "";
            }
            summary = readme.substring(0, readme.indexOf('\n'));
        }
        return summary;
    }

    private static int getColWidth(double remainingWidth, double colFactor, double... otherColFactors) {
        return (int) Math.round(remainingWidth * (colFactor / (colFactor + Arrays.stream(otherColFactors).sum())));
    }
}
