import {
    Assignment, ASTNode, Block, ExpressionStatement,
    Function, If, VariableDef, Visitor, While, Foreach
} from "@ballerina/ast-model";
import * as _ from "lodash";
import { DiagramConfig } from "../config/default";
import { DiagramUtils } from "../diagram/diagram-utils";
import { FunctionViewState, SimpleBBox, ViewState } from "../view-model";

// Following element is created to calculate the width of a text rendered in an svg.
// Please see getTextWidth on how we do the calculation.
const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
svg.setAttribute("style", "border: 0px");
svg.setAttribute("width", "600");
svg.setAttribute("height", "50");
svg.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xlink", "http://www.w3.org/1999/xlink");
const textElement = document.createElementNS("http://www.w3.org/2000/svg", "text");
svg.appendChild(textElement);
document.body.appendChild(svg);

const config: DiagramConfig = DiagramUtils.getConfig();

/**
 * Get width of a given text and processed text
 * considering provided min and max width.
 * @param {string} text
 * @param {number} minWidth
 * @param {number} maxWidth
 * @return {object} {width,text}
 */
/*
function getTextWidth(text: string, minWidth = config.statement.width , maxWidth = config.statement.maxWidth) {
    textElement.innerHTML = _.escape(text);
    let width = config.statement.padding.left +
        textElement.getComputedTextLength() + config.statement.padding.right;
    // if the width is more then max width crop the text
    if (width <= minWidth) {
        // set the width to minimam width
        width = minWidth;
    } else if (width > minWidth && width <= maxWidth) {
        // do nothing
    } else {
        // We need to truncate displayText and show an ellipses at the end.
        const ellipses = "...";
        let possibleCharactersCount = 0;
        for (let i = (text.length - 1); i > 1; i--) {
            if ((config.statement.padding.left + textElement.getSubStringLength(0, i) +
                config.statement.padding.right) < maxWidth) {
                possibleCharactersCount = i;
                break;
            }
        }
        // We need room for the ellipses as well, hence removing 'ellipses.length' no. of characters.
        text = text.substring(0, (possibleCharactersCount - ellipses.length)) + ellipses; // Appending ellipses.

        width = maxWidth;
    }
    return {
        text,
        w: width,
    };
}
*/

function sizeStatement(node: ASTNode) {
    const viewState: ViewState = node.viewState;
    viewState.bBox.h = config.statement.height;
    viewState.bBox.w = config.statement.width;
}

export const visitor: Visitor = {

    // tslint:disable-next-line:ban-types
    endVisitFunction(node: Function) {
        const viewState: FunctionViewState = node.viewState;
        const body = viewState.body;
        const header = viewState.header;
        const client = viewState.client;
        const defaultWorker = viewState.defaultWorker;

        client.h = config.lifeLine.line.height + (config.lifeLine.header.height * 2);
        client.w = config.lifeLine.width;

        defaultWorker.h = node.body!.viewState.bBox.h + (config.lifeLine.header.height * 2)
            + config.statement.height; // for bottom plus
        defaultWorker.w = config.lifeLine.width;

        const lineHeight = (client.h > defaultWorker.h) ? client.h : defaultWorker.h;
        // Sync up the heights of lifelines
        client.h = defaultWorker.h = lineHeight;

        body.w = config.panel.padding.left + config.panel.padding.right;
        body.h = config.panel.padding.top + lineHeight + config.panel.padding.bottom;

        header.w = config.panelHeading.padding.left + config.panelHeading.padding.right;
        header.h = config.panelHeading.height;

        // Set the margin
        defaultWorker.leftMargin = (defaultWorker.w / 2);

        viewState.bBox.w = (body.w > header.w) ? body.w : header.w;
        viewState.bBox.h = body.h + header.h;
    },

    endVisitBlock(node: Block) {
        const viewState: ViewState = node.viewState;
        let height = 0;
        node.statements.forEach((element) => {
            viewState.bBox.w = (viewState.bBox.w < element.viewState.bBox.w)
                ? element.viewState.bBox.w : viewState.bBox.w;
            viewState.bBox.leftMargin = (viewState.bBox.leftMargin < element.viewState.bBox.leftMargin)
                ? element.viewState.bBox.leftMargin : viewState.bBox.leftMargin;
            height += element.viewState.bBox.h;
        });
        viewState.bBox.h = height;
    },

    endVisitWhile(node: While) {
        const viewState: ViewState = node.viewState;
        const bodyBBox: SimpleBBox = node.body.viewState.bBox;

        viewState.bBox.w = node.body.viewState.bBox.w;
        viewState.bBox.h = node.body.viewState.bBox.h + config.flowCtrl.header.height
            + config.flowCtrl.whileGap + config.flowCtrl.bottomMargin;
        // If body has a left margin assign to while
        // tslint:disable-next-line:prefer-conditional-expression
        if (bodyBBox.leftMargin) {
            viewState.bBox.leftMargin = bodyBBox.leftMargin + config.flowCtrl.leftMargin;
        } else {
            viewState.bBox.leftMargin = config.flowCtrl.leftMarginDefault;
        }
    },

    endVisitForeach(node: Foreach) {
        const viewState: ViewState = node.viewState;
        const bodyBBox: SimpleBBox = node.body.viewState.bBox;

        viewState.bBox.w = node.body.viewState.bBox.w;
        viewState.bBox.h = node.body.viewState.bBox.h + config.flowCtrl.header.height
            + config.flowCtrl.whileGap + config.flowCtrl.bottomMargin;
        // If body has a left margin assign to while
        // tslint:disable-next-line:prefer-conditional-expression
        if (bodyBBox.leftMargin) {
            viewState.bBox.leftMargin = bodyBBox.leftMargin + config.flowCtrl.leftMargin;
        } else {
            viewState.bBox.leftMargin = config.flowCtrl.leftMarginDefault;
        }
    },

    endVisitIf(node: If) {
        const viewState: ViewState = node.viewState;
        const bodyBBox: SimpleBBox = node.body.viewState.bBox;

        viewState.bBox.w = node.body.viewState.bBox.w;
        viewState.bBox.h = node.body.viewState.bBox.h + config.flowCtrl.header.height
            + config.flowCtrl.bottomMargin;
        // If body has a left margin assign to while
        // tslint:disable-next-line:prefer-conditional-expression
        if (bodyBBox.leftMargin) {
            viewState.bBox.leftMargin = bodyBBox.leftMargin + config.flowCtrl.leftMargin;
        } else {
            viewState.bBox.leftMargin = config.flowCtrl.leftMarginDefault;
        }

        // Add Else block
        if (node.elseStatement) {
            viewState.bBox.h += node.elseStatement.viewState.bBox.h;
            viewState.bBox.w += node.elseStatement.viewState.bBox.w;
        }
    },

    endVisitExpressionStatement(node: ExpressionStatement) {
        sizeStatement(node);
    },

    endVisitVariableDef(node: VariableDef) {
        sizeStatement(node);
    },

    endVisitAssignment(node: Assignment) {
        sizeStatement(node);
    }
};
