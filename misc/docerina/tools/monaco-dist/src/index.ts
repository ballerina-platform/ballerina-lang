import * as monaco from 'monaco-editor';
import { loadWASM } from 'onigasm'; // peer dependency of 'monaco-textmate'
import { Registry } from 'monaco-textmate'; // peer dependency
import { wireTmGrammars } from 'monaco-editor-textmate';

declare global {
    interface Window { MonacoEnvironment: any; }
}

export async function highlightSnippets() {
    await loadWASM(require("onigasm/lib/onigasm.wasm"));

    const registry = new Registry({
        getGrammarDefinition: async (scopeName) => {
            return {
                format: 'plist',
                content: require("./tmballerina.plist").default
            }
        }
    });
 
    monaco.languages.register({ id: "ballerina"});
    // map of monaco "language id's" to TextMate scopeNames
    const grammars = new Map();
    grammars.set('ballerina', 'source.ballerina');
 
    await wireTmGrammars(monaco, registry, grammars);
 
    const balCodeSnippets = document.querySelectorAll("code.language-ballerina");
    balCodeSnippets.forEach((snippet) => {
        if (snippet.parentElement) {
            snippet.parentElement.classList.add("ballerina-snippet");
        }
        monaco.editor.colorize(snippet.innerHTML, "ballerina", {})
            .then((coloredString) => {
                snippet.innerHTML = coloredString;
            });
    });
}