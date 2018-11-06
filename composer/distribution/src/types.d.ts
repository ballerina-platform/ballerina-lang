declare module '@ballerina/documentation' {
    export function renderDocPreview(ast: any, el: any): void;
}

declare module '@ballerina/diagram' {
    export interface BallerinaDiagramWrapperProps {
        getAST: (arg: any) => any;
        onChange: (arg: any) => any;
        docUri: string;
        width: number;
        height: number;
        getEndpoints: (arg: any) => any;
        parseFragment: (arg: any) => any;
        goToSource: (arg: any) => any;
    }
    export function renderStaticDiagram(args: any, callback: any) : void;
    export function renderEditableDiagram(target: HTMLElement | null,
        docUri: string, width: number, height: number, getAST: Function) : void;
    export class BallerinaDiagramWrapper extends React.Component<BallerinaDiagramWrapperProps> {
    }
}