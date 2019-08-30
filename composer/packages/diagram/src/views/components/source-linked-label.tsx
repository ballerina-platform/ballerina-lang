import { ASTNode } from "@ballerina/ast-model";
import classNames from "classnames";
import * as React from "react";
import { Popup } from "semantic-ui-react";
import { DiagramContext } from "../../diagram/index";

// We need these states globally as these components gets rerendered when dragging
// As two source linked labes are not clicked in the same time its okay to have these globally
let mouseButtonDown = false;
let dragged = false;

export const SourceLinkedLabel: React.StatelessComponent<{
    x: number,
    y: number,
    target: ASTNode,
    fullText?: string,
    text: string,
    className?: string,
}> = ({
    x,
    y,
    text,
    fullText,
    className,
    target
}) => {
        const labelComponent = (
            <DiagramContext.Consumer>{({ langClient, docUri }) => (
                <text
                    {...{ x, y }}
                    className={classNames(className, "source-linked-label")}
                    onMouseDown={() => {
                        mouseButtonDown = true;
                    }}
                    onMouseMove={() => {
                        if (mouseButtonDown) {
                            dragged = true;
                        }
                    }}
                    onMouseUp={() => {
                        // Instead of onClick we use this method to avoid source linking triggering
                        // while dragging the diagram
                        mouseButtonDown = false;
                        if (dragged) {
                            dragged = false;
                            return;
                        }
                        dragged = false;
                        if (langClient) {
                            const position = target.position;
                            if (!position) {
                                return;
                            }
                            const { startLine, startColumn, endColumn, endLine }
                                = position;
                            langClient.revealRange({
                                range: {
                                    end: {
                                        character: endColumn,
                                        line: endLine
                                    },
                                    start: {
                                        character: startColumn,
                                        line: startLine
                                    }
                                },
                                textDocumentIdentifier: {
                                    uri: docUri as string,
                                }
                            });
                        }
                    }}
                >
                    {text}
                </text>
                )
            }
            </DiagramContext.Consumer>
        );

        return (
            <g>
                {fullText &&
                    <Popup
                        trigger={
                            <g>
                                {labelComponent}
                            </g>
                        }
                        content={fullText}
                        size="mini"
                        inverted
                    />
                }
                {!fullText && labelComponent}
            </g>
        );
    };
