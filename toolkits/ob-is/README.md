### **WSO2 Open Banking IS Accelerator 1.0.0**

**Try Locally:**

Prerequisites:
1. Download WSO2 IS product
2. Download the WSO2 Open Banking Identity Server Accelerator
3. Download the WSO2 Open Banking Identity Server CDS Toolkit
4. Setup MySQL database server
5. Install Java on your local machine

Steps:
1. Extract WSO2 IS <WSO2_IS_HOME>
2. Extract WSO2 OB IS Accelerator to WSO2_IS_HOME
3. Run <WSO2_OB_IS_ACC_HOME>/bin/merge.sh. This will copy the artifacts to the WSO2 IS
4. Run <WSO2_OB_IS_ACC_HOME>/bin/configure.sh. This will configure the server and create databases and  tables.
5. Extract WSO2 OB IS CDS Toolkit to WSO2_IS_HOME
6. Run <WSO2_OB_IS_CDS_TOOLKIT_HOME>/bin/merge.sh. This will copy the artifacts to the WSO2 IS
7. Run <WSO2_OB_IS_CDS_TOOLKIT_HOME>/bin/configure.sh. This will configure the server.
8. Run <WSO2_IS_HOME>/bin/wso2server.sh to start the server

