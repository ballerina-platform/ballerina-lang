import { createElement } from "react";
import ReactDOM from "react-dom";
// tslint:disable-next-line:no-submodule-imports
import { renderToStaticMarkup } from "react-dom/server";
import { Diagram, DiagramProps, EdiatableDiagramProps,
    EditableDiagram } from "./diagram";

export interface EditableDiagramArgs {
    target: HTMLElement;
    editorProps: EdiatableDiagramProps;
}

export interface StaticDiagramArgs {
    target: HTMLElement;
    diagramProps: DiagramProps;
}

export const renderDiagramEditor = (args: EditableDiagramArgs) => {
    const EditableDiagramElement = createElement(EditableDiagram, args.editorProps);
    return ReactDOM.render(EditableDiagramElement, args.target);
};

export const renderStaticDiagram = (args: StaticDiagramArgs) => {
    const BalDiagramElement = createElement(Diagram, args.diagramProps);
    // render static markup to target element
    args.target.innerHTML = renderToStaticMarkup(BalDiagramElement);
};
