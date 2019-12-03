import { Range } from "vscode";

export interface HighlightToken {
    scope: number;
    range: Range;
}