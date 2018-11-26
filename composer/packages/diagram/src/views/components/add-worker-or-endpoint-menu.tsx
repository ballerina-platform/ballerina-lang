import React, { Fragment } from "react";
import { SimplePoint } from "../../view-model/simple-point";
import { DropDownMenu } from "./dropdown-menu";
import { EndpointSearchDialog } from "./endpoint-search-dialog";

export class AddWorkerOrEndpointMenu extends React.Component<{
        triggerPosition: SimplePoint;
        onAddEndpoint?: (endpointDef: any) => void;
        onAddWorker?: () => void;
    }, {
        showEndpoitDialog: boolean
    }> {

    public state = {
        showEndpoitDialog: false
    };

    public render() {
        const { triggerPosition, onAddWorker, onAddEndpoint } = this.props;

        return <Fragment>
            <DropDownMenu
                    triggerPosition={triggerPosition}
                    triggerIcon="add"
                    items={[
                        {
                            onSelect: onAddWorker,
                            title: "Worker",
                        },
                        {
                            onSelect: this.openEpSearchDialog,
                            title: "Endpoint",
                        }
                    ]}
                />
            <EndpointSearchDialog
                show={this.state.showEndpoitDialog}
                onClose={this.closeEpSearchDialog}
                onEndpointSelect={onAddEndpoint}
            />
        </Fragment>;
    }

    private openEpSearchDialog = () => this.setState({ showEndpoitDialog: true });
    private closeEpSearchDialog = () => this.setState({ showEndpoitDialog: false });
}
