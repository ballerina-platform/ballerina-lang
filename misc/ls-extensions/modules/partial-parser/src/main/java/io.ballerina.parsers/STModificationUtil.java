/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.parsers;

import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocumentChange;
import io.ballerina.tools.text.TextDocuments;
import io.ballerina.tools.text.TextEdit;
import io.ballerina.tools.text.TextRange;

/**
 * Represents utils for partial ST Modify.
 *
 * @since 1.3.0
 */
public class STModificationUtil {
    private STModificationUtil() {
    }

    static String getModifiedStatement(String oldStatement, STModification stModification) {
        TextDocument oldTextDocument = TextDocuments.from(oldStatement);
        TextEdit[] textEdits = {constructEdit(oldTextDocument, stModification)};
        TextDocumentChange textDocumentChange = TextDocumentChange.from(textEdits);
        TextDocument newTextDocument = oldTextDocument.apply(textDocumentChange);
        return newTextDocument.toString();
    }

    private static TextEdit constructEdit(TextDocument oldTextDocument, STModification stModification) {
        LinePosition startLinePos = LinePosition.from(stModification.getStartLine(),
                stModification.getStartColumn());
        LinePosition endLinePos = LinePosition.from(stModification.getEndLine(),
                stModification.getEndColumn());
        int startOffset = oldTextDocument.textPositionFrom(startLinePos);
        int endOffset = oldTextDocument.textPositionFrom(endLinePos);
        return TextEdit.from(
                TextRange.from(startOffset, endOffset - startOffset), stModification.getNewCodeSnippet());
    }
}
