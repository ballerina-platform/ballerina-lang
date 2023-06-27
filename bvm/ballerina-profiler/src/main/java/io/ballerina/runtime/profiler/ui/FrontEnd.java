/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.runtime.profiler.ui;

/**
 * This class is used to profile Ballerina programs.
 *
 * @since 2201.7.0
 */
public class FrontEnd {

    static String getSiteData(String contents) {

        String htmlCode = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "\n" +
                "<head>\n" +
                "   <meta charset=\"utf-8\">\n" +
                "   <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "   <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +

                "   <link rel=\"stylesheet\"" +
                "href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\">\n" +
                "   <link rel=\"stylesheet\" type=\"text/css\"\n" +
                "      href=\"https://cdn.jsdelivr.net/gh/spiermar/d3-flame-graph@2.0.3/dist/d3-flamegraph.css\">\n" +
                "\n" +
                "<style>\n" +

                "#profilerLogo{\n" +
                "         color: #20b6b0;\n" +
                "         margin-top: -33px;\n" +
                "         margin-left: 190px;\n" +
                "         position: absolute;\n" +
                "         font-size: 32px;\n" +
                "         font-weight: bold;\n" +
                "      }" +


                "      body {\n" +
                "         background-color: #fefefe;\n" +
                "      }\n" +
                "\n" +
                "      .container {\n" +
                "         padding-left: 0px;\n" +
                "         margin-left: 40px;\n" +
                "      }" +
                "\n" +
                "      .btn {\n" +
                "         box-shadow: 0 0 2px rgba(0, 0, 0, 0.2);\n" +
                "         border-radius: 10px;\n" +
                "         border: 1px solid #20b6b0;\n" +
                "         background-color: #20b6b0;\n" +
                "         color: #1B2024;\n" +
                "         font-weight: bold;\n" +
                "         font-family: pragmatica, sans-serif;\n" +
                "      }\n" +
                "\n" +
                "      .btn:hover {\n" +
                "         background-color: black;\n" +
                "         color: #fff;\n" +
                "      }\n" +
                "\n" +
                "      .btn:focus {\n" +
                "         border-radius: 10px;\n" +
                "         border: 1px solid #20b6b0;\n" +
                "         background-color: #20b6b0;\n" +
                "         color: #fff;\n" +
                "      }\n" +
                "\n" +
                "      .btn:active {\n" +
                "         color: #fff;\n" +
                "         background-color: #494949;\n" +
                "      }\n" +
                "\n" +
                "      .header {\n" +
                "         margin-left: -40px;\n" +
                "         box-shadow: 0 0 2px rgba(0, 0, 0, 0.2);\n" +
                "         padding-right: 40px;\n" +
                "         width: 1920px;\n" +
                "         background-color: #fbfbfd;\n" +
                "         padding-bottom: 25px;\n" +
                "      }\n" +

                ".d3-flame-graph rect {\n" +
                "         outline: none;\n" +
                "         stroke: #f0efef;\n" +
                "         fill-opacity: .8;\n" +
                "      }" +
                "\n" +
                "      .header h3 {\n" +
                "         margin-left: 40px;\n" +
                "         margin-top: 0px;\n" +
                "         margin-bottom: 0;\n" +
                "         line-height: 40px;\n" +
                "      }\n" +
                "\n" +
                "      .text-muted {\n" +
                "         padding-top: 20px;\n" +
                "      }\n" +
                "\n" +
                "      .pull-right {\n" +
                "         margin-top: 20px;\n" +
                "      }\n" +
                "\n" +
                "      .chart {\n" +
                "         margin-left: 50px;\n" +
                "      }\n" +
                "\n" +
                "      .highlight {\n" +
                "         fill: greenyellow !important;\n" +
                "      }\n" +
                "\n" +
                ".d3-flame-graph-tip {\n" +
                "         visibility: hidden !important;\n" +
                "      }" +

                ".balLogo {\n" +
                "         padding-left: 20px !important;\n" +
                "         padding-top: 25px !important;\n" +
                "         width: 200px !important;\n" +
                "         height: 50px !important;\n" +
                "      }\n" +
                "\n" +
                "#chart {\n" +
                "         box-shadow: 0 0 2px rgba(0, 0, 0, 0.2);\n" +
                "         margin-top: 40px;\n" +
                "         margin-bottom: 40px;\n" +
                "         max-height: 780px;\n" +
                "         width: 1840px;\n" +
                "         border-radius: 20px;\n" +
                "         overflow-y: auto;\n" +
                "         overflow-x: auto;\n" +
                "         background-color: #fbfbfd;\n" +
                "      }" +

                "#details{\n" +
                "         color: black;\n" +
                "         padding: 20px;\n" +
                "         position: absolute;\n" +
                "         left: 50%;\n" +
                "         font-weight: bold;\n" +
                "         transform: translate(-50%, -50%);\n" +
                "      }" +
                ".form-control {\n" +
                "         border: solid 3px #fff;\n" +
                "         box-sizing: border-box;\n" +
                "         font-size: 17px;\n" +
                "         height: 2em;\n" +
                "         padding: .5em;\n" +
                "         transition: all 2s ease-in;\n" +
                "         width: 300px;\n" +
                "         z-index: 1;\n" +
                "      }\n" +
                "\n" +
                "   </style>" +
                "   <title>b7a-flamegraph</title>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "   <div class=\"container\">\n" +
                "      <div class=\"header clearfix\">\n" +
                "         <nav>\n" +
                "            <div class=\"pull-right\">\n" +
                "               <form class=\"form-inline\" id=\"form\">\n" +
                "                  <div class=\"form-group\">\n" +
                "                     <input placeholder=\"Search...\"" +
                "                     id=\"searchBox\" type=\"text\" class=\"form-control\" id=\"term\">\n" +
                "                  </div>\n" +
                "                  <a class=\"btn btn-primary\" href=\"javascript: search();\">Search</a>\n" +
                "                  <a class=\"btn\" href=\"javascript: resetZoom();\">Reset zoom</a>\n" +
                "                  <a class=\"btn\" href=\"javascript: clearSearch();\">Clear</a>\n" +
                "               </form>\n" +
                "            </div>\n" +
                "         </nav>\n" +
                "         <img class=\"balLogo\" src=\"https://ballerina.io/images/ballerina-logo.svg\"" +
                "         alt=\"B7A\" /> <div id=\"profilerLogo\"> Profiler </div>\n" +
                "      </div>\n" +
                "      <div id=\"chart\"></div>\n" +
                "      <div id=\"details\"></div>\n" +
                "   </div>\n" +
                "   <script type=\"text/javascript\" src=\"https://d3js.org/d3.v7.js\"></script>\n" +
                "   <script type=\"text/javascript\"\n" +
                "      src=\"https://cdn.jsdelivr.net/gh/spiermar/" +
                "d3-flame-graph@2.0.3/dist/d3-flamegraph.min.js\"></script>" +
                "   <script type=\"text/javascript\">\n" +
                "\n" +
                "\n" +
                contents +
                "\n" +
                "\n" +
                "var flameGraph = d3.flamegraph()\n" +
                "    .width(1840)\n" +
                "    .selfValue(false)\n" +
                "    .cellHeight(18)\n" +
                "    .transitionDuration(750)\n" +
                "    .minFrameSize(5)\n" +
                "    .transitionEase(d3.easeCubic)\n" +
                "    .sort(false)\n" +
                "    .onClick(onClick)\n" +
                "    .differential(false);\n" +
                "\n" +
                "      var details = document.getElementById(\"details\");\n" +
                "      flameGraph.setDetailsElement(details);\n" +
                "\n" +
                "      var start = data;" +
                "\n" +
                "      // Render the flame graph\n" +
                "      d3.select(\"#chart\")\n" +
                "         .datum(start)\n" +
                "         .call(flameGraph);\n" +
                "\n" +
                "function search() {\n" +
                "         var term = document.getElementById(\"searchBox\").value.toLowerCase();\n" +
                "         if (term) {\n" +
                "            var cells = d3.selectAll(\"rect\");\n" +
                "            cells.classed(\"highlight\", false);\n" +
                "            cells.each(function (d) {\n" +
                "               if (d.data.name.toLowerCase().indexOf(term) >= 0) {\n" +
                "                  d3.select(this).classed(\"highlight\", true);\n" +
                "               }\n" +
                "            });\n" +
                "         }\n" +
                "\n" +
                "         document.getElementById(\"searchBox\").addEventListener(\"input\", function () {\n" +
                "            if (this.value == \"\") {\n" +
                "               d3.selectAll(\"rect\").classed(\"highlight\", false);\n" +
                "            }\n" +
                "         });\n" +
                "      }\n" +
                "\n" +
                "      function clearSearch() {\n" +
                "         document.getElementById(\"searchBox\").value = \"\";\n" +
                "         d3.selectAll(\"rect\").classed(\"highlight\", false);\n" +
                "      }" +
                "      // Define a clear function to clear the search bar and reset the flame graph\n" +
                "      function stop() {\n" +
                "const answer = window.confirm(\"Leaving this page will end the profiling process\");\n" +
                "\n" +
                "  // return the user's answer\n" +
                "  if (answer) {\n" +
                "         window.location.href = \"http://localhost:2324/terminate\";" +
                "           window.close();" +
                "  } " +
                "}\n" +
                "\n" +
                "function saveAsHtml() {\n" +
                "  // Send an AJAX request to retrieve the current HTML content\n" +
                "  var xhr = new XMLHttpRequest();\n" +
                "  xhr.open(\"GET\", window.location.href, true);\n" +
                "  xhr.onreadystatechange = function() {\n" +
                "    if (xhr.readyState === 4 && xhr.status === 200) {\n" +
                "      // Get the response text (the HTML code)\n" +
                "      var htmlContent = xhr.responseText;\n" +
                "\n" +
                "      // Remove the button from the HTML content\n" +
                "      var parser = new DOMParser();\n" +
                "      var doc = parser.parseFromString(htmlContent, \"text/html\");\n" +
                "      var buttonToRemove = doc.getElementById(\"remove-me\");\n" +
                "      if (buttonToRemove) {\n" +
                "        buttonToRemove.parentNode.removeChild(buttonToRemove);\n" +
                "      }\n" +
                "      var buttonToRemove1 = doc.getElementById(\"remove-me1\");\n" +
                "      if (buttonToRemove1) {\n" +
                "        buttonToRemove1.parentNode.removeChild(buttonToRemove1);\n" +
                "      }\n" +
                "      htmlContent = doc.documentElement.outerHTML;\n" +
                "\n" +
                "      // Create a new Blob object with the modified HTML content\n" +
                "      var blob = new Blob([htmlContent], {type: \"text/html;charset=utf-8\"});\n" +
                "\n" +
                "      // Create a new anchor element to download the file\n" +
                "      var anchor = document.createElement(\"a\");\n" +
                "\n" +
                "      // Set the download attribute to the file name\n" +
                "      const currentTime = new Date().toLocaleString(); \n" +
                "      const fileName = `Profiler_Result_${currentTime}.html`;" +
                "      anchor.setAttribute(\"download\", fileName);\n" +
                "\n" +
                "      // Set the href attribute to the URL of the Blob object\n" +
                "      anchor.setAttribute(\"href\", URL.createObjectURL(blob));\n" +
                "\n" +
                "      // Click the anchor element to download the file\n" +
                "      anchor.click();\n" +
                "    }\n" +
                "  };\n" +
                "  xhr.send();\n" +
                "}\n" +
                "      // Define a function to reset the zoom on the flame graph\n" +
                "      function resetZoom() {\n" +
                "         flameGraph.resetZoom();\n" +
                "      }\n" +
                "\n" +
                "      // Define a function to logs a message to the console\n" +
                "      function onClick(d) {\n" +
                "         console.info(\"Clicked on \" + d.data.name);\n" +
                "      }\n" +
                "\n" +
                "   </script>\n" +

                "   <script>\n" +
                "      const myDiv = document.getElementById('details');\n" +
                "\n" +
                "      new MutationObserver(() => {\n" +
                "         const currentText = myDiv.textContent;\n" +
                "         if (currentText.includes('samples')) {\n" +
                "            const newText = currentText.replace('samples', 'ms');\n" +
                "            myDiv.textContent = newText;\n" +
                "         }\n" +
                "      }).observe(myDiv, { childList: true });\n" +
                "   </script>\n" +

                "</body>\n" +
                "\n" +
                "</html>";

        return htmlCode;
    }
}
