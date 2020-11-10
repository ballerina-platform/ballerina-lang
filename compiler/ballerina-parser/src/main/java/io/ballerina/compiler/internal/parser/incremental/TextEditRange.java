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
package io.ballerina.compiler.internal.parser.incremental;

/**
 * Represents a text range in the old source code that has been
 * modified in the new source code.
 *
 * @since 1.3.0
 */
class TextEditRange {
    final int oldStartOffset;
    final int oldEndOffset;
    final int oldLength;
    final int newTextLength;

    TextEditRange(int oldStartOffset, int oldEndOffset, int newTextLength) {
        this.oldStartOffset = oldStartOffset;
        this.oldEndOffset = oldEndOffset;
        this.oldLength = oldEndOffset - oldStartOffset;
        this.newTextLength = newTextLength;
    }
}
