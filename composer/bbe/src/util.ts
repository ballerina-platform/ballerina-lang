import { createElement } from "react";
import { render } from "react-dom";
import { SamplesList } from "./ExampleList";
import { BallerinaExampleCategory } from "./model";

export function renderSamplesList(target: HTMLElement,
                                  openSample: (url: string) => void,
                                  getSamples: () => Promise<BallerinaExampleCategory[]>,
                                  openLink: (url: string) => void) {
    const props = {
        getSamples,
        openLink,
        openSample,
    };
    target.classList.add("composer-library");
    const SamplesListElement = createElement(SamplesList, props);
    render(SamplesListElement, target);
}
