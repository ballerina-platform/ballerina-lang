/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import _ from 'lodash';

class SourceGenUtil {

    static getTailingIndentation(content) {
        // get the array of chars from content
        const charArray = content.split('');
        // capture all chars from end, upto the last non-ws char (excluding it)
        const charsTillLastNonWSChar = _.takeRightWhile(charArray,
          charAtIndex => !(/\S/g.test(charAtIndex)));
        // out of found tailing whiteSpace, find whiteSpace upto last new line char
        // or if there is no new line chars, get upto last non WS
        // or empty if there is no any tailing whiteSpace at all
        return _.last(_.split(_.join(charsTillLastNonWSChar, ''), '\n'));
    }

    static replaceTailingIndentation(content, newIndentation) {
        let newContent = '';
        // get the array of chars from content
        const charArray = content.split('');
        let lastNonWSCharIndex = -1;
        let lastNewLineCharIndex = -1;
        // iterate through current chars from end of the array
        for (let index = charArray.length - 1; index >= 0; index--) {
            // if current char is a new line & we haven't still found last new line char
            // withing tailing whiteSpace
            if (_.isEqual(lastNewLineCharIndex, -1) && _.isEqual(_.nth(charArray, index), '\n')) {
                lastNewLineCharIndex = index;
            }
            // capture the index of last non-ws & char and break the loop since we
            // are only interested in tailing ws
            if (/\S/g.test(_.nth(charArray, index))) {
                lastNonWSCharIndex = index;
                break;
            }
        }
        // We found a new line char within the tailing whiteSpace of content
        // so remove tailing ws upto last new line
        if (lastNewLineCharIndex > -1) {
            charArray.splice(lastNewLineCharIndex + 1,
                charArray.length - (lastNewLineCharIndex + 1));
        } else if (lastNonWSCharIndex > -1) {
            // We did not find a new line char in tailing whitespace,
            // yet we found some tailing whitespace - so remove them
            charArray.splice(lastNonWSCharIndex + 1,
                charArray.length - (lastNonWSCharIndex + 1));
        } else {
            // there is no tailing whitespace in exisiting content
        }
        // get the remaining part of existing content & append new indentation
        newContent = _.join(charArray, '') + newIndentation;
        return newContent;
    }
}

export default SourceGenUtil;
