# Guidelines for Writing Ballerina By Examples (BBEs)

## General Best Practices

1. The maximum character count per line should be 80 in .bal files. Otherwise, the lines will be wrapped in the [Ballerina](https://ballerina.io/) website reducing the readability.

Example:




<p id="gdcalert1" ><span style="color: red; font-weight: bold">>>>>>  gd2md-html alert: inline image link here (to images/Guidelines-in0.png). Store image on your image server and adjust path/filename if necessary. </span><br>(<a href="#">Back to top</a>)(<a href="#gdcalert2">Next alert</a>)<br><span style="color: red; font-weight: bold">>>>>> </span></p>


![alt_text](images/Guidelines-in0.png "image_tooltip")




*   **Add comments to the code blocks as much as possible with “//”. They will move to the RHS box in b.io. So comments are used as a mechanism to describe the code.**

Ex:

Code



<p id="gdcalert2" ><span style="color: red; font-weight: bold">>>>>>  gd2md-html alert: inline image link here (to images/Guidelines-in1.png). Store image on your image server and adjust path/filename if necessary. </span><br>(<a href="#">Back to top</a>)(<a href="#gdcalert3">Next alert</a>)<br><span style="color: red; font-weight: bold">>>>>> </span></p>


![alt_text](images/Guidelines-in1.png "image_tooltip")


b.io:



<p id="gdcalert3" ><span style="color: red; font-weight: bold">>>>>>  gd2md-html alert: inline image link here (to images/Guidelines-in2.png). Store image on your image server and adjust path/filename if necessary. </span><br>(<a href="#">Back to top</a>)(<a href="#gdcalert4">Next alert</a>)<br><span style="color: red; font-weight: bold">>>>>> </span></p>


![alt_text](images/Guidelines-in2.png "image_tooltip")




*   **Since comments are used in RHS they should be a valid sentence. (i.e. Start with upper case letter, ends with full stop etc)**
*   **A comment applies to the subsequent lines in the file until another comment or an empty line is found. Use comments/new lines appropriately to ensure that it applies only to the relevant lines.**
*   **No restriction for the max chars in comment lines as they will go to RHS side and wraps automatically. But since users can refer the code in GitHub, it is better if we can have the same char limit as the code line (i.e. 80) as it increases the readability of the code file.**

Ex: Comments are significantly longer than the code line, which is not readable. 



<p id="gdcalert4" ><span style="color: red; font-weight: bold">>>>>>  gd2md-html alert: inline image link here (to images/Guidelines-in3.png). Store image on your image server and adjust path/filename if necessary. </span><br>(<a href="#">Back to top</a>)(<a href="#gdcalert5">Next alert</a>)<br><span style="color: red; font-weight: bold">>>>>> </span></p>


![alt_text](images/Guidelines-in3.png "image_tooltip")




*   **All keywords and any other word which needs to be highlighted should be used with backquotes. (ex:  `xml`).  Don’t use a single quote as it won’t get highlighted. **

Ex:

Code:



<p id="gdcalert5" ><span style="color: red; font-weight: bold">>>>>>  gd2md-html alert: inline image link here (to images/Guidelines-in4.png). Store image on your image server and adjust path/filename if necessary. </span><br>(<a href="#">Back to top</a>)(<a href="#gdcalert6">Next alert</a>)<br><span style="color: red; font-weight: bold">>>>>> </span></p>


![alt_text](images/Guidelines-in4.png "image_tooltip")


b.io:



<p id="gdcalert6" ><span style="color: red; font-weight: bold">>>>>>  gd2md-html alert: inline image link here (to images/Guidelines-in5.png). Store image on your image server and adjust path/filename if necessary. </span><br>(<a href="#">Back to top</a>)(<a href="#gdcalert7">Next alert</a>)<br><span style="color: red; font-weight: bold">>>>>> </span></p>


![alt_text](images/Guidelines-in5.png "image_tooltip")




*   **We use a common pattern in `.out` files. Use the following format and customize only when necessary (ex: When needed to add more command line args etc)**

<span style="text-decoration:underline;">For an example with main:</span>

# To run this sample, navigate to the directory that contains the

# `.bal` file and issue the `ballerina run` command.

$ ballerina run <sample_file_name>.bal

<span style="text-decoration:underline;">For an example with a service:</span>

# To start the service, navigate to the directory that contains the

# `.bal` file and issue the `ballerina run` command.

$ ballerina run hello_world_service.bal



*   **As a practice, we use `<code>ballerina/io`</code> methods in main examples and `<code>ballerina/log</code>` in services examples. Don’t use both `<code>io:println</code>` and `<code>log:printInfo</code>` in the same sample.</strong>
*   <strong>Remove unused imports in `.bal` files.</strong>
*   <strong>Format the ballerina source using an IDE Plugin. </strong>
*   <strong>Always think about the length of the sample. A good example should be short.</strong>
*   <strong>If a new example is added/deleted, update the [index.json](https://github.com/ballerina-platform/ballerina-lang/blob/master/examples/index.json) file as well.</strong>



**<span style="text-decoration:underline;">Folder Structure</span>**



*   **Each BBE is in a separate directory with the sample name. Folder name should be all lowercase and words are separated by “-”. (ex: abstract-objects)**
*   **Each example should contains at least following files. File name should be same as folder name, but “-” is replaced with underscore. (ex: abstract_objects)**



<p id="gdcalert7" ><span style="color: red; font-weight: bold">>>>>>  gd2md-html alert: inline image link here (to images/Guidelines-in6.png). Store image on your image server and adjust path/filename if necessary. </span><br>(<a href="#">Back to top</a>)(<a href="#gdcalert8">Next alert</a>)<br><span style="color: red; font-weight: bold">>>>>> </span></p>


![alt_text](images/Guidelines-in6.png "image_tooltip")


           .bal - Sample code to display in b.io.

           .description - The sample description displayed at the top of each example in b.io

           .out - Output of the sample displayed at the bottom black colour box in b.io.

           _test.bal - Contains the test to validate the output of the BBE during the build time. 



*   **For service examples, we need to have two .out files. One to display the server output (.server.out file) and the other (.client.out file)  to display the curl command and the output.**

    

<p id="gdcalert8" ><span style="color: red; font-weight: bold">>>>>>  gd2md-html alert: inline image link here (to images/Guidelines-in7.png). Store image on your image server and adjust path/filename if necessary. </span><br>(<a href="#">Back to top</a>)(<a href="#gdcalert9">Next alert</a>)<br><span style="color: red; font-weight: bold">>>>>> </span></p>


![alt_text](images/Guidelines-in7.png "image_tooltip")


<p id="gdcalert9" ><span style="color: red; font-weight: bold">>>>>>  gd2md-html alert: inline image link here (to images/Guidelines-in8.png). Store image on your image server and adjust path/filename if necessary. </span><br>(<a href="#">Back to top</a>)(<a href="#gdcalert10">Next alert</a>)<br><span style="color: red; font-weight: bold">>>>>> </span></p>


![alt_text](images/Guidelines-in8.png "image_tooltip")


<p id="gdcalert10" ><span style="color: red; font-weight: bold">>>>>>  gd2md-html alert: inline image link here (to images/Guidelines-in9.png). Store image on your image server and adjust path/filename if necessary. </span><br>(<a href="#">Back to top</a>)(<a href="#gdcalert11">Next alert</a>)<br><span style="color: red; font-weight: bold">>>>>> </span></p>


![alt_text](images/Guidelines-in9.png "image_tooltip")


*   **Unless it is really required, it is not encouraged to have multiple .bal files in the same sample. In that case, each bal file can have its own name and .out file should match with the name of the .bal file. **

		

<p id="gdcalert11" ><span style="color: red; font-weight: bold">>>>>>  gd2md-html alert: inline image link here (to images/Guidelines-in10.png). Store image on your image server and adjust path/filename if necessary. </span><br>(<a href="#">Back to top</a>)(<a href="#gdcalert12">Next alert</a>)<br><span style="color: red; font-weight: bold">>>>>> </span></p>


![alt_text](images/Guidelines-in10.png "image_tooltip")



<!-- Docs to Markdown version 1.0β17 -->
