import * as Swagger from "openapi3-ts";
import * as React from "react";
import { Accordion, AccordionTitleProps, Button } from "semantic-ui-react";

import { OpenApiContext, OpenApiContextConsumer } from "../components/context/open-api-context";

import AddOpenApiOperation from "../operations/add-operation";
import OpenApiOperation from "../operations/operations-list";

import InlineEdit from "../components/utils/inline-edit";

interface OpenApiPathProps {
    paths: Swagger.PathsObject;
}

interface OpenApiPathState {
    activeIndex: number[];
    showAddOperation: boolean;
}

class OpenApiPathList extends React.Component<OpenApiPathProps, OpenApiPathState> {
    constructor(props: OpenApiPathProps) {
        super(props);

        this.state = {
            activeIndex: [],
            showAddOperation: false
        };

        this.onAccordionTitleClick = this.onAccordionTitleClick.bind(this);
        this.onAddOperationClick = this.onAddOperationClick.bind(this);
    }

    public render() {
        const { paths } = this.props;
        const { activeIndex, showAddOperation } = this.state;

        return (
            <OpenApiContextConsumer>
                {(context: OpenApiContext | null) => {
                    return (
                        <Accordion fluid>
                            {Object.keys(paths).sort().map((openApiResource, index) => {
                                return(
                                    <React.Fragment>
                                        <Accordion.Title
                                            active={activeIndex.includes(index)}
                                            index={index}
                                            onClick={this.onAccordionTitleClick}>
                                            <OpenApiContextConsumer>
                                                {(appContext: OpenApiContext) => {
                                                    return (
                                                        <InlineEdit
                                                            changeModel={appContext.openApiJson}
                                                            changeAttribute={{
                                                                changeValue: openApiResource,
                                                                key: "resource.name",
                                                            }}
                                                            text={openApiResource}
                                                            placeholderText="Add a summary"
                                                            onInlineValueChange={appContext.onInlineValueChange}
                                                        />
                                                    );
                                                }}
                                            </OpenApiContextConsumer>
                                            <Button
                                                title="Add operation to resource."
                                                size="mini"
                                                compact
                                                className="add-operation-action"
                                                circular
                                                icon="plus"
                                                onClick={this.onAddOperationClick}
                                            />
                                        </Accordion.Title>
                                        <Accordion.Content
                                            active={activeIndex.includes(index)}>
                                            {showAddOperation &&
                                                <AddOpenApiOperation
                                                    openApiJson={context!.openApiJson}
                                                    onAddOperation={context!.onAddOpenApiOperation}
                                                    resourcePath={openApiResource}
                                                />
                                            }
                                            <OpenApiOperation
                                                path={openApiResource}
                                                pathItem={paths[openApiResource]}
                                            />
                                        </Accordion.Content>
                                    </React.Fragment>
                                );
                            })}
                        </Accordion>
                    );
                }}
            </OpenApiContextConsumer>
        );
    }

    private onAddOperationClick(e: React.MouseEvent) {
        e.stopPropagation();
        const { showAddOperation } = this.state;
        this.setState({
            showAddOperation: !showAddOperation,
        });
    }

    private onAccordionTitleClick(e: React.MouseEvent, titleProps: AccordionTitleProps) {
        const { index } = titleProps;
        const { activeIndex } = this.state;

        this.setState({
            activeIndex: !activeIndex.includes(Number(index)) ?
                [...this.state.activeIndex, Number(index)] : this.state.activeIndex.filter((i) => i !== index)
        });
    }
}

export default OpenApiPathList;
