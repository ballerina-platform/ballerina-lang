import { Base64 } from 'js-base64';
import { HighlightToken } from './highlight-token';
import { Range, window, TextEditorDecorationType } from 'vscode';

function decodeTokens(element: { line: number, token: string }): HighlightToken[] {
    const tokenArray: HighlightToken[] = [];
    let decodedText = Base64.atob(element.token);
    let decodedArray: number[] = JSON.parse(decodedText);

    for (let index = 0; index < decodedArray.length; index = index + 3) {
        let range: Range = new Range(element.line, decodedArray[index], element.line, decodedArray[index] + decodedArray[index + 1]);
        let scope = decodedArray[index + 2];
        tokenArray.push({ scope, range });
    }
    return tokenArray;
}

function highlightLines(highlightingInfo: { line: number, token: string }[]): { [scope: number]: Range[]; } {
    let highlights: HighlightToken[] = [];
    let highlightByScope: { [scope: number]: Range[]; } = {};

    highlightingInfo.forEach(element => {
        highlights.push(...decodeTokens(element));
    });

    highlights.forEach(element => {
        if (!highlightByScope[element.scope]) { highlightByScope[element.scope] = [element.range]; }
        else { highlightByScope[element.scope].push(element.range); }
    });

    return highlightByScope;
}

export function setEditorDecorations(highlightingInfo: { line: number, token: string }[]) {
    const scopeObj = highlightLines(highlightingInfo);
    const decorationType1 = window.createTextEditorDecorationType({
        backgroundColor: 'blue',
        color: 'red',
        fontStyle: 'bold'
    });
    const decorationType2 = window.createTextEditorDecorationType({
        backgroundColor: 'red',
        color: 'blue',
        fontStyle: 'bold'
    });
    const decorationTypes: TextEditorDecorationType[] = [decorationType1, decorationType2];

    let activeEditor = window.activeTextEditor;
    if (!activeEditor) { return; }
    for (let index = 0; index < decorationTypes.length; index++) {
        activeEditor.setDecorations(decorationTypes[index], scopeObj[index]);
    }
}