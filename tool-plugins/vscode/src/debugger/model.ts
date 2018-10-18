import { DebugProtocol } from "vscode-debugprotocol";

export interface AttachRequestArguments extends DebugProtocol.AttachRequestArguments {
    host: string;
    port: number;
    script: string;
}

export interface LaunchRequestArguments extends DebugProtocol.LaunchRequestArguments {
    script: string;
    scriptArguments: Array<string>;
    commandOptions: Array<string>;
    'ballerina.home': string; 
}

export interface RunningInfo {
    sourceRoot? : string;
    ballerinaPackage? : string;
}

export interface ProjectConfig {
    version: string;
    'org-name': string;
}

export interface Thread {
    id: number;
    name: string;
    serverThreadId: string;
    frameIds: number[];
}

export interface Frame {
    threadId: number;
    id: number;
    scopes: Scope[];
    fileName: string;
    frameName: string;
    line: number;
}

interface Scope {
    name: string;
    variablesReference: number;
    expensive: boolean;
}

export interface VariableRef {
    threadId: number;
    variables: Array<Variable>; // TODO fix any type
}

interface Variable {
    name: string;
    type: string;
    value: any;
    variablesReference: number;
}