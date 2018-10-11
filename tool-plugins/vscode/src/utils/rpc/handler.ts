import { WebViewMethod, WebViewRPCMessage } from './model';
import { Webview } from 'vscode';

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