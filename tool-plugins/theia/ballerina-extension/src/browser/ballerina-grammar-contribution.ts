import { LanguageGrammarDefinitionContribution, TextmateRegistry } from "@theia/monaco/lib/browser/textmate";

import { injectable } from "inversify";
import { BALLERINA_LANGUAGE_ID } from '../common';

@injectable()
export class BallerinaGrammarContribution implements LanguageGrammarDefinitionContribution {

    readonly config: monaco.languages.LanguageConfiguration = {
		comments: {
			// symbol used for single line comment. Remove this entry if your language does not support line comments
			lineComment: "//"
		},
		// symbols used as brackets
		brackets: [
			["{", "}"],
			["[", "]"],
			["(", ")"]
		],
		// symbols that are auto closed when typing
		autoClosingPairs: [
			{ open: "{", close: "}" },
			{ open: "[", close: "]" },
			{ open: "(", close: ")" },
			{ open: "\\", close: "\\" }
		],
		// symbols that that can be used to surround a selection
		surroundingPairs: [
			{ open: "{", close: "}" },
			{ open: "[", close: "]" },
			{ open: "(", close: ")" },
			{ open: "\\", close: "\\" }
		]
	}

	registerTextmateLanguage(registry: TextmateRegistry) {
        monaco.languages.register({
            id: BALLERINA_LANGUAGE_ID,
            extensions: ['.bal'],
            aliases: ['Ballerina', 'ballerina']
        });

        monaco.languages.setLanguageConfiguration(BALLERINA_LANGUAGE_ID, this.config);

        const balGrammar = require('raw-loader!../../../resources/ballerina.tmLanguage');
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