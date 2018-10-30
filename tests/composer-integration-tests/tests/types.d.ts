
declare module '@ballerina/distribution' {
    
    export interface BallerinaDiagramWrapperProps {
        getAST: (uri: string) => Thenable<any>;
        onChange: (t: any) => any;
        docUri: string;
        width: number;
        height: number;
        getEndpoints: (t: any) => any;
        parseFragment: (t: any) => any;
        goToSource: (t: any) => any;
    }
    export function renderStaticDiagram(args: any, callback: any) : void;
    export function renderEditableDiagram(target: HTMLElement | null,
        docUri: string, width: number, height: number, getAST: Function) : void;
    export class BallerinaDiagramWrapper extends React.Component<BallerinaDiagramWrapperProps> {
    }
}

