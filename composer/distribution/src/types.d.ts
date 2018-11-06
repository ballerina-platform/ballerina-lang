declare module '@ballerina/diagram' {
    export function renderStaticDiagram(args: any, callback: any) : void;
    export function renderEditableDiagram(target: HTMLElement | null,
        docUri: string, width: number, height: number, getAST: Function) : void;
}

declare module '@ballerina/documentation' {
    export function renderDocPreview(ast: any, el: any): void;
}