#!/bin/bash

# Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
#
# WSO2 LLC. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

set -o xtrace

HOME=`pwd`
TEST_SCRIPT=test.sh
MVNSTATE=1 #This variable is read by the test-grid to determine success or failure of the build. (0=Successful)

function usage()
{
    echo "
    Usage bash test.sh --input-dir /workspace/data-bucket.....
    Following are the expected input parameters. all of these are optional
    --input-dir       | -i    : input directory for test.sh
    --output-dir      | -o    : output directory for test.sh
    "
}

#=== FUNCTION ==================================================================
# NAME: get_prop
# DESCRIPTION: Retrieve specific property from deployment.properties file
# PARAMETER 1: property_value
#===============================================================================
function get_prop {
    local prop=$(grep -w "${1}" "${INPUT_DIR}/deployment.properties" | cut -d'=' -f2)
    echo $prop
}

optspec=":hiom-:"
while getopts "$optspec" optchar; do
    case "${optchar}" in
        -)
            case "${OPTARG}" in
                input-dir)
                    val="${!OPTIND}"; OPTIND=$(( $OPTIND + 1 ))
                    INPUT_DIR=$val
                    ;;
                output-dir)
                    val="${!OPTIND}"; OPTIND=$(( $OPTIND + 1 ))
                    OUTPUT_DIR=$val
                    ;;
                mvn-opts)
                    val="${!OPTIND}"; OPTIND=$(( $OPTIND + 1 ))
                    MAVEN_OPTS=$val
                    ;;
                *)
                    usage
                    if [ "$OPTERR" = 1 ] && [ "${optspec:0:1}" != ":" ]; then
                        echo "Unknown option --${OPTARG}" >&2
                    fi
                    ;;
            esac;;
        h)
            usage
            exit 2
            ;;
        o)
            OUTPUT_DIR=$val
            ;;
        m)
            MVN_OPTS=$val
            ;;
        i)
            INPUT_DIR=$val
            ;;
        *)
            usage
            if [ "$OPTERR" != 1 ] || [ "${optspec:0:1}" = ":" ]; then
                echo "Non-option argument: '-${OPTARG}'" >&2
            fi
            ;;
    esac
done

export DATA_BUCKET_LOCATION=${INPUT_DIR}

cat ${INPUT_DIR}/deployment.properties

echo "--- Go to reference-implementation-consumerdatastandards-au folder"
cd ../../
SOURCE_HOME=`pwd`
APIS_HOME=${SOURCE_HOME}/toolkits/ob-apim/repository/resources/apis/consumerdatastandards.org.au
TEST_FRAMEWORK_HOME=${SOURCE_HOME}/integration-test-suite/cds-toolkit-test-framework
TEST_CONFIG_FILE=${TEST_FRAMEWORK_HOME}/src/main/resources/TestConfiguration.xml
TEST_ARTIFACTS=${SOURCE_HOME}/integration-test-suite/test-artifacts
API_VERSION=$(get_prop "ApiVersion")

echo "Test Suite execution for API Version : ${API_VERSION}"

#--------------set configs in TestConfiguration.xml-----------------#
cp ${TEST_FRAMEWORK_HOME}/src/main/resources/SampleTestConfiguration.xml ${TEST_CONFIG_FILE}

sed -i -e "s|Common.ApiVersion|${API_VERSION}|g" $TEST_CONFIG_FILE
sed -i -e "s|{TestSuiteDirectoryPath}|${SOURCE_HOME}|g" $TEST_CONFIG_FILE

##----------------set hostnames for sequences -----------#
sed -i -e "s|{AM_HOST}|$(get_prop "ApimHostname")|g" $TEST_CONFIG_FILE
sed -i -e "s|{IS_HOST}|$(get_prop "IamHostname")|g" $TEST_CONFIG_FILE

# Set client trust store
#TODO:If Remote Server, Need to copy the client-truststore.jks manually.
cp $(get_prop "ApimServerPath")/repository/resources/security/client-truststore.jks ${TEST_ARTIFACTS}/am-certs/

# Set Web Browser Configuration
sed -i -e "s|BrowserAutomation.HeadlessEnabled|$(get_prop "BrowserAutomation.HeadlessEnabled")|g" $TEST_CONFIG_FILE
if [ $(get_prop "OSName") == "mac" ]; then
    sed -i -e "s|BrowserAutomation.WebDriverLocation|${TEST_ARTIFACTS}/selenium-libs/firefox/mac/geckodriver|g" $TEST_CONFIG_FILE
else
    sed -i -e "s|BrowserAutomation.WebDriverLocation|${TEST_ARTIFACTS}/selenium-libs/firefox/ubuntu/geckodriver|g" $TEST_CONFIG_FILE
fi

# Database Configurations
sed -i -e "s|DataBaseConfiguration.DBType|$(get_prop "DbType")|g" $TEST_CONFIG_FILE
sed -i -e "s|DataBaseConfiguration.DBServerHost|$(get_prop "DBServerHost")|g" $TEST_CONFIG_FILE
sed -i -e "s|DataBaseConfiguration.DBUsername|$(get_prop "DBUsername")|g" $TEST_CONFIG_FILE
sed -i -e "s|DataBaseConfiguration.DBPassword|$(get_prop "DBPassword")|g" $TEST_CONFIG_FILE
sed -i -e "s|DataBaseConfiguration.DBDriverClass|$(get_prop "DBDriverClass")|g" $TEST_CONFIG_FILE
sed -i -e "s|DataBaseConfiguration.OracleDBSID|$(get_prop "OracleDBSID")|g" $TEST_CONFIG_FILE

#--------------build the Base test framework-----------------#
git clone https://github.com/wso2/financial-services-accelerator.git
cd financial-services-accelerator/integration-test-framework
mvn clean install

#--------------build the CDS Toolkit test Framework-----------------#
cd ${TEST_FRAMEWORK_HOME}
echo "${TEST_FRAMEWORK_HOME}/cds-toolkit-test-framework"
mvn clean install

#--------------run the Pre Configuration Steps (Create Key Manager, Publish APIs, Create Users, Create Common TPP Application)-----------------#
cd ${TEST_FRAMEWORK_HOME}/../cds-toolkit-preconfiguration-test/
mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -fae -B -f pom.xml
  MVNSTATE=$((MVNSTATE+$?))
  mkdir -p ${OUTPUT_DIR}/scenarios/au-$API_VERSION
  find . -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios/ \;

#--------------run the Test Suite - CDS Scenarios-----------------#
cd ${TEST_FRAMEWORK_HOME}/../cds-toolkit-integration-test/
if [ $(get_prop "TestType") == "Smoke" ]; then
  SMOKE_TESTNG=${TEST_FRAMEWORK_HOME}/../cds-toolkit-integration-test/src/test/resources/testngSmokeTest.xml
  mvn clean install -DgroupToRun=SmokeTest -Dsurefire.suiteXmlFiles=${SMOKE_TESTNG} -fae -B -f pom.xml
  MVNSTATE=$((MVNSTATE+$?))
  mkdir -p ${OUTPUT_DIR}/scenarios/au-$API_VERSION
  find . -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios/ \;
else
  mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -fae -B -f pom.xml
  MVNSTATE=$((MVNSTATE+$?))
  mkdir -p ${OUTPUT_DIR}/scenarios/au-$API_VERSION
  find . -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios/ \;
fi
