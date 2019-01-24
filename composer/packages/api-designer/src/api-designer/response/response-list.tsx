import * as Swagger from "openapi3-ts";
import * as React from "react";
import { Table } from "semantic-ui-react";

export interface OpenApiResponseListProps {
    responsesList: Swagger.ResponsesObject[];
}

class OpenApiResponseList extends React.Component<OpenApiResponseListProps, any> {
    constructor(props: OpenApiResponseListProps) {
        super(props);
    }

    public render() {
        const { responsesList } = this.props;

        return (
            <Table celled>
                <Table.Header>
                <Table.Row>
                    <Table.HeaderCell>Name</Table.HeaderCell>
                    <Table.HeaderCell>Description</Table.HeaderCell>
                </Table.Row>
                </Table.Header>
                <Table.Body>
                    {Object.keys(responsesList).map((param: any, index: number) => {
                        return (
                            <Table.Row>
                                <Table.Cell className="parameter-name-cell">
                                    <div className="parameter__name required">
                                        {param}
                                    </div>
                                </Table.Cell>
                                <Table.Cell className="parameter-desc-cell">
                                    <div className="markdown">
                                        {responsesList[param].description}
                                    </div>
                                </Table.Cell>
                            </Table.Row>
                        );
                    })}
                </Table.Body>
            </Table>
        );
    }
}

export default OpenApiResponseList;
