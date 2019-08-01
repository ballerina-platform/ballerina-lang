import { WebviewPanel } from "vscode";

export interface WebViewMethodHandler {
    (args: any[], webviewPanel: WebviewPanel) : Thenable<any> | void | any;
}

export interface WebViewMethod {
    methodName: string;
    handler: WebViewMethodHandler;
}

export interface WebViewRPCMessage {
    id?: number;
    methodName?: any;
    arguments?: any[];
    originId?: number;
    response?: any;
}