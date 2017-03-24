import {csvParse} from "d3-dsv";
import dsv from "./dsv";

export default dsv("text/csv", csvParse);
