/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

import * as React from "react";

declare var rootPath: any;

interface ListProps extends React.Props<any> {
    searchJson: any;
    searchTxt: string;
}

interface ListState {
    filteredModules: any[];
    filteredFunctions: any[];
    filteredObjects: any[];
    filteredRecords: any[];
    filteredConstants: any[];
    filteredErrors: any[];
    filteredTypes: any[];
    searchText: string;
}

export class List extends React.Component<ListProps, ListState> {
    constructor(props: ListProps) {
        super(props);
        this.state = {
            filteredModules: [],
            filteredFunctions: [],
            filteredObjects: [],
            filteredRecords: [],
            filteredConstants: [],
            filteredErrors: [],
            filteredTypes: [],
            searchText: this.props.searchTxt
        };
        this.handleChange = this.handleChange.bind(this);
    }

    componentDidMount() {
        document.addEventListener('keypress', e => {
            if (e.key === "s") {
                const searchBox = document.getElementById("searchBox");
                var isFocused = (document.activeElement === searchBox);
                if (!isFocused && searchBox != null) {
                    searchBox.focus();
                    e.preventDefault();
                }

            }
        });

        this.setState({
            filteredModules: this.props.searchJson.modules,
            filteredFunctions: this.props.searchJson.functions,
            filteredObjects: this.props.searchJson.objects,
            filteredRecords: this.props.searchJson.records,
            filteredConstants: this.props.searchJson.constants,
            filteredErrors: this.props.searchJson.errors,
            filteredTypes: this.props.searchJson.types,
        });
        this.handleChange();
    }

    componentWillReceiveProps(nextProps: ListProps) {
        this.setState({
            filteredModules: nextProps.searchJson.modules,
            filteredFunctions: nextProps.searchJson.functions,
            filteredObjects: nextProps.searchJson.objects,
            filteredRecords: nextProps.searchJson.records,
            filteredConstants: nextProps.searchJson.constants,
            filteredErrors: nextProps.searchJson.errors,
            filteredTypes: nextProps.searchJson.types
        });
        this.handleChange();
    }

    handleChange() {
        const mainDiv = document.getElementById("main");
        if (mainDiv != null) {
            mainDiv.classList.add('hidden');
        }
        const searchTxt = (document.getElementById("searchBox") as HTMLInputElement).value;
        this.setState({
            searchText: searchTxt
        });
        // Variable to hold the original version of the list
        let currentModuleList = [];
        let currentFunctionsList = [];
        let currentObjectsList = [];
        let currentRecordsList = [];
        let currentConstantsList = [];
        let currentErrorsList = [];
        let currentTypesList = [];
        // Variable to hold the filtered list before putting into state
        let newModuleList = [];
        let newFunctionsList = [];
        let newObjectsList = [];
        let newRecordsList = [];
        let newConstantsList = [];
        let newErrorsList = [];
        let newTypesList = [];

        // If the search bar isn't empty
        if (searchTxt !== "") {
            // Assign the original list to currentList
            currentModuleList = this.props.searchJson.modules;
            currentFunctionsList = this.props.searchJson.functions;
            currentObjectsList = this.props.searchJson.objects;
            currentRecordsList = this.props.searchJson.records;
            currentConstantsList = this.props.searchJson.constants;
            currentErrorsList = this.props.searchJson.errors;
            currentTypesList = this.props.searchJson.types;
            // Use .filter() to determine which items should be displayed
            // based on the search terms
            newModuleList = currentModuleList.filter((item: any) => {
                // change current item to lowercase
                const lc = item.id.toLowerCase();
                // change search term to lowercase
                const filter = searchTxt.toLowerCase();
                // check to see if the current list item includes the search term
                // If it does, it will be added to newList. Using lowercase eliminates
                // issues with capitalization in search terms and search content
                return lc.includes(filter);
            });

            newFunctionsList = currentFunctionsList.filter((item: any) => {
                const lc = item.id.toLowerCase();
                const filter = searchTxt.toLowerCase();
                return lc.includes(filter);
            });

            newObjectsList = currentObjectsList.filter((item: any) => {
                const lc = item.id.toLowerCase();
                const filter = searchTxt.toLowerCase();
                return lc.includes(filter);
            });

            newRecordsList = currentRecordsList.filter((item: any) => {
                const lc = item.id.toLowerCase();
                const filter = searchTxt.toLowerCase();
                return lc.includes(filter);
            });

            newConstantsList = currentConstantsList.filter((item: any) => {
                const lc = item.id.toLowerCase();
                const filter = searchTxt.toLowerCase();
                return lc.includes(filter);
            });

            newErrorsList = currentErrorsList.filter((item: any) => {
                const lc = item.id.toLowerCase();
                const filter = searchTxt.toLowerCase();
                return lc.includes(filter);
            });

            newTypesList = currentTypesList.filter((item: any) => {
                const lc = item.id.toLowerCase();
                const filter = searchTxt.toLowerCase();
                return lc.includes(filter);
            });

        } else {
            if (mainDiv != null) {
                mainDiv.classList.remove('hidden');
            }
        }
        // Set the filtered state based on what our rules added to newList
        this.setState({
            filteredModules: newModuleList,
            filteredFunctions: newFunctionsList,
            filteredObjects: newObjectsList,
            filteredRecords: newRecordsList,
            filteredConstants: newConstantsList,
            filteredErrors: newErrorsList,
            filteredTypes: newTypesList
        });
    }

    render() {
        return (
            <div>
                <div className="ui fluid icon input search-bar">
                    <input type="text" id="searchBox" onKeyUp={this.handleChange} placeholder="Search..." />
                    <i className="search icon"></i>
                </div>
                {this.state.searchText &&
                    <div className="search-list">
                        <h1>Search results for '{this.state.searchText}'</h1>
                        {this.state.filteredModules.length > 0 &&
                            <div>
                                <h3>Modules: {this.state.filteredModules.length}</h3>
                                <table>
                                    <tbody>
                                        {this.state.filteredModules.map(item => (
                                            <tr>
                                                <td className="search-title" id={item.id} title={item.id}>
                                                    <a href={rootPath + item.id + "/index.html"} className="objects">
                                                    {item.id}</a></td>
                                                <td className="search-desc"><span
                                                dangerouslySetInnerHTML={{ __html: item.searchString }} /></td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        }

                        {this.state.filteredObjects.length > 0 &&
                            <div>
                                <h3>Objects: {this.state.filteredObjects.length}</h3>
                                <table>
                                    <tbody>
                                        {this.state.filteredObjects.map(item => (
                                            <tr>
                                                <td className="search-title" id={item.id} title={item.id}>
                                                    <a href={rootPath + item.moduleId + "/objects/" + item.id + ".html"}
                                                     className="objects">{item.moduleId + ": " + item.id}</a></td>
                                                <td className="search-desc"><span
                                                dangerouslySetInnerHTML={{ __html: item.searchString }} /></td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        }

                        {this.state.filteredFunctions.length > 0 &&
                            <div>
                                <h3>Functions: {this.state.filteredFunctions.length}</h3>
                                <table>
                                    <tbody>
                                        {this.state.filteredFunctions.map(item => (
                                            <tr>
                                                <td className="search-title" id={item.id} title={item.id}>
                                                    <a href={rootPath + item.moduleId + "/functions.html#" + item.id}
                                                    className="functions">{item.moduleId + ": " + item.id}</a></td>
                                                <td className="search-desc"><span
                                                dangerouslySetInnerHTML={{ __html: item.searchString }} /></td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        }

                        {this.state.filteredRecords.length > 0 &&
                            <div>
                                <h3>Records: {this.state.filteredRecords.length}</h3>
                                <table>
                                    <tbody>
                                        {this.state.filteredRecords.map(item => (
                                            <tr>
                                                <td className="search-title" id={item.id} title={item.id}>
                                                    <a href={rootPath + item.moduleId + "/records/" + item.id + ".html"}
                                                     className="records">{item.moduleId + ": " + item.id}</a></td>
                                                <td className="search-desc"><span
                                                dangerouslySetInnerHTML={{ __html: item.searchString }} /></td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        }

                        {this.state.filteredConstants.length > 0 &&
                            <div>
                                <h3>Constants: {this.state.filteredConstants.length}</h3>
                                <table>
                                    <tbody>
                                        {this.state.filteredConstants.map(item => (
                                            <tr>
                                                <td className="search-title" id={item.id} title={item.id}>
                                                    <a href={rootPath + item.moduleId + "/constants.html#" + item.id}
                                                    className="constant">{item.moduleId + ": " + item.id}</a></td>
                                                <td className="search-desc"><span
                                                dangerouslySetInnerHTML={{ __html: item.searchString }} /></td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        }

                        {this.state.filteredTypes.length > 0 &&
                            <div>
                                <h3>Constants: {this.state.filteredTypes.length}</h3>
                                <table>
                                    <tbody>
                                        {this.state.filteredTypes.map(item => (
                                            <tr>
                                                <td className="search-title" id={item.id} title={item.id}>
                                                    <a href={rootPath + item.moduleId + "/types.html#" + item.id}
                                                    className="types">{item.moduleId + ": " + item.id}</a></td>
                                                <td className="search-desc"><span
                                                dangerouslySetInnerHTML={{ __html: item.searchString }} /></td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        }

                        {this.state.filteredErrors.length > 0 &&
                            <div>
                                <h3>Errors: {this.state.filteredErrors.length}</h3>
                                <table>
                                    <tbody>
                                        {this.state.filteredErrors.map(item => (
                                            <tr>
                                                <td className="search-title" id={item.id} title={item.id}>
                                                    <a href={rootPath + item.moduleId + "/errors.html#" + item.id}
                                                    className="errors">{item.moduleId + ": " + item.id}</a></td>
                                                <td className="search-desc"><span
                                                dangerouslySetInnerHTML={{ __html: item.searchString }} /></td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        }
                    </div>
                }
            </div>
        )
    }
}
