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