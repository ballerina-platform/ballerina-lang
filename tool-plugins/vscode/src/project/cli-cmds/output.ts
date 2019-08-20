import { window, OutputChannel } from "vscode";

const channel = window.createOutputChannel('Ballerina CLI');

export function getCLIOutputChannel(): OutputChannel {
    return channel;
}