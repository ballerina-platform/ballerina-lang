/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import _ from 'lodash';
/**
 * Class to represent source view completer factory
 */
class CompleterFactory {
    /**
     * Constructor for completer factory
     * @constructor
     */
    constructor() {
        this.variable_dec = /([a-z])+ .*/i;
        this.package_dec = /([a-z0-9])+:.*/i;
    }

    /**
     * Return the Source view completer
     * @param {object} langserverController - language server controller
     * @return {*[]} - array
     */
    getSourceViewCompleter(langserverController, fileData) {
        return [{
            getCompletions: (editor, session, pos, prefix, callback) => {
                const cursorPosition = editor.getCursorPosition();
                const content = editor.getValue();
                this.getCompletions(cursorPosition, content, fileData, langserverController, callback);
            },
        }];
    }

    /**
     * Get the completions and call the given callback function with the completions
     * @param {object} cursorPosition
     * @param {string} content
     * @param {object} fileData
     * @param {object} langserverController
     * @param {function} callback
     */
    getCompletions(cursorPosition, content, fileData, langserverController, callback) {
        const completions = [];
        const options = {
            textDocument: content,
            position: {
                line: cursorPosition.row + 1,
                character: cursorPosition.column,
            },
            fileName: fileData.fileName,
            filePath: fileData.filePath,
            fullPath: fileData.fullPath,
            uri: fileData.toURI(),
            packageName: fileData.packageName,
        };
        langserverController.getCompletions(options, (response) => {
            const sortedArr = _.orderBy(response.result.left, ['sortText'], ['desc']);
            let score = sortedArr.length;
            sortedArr.map((completionItem) => {
                completions.push(
                    {
                        caption: completionItem.label,
                        snippet: completionItem.insertText,
                        meta: completionItem.detail,
                        score: 100 + (score || 0),
                    });
                score--;
            });
            callback(null, completions);
        });
    }

}

export default CompleterFactory;
