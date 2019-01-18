import * as Swagger from "openapi3-ts";
import * as React from "react";
import { Accordion, AccordionTitleProps, Button } from "semantic-ui-react";

import OpenApiPath from "../operations/operations-list";

interface OpenApiPathProps {
    paths: Swagger.PathsObject;
}

interface OpenApiPathState {
    activeIndex: number[];
}

class OpenApiPathList extends React.Component<OpenApiPathProps, OpenApiPathState> {
    constructor(props: OpenApiPathProps) {
        super(props);

        this.state = {
            activeIndex: []
        };

        this.onAccordionTitleClick = this.onAccordionTitleClick.bind(this);
    }

    public render() {
        const { paths } = this.props;
        const { activeIndex } = this.state;

        return (
            <Accordion>
                {Object.keys(paths).sort().map((openApiResource, index) => {
                    return(
                        <React.Fragment>
                            <Accordion.Title
                                active={activeIndex.includes(index)}
                                index={index}
                                onClick={this.onAccordionTitleClick}>
                                {openApiResource}
                                <Button
                                    title="Add operation to resource."
                                    size="mini"
                                    compact
                                    className="add-operation-action"
                                    circular
                                    icon="plus"
                                />
                            </Accordion.Title>
                            <Accordion.Content
                                active={activeIndex.includes(index)}>
                                <OpenApiPath pathItem={paths[openApiResource]} />
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

export default OpenApiPathList;
