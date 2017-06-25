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
    static getSourceViewCompleter(langserverController) {
        return [{
            getCompletions: (editor, session, pos, prefix, callback) => {
                const completions = [{ name: 'hello1', value: 'typess.row ', meta: 'type' },
                    { name: 'hello2', value: 'typess.row ', meta: 'type' }];
                const cursorPosition = editor.getCursorPosition();
                const options = {
                    textDocument: editor.getValue(),
                    position: {
                        line: cursorPosition.row + 1,
                        character: cursorPosition.column,
                    },
                };
                langserverController.getCompletions(options, (response) => {
                    response.result.map((completionItem) => {
                        completions.push({ name: completionItem.label, value: completionItem.label, meta: 'type' });
                    });
                    callback(null, completions);
                });
            },
        }];
    }
}

export default CompleterFactory;
