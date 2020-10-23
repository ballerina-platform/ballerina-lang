import * as monaco from 'monaco-editor';
import { wireTmGrammars } from 'monaco-editor-textmate';
import { Registry } from 'monaco-textmate'; // peer dependency
import { loadWASM } from 'onigasm'; // peer dependency of 'monaco-textmate'

declare global {
    interface Window { MonacoEnvironment: any; }
}

/**
 * Enables Syntax Highlighting for Ballerina Code snippets.
 *
 * @param selector Element selector. default is "code.language-ballerina"
 */
export async function highlightSnippets(selector = "code.language-ballerina") {
    await loadWASM(require("onigasm/lib/onigasm.wasm"));
    // tslint:disable-next-line: max-line-length
    const content = require("./../../../../misc/ballerina-grammar/syntaxes/ballerina.tmLanguage").default;
    const registry = new Registry({
        getGrammarDefinition: async (scopeName) => {
            return {
                format: "plist",
                content
            };
        }
    });

    monaco.languages.register({ id: "ballerina"});
    // map of monaco "language id's" to TextMate scopeNames
    const grammars = new Map();
    grammars.set("ballerina", "source.ballerina");

    await wireTmGrammars(monaco, registry, grammars);

    const balCodeSnippets = document.querySelectorAll(selector);
    balCodeSnippets.forEach((snippet) => {
        if (snippet.parentElement) {
            snippet.parentElement.classList.add("ballerina-snippet");
        }
        monaco.editor.colorize(snippet.textContent as string, "ballerina", {})
            .then((coloredString) => {
                snippet.innerHTML = coloredString;
            });
    });
}
