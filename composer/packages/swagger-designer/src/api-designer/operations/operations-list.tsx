import * as Swagger from "openapi3-ts";
import * as React from "react";
import { Accordion, AccordionTitleProps } from "semantic-ui-react";

interface OpenApiOperationProp {
    pathItem: Swagger.PathItemObject;
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
        const { pathItem } = this.props;
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
                            </Accordion.Title>
                            <Accordion.Content active={activeIndex.includes(index)}>
                                <p>adfa</p>
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
