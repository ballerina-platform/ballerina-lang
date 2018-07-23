/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { LanguageGrammarDefinitionContribution, TextmateRegistry } from "@theia/monaco/lib/browser/textmate";

import { injectable } from "inversify";
import { BALLERINA_LANGUAGE_ID } from '../common';

@injectable()
export class BallerinaGrammarContribution implements LanguageGrammarDefinitionContribution {

    readonly config: monaco.languages.LanguageConfiguration = {
		comments: {
			lineComment: '//',
			blockComment: ['/*', '*/'],
		},
		brackets: [
			['{', '}'],
			['[', ']'],
			['(', ')']
		],
		autoClosingPairs: [
			{ open: '{', close: '}' },
			{ open: '[', close: ']' },
			{ open: '(', close: ')' },
			{ open: '`', close: '`', notIn: ['string'] },
			{ open: '"', close: '"', notIn: ['string'] },
			{ open: '\'', close: '\'', notIn: ['string', 'comment'] },
		],
		surroundingPairs: [
			{ open: '{', close: '}' },
			{ open: '[', close: ']' },
			{ open: '(', close: ')' },
			{ open: '`', close: '`' },
			{ open: '"', close: '"' },
			{ open: '\'', close: '\'' },
		]
	};

	registerTextmateLanguage(registry: TextmateRegistry) {
        monaco.languages.register({
            id: BALLERINA_LANGUAGE_ID,
            extensions: ['.bal'],
            aliases: ['Ballerina', 'ballerina']
        });

        monaco.languages.setLanguageConfiguration(BALLERINA_LANGUAGE_ID, this.config);

        const balGrammar = require('raw-loader!../../../../vscode/plugin/ballerina-grammar/syntaxes/ballerina.tmLanguage');
        registry.registerTextMateGrammarScope('source.ballerina', {
            async getGrammarDefinition() {
                return {
                    format: 'plist',
                    content: balGrammar
                };
            }
        });
        registry.mapLanguageIdToTextmateGrammar(BALLERINA_LANGUAGE_ID, 'source.ballerina');
    }
}