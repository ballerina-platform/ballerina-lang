import { ASTKindChecker, Block, CompilationUnit, Function, If, Visitor, While, Foreach } from "@ballerina/ast-model";
import { DiagramConfig } from "../config/default";
import { DiagramUtils } from "../diagram/diagram-utils";
import { CompilationUnitViewState, FunctionViewState, ViewState } from "../view-model/index";

const config: DiagramConfig = DiagramUtils.getConfig();

export const visitor: Visitor = {

    beginVisitCompilationUnit(node: CompilationUnit) {
        const viewState: CompilationUnitViewState = node.viewState;

        // filter out visible children from top level nodes.
        const visibleChildren = node.topLevelNodes.filter((child) => {
            return ASTKindChecker.isFunction(child) || ASTKindChecker.isService(child);
        });

        let width = 0;
        // Set the height of the toplevel nodes so that the other nodes would be positioned relative to it
        let height = config.canvas.padding.top;
        visibleChildren.forEach((child, index) => {
            // we will get the maximum node's width.
            if (width <= child.viewState.bBox.w) {
                width = child.viewState.bBox.w;
            }
            // for each top level node set x and y.
            child.viewState.bBox.x = config.panel.gutter.h;
            // const hasTopMargin = (index > 0) ? 1 : 0; // this is to remove top margin of the first panel.
            child.viewState.bBox.y = height;
            height = config.panel.gutter.v + child.viewState.bBox.h + height;
        });
        // add the padding for the width
        width += (config.panel.gutter.h * 2);
        // add the bottom padding to the canvas height.
        height += (config.panel.gutter.v * 2);
        // if  the view port is taller we take the height of the view port.
        height = (height > viewState.container.h) ? height : viewState.container.h;

        // re-adjust the width of each node to fill the container.
        if (width < viewState.container.w) {
            width = viewState.container.w;
        }
        // set all the children to same width.
        visibleChildren.forEach((child) => {
            child.viewState.bBox.w = width - (config.panel.gutter.h * 2);
        });

        viewState.bBox.h = height;
        viewState.bBox.w = width;

    },

    // tslint:disable-next-line:ban-types
    beginVisitFunction(node: Function) {
        const viewState: FunctionViewState = node.viewState;

        // Position the header
        viewState.header.x = viewState.bBox.x;
        viewState.header.y = viewState.bBox.y;
        // Position the body
        viewState.body.x = viewState.bBox.x;
        viewState.body.y = viewState.header.y + viewState.header.h;
        // Position client line
        viewState.client.x = viewState.body.x + config.panel.padding.left;
        viewState.client.y = viewState.body.y + config.panel.padding.top;
        // Position default worker
        viewState.defaultWorker.x = viewState.client.x + viewState.client.w + config.lifeLine.gutter.h;
        viewState.defaultWorker.y = viewState.client.y;
        // Position drop down menu for adding workers and endpoints
        viewState.menu.x = viewState.defaultWorker.x + viewState.defaultWorker.w + config.lifeLine.gutter.h;
        viewState.menu.y = viewState.defaultWorker.y;

        // Position the body block node
        if (node.body) {
            const bodyViewState: ViewState = node.body.viewState;
            bodyViewState.bBox.x = viewState.defaultWorker.x + viewState.defaultWorker.leftMargin;
            bodyViewState.bBox.y = viewState.defaultWorker.y + config.lifeLine.header.height;
        }

        // Update the width of children
        viewState.body.w = viewState.bBox.w;
        viewState.header.w = viewState.bBox.w;
    },

    beginVisitBlock(node: Block) {
        const viewState: ViewState = node.viewState;
        let height = 0;
        node.statements.forEach((element) => {
            element.viewState.bBox.x = viewState.bBox.x;
            element.viewState.bBox.y = viewState.bBox.y + height;
            height += element.viewState.bBox.h;
        });
    },

    beginVisitWhile(node: While) {
        const viewState: ViewState = node.viewState;
        node.body.viewState.bBox.x = viewState.bBox.x;
        node.body.viewState.bBox.y = viewState.bBox.y + config.flowCtrl.header.height;
    },

    beginVisitForeach(node: Foreach) {
        const viewState: ViewState = node.viewState;
        node.body.viewState.bBox.x = viewState.bBox.x;
        node.body.viewState.bBox.y = viewState.bBox.y + config.flowCtrl.header.height;
    },

    beginVisitIf(node: If) {
        const viewState: ViewState = node.viewState;
        node.body.viewState.bBox.x = viewState.bBox.x;
        node.body.viewState.bBox.y = viewState.bBox.y + config.flowCtrl.header.height;

        if (node.elseStatement) {
            node.elseStatement.viewState.bBox.x = viewState.bBox.x + node.body.viewState.bBox.w;
            node.elseStatement.viewState.bBox.y = viewState.bBox.y +
                config.flowCtrl.header.height + node.body.viewState.bBox.h;
        }
    }
};
