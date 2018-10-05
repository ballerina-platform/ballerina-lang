class WebViewRPCHandler {

    constructor(methods) {
        this._sequence = 0;
        this._callbacks = {};
        this.methods = methods || [];
        this._onRemoteMessage = this._onRemoteMessage.bind(this);
        window.addEventListener('message', this._onRemoteMessage);
    }

    _onRemoteMessage(evt) {
        const msg = evt.data;
        if (msg.id) {
            const methodName = msg.methodName;
            // this is a request from remote
            const method = this.methods.find(method => method.methodName === methodName);
            if (method) {
                method.handler(msg.arguments)
                    .then((response) => {
                        vscode.postMessage({
                            originId: msg.id,
                            response: response
                        });
                    });
            }
        } else if (msg.originId) {
            // this is a response from remote
            const seqId = msg.originId;
            if (this._callbacks[seqId]) {
                this._callbacks[seqId](msg.response);
                delete this._callbacks[seqId];
            }
        }
    }

    invokeRemoteMethod(methodName, args, onReply = () => {}) {
        const msg = {
            id: this._sequence,
            methodName: methodName,
            arguments: args,
        }
        this._callbacks[this._sequence] = onReply;
        vscode.postMessage(msg);
        this._sequence++;
    }

    dispose() {
        window.removeEventListener('message', this._onRemoteMessage);
    }
}

var webViewRPCHandler = new WebViewRPCHandler([]);

var vscode = acquireVsCodeApi();