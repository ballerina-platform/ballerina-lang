import React from "react";
import { Dropdown, DropdownItemProps, Grid, Icon, Label, Popup} from "semantic-ui-react";
import { DiagramMode } from "./diagram-context";

export interface TopMenuProps {
    modes: Array<{
        text: string;
        type: DiagramMode;
    }>;
    handleModeChange: (e: React.MouseEvent<HTMLDivElement, MouseEvent>, props: DropdownItemProps) => void;
    handleBackClick: () => void;
    handleFitClick: () => void;
    handleZoomIn: () => void;
    handleZoomOut: () => void;
    selectedModeText: string;
    openedState: boolean;
    handleOpened: () => void;
    handleClosed: () => void;
    handleReset: () => void;
    handleDepthSelect: (depth: number) => void;
    maxInvocationDepth: number;
    zoomFactor: number;
}

export const TopMenu = (props: TopMenuProps) => {
    const {
        modes,
        handleModeChange,
        selectedModeText,
        handleFitClick,
        handleZoomIn,
        handleZoomOut,
        handleOpened,
        handleClosed,
        handleReset,
        handleDepthSelect,
        openedState,
        zoomFactor = 1,
        maxInvocationDepth,
    } = props;

    return (
        <div>{
            !openedState ?
            <div>
                <div onClick={handleOpened} className="menu-icon">
                    <Icon className="fw fw-search" />
                </div>
                <div onClick={handleOpened} className="status-wrapper">
                    Zoom : <Label> {`${Math.floor(zoomFactor * 100)}%`} </Label>
                    Depth : <Label>{maxInvocationDepth === -1 ? "All" : maxInvocationDepth.toString()}</Label>
                    Design : <Label>{selectedModeText}</Label>
                </div>
            </div> :
            <Grid className="menu-container">
                <Grid.Row className="menu-row" columns={3}>
                    <Grid.Column className="menu-label" width={4}>
                        Zoom
                        </Grid.Column>
                    <Grid.Column className="selection-row" width={9}>
                        <Popup
                            trigger={<Icon onClick={handleZoomOut} className="fw fw-minus"/>}
                            content="Zoom out"
                            position="top center"
                            size="small"
                            inverted
                            />
                        <Label className="menu-dropdown-small"> {`${Math.floor(zoomFactor * 100)}%`} </Label>
                        <Popup
                            trigger={<Icon onClick={handleZoomIn} className="fw fw-add"/>}
                            content="Zoom in"
                            position="top center"
                            size="small"
                            inverted
                            />
                        <Popup
                            trigger={<Icon onClick={handleFitClick} className="fw fw-fit-to-screen"/>}
                            content="Fit to Screen"
                            position="top center"
                            size="small"
                            inverted
                            />
                    </Grid.Column>
                    <Grid.Column className="menu-control" width={3}>
                        <Popup
                            trigger={<Icon onClick={handleReset} className="fw fw-refresh"/>}
                            content="Reset"
                            position="top center"
                            size="small"
                            inverted
                        />
                        <Popup
                            trigger={<Icon onClick={handleClosed} className="fw fw-uncheck"/>}
                            content="Close"
                            position="top center"
                            size="small"
                            inverted
                        />
                    </Grid.Column>
                </Grid.Row>
                <Grid.Row className="menu-row" columns={3}>
                    <Grid.Column className="menu-label" width={4}>
                        Depth
                    </Grid.Column>
                    <Grid.Column className="selection-row" width={9}>
                        <Dropdown
                            text={maxInvocationDepth === -1 ? "All" : maxInvocationDepth.toString()}
                            className="menu-dropdown-small"
                        >
                            <Dropdown.Menu>
                                <Dropdown.Item onClick={() => handleDepthSelect(0)} text={0} />
                                <Dropdown.Item onClick={() => handleDepthSelect(1)} text={1} />
                                <Dropdown.Item onClick={() => handleDepthSelect(2)} text={2} />
                                <Dropdown.Item onClick={() => handleDepthSelect(3)} text={3} />
                                <Dropdown.Item onClick={() => handleDepthSelect(-1)} text={"All"} />
                            </Dropdown.Menu>
                        </Dropdown>
                        <Popup
                            trigger={<Icon onClick={() => handleDepthSelect(-1)}  className="fw fw-expand-all"/>}
                            content="Expand all"
                            position="top center"
                            size="small"
                            inverted
                        />
                        <Popup
                            trigger={<Icon onClick={() => handleDepthSelect(0)} className="fw fw-collapse-all"/>}
                            content="Collapse all"
                            position="top center"
                            size="small"
                            inverted
                        />
                    </Grid.Column>
                    <Grid.Column width={3} />
                </Grid.Row>
                <Grid.Row className="menu-row" columns={3}>
                    <Grid.Column className="menu-label" width={4}>
                        Design
                        </Grid.Column>
                    <Grid.Column className="selection-row" width={9}>
                        <Dropdown className="menu-dropdown-mid " text={selectedModeText}>
                            <Dropdown.Menu>
                                {
                                    modes.map((mode) => (
                                        <Dropdown.Item
                                            text={mode.text}
                                            value={mode.type}
                                            onClick={handleModeChange}
                                            data={{
                                                text: mode.text,
                                                type: mode.type,
                                            }}
                                        />
                                    ))
                                }
                            </Dropdown.Menu>
                        </Dropdown>
                    </Grid.Column>
                    <Grid.Column width={3} />
                </Grid.Row>
            </Grid>
        }
        </div>
    );
};
