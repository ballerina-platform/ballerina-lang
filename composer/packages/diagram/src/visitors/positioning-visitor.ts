import { ASTKindChecker, CompilationUnit, Visitor} from "@ballerina/ast-model";
import { DiagramConfig } from "../config/default";
import { DiagramUtils } from "../diagram/diagram-utils";
import { ViewState } from "../view-model/index";

const config: DiagramConfig = DiagramUtils.getConfig();

export const visitor: Visitor = {

    beginVisitCompilationUnit(node: CompilationUnit) {
        const viewState: ViewState = node.viewState;

        const visibleChildren = node.topLevelNodes.filter((child) => {
            return ASTKindChecker.isFunction(child) || ASTKindChecker.isService(child);
        });

        let width = 0;
        // Set the height of the toplevel nodes so that the other nodes would be positioned relative to it
        let height = config.canvas.padding.top;
        // filter out visible children from top level nodes.

        visibleChildren.forEach((child) => {
            // we will get the maximum node's width.
            if (width <= child.viewState.bBox.w) {
                width = child.viewState.bBox.w;
            }
            // for each top level node set x and y.
            child.viewState.bBox.x = config.panel.margin.left;
            child.viewState.bBox.y = height + config.panel.margin.top;
            height = config.panel.margin.top + config.panel.margin.bottom + child.viewState.bBox.h + height;
        });
        // add the padding for the width
        width += (config.panel.margin.left + config.panel.margin.right);
        // add the bottom padding to the canvas height.
        height += (config.panel.margin.top + config.panel.margin.bottom);
        // if  the view port is taller we take the height of the view port.
        // @TODO height = (height > viewState.container.height) ? height : viewState.container.height;

        // re-adjust the width of each node to fill the container.
        if (width < viewState.bBox.w) {
            width = viewState.bBox.w;
        }
        // set all the children to same width.
        visibleChildren.forEach((child) => {
            child.viewState.bBox.w = width - (config.panel.margin.left + config.panel.margin.right);
        });

        viewState.bBox.h = height;
        viewState.bBox.w = width;

    }
};
