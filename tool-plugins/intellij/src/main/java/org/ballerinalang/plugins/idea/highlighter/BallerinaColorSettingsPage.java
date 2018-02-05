/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.highlighter;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.ballerinalang.plugins.idea.BallerinaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import javax.swing.Icon;

public class BallerinaColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            //todo: add more attributes
            new AttributesDescriptor("Keywords", BallerinaSyntaxHighlightingColors.KEYWORD),
            new AttributesDescriptor("Strings", BallerinaSyntaxHighlightingColors.STRING),
            new AttributesDescriptor("Numbers", BallerinaSyntaxHighlightingColors.NUMBER),
            new AttributesDescriptor("Identifiers", BallerinaSyntaxHighlightingColors.IDENTIFIER),
            new AttributesDescriptor("Comments", BallerinaSyntaxHighlightingColors.LINE_COMMENT),
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return BallerinaIcons.ICON;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new BallerinaSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "import ballerina.lang.messages;\n" +
                "import ballerina.net.http;\n" +
                "\n" +
                "service<http> helloWorld {\n" +
                "    \n" +
                "    resource sayHello (message m) {\n" +
                "        // Creates an empty message.\n" +
                "        message response = {};\n" +
                "        // A util method that can be used to set string payload.\n" +
                "        messages:setStringPayload(response, \"Hello, World!\");\n" +
                "        // Reply keyword sends the response back to the client.\n" +
                "        reply response;\n" +
                "    }\n" +
                "}\n";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Ballerina";
    }
}
