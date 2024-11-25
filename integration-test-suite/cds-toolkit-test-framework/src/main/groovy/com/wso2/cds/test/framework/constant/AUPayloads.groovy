package com.wso2.cds.test.framework.constant

class AUPayloads {

    /**
     * Get the payload for Single User Nomination.
     * @param accountId
     * @param accountOwnerUserID
     * @param nominatedRepUserID
     * @param permissionType
     * @return
     */
    static String getSingleUserNominationPayload(String accountId, String accountOwnerUserID, String nominatedRepUserID,
                                                 String permissionType) {

        return """
               {
                    "data":[
                         {
                         "accountID":"${accountId}",
                         "accountOwners":[                     
                                "${accountOwnerUserID}"
                             ],
                          "nominatedRepresentatives":[
                             {
                                "name": "${nominatedRepUserID}",
                                "permission": "${permissionType}"
                              }
                            ]
                         }
                        ]
                 }
            """.stripIndent()
    }

    /**
     * Get the payload for Multi User Nomination.
     * @param accountId
     * @param accountOwnerUserID
     * @param nominatedRepUserID
     * @param permissionType
     * @return
     */
    static String getMultiUserNominationPayload(String accountId, String accountOwnerUserID, String nominatedRepUserID,
                                                String permissionType, String nominatedRepUserID2, String permissionType2) {

        return """
               {
                    "data":[
                         {
                         "accountID":"${accountId}",
                         "accountOwners":[                     
                                "${accountOwnerUserID}"
                             ],
                          "nominatedRepresentatives":[
                             {
                                "name": "${nominatedRepUserID}",
                                "permission": "${permissionType}"
                              },
                              {
                                "name": "${nominatedRepUserID2}",
                                "permission": "${permissionType2}"
                              }
                            ]
                         }
                        ]
                 }
            """.stripIndent()
    }

    /**
     * Get payload to delete Single Business User Nomination
     * @param accountId
     * @param accountOwnerUserID
     * @param nominatedRepUserID
     * @return
     */
    static String getSingleUserDeletePayload(String accountId, String accountOwnerUserID, String nominatedRepUserID) {

        return """
               {
                  "data":[
                     {
                        "accountID":"${accountId}",
                        "accountOwners":[
                            "${accountOwnerUserID}"
                        ],
                        "nominatedRepresentatives":[
                           "${nominatedRepUserID}"
                        ]
                     }
                  ]
               }
            """.stripIndent()
    }

    /**
     * Get payload to delete Multiple Business User Nomination
     * @param accountId
     * @param accountOwnerUserID
     * @param nominatedRepUserID
     * @return
     */
    static String getMultiUserDeletePayload(String accountId, String accountOwnerUserID, String nominatedRepUserID,
                                            String nominatedRepUserID2) {

        return """
               {
                    "data":[
                         {
                         "accountID":"${accountId}",
                         "accountOwners":[                     
                                "${accountOwnerUserID}"
                             ],
                          "nominatedRepresentatives":[
                             {
                                "name": "${nominatedRepUserID}"
                              },
                              {
                                "name": "${nominatedRepUserID2}"
                              }
                            ]
                         }
                        ]
                 }
            """.stripIndent()
    }

    /**
     * Get the incorrect payload for Single User Nomination.
     * @param accountId
     * @param accountOwnerUserID
     * @param nominatedRepUserID
     * @param permissionType
     * @return
     */
    static String getIncorrectNominationPayload(String accountId, String accountOwnerUserID, String nominatedRepUserID,
                                                String permissionType) {

        return """
               {
                    "data":[
                         {
                         "accountID":${accountId},
                         "accountOwners":[                     
                                "${accountOwnerUserID}"
                             ],
                          "nominatedRepresentatives":[
                             {
                                "name": "${nominatedRepUserID}",
                                "permission": "${permissionType}"
                              }
                            ]
                         }
                        ]
                 }
            """.stripIndent()
    }

    /**
     * Get incorrect payload to delete Single Business User Nomination
     * @param accountId
     * @param accountOwnerUserID
     * @param nominatedRepUserID
     * @return
     */
    static String getIncorrectUserDeletePayload(String accountId, String accountOwnerUserID, String nominatedRepUserID) {

        return """
               {
                  "data":[
                     {
                        "accountID":${accountId},
                        "accountOwners":[
                            "${accountOwnerUserID}"
                        ],
                        "nominatedRepresentatives":[
                           "${nominatedRepUserID}"
                        ]
                     }
                  ]
               }
            """.stripIndent()
    }

    /**
     * Get the payload for Update Disclosure Options Management Service.
     * @param accountId
     * @param disclosureSharingStatus
     * @return
     */
    static String getDOMSStatusUpdatePayload(Map<String, String> domsStatusMap) {

        def requestBody
        String accountId1, accountId2, sharingStatus1, sharingStatus2

        List<Map.Entry<String, String>> entryList = new ArrayList<>(domsStatusMap.entrySet())
        if (entryList.size() > 1) {
            Map.Entry<String, String> entry1 = entryList.get(0)
            accountId1 = entry1.getKey()
            sharingStatus1 = entry1.getValue()

            Map.Entry<String, String> entry2 = entryList.get(1)
            accountId2 = entry2.getKey()
            sharingStatus2 = entry2.getValue()

            return """
            {
               "data":[
                   {
                        "accountID": "${accountId1}",
                        "disclosureOption": "${sharingStatus1}"
                    },              
                    {
                        "accountID": "${accountId2}",
                        "disclosureOption": "${sharingStatus2}"
                    }
               ]
            }
            """.stripIndent()

        } else {
            Map.Entry<String, String> entry1 = entryList.get(0)
            accountId1 = entry1.getKey()
            sharingStatus1 = entry1.getValue()

            return """
            {
               "data":[
                     {
                        "accountID": "${accountId1}",
                        "disclosureOption": "${sharingStatus1}"
                    }
               ]
            }
            """.stripIndent()
        }
    }

    /**
     * Get the payload for Secondary User Instruction Permission Update.
     * @param accountId - Secondary Account ID
     * @param disclosureSharingStatus - Secondary Account Instruction Status
     * @return - Payload
     */
    static String getSecondaryUserInstructionPermissionPayload(String secondaryAccountId, String secondaryUserId,
                                                               String secondaryAccountInstructionStatus = "active",
                                                               boolean otherAccountsAvailability = true) {
        return """
        {
            "data": [
                {
                    "secondaryAccountID": "${secondaryAccountId}",
                    "secondaryUserID": "${secondaryUserId}",
                    "otherAccountsAvailability": ${otherAccountsAvailability},
                    "secondaryAccountInstructionStatus": "${secondaryAccountInstructionStatus}"
                }
            ]
        }
        """.stripIndent()
    }

    /**
     * Get Payload for Block Legal Entity
     * @param secondaryUserId - Secondary User ID
     * @param accountId - Secondary Account ID
     * @param legalEntityId - Legal Entity ID
     * @param sharingStatus - Sharing Status
     * @param isMultipleLegalEntity - Multiple Legal Entity
     * @param secondaryUserId2 - Secondary User ID (Pass only if isMultipleLegalEntity is true)
     * @param accountId2 - Secondary Account ID (Pass only if isMultipleLegalEntity is true)
     * @param legalEntityId2 - Legal Entity ID (Pass only if isMultipleLegalEntity is true)
     * @param sharingStatus2 - Sharing Status (Pass only if isMultipleLegalEntity is true)
     * @return - Payload
     */
    static String getBlockLegalEntityPayload(String secondaryUserId, String accountId, String legalEntityId,
                                             String sharingStatus, boolean isMultipleLegalEntity = false,
                                             String secondaryUserId2 = null, String accountId2 = null,
                                             String legalEntityId2 = null, String sharingStatus2 = null) {

        String payload

        if(isMultipleLegalEntity) {

            payload = """
            {
                "data": [
                    {
                        "accountID": "${accountId}",
                        "secondaryUserID": "${secondaryUserId}",
                        "legalEntitySharingStatus": "${sharingStatus}",
                        "legalEntityID": "${legalEntityId}"
                    }
                ]
            }
            """.stripIndent()

        } else {
            payload = """
            {
                "data": [
                    {
                        "accountID": "${accountId}",
                        "secondaryUserID": "${secondaryUserId}",
                        "legalEntitySharingStatus": "${sharingStatus}",
                        "legalEntityID": "${legalEntityId}"
                    }
                ]
            }
            """.stripIndent()
        }

        return payload
    }
}
