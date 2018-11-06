
declare module '@ballerina/diagram' {
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
        updateAST: (ast: any) => void;
    }
}

declare module '@ballerina/diagram/lib/plugins/ballerina/diagram/views/default/sizing-util' {
    export default class SizingUtil {
        getTextWidth: (arg1: any, arg2: any) => any;
    }
}

declare module '@ballerina/diagram/lib/plugins/ballerina/diagram/views/default/components/decorators/editable-text' {
    export default class EditableText {
        renderTextBox: () => void;
    }
}

declare module '@ballerina/diagram/lib/plugins/ballerina/views/diagram-menu' {
    export default class DiagramMenu {
        render: () => any;
    }
}