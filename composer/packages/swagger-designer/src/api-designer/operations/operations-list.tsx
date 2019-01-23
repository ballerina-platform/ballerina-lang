import * as Swagger from "openapi3-ts";
import * as React from "react";
import { Accordion, AccordionTitleProps } from "semantic-ui-react";

import { OpenApiContext, OpenApiContextConsumer } from "../components/context/open-api-context";

import InlineEdit from "../components/utils/inline-edit";

import OpenApiParameterList from "../parameter/parameter-list";

interface OpenApiOperationProp {
    pathItem: Swagger.PathItemObject;
    path: string;
}

interface OpenApiOperationState {
    activeIndex: number[];
}

class OpenApiOperation extends React.Component<OpenApiOperationProp, OpenApiOperationState> {
    constructor(props: OpenApiOperationProp) {
        super(props);

        this.state = {
            activeIndex: []
        };

        this.onAccordionTitleClick = this.onAccordionTitleClick.bind(this);
    }

    public render() {
        const { pathItem, path } = this.props;
        const { activeIndex } = this.state;

        return (
            <Accordion>
                {Object.keys(pathItem).sort().map((openApiOperation, index) => {
                    return(
                        <React.Fragment>
                            <Accordion.Title
                                active={activeIndex.includes(index)}
                                index={index}
                                onClick={this.onAccordionTitleClick} >
                                <span className="op-method">{openApiOperation}</span>
                                <OpenApiContextConsumer>
                                    {(appContext: OpenApiContext) => {
                                        return (
                                            <InlineEdit
                                                changeModel={appContext.openApiJson}
                                                changeAttribute={{
                                                    changeValue: openApiOperation,
                                                    key: "operation.summary",
                                                    path
                                                }}
                                                text={pathItem[openApiOperation].summary}
                                                placeholderText="Add a summary"
                                                onInlineValueChange={appContext.onInlineValueChange}
                                            />
                                        );
                                    }}
                                </OpenApiContextConsumer>
                            </Accordion.Title>
                            <Accordion.Content active={activeIndex.includes(index)}>
                                <OpenApiContextConsumer>
                                    {(appContext: OpenApiContext) => {
                                        return (
                                            <InlineEdit
                                                changeModel={appContext.openApiJson}
                                                changeAttribute={{
                                                    changeValue: openApiOperation,
                                                    key: "operation.description",
                                                    path
                                                }}
                                                text={pathItem[openApiOperation].description}
                                                placeholderText="Add a description"
                                                onInlineValueChange={appContext.onInlineValueChange}
                                            />
                                        );
                                    }}
                                </OpenApiContextConsumer>
                                <div className="op-section">
                                    <div className="title">
                                        <p>Parameters</p>
                                        <a >Add Parameter</a>
                                    </div>
                                    {pathItem[openApiOperation].parameters &&
                                        <OpenApiParameterList
                                            parameterList={pathItem[openApiOperation].parameters}
                                        />
                                    }
                                </div>
                            </Accordion.Content>
                        </React.Fragment>
                    );
                })}
            </Accordion>
        );
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

export default OpenApiOperation;
