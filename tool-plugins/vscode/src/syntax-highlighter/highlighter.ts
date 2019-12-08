import { Base64 } from 'js-base64';
import { HighlightToken, SemanticHighlightingInformation } from './model';
import { Range, window, TextEditorDecorationType } from 'vscode';
import { getScopeColor, getScopeName } from './scopeTree';

export class Highlighter {

    private decorationTypes: { [line: number]: TextEditorDecorationType; } = {};

    public decodeTokens(element: SemanticHighlightingInformation): HighlightToken[] {
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

    public highlightLines(highlightingInfo: SemanticHighlightingInformation): { [scope: number]: Range[]; } {
        let highlights: HighlightToken[] = [];
        let highlightByScope: { [scope: number]: Range[]; } = {};

        highlights.push(...this.decodeTokens(highlightingInfo));

        highlights.forEach(element => {
            if (!highlightByScope[element.scope]) { highlightByScope[element.scope] = [element.range]; }
            else { highlightByScope[element.scope].push(element.range); }
        });

        return highlightByScope;
    }

    public setEditorDecorations(highlightingInfo: SemanticHighlightingInformation) {
        const scopeObj = this.highlightLines(highlightingInfo);
        let activeEditor = window.activeTextEditor;
        if (!activeEditor) { return; }
        for (let key in scopeObj) {
            let decorationType = window.createTextEditorDecorationType({
                color: getScopeColor(getScopeName(Number(key)))
            });
            if (!this.decorationTypes[highlightingInfo.line]) {
                this.decorationTypes[highlightingInfo.line] = decorationType;
                activeEditor.setDecorations(decorationType, scopeObj[key]);
            }
        }
    }

    public dispose(start: number, end: number) {
        let activeEditor = window.activeTextEditor;
        if (!activeEditor) { return; }

        for (let line = start; line <= end; line++) {
            if (this.decorationTypes[line]) {
                this.decorationTypes[line].dispose();
                delete this.decorationTypes[line];
            }
        }
    }
}
