/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jvm.values;

import org.apache.axiom.om.OMText;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.api.BString;

/**
 * <p>
 * {@link XMLIterator} private iteration providers for ballerina xml.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 * 
 * @since 0.995.0
 */
public class XMLIterator {

    enum IterMode {
        SEQUENCE, CODE_POINT
    }

    /**
     * {@code {@link ItemIterator}} provides iterator for xml items.
     *
     * @since 0.995.0
     */
    static class ItemIterator implements IteratorValue {

        XMLItem value;
        int cursor = 0;
        CodePointIterator codePointIterator;

        ItemIterator(XMLItem bxmlItem) {
            value = bxmlItem;
        }

        @Override
        public Object next() {
            if (value.getNodeType() == XMLNodeType.TEXT) {
                if (codePointIterator == null) {
                    codePointIterator = createCodePointIterator(value);
                }
                cursor++;
                return codePointIterator.next();
            } else if (hasNext()) {
                cursor++;
                return value;
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            if (value.getNodeType() == XMLNodeType.TEXT) {
                if (codePointIterator == null) {
                    codePointIterator = createCodePointIterator(value);
                }
                return codePointIterator.hasNext();
            }
            return cursor == 0;
        }

        private CodePointIterator createCodePointIterator(XMLItem value) {
            return new CodePointIterator(((OMText) value.omNode).getText());
        }

        @Override
        public StringValue bStringValue() {
            return null;
        }
    }

    /**
     * {@code {@link SequenceIterator }} provides iterator for an XML sequence.
     *
     * @since 0.995.0
     */
    static class SequenceIterator implements IteratorValue {

        XMLSequence value;
        int cursor = 0;
        IterMode iterMode = IterMode.SEQUENCE;
        CodePointIterator codePointIterator;

        SequenceIterator(XMLSequence bxmlSequence) {
            value = bxmlSequence;
        }

        @Override
        public Object next() {
            if (iterMode == IterMode.CODE_POINT) {
                if (codePointIterator.hasNext()) {
                    return codePointIterator.next();
                }

                iterMode = IterMode.SEQUENCE;
                codePointIterator = null;
            }

            Object curVal = value.sequence.getRefValue(cursor++);
            if (TypeChecker.getType(curVal).getTag() == TypeTags.XML_TAG &&
                    ((XMLItem) curVal).getNodeType() == XMLNodeType.TEXT) {
                iterMode = IterMode.CODE_POINT;
                codePointIterator = CodePointIterator.from(curVal.toString());
                return codePointIterator.next();
            }
            return curVal;
        }

        @Override
        public boolean hasNext() {
            boolean hasMoreXmlItems = cursor < value.sequence.size();
            return iterMode == IterMode.SEQUENCE ? hasMoreXmlItems : (codePointIterator.hasNext() || hasMoreXmlItems);
        }

        @Override
        public StringValue bStringValue() {
            return null;
        }
    }

    /**
     * {@link CodePointIterator} private iteration provider for XML char sequence.
     *
     * @since 0.995.0
     */
    public static class CodePointIterator implements IteratorValue {

        private String charSequence;
        private int offset;

        public CodePointIterator(String charSequence) {
            this.charSequence = charSequence;
            this.offset = 0;
        }

        static CodePointIterator from(String seq) {
            return new CodePointIterator(seq);
        }

        @Override
        public Object next() {
            int codePoint = charSequence.codePointAt(offset);
            offset += Character.charCount(codePoint);

            // Max 2 chars per code point.
            StringBuilder sb = new StringBuilder(2);
            sb.appendCodePoint(codePoint);
            return sb.toString();
        }

        @Override
        public boolean hasNext() {
            return offset < charSequence.length();
        }

        @Override
        public BString bStringValue() {
            return null;
        }
    }
}
