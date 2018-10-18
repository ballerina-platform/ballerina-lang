
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