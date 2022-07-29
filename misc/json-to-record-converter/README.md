# JSON to Record Converter

The tool is intended to map JSON values directly to Ballerina records without converting the JSON value to its schema. The tool individually analyzes the JSON value nodes and maps it to the Ballerina record either in-line or other. The JSON value is parsed as a parse tree using Google’s gson package. And the tree is traversed to generate the Ballerina record.

## Features
In the current implementation we support JSON value to Ballerina Record Conversion

##### _Example JSON Value_
```json
{
  "id": "0001",
  "type": "donut",
  "name": "Cake",
  "images": [
    {
      "url": "images/0001.jpg",
      "width": 200,
      "height": 200,
      "isTransparent": true
    },
    {
      "url": "images/0001.jpg",
      "width": 200,
      "height": 200
    }
  ],
  "thumbnail": {
    "url": "images/thumbnails/0001.jpg",
    "width": 32,
    "height": 32
  }
}
```

##### _Converted Ballerina Record_
```ballerina
type ImagesItem record {
    string url;
    int width;
    int height;
    boolean isTransparent?;
};

type Thumbnail record {
    string url;
    int width;
    int height;
};

type NewRecord record {
    string id;
    string 'type;
    string name;
    ImagesItem[] images;
    Thumbnail thumbnail;
};
```

##### _Converter Demo_
![alt text](./docs/images/converterDemo.gif?raw=true "Converter Demo")

## Use Case
JSON to Record converted API is primarily used by the [Ballerina vscode plugin](https://marketplace.visualstudio.com/items?itemName=WSO2.ballerina) to convert JSON value to Ballerina record interactively.


## Contact Us
Managed By [WSO2 Inc.](https://wso2.com/)
Slack channel : [Ballerina Platform](https://ballerina-platform.slack.com/)
