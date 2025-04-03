#!/bin/bash
# ------------------------------------------------------------------------
#
# Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
#
# WSO2 LLC. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#
# ------------------------------------------------------------------------

# merge.sh script copy the WSO2 OB BI CDS Toolkit artifacts on top of WSO2 SI base product
#
# merge.sh <WSO2_OB_BI_HOME>

WSO2_OB_BI_HOME=$1

# set toolkit home
cd ../
TOOLKIT_HOME=$(pwd)
echo "Toolkit home is: ${TOOLKIT_HOME}"

# set product home
if [ "${WSO2_OB_BI_HOME}" == "" ];
  then
    cd ../
    WSO2_OB_BI_HOME=$(pwd)
    echo "Product home is: ${WSO2_OB_BI_HOME}"
fi

# validate product home
if [ ! -d "${WSO2_OB_BI_HOME}/deployment/siddhi-files" ]; then
  echo -e "\n\aERROR:specified product path is not a valid carbon product path\n";
  exit 2;
else
  echo -e "\nValid carbon product path.\n";
fi

echo -e "\nRemoving default APIRawDataSubmissionApp\n"
echo -e "================================================\n"
rm ${WSO2_OB_BI_HOME}/deployment/siddhi-files/APIRawDataSubmissionApp.siddhi

echo -e "\nRemoving default APILatencyRawDataApp\n"
echo -e "================================================\n"
rm ${WSO2_OB_BI_HOME}/deployment/siddhi-files/APILatencyRawDataApp.siddhi

echo -e "\nRemoving default AccessTokenRawDataApp\n"
echo -e "================================================\n"
if [ -f "${WSO2_OB_BI_HOME}/deployment/siddhi-files/AccessTokenRawDataApp.siddhi" ]; then
  rm "${WSO2_OB_BI_HOME}/deployment/siddhi-files/AccessTokenRawDataApp.siddhi"
fi

echo -e "\nRemoving default AuthenticationDataSubmissionApp\n"
echo -e "================================================\n"
if [ -f "${WSO2_OB_BI_HOME}/deployment/siddhi-files/AuthenticationDataSubmissionApp.siddhi" ]; then
  rm ${WSO2_OB_BI_HOME}/deployment/siddhi-files/AuthenticationDataSubmissionApp.siddhi
fi

echo -e "\nCopying open banking artifacts\n"
echo -e "================================================\n"
cp -r ${TOOLKIT_HOME}/carbon-home/* "${WSO2_OB_BI_HOME}"/
echo -e "\nComplete!\n"
