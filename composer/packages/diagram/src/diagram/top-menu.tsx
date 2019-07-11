import React from "react";
import { Dropdown, DropdownItemProps, Grid, Icon } from "semantic-ui-react";
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
    fitActive: boolean;
    openedState: boolean;
    handleOpened: () => void;
    handleClosed: () => void;
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
        openedState,
        fitActive
    } = props;

    return (
        <div>{
            !openedState ?
            <div>
                <div className="menu-icon">
                    <Icon onClick={handleOpened} className="fw fw-search" />
                </div>
            </div> :
            <Grid className="menu-container">
                <Grid.Row className="menu-row" columns={3}>
                    <Grid.Column className="menu-label" width={4}>
                        Zoom
                        </Grid.Column>
                    <Grid.Column className="selection-row" width={9}>
                        <Icon onClick={handleZoomOut} className="fw fw-minus" />
                        <Dropdown text={"4%"} className="menu-dropdown-small">
                            <Dropdown.Menu>
                                <Dropdown.Item text="10%" />
                                <Dropdown.Item text="20%" />
                                <Dropdown.Item text="30%" />
                            </Dropdown.Menu>
                        </Dropdown>
                        <Icon onClick={handleZoomIn} className="fw fw-add" />
                        <Icon onClick={handleFitClick} active={fitActive} className="fw fw-fit" />
                    </Grid.Column>
                    <Grid.Column className="menu-control" width={3}>
                        <Icon className="fw fw-refresh" />
                        <Icon onClick={handleClosed} className="fw fw-uncheck" />
                    </Grid.Column>
                </Grid.Row>
                <Grid.Row className="menu-row" columns={3}>
                    <Grid.Column className="menu-label" width={4}>
                        Depth
                        </Grid.Column>
                    <Grid.Column className="selection-row" width={9}>
                        <Icon className="fw fw-minus" />
                        <Dropdown className="menu-dropdown-small">
                            <Dropdown.Menu>
                                <Dropdown.Item text={1} />
                                <Dropdown.Item text={2} />
                                <Dropdown.Item text={3} />
                                <Dropdown.Item text={4} />
                            </Dropdown.Menu>
                        </Dropdown>
                        <Icon className="fw fw-add" />
                        <Icon className="fw fw-expand-all" />
                        <Icon className="fw fw-collapse-all" />
                    </Grid.Column>
                    <Grid.Column width={3} />
                </Grid.Row>
                <Grid.Row className="menu-row" columns={3}>
                    <Grid.Column className="menu-label" width={4}>
                        Design
                        </Grid.Column>
                    <Grid.Column className="selection-row" width={9}>
                        <Icon className="fw fw-minus" />
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
                        <Icon className="fw fw-add" />
                    </Grid.Column>
                    <Grid.Column width={3} />
                </Grid.Row>
            </Grid>
        }
        </div>
    );
};
