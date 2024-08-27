### **WSO2 Open Banking BI CDS Reference Toolkit 1.0.0**

**Try Locally:**

Prerequisites:
1. Download WSO2 SI product 
2. Download the WSO2 Open Banking Business Intelligence Accelerator 
3. Download the WSO2 Open Banking Business Intelligence CDS Toolkit
4. Setup MySQL database server
5. Install Java on your local machine

Steps:
1. Extract WSO2 SI <WSO2_BI_HOME>
2. Extract WSO2 OB BI Accelerator to WSO2_BI_HOME 
3. Run <WSO2_OB_BI_ACC_HOME>/bin/merge.sh. This will copy the artifacts to the WSO2 SI
4. Run <WSO2_OB_BI_ACC_HOME>/bin/configure.sh. This will configure the server and create databases.
5. Extract WSO2 OB BI CDS Toolkit to WSO2_BI_HOME
6. Run <WSO2_OB_BI_CDS_TOOLKIT_HOME>/bin/merge.sh. This will copy the artifacts to the WSO2 SI.
7. Run <WSO2_BI_HOME>/bin/server.sh to start the server
