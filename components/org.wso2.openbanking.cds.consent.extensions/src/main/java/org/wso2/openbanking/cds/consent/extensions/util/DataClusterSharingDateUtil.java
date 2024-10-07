/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
package org.wso2.openbanking.cds.consent.extensions.util;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.SPQueryExecutorUtil;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.utils.CommonConstants;
import org.wso2.openbanking.cds.consent.extensions.model.DataClusterSharingDateModel;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for data cluster sharing date related operations.
 */
public class DataClusterSharingDateUtil {
    private static final Log log = LogFactory.getLog(DataClusterSharingDateUtil.class);

    /**
     * Get sharing date map for a given consent ID.
     *
     * @param consentId
     * @throws OpenBankingException
     */

    public static Map<String, DataClusterSharingDateModel> getSharingDateMap(String consentId)
            throws OpenBankingException {

        OpenBankingCDSConfigParser configParser = OpenBankingCDSConfigParser.getInstance();
        String spApiHost = configParser.getConfiguration().get(CommonConstants.SP_SERVER_URL).toString();
        String spUserName = configParser.getConfiguration().get(CommonConstants.SP_USERNAME).toString();
        String spPassword = configParser.getConfiguration().get(CommonConstants.SP_PASSWORD).toString();

        String appName = "CDSSharingDateSummarizationApp";
        String spQuery = "from CDS_SHARING_START_END_DATE select CONSENT_ID, DATA_CLUSTER, " +
                "SHARING_START_DATE, LAST_SHARED_DATE having CONSENT_ID == '" + consentId + "';";
        try {
            JSONObject sharingDateJsonObject = SPQueryExecutorUtil
                    .executeQueryOnStreamProcessor(appName, spQuery, spUserName, spPassword, spApiHost);
            return getListFromSharingDateData(sharingDateJsonObject);
        } catch (OpenBankingException | IOException | ParseException e) {
            log.error("Error occurred while retrieving sharing dates for consent ID: " + consentId, e);
            throw new OpenBankingException("Error occurred while retrieving sharing date", e);
        }
    }

    /**
     * Convert JSON object returned from sharing date table to a map.
     *
     * @param sharingDateJsonObject
     */
    static Map<String, DataClusterSharingDateModel> getListFromSharingDateData(JSONObject sharingDateJsonObject) {

        JSONArray recordsArray = (JSONArray) sharingDateJsonObject.get("records");
        Map<String, DataClusterSharingDateModel> sharingDateMap = new HashMap<>();

        for (Object object : recordsArray) {
            JSONArray countArray = (JSONArray) object;
            String dataCluster = (String) (countArray.get(1));
            Timestamp sharingStartDate = new Timestamp(((Integer) countArray.get(2)).longValue() * 1000L);
            ;
            Timestamp lastSharedDate = new Timestamp(((Integer) countArray.get(3)).longValue() * 1000L);
            ;

            DataClusterSharingDateModel sharingDates = new DataClusterSharingDateModel();
            sharingDates.setDataCluster(dataCluster);
            sharingDates.setSharingStartDate(sharingStartDate);
            sharingDates.setLastSharedDate(lastSharedDate);

            sharingDateMap.put(dataCluster, sharingDates);
        }
        return sharingDateMap;
    }
}
