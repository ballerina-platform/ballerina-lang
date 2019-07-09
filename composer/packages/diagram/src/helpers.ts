import { createElement } from "react";
import ReactDOM from "react-dom";
// tslint:disable-next-line:no-submodule-imports
import { renderToStaticMarkup } from "react-dom/server";
import { Diagram, DiagramMode, DiagramProps,
    EditableDiagram, EditableDiagramProps, Overview } from "./diagram";

export interface EditableDiagramArgs {
    target: HTMLElement;
    editorProps: EditableDiagramProps;
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

export const renderOverview = (args: EditableDiagramArgs) => {
    const overviewProps = {
        fitToWidthOrHeight: true,
        langClient: args.editorProps.langClient,
        mode: DiagramMode.DEFAULT,
        sourceRoot: args.editorProps.docUri,
        zoom: 1,
    };
    const OverviewElement = createElement(Overview, overviewProps);
    return ReactDOM.render(OverviewElement, args.target);
};
