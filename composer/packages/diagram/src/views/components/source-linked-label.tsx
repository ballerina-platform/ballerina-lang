import { ASTNode } from "@ballerina/ast-model";
import classNames from "classnames";
import * as React from "react";
import { DiagramContext } from "../../diagram/index";

export const SourceLinkedLabel: React.StatelessComponent<{
        x: number,
        y: number,
        target: ASTNode,
        text: string,
        className?: string
    }> = ({
        x,
        y,
        text,
        className,
        target
    }) => {

        return (
            <DiagramContext.Consumer>{({ langClient, docUri }) => (
                <text
                    {...{ x, y}}
                    className={classNames(className, "source-linked-label")}
                    onClick={() => {
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
                </text>)
            }
            </DiagramContext.Consumer>
        );
    };
