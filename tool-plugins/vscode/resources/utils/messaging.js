class WebViewRPCHandler {

    constructor(methods) {
        this._sequence = 1;
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
                            response: JSON.stringify(response)
                        });
                    });
            }
        } else if (msg.originId) {
            // this is a response from remote
            const seqId = msg.originId;
            if (this._callbacks[seqId]) {
                this._callbacks[seqId](JSON.parse(msg.response));
                delete this._callbacks[seqId];
            }
        }
    }

    addMethod(methodName, handler = () => {}) {
        this.methods.push({ methodName, handler });
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


function getLangClient() {
    return {
        isInitialized: true,
        getProjectAST: (params) => {
            return new Promise((resolve, reject) => {
                webViewRPCHandler.invokeRemoteMethod('getProjectAST', [params.sourceRoot], (resp) => {
                    resolve(resp);
                });
            });
        },
        getAST: (params) => {
            return new Promise((resolve, reject) => {
                webViewRPCHandler.invokeRemoteMethod('getAST', [params.documentIdentifier.uri], (resp) => {
                    resolve(resp);
                });
            });
        },
        astDidChange: (params) => {
            return new Promise((resolve, reject) => {
                webViewRPCHandler.invokeRemoteMethod('astDidChange', [params.ast, params.textDocumentIdentifier.uri], (resp) => {
                    resolve(resp);
                });
            })
        },
        getEndpoints: () => {
            return new Promise((resolve, reject) => {
                webViewRPCHandler.invokeRemoteMethod('getEndpoints', [], (resp) => {
                    resolve(resp);
                });
            })
        },
        parseFragment: (fragment) => {
            return new Promise((resolve, reject) => {
                webViewRPCHandler.invokeRemoteMethod('parseFragment', [fragment], (resp) => {
                    resolve(resp);
                });
            })
        },
        revealRange: (params) => {
            if (params) {
                return new Promise((resolve, reject) => {
                    webViewRPCHandler.invokeRemoteMethod(
                        'revealRange', 
                        [JSON.stringify(params)], 
                        (resp) => {
                            resolve(resp);
                        }
                    );
                })
            }
        },
        goToSource: (params) => {
            return new Promise((resolve, reject) => {
                webViewRPCHandler.invokeRemoteMethod(
                    'goToSource', 
                    [JSON.stringify(params)], 
                    (resp) => {
                        resolve(resp);
                    }
                );
            })
        },
        getExamples: () => {
            return new Promise((resolve, reject) => {
                webViewRPCHandler.invokeRemoteMethod('getExamples', [], (resp) => {
                    resolve(resp.samples);
                });
            })
        },
        getDefinitionPosition: (params) => {
            return new Promise((resolve, reject) => {
                webViewRPCHandler.invokeRemoteMethod('getDefinitionPosition', [params], (resp) => {
                    resolve(resp);
                });
            })
        }
    }
}