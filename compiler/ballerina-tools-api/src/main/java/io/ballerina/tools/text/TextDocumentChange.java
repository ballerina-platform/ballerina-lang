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
package io.ballerina.tools.text;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Represents a textual changes on a single {@code TextDocument}.
 *
 * @since 1.3.0
 */
public class TextDocumentChange {
    private final TextEdit[] textEdits;

    private TextDocumentChange(TextEdit[] textEdits) {
        this.textEdits = Arrays.copyOf(textEdits, textEdits.length);
    }

    public static TextDocumentChange from(TextEdit[] textEdits) {
        return new TextDocumentChange(textEdits);
    }

    public int getTextEditCount() {
        return textEdits.length;
    }

    public TextEdit getTextEdit(int index) {
        return textEdits[index];
    }

    public String toString() {
        StringJoiner sj = new StringJoiner(",");
        for (TextEdit textEdit : textEdits) {
            sj.add(textEdit.toString());
        }
        return sj.toString();
    }
}
