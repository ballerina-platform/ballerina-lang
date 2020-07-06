/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.testutils;

import org.apache.commons.text.diff.CommandVisitor;
import org.apache.commons.text.diff.StringsComparator;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Generate HTML test Report.
 *
 * @since 2.0.0
 */

public class GenerateHtmlReportForTest {
    private static final String CSS_FILE_NAME = "css/style.css";
    private static final String JS_FILE_NAME = "js/report.js";

    public static String getHead(String className) {
        String head = "<head>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\n" +
                "<meta http-equiv=\"x-ua-compatible\" content=\"IE=edge\"/>\n" +
                "<title>Test results " + className + "</title>\n" +
                "<link href=\"" + CSS_FILE_NAME + "\" rel=\"stylesheet\" type=\"text/css\"/>\n" +
                "<script src=\"" + JS_FILE_NAME + "\" type=\"text/javascript\"></script>\n" +
                "</head>\n";
        return head;
    }

    public static void generateReport(String fileName, ArrayList<ReportDetails> records) {
        String reportHtml = generateHtml(records);
        try (PrintWriter writer = new PrintWriter("build/reportClass/" + fileName + ".html",
                StandardCharsets.UTF_8.name())) {
            writer.append(reportHtml);
        } catch (IOException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    public static String generateHtml(ArrayList<ReportDetails> records) {

        StringBuilder writer = new StringBuilder();
        writer
                .append("<html>\n")
                .append(getHead(""))
                .append("<body>\n").append(" <div id=\"content\">\n")
                .append("<h1>" + records.get(0).getClassName() + "</h1>\n")
                .append("<div id=\"tabs\">\n")
                .append("<ul class=\"tabLinks\">\n")
                .append("<li>\n")
                .append("<a href=\"#tab0\">Tests</a>\n")
                .append("</li>\n")
                .append("<li>\n")
                .append("<a href=\"#tab1\">Failed tests</a>\n")
                .append("</li>\n")
                .append("</ul>\n")
                .append("<div id=\"tab0\" class=\"tab\">\n")
                .append("<h2>Tests</h2>\n")
                .append("<table>\n")
                .append("<thead>\n")
                .append("<tr>\n")
                .append("<th>Test</th>\n")
                .append("<th>Duration(ms)</th>\n")
                .append("<th>Result</th>\n")
                .append("</tr>\n")
                .append("</thead>\n");
        for (ReportDetails reportDetails : records) {
            writer
                    .append("<tr>\n")
                    .append("<td class=\"success\">" + reportDetails.getFunctionName() + "</td>\n")
                    .append("<td class=\"success\">" + reportDetails.getExecutionTime() + "</td>\n");
            if (reportDetails.getTestStatus().equals("passed")) {
                writer
                        .append("<td class=\"success\">" + reportDetails.getTestStatus() + "</td>\n");
            } else {
                writer
                        .append("<td class=\"success\" style=\"color: #b60808;\">" +
                                reportDetails.getTestStatus() + "</td>\n");
            }
            writer
                    .append("</tr>\n");
        }
        writer
                .append("</table>\n")
                .append("</div>\n")
                .append("<div id=\"tab1\" class=\"tab\">\n")
                .append("<h2>Failed tests</h2>\n")
                .append("<div class=\"test\">\n");
        for (ReportDetails reportDetails : records) {
            if (reportDetails.getTestStatus().equals("failed")) {
                writer
                        .append("<h3 class=\"failures\">" + reportDetails.getFunctionName() + "</h3>\n")
                        .append("<div style=\"width:100%;overflow:auto;background-color: #f7f7f7\" >\n")
                        .append("<pre>" + getDiff(reportDetails.getStackTrace()) + "</pre>\n")
                        .append("</div>\n");
            }
        }
        writer
                .append("</div>\n")
                .append("</div>\n")
                .append("</div>\n")
                .append("</div>\n")
                .append("</body>\n</html>");
        return writer.toString();
    }

    public static String getDiff(String message) {
        if (message.contains("expected") && message.contains("but found")) {
            int i1 = message.indexOf("expected: ");
            int i2 = message.indexOf(" but found: ");
            String str1 = message.substring(i1 + 10, i2);
            String str2 = "";
            if (message.contains("\n")) {
                str2 = message.substring(i2 + 12, message.indexOf("\n"));
            } else {
                str2 = message.substring(i2 + 12);
            }
            StringsComparator comparator = new StringsComparator(str1, str2);
            TextDiffHighlighter textDiffHighlighter = new TextDiffHighlighter();
            comparator.getScript().visit(textDiffHighlighter);
            message = message.replace(str1, textDiffHighlighter.leftStr);
            message = message.replace(str2, textDiffHighlighter.rightStr);
        }
        return message;
    }
}

class TextDiffHighlighter implements CommandVisitor<Character> {

    String leftStr = "";
    String rightStr = "";

    @Override
    public void visitKeepCommand(Character c) {
        // Character is present in both files.
        // ignore xml tags.
        if (c == '<') {
            leftStr = leftStr + "&lt;";
            rightStr = rightStr + "&lt;";
        } else {
            leftStr = leftStr + c;
            rightStr = rightStr + c;
        }
    }

    @Override
    public void visitInsertCommand(Character c) {
        rightStr = rightStr + "<span style = \"background-color: #7CFC00;\" >" + c + "</span>";
    }

    @Override
    public void visitDeleteCommand(Character c) {
        leftStr = leftStr + "<span style = \"background-color: #FA8072;\" >" + c + "</span>";
    }
}
