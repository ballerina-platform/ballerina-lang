import * as _ from "lodash";
import React from "react";
import { DefaultConfig, DiagramConfig } from "../config/default";
import * as components from "../views/index";

// Following element is created to calculate the width of a text rendered in an svg.
// Please see getTextWidth on how we do the calculation.
const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
svg.setAttribute("style", "border: 0px; visibility: hidden;");
svg.setAttribute("width", "600");
svg.setAttribute("height", "50");
svg.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xlink", "http://www.w3.org/1999/xlink");
const textElement = document.createElementNS("http://www.w3.org/2000/svg", "text");
svg.appendChild(textElement);
document.body.appendChild(svg);

let ellipsesLength: number | undefined;

interface CalcTextLengthOptions {
    bold: boolean;
}

interface GetComponentsOptions {
    skipHidden?: boolean;
    commonProps?: any;
}
export class DiagramUtils {
    public static isDrawable(node: any): boolean {
        return (components as any)[node.kind] !== undefined;
    }

    public static getComponents(nodeArray: any, options: GetComponentsOptions = {}): React.ReactNode[] {
        // Convert to array
        if (!(nodeArray instanceof Array)) {
            nodeArray = [nodeArray];
        }

        const children: any = [];
        nodeArray.forEach((node: any) => {
            const ChildComp = (components as any)[node.kind];
            if (!ChildComp) { return; }
            // Do not return hidden elements
            if (node.viewState && node.viewState.hidden) { return; }
            children.push(<ChildComp model={node} />);
        });
        return children;
    }

    /**
     * Get width of a given text and processed text
     * considering provided min and max width.
     * @param {string} text
     * @param {number} minWidth
     * @param {number} maxWidth
     * @return {object} {width,text}
     */
    public static getTextWidth(
        text: string,
        minWidth = DefaultConfig.statement.width,
        maxWidth = DefaultConfig.statement.maxWidth,
        paddingLeft = DefaultConfig.statement.padding.left,
        paddingRight = DefaultConfig.statement.padding.right, isBold = false) {
        if (!ellipsesLength) {
            ellipsesLength = getEllipsesLength();
        }
        text = text.trim();
        text = text.replace(/\/\/.*$/gm, "");
        text = text.trim();
        textElement.style.fontWeight = isBold ? "bold" : "";

        textElement.innerHTML = _.escape(text);

        let labelWidth = textElement.getComputedTextLength();
        let width = paddingLeft + labelWidth + paddingRight;

        // if the width is more then max width crop the text
        if (width <= minWidth) {
            // set the width to minimum width
            width = minWidth;
        } else if (width > minWidth && width <= maxWidth) {
            // do nothing
        } else {
            // We need to truncate displayText and show an ellipses at the end.
            const ellipses = "...";
            let possibleCharactersCount = 0;
            for (let i = (text.length - 1); i > 1; i--) {
                if ((DefaultConfig.statement.padding.left + textElement.getSubStringLength(0, i) +
                DefaultConfig.statement.padding.right) < maxWidth) {
                    possibleCharactersCount = i;
                    break;
                }
            }
            // We need room for the ellipses as well, hence removing 'ellipses.length' no. of characters.
            text = text.substring(0, (possibleCharactersCount - ellipses.length)) + ellipses; // Appending ellipses.
            labelWidth = textElement.getSubStringLength(
                0, (possibleCharactersCount - ellipses.length)) + ellipsesLength;
            width = maxWidth;
        }
        return {
            labelWidth,
            text,
            w: width,
        };
    }

    public static calcTextLength(text: string, options: CalcTextLengthOptions) {
        textElement.style.fontWeight = options.bold ? "bold" : "";
        const escaped = _.escape(text);
        textElement.innerHTML = escaped;
        const length = textElement.getSubStringLength(0, escaped.length);
        textElement.style.fontWeight = "";
        return length;
    }

    /**
     * Get diagram config
     */
    public static getConfig(): DiagramConfig {
        return DefaultConfig;
    }
}

/**
 * Get text length of "..."
 */
function getEllipsesLength(): number {
    textElement.textContent = "...";
    return textElement.getComputedTextLength();
}
