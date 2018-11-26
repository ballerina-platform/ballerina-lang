import { ASTNode, ASTUtil, CompilationUnit } from "@ballerina/ast-model";
import React, { StatelessComponent } from "react";
import { Dropdown } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";

const definitions: any[] = [{
    icon: "function",
    id: "function",
    name: "Function"
},
{
    icon: "service",
    id: "service",
    name: "Service"
}];

export const AddDefinitionsMenu: StatelessComponent<{}> = (
            { children }
        ) => {
    return  (
        <DiagramContext.Consumer>
            {(diagContext) => {
                return (diagContext.editingEnabled &&
                        <Dropdown
                            button
                            className="icon primary add-definitions"
                            floating
                            labeled
                            icon="fw fw-add"
                            text="Definition"
                        >
                            <Dropdown.Menu>
                                {
                                    definitions.map((definition) => {
                                        if (definition.id === "seperator") {
                                            return <Dropdown.Divider />;
                                        }
                                        return (<Dropdown.Item
                                            onClick={(event, item) => {
                                                const {ast} = diagContext;
                                                if (ast) {
                                                    addDefinition(item.data.name, ast as CompilationUnit);
                                                }
                                            }}
                                            data={definition}
                                            key={definition.id}
                                        >
                                            <i className={`fw fw-${definition.icon}`} />
                                            {definition.name}
                                        </Dropdown.Item>);
                                    })
                                }
                                { children }
                            </Dropdown.Menu>
                        </Dropdown>
                );
            }}
        </DiagramContext.Consumer>
    );
};

function addDefinition(type: string, tree: CompilationUnit) {
    if (!(ASTUtil as any)[`create${type}Node`]) {
        return;
    }

    const newNode: ASTNode = (ASTUtil as any)[`create${type}Node`]();
    ASTUtil.attachNode(newNode, tree, "topLevelNodes", tree.topLevelNodes.length);
}
