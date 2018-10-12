import { Uri, ExtensionContext, Webview } from "vscode";
import { join } from "path";

export function getWebViewResourceRoot(context: ExtensionContext): Uri {
    return Uri.file(join(context.extensionPath, 'resources')).with({ scheme: 'vscode-resource' });
}

export function getLibraryWebViewContent(context: ExtensionContext,
        body: string, scripts: string, styles: string) {
    const resourceRoot = getWebViewResourceRoot(context).toString();
    const diagramResourceRoot = process.env.DIAGRAM_DEBUG === "true" 
                ? process.env.DIAGRAM_DEV_HOST
                : `${resourceRoot}/diagram`;
    return `
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="${diagramResourceRoot}/bundle.css" />
        <link rel="stylesheet" href="${diagramResourceRoot}/theme.css" />
        <link rel="stylesheet" href="${diagramResourceRoot}/less.css" />
        ${process.env.DIAGRAM_DEBUG === "true" ? '<script src="http://localhost:8097"></script>' : ''}
        <style>
            ${styles}
        </style>
    </head>
    
    <body style="overflow-y: scroll;">
        ${body}
        <script charset="UTF-8" src="${resourceRoot}/utils/messaging.js"></script>
        <script charset="UTF-8" src="${diagramResourceRoot}/ballerina-diagram-library.js"></script>
        <script>
            ${scripts}
        </script>
    </body>
    </html>
    `;
}
export interface WebViewMethodHandler {
    (args: any[]) : Thenable<any>;
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

export class WebViewRPCHandler {

    private _sequence: number = 0;
    private _callbacks: Map<number, Function> = new Map();

    constructor(public methods: Array<WebViewMethod>, public webView: Webview){
        webView.onDidReceiveMessage(this._onRemoteMessage.bind(this));
    }

    private _getMethod(methodName: string) {
        return this.methods.find(method => method.methodName === methodName);
    }
    
    private _onRemoteMessage(msg: WebViewRPCMessage) {
        if (msg.id) {
            // this is a request from remote
            const method = this._getMethod(msg.methodName);
            if (method) {
                method.handler(msg.arguments || [])
                    .then((response) => {
                        this.webView.postMessage({
                            originId: msg.id,
                            response,
                        });
                    });
            }
        } else if (msg.originId) {
            // this is a response from remote to one of our requests
            const callback = this._callbacks.get(msg.originId);
            if (callback) {
                callback(msg.response);
                this._callbacks.delete(msg.originId);
            }
        }
    }

    invokeRemoteMethod(methodName: string, args: any[] = [], callback: Function) {
        const msg = {
            id: this._sequence,
            methodName,
            arguments: args,
        };
        this._callbacks.set(this._sequence, callback);
        this.webView.postMessage(msg);
        this._sequence++;
    }

    static create(methods: Array<WebViewMethod>, webView: Webview) : WebViewRPCHandler {
        return new WebViewRPCHandler(methods, webView);
    }

    dispose() {
        // TODO unregister event handlers
    }
}