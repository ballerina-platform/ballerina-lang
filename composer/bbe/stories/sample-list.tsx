import { storiesOf } from "@storybook/react";
import * as React from "react";
import { SamplesList } from "./../src";
import getBBEs from "./bbes";

function openSample(url: string) {
  // mock
}

storiesOf("Ballerina By Example", module)
  .add("Example List Page", () => (
    <SamplesList getSamples={getBBEs} openSample={openSample} />
  ));
