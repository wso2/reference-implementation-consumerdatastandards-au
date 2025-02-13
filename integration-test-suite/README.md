# Integration Test Suite (integration-test-suite)

This test suite covers all the functional scenarios and use cases of CDS Toolkit.

### How to run Integration Test Suite (integration-test-suite)

#### 1. End-to-End Test Suite Execution via Script

`end-to-end-test-suite-execution` is a directory which contains a script that covers all the functional scenarios and use cases of CDS Toolkit.

This directory contains the following files:
* [test.sh](end-to-end-test-suite-execution%2Ftest.sh) - This script is used to run the end-to-end test suite.

  Script it-self contains the step to configure the Test Configuration file which is use as an input file to the test suite.
  And also it builds the relevant test frameworks and run the test suite according to the user requirement.

* [deployment.properties](end-to-end-test-suite-execution%2Fdeployment.properties) - This file contains the configurations required to run the test.sh script.

  This file is the input to the test.sh script.

#### How to run End-to-End Test Suite (end-to-end-test-suite-execution)

1. Clone the main branch of the [reference-implementation-consumerdatastandards-au](https://github.com/wso2/reference-implementation-consumerdatastandards-au)
2. Goto "integration-test-suite/end-to-end-test-suite-execution" directory in main branch of "reference-implementation-consumerdatastandards-au" repository.
3. Configure the deployment.properties file with required data.
4. Run the test.sh script. Goto the end-to-end-test-suite-execution directory.

   Command : `./test.sh --input-dir <path to the end-to-end-test-suite-execution directory> --output-dir <path to the output directory>`

5. Reports will be generated in the target folder inside the [cds-toolkit-integration-test](..%2Fcds-toolkit-integration-test) module.

This script can easily be run in any environment or in a CI/CD pipeline.


#### 2. Run Test Suite without Script

1. Clone the master branch of the [financial-open-banking](https://github.com/wso2-enterprise/financial-open-banking/tree/master) repository.
2. Goto "integration-test-suite" and build the module (These are the base modules of open banking test framework);
   1. [integration-test-framework](https://github.com/wso2/financial-services-accelerator/tree/main/integration-test-framework)

    Command : `mvn clean install`

3. Then goto the "[integration-test-suite](https://github.com/wso2/reference-implementation-consumerdatastandards-au/tree/main/integration-test-suite)" 
   module in main branch of "reference-implementation-consumerdatastandards-au" repository.
4. Goto the [resources]https://github.com/wso2/reference-implementation-consumerdatastandards-au/tree/main/integration-test-suite/cds-toolkit-test-framework/src/main/resources) 
   folder in cds-toolkit-test-framework.
5. Take a copy SampleTestConfiguration.xml to the same location and rename it as **TestConfiguration.xml**.
6. Configure the TestConfiguration.xml file by replacing the placeholders.
7. Then build the "[cds-toolkit-test-framework](cds-toolkit-test-framework)" 

   Command : `mvn clean install`

8. Then goto the "[cds-toolkit-integration-test](cds-toolkit-integration-test)" and build the module by skipping the tests.

   Command : `mvn clean install -Dmaven.test.skip=true`

9. Then you can run the test cases via;
    1. Maven Command: `mvn clean install`
    2. Test Class from IDE (Eg: IntelliJ IDEA): Click on run button in front of the class
    3. Run testng.xml: Goto [testng.xml](integration-test-suite/cds-toolkit-integration-test/src/test/resources/testng.xml) 
    in resources folder and right click can run the test suite.

10. Reports will be generated cds-toolkit-integration-test/target/surefire-reports folder.
