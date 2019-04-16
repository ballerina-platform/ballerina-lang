import { configure } from "@storybook/react";
import { getAllCodePoints } from "@ballerina/font";
import "@ballerina/theme";

function loadStories() {
    document.body.classList.add("diagram");
    window.BallerinaFontCodepoints = getAllCodePoints();
    require("../stories/editable-diagram.tsx");
}

configure(loadStories, module);
