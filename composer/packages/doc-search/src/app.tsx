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
import { render } from "react-dom";
import { List } from "./components/list"

declare var searchData: any;

class App extends React.Component<any, { searchJson: any, searchTxt: string }> {
  constructor(props: {searchData: any}) {
    super(props);
    const searchQuery = new URLSearchParams(window.location.search);
    const searchStr = searchQuery.get('search');

    this.state = {
      searchJson: searchData,
      searchTxt: searchStr || ""
    };

  }

  render() {

    return (
      <div>
        {this.state.searchJson != null &&
          <section>
            <List searchJson={this.state.searchJson} searchTxt={this.state.searchTxt} />
          </section>
        }
      </div>

    );
  }
}


export default App;

render(
    <App />, document.getElementById("search-impl")
);
