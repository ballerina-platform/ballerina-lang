/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import * as _ from "lodash";
import * as React from "react";
import { Dropdown, Icon, Menu } from "semantic-ui-react";

export interface ToolBarState {
    filterValue: any;
}

export interface ToolBarProps {
    traces: any[];
    onFilteredTraces: (traces: any) => void;
    filters: any;
    clearLogs: () => void;
}

/**
 *
 * @extends React.Component
 */
export default class ToolBar extends React.Component<ToolBarProps, ToolBarState> {
    constructor(props: ToolBarProps, context: ToolBarState) {
        super(props);
        _.forEach(props.filters, (label: string, filter: string) => {
            this.state = {
                filterValue: {},
            };
            this.state.filterValue[filter] = "all";
        });
    }

    public onChangeFilter(key: string, filterBy: any) {
        this.setState({
            filterValue: {
                ...this.state.filterValue,
                [key]: filterBy,
            },
        });

        setTimeout(() => {
            const filtered = this.props.traces.filter((item) => {
                let isIncluded = true;
                _.forEach(this.state.filterValue, (value, k) => {
                    // tslint:disable-next-line:prefer-conditional-expression
                    if (value === "all") {
                        isIncluded = isIncluded && true;
                    } else {
                        isIncluded = isIncluded && (_.get(item, k) === value);
                    }
                });
                return isIncluded;
            });
            this.props.onFilteredTraces(filtered);
        }, 0);
    }
    /**
     * @inheritdoc
     */
    public render() {
        const { filters, traces } = this.props;
        const keys = _.keys(filters) || [];
        const groupedTraces: any = {};

        traces.forEach((message) => {
            keys.forEach((key) => {
                groupedTraces[key] = groupedTraces[key] || [];
                if (groupedTraces[key].indexOf(_.get(message, key)) === -1) {
                    groupedTraces[key].push(_.get(message, key));
                }
            });
        });

        return (
            <div className="logs-console-toolbar">
                <Menu inverted>
                    <Menu.Item name="ban" onClick={() => this.props.clearLogs()}>
                        <Icon name="ban" />
                    </Menu.Item>
                    {
                        keys.map((key) => {
                            groupedTraces[key] = groupedTraces[key] || [];
                            const options = groupedTraces[key].map((option: string) => {
                                return {
                                    key: option,
                                    text: option,
                                    value: option,
                                };
                            });
                            options.unshift({
                                key: "all",
                                text: "All",
                                value: "all",
                            });

                            this.state.filterValue[key] = this.state.filterValue[key] || "all";

                            return (
                                <Dropdown
                                    item
                                    inverted
                                    text={filters[key] + ": " + this.state.filterValue[key]}
                                    key={key}
                                    className={`filter-${key} pull-right`}
                                    value={this.state.filterValue[key]}
                                    options={options}
                                    onChange={(e, data) => this.onChangeFilter(key, data.value)}
                                />
                            );
                        })
                    }
                </Menu>
            </div>
        );
    }
}
