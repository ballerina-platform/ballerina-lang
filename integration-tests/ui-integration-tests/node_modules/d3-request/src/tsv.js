import {tsvParse} from "d3-dsv";
import dsv from "./dsv";

export default dsv("text/tab-separated-values", tsvParse);
