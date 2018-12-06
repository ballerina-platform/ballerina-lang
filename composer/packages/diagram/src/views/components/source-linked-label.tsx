import { ASTNode } from "@ballerina/ast-model";
import cn from "classnames";
import * as React from "react";
import { DiagramContext } from "../../diagram/index";

export interface NodePosition {
    endColumn: number;
    endLine: number;
    startColumn: number;
    startLine: number;
}

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
                    className={cn(className, "source-linked-label")}
                    onClick={() => {
                        if (langClient) {
                            // FIXME: update this to use interface from @ballerina/ast-model
                            // once new interfaces are generated with NodePosition
                            const { startLine, startColumn, endColumn, endLine }
                                = (target as any).position as NodePosition;
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
