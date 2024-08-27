<!--
 ~ Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 ~
 ~ WSO2 LLC. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied. See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->

# WSO2 OB Reference Toolkit CDS

Reference Implementation for Consumer Data Standards AU Specification

### Building from the source

If you want to build WSO2 Consumer Data Standards AU Reference Implementation from the source code:

1. Install Java8 or above.
1. Install [Apache Maven 3.0.5](https://maven.apache.org/download.cgi) or above.
1. Install [MySQL](https://dev.mysql.com/doc/refman/5.5/en/windows-installation.html).
1. To get the WSO2 Consumer Data Standards AU Reference Implementation from [this repository](https://github.com/wso2/reference-implementation-consumerdatastandards-au.git), click **Clone or download**.
    * To **clone the solution**, copy the URL and execute the following command in a command prompt.
      `git clone <the copiedURL>`
    * To **download the pack**, click **Download ZIP** and unzip the downloaded file.
1. Navigate to the downloaded solution using a command prompt and run the Maven command.

   |  Command | Description |
         | :--- |:--- |
   | ```mvn install``` | This starts building the pack without cleaning the folders. |
   | ```mvn clean install``` | This cleans the folders and starts building the solution pack from scratch. |
   | ```mvn clean install -P solution``` | This cleans the folders and starts building the solution pack from scratch. Finally creates the toolkit zip files containing the artifacts required to setup the toolkit. |

1. Once the build is created, navigate to the relevant folder to get the toolkit for each product.

|  Product | Toolkit Path |
      | :--- |:--- |
| ```Identity Server``` | `/reference-implementation-consumerdatastandards-au/toolkits/ob-is/target` |
| ```API Manager``` | `/reference-implementation-consumerdatastandards-au/toolkits/ob-apim/target` |


### Running the products

Please refer the following READ.ME files to run the products.

|  Product | Instructions Path |
| :--- |:--- |
| ```Identity Server``` | `/wso2ob-is-toolkit-cds-1.0.0/README.md` |
| ```API Manager``` | `/wso2ob-apim-toolkit-cds-1.0.0/README.md` |


### Reporting Issues

We encourage you to report issues, documentation faults, and feature requests regarding the WSO2 Consumer Data Standards AU Reference Implementation through the [WSO2 OB Reference Toolkit CDS Issue Tracker](https://github.com/wso2/reference-implementation-consumerdatastandards-au/issues).

### License

This source is licensed under the Apache License Version 2.0 ([LICENSE](LICENSE)).
