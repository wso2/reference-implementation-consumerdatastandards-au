# Integration Test Suite (integration-test-suite)

This test suite covers all the functional scenarios and use cases of CDS Toolkit.

### How to run Integration Test Suite (integration-test-suite)

1. Clone the master branch of the [financial-open-banking](https://github.com/wso2-enterprise/financial-open-banking/tree/master) repository.
2. Goto "integration-test-suite" and build the module (These are the base modules of open banking test framework);
   1. [integration-test-framework](https://github.com/wso2/financial-services-accelerator/tree/main/integration-test-framework)

    Command : `mvn clean install`

3. Then goto the "[integration-test-suite](https://github.com/wso2/reference-implementation-consumerdatastandards-au/tree/main/integration-test-suite)" 
   module in main branch of "reference-implementation-consumerdatastandards-au" repository.
4. Goto the [resources]https://github.com/wso2/reference-implementation-consumerdatastandards-au/tree/main/integration-test-suite/cds-toolkit-test-framework/src/main/resources) 
   folder in cds-toolkit-test-framework.
5. Take a copy TestConfigurationExample.xml to the same location and rename it as **TestConfiguration.xml**.
6. Configure the TestConfiguration.xml file according to the example given in the file it-self.
7. Then build the "[cds-toolkit-test-framework](cds-toolkit-test-framework)" 

   Command : `mvn clean install`

8. Then goto the "[cds-toolkit-integration-test](cds-toolkit-integration-test)" and build the module by skipping the tests.

   Command : `mvn clean install -Dmaven.test.skip=true`

9. Before running the test suite, make sure to create the following users with subscribe permissions.
    1. psu1@wso2.com (Password: wso2123)
    2. nominatedUser1@wso2.com (Password: wso2123)
    3. nominatedUser2@wso2.com (Password: wso2123)
    4. amy@gold.com (Password: wso2123)

10. Then you can run the test cases via;
    1. Maven Command: `mvn clean install`
    2. Test Class from IDE (Eg: IntelliJ IDEA): Click on run button in front of the class
    3. Run testng.xml: Goto [testng.xml](integration-test-suite/cds-toolkit-integration-test/src/test/resources/testng.xml) 
    in resources folder and right click can run the test suite.
11. Reports will be generated cds-toolkit-integration-test/target/surefire-reports folder.
