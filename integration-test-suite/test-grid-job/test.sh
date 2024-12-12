#!/bin/bash

# Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
API_VERSION="1.3.0"

echo "Test Suite execution for API Version : CDS 1.3.0"

#--------------set configs in test-config.xml-----------------#
cp ${TEST_FRAMEWORK_HOME}/src/main/resources/TestConfigurationExample.xml ${TEST_CONFIG_FILE}

sed -i -e "s|Common.SolutionVersion|3.0.0|g" $TEST_CONFIG_FILE
sed -i -e "s|Common.ApiVersion|$(get_prop "ApiVersion")|g" $TEST_CONFIG_FILE
sed -i -e "s|Common.AccessTokenExpireTime|30|g" $TEST_CONFIG_FILE
sed -i -e "s|Common.TenantDomain|carbon.super|g" $TEST_CONFIG_FILE
sed -i -e "s|Common.SigningAlgorithm|PS256|g" $TEST_CONFIG_FILE
sed -i -e "s|Provisioning.Enabled|true|g" $TEST_CONFIG_FILE
sed -i -e "s|Provisioning.ProvisionFilePath|${TEST_FRAMEWORK_HOME}/src/main/resources/provisioningFiles/api-config-provisioning.yaml|g" $TEST_CONFIG_FILE

#----------------set hostnames for sequences -----------#
#__replace hostname before deploy
sed -i -e "s|Server.BaseURL|https://$(get_prop "ApimHostname"):8243|g" $TEST_CONFIG_FILE
sed -i -e "s|Server.GatewayURL|https://$(get_prop "ApimHostname"):9443|g" $TEST_CONFIG_FILE
sed -i -e "s|Server.AuthorisationServerURL|https://$(get_prop "IamHostname"):9446|g" $TEST_CONFIG_FILE
sed -i -e "s|Server.SharableAccountsURL|http://$(get_prop "ApimHostname"):9763|g" $TEST_CONFIG_FILE

# configs for application 1
sed -i -e "s|AppConfig1.KeyStore.Location|${TEST_ARTIFACTS}/DynamicClientRegistration/AU/sample-client-resources/signing-keystore/signing.jks|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig1.KeyStore.Alias|tpp7-signing|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig1.KeyStore.Password|wso2carbon|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig1.KeyStore.DomainName|https://wso2.com|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig1.KeyStore.SigningKid|7eJ8S_ZgvlYxFAFSghV9xMJROvk|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig1.Transport.MTLSEnabled|true|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig1.Transport.KeyStore.Location|${TEST_ARTIFACTS}/DynamicClientRegistration/AU/sample-client-resources/transport-keystore/transport.jks|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig1.Transport.KeyStore.Type|jks|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig1.Transport.KeyStore.Password|wso2carbon|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig1.DCR.SSAPath|${TEST_ARTIFACTS}/DynamicClientRegistration/AU/sample-client-resources/ssa.txt|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig1.DCR.SoftwareId|SP2|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig1.DCR.RedirectUri|https://www.google.com/redirects/redirect1|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig1.DCR.AlternateRedirectUri|https://www.google.com/redirects/redirect2|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig1.Application.ClientID|Application.ClientID|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig1.Application.RedirectURL|https://www.google.com/redirects/redirect1|g" $TEST_CONFIG_FILE

# configs for application 2
sed -i -e "s|AppConfig2.KeyStore.Location|${TEST_ARTIFACTS}/DynamicClientRegistration/AU/sample-client-resources-2/signing-keystore/signing.jks|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.KeyStore.Alias|tpp7-signing|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.KeyStore.Password|wso2carbon|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.KeyStore.DomainName|https://wso2.com|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.KeyStore.SigningKid|7eJ8S_ZgvlYxFAFSghV9xMJROvk|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.Transport.MTLSEnabled|true|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.Transport.KeyStore.Location|${TEST_ARTIFACTS}/DynamicClientRegistration/AU/sample-client-resources-2/transport-keystore/transport.jks|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.Transport.KeyStore.Type|jks|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.Transport.KeyStore.Password|wso2carbon|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.DCR.SSAPath|${TEST_ARTIFACTS}/DynamicClientRegistration/AU/sample-client-resources-2/ssa.txt|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.DCR.SoftwareId|SP3|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.DCR.RedirectUri|https://www.google.com/redirects/redirect1|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.DCR.AlternateRedirectUri|https://www.google.com/redirects/redirect2|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.Application.ClientID|Application.ClientID|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.Application.RedirectURL|https://www.google.com/redirects/redirect1|g" $TEST_CONFIG_FILE

# Set client trust store
#TODO:If Remote Server, Need to copy the client-truststore.jks manually.
cp $(get_prop "ApimServerPath")/repository/resources/security/client-truststore.jks ${TEST_ARTIFACTS}/am-certs/
sed -i -e "s|Transport.Truststore.Location|${TEST_ARTIFACTS}/am-certs/client-truststore.jks|g" $TEST_CONFIG_FILE

sed -i -e "s|Transport.Truststore.Type|jks|g" $TEST_CONFIG_FILE
sed -i -e "s|Transport.Truststore.Password|wso2carbon|g" $TEST_CONFIG_FILE

sed -i -e "s|BrowserAutomation.BrowserPreference|firefox|g" $TEST_CONFIG_FILE
sed -i -e "s|BrowserAutomation.HeadlessEnabled|$(get_prop "BrowserAutomation.HeadlessEnabled")|g" $TEST_CONFIG_FILE
if [ $(get_prop "OSName") == "mac" ]; then
    sed -i -e "s|BrowserAutomation.WebDriverLocation|${TEST_ARTIFACTS}/selenium-libs/mac/geckodriver|g" $TEST_CONFIG_FILE
else
    sed -i -e "s|BrowserAutomation.WebDriverLocation|${TEST_ARTIFACTS}/selenium-libs/ubuntu/geckodriver|g" $TEST_CONFIG_FILE
fi

sed -i -e "s|ConsentApi.AudienceValue|https://$(get_prop "IamHostname"):9446/oauth2/token|g" $TEST_CONFIG_FILE
sed -i -e "s|ConsentApi.RevocationAudienceValue|https://$(get_prop "IamHostname"):9446/oauth2/revoke|g" $TEST_CONFIG_FILE

#Test Related Additional Configurations
sed -i -e "s|AUMockCDRRegister.Enabled|false|g" $TEST_CONFIG_FILE
sed -i -e "s|IdPermanence.SecretKey|wso2|g" $TEST_CONFIG_FILE
sed -i -e "s|ProfileSelection.Enabled|true|g" $TEST_CONFIG_FILE

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

#--------------run the Test Suite-----------------#
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
