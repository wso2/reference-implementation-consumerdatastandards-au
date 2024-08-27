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
package org.wso2.openbanking.cds.identity.auth.extensions.request.validator.util;

/**
 * Data provider class for Request Object.
 */
public class ReqObjectTestDataProvider {

    public static final String SCOPES_INVALID_REQ = "eyJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IkR3TUtkV01tajdQ" +
            "V2ludm9xZlF5WFZ6eVo2USJ9.eyJhdWQiOiJodHRwczovL2xvY2FsaG9zdDo4MjQzL3Rva2VuIiwicmVzcG9uc2VfdHlwZSI6ImN" +
            "vZGUgaWRfdG9rZW4iLCJjbGllbnRfaWQiOiJpVEtPZnVxejQ2WTFIVlkyQkYwWjdKTTE4QXdhIiwicmVkaXJlY3RfdXJpIjoiaHR0" +
            "cHM6Ly8xMC4xMTAuNS4yMzI6ODAwMC90ZXN0L2EvYXBwMS9jYWxsYmFjayIsInNjb3BlIjoiYWNjb3VudHMiLCJzdGF0ZSI6ImFmMG" +
            "lmanNsZGtqIiwibm9uY2UiOiJuLTBTNl9XekEyTWoiLCJjbGFpbXMiOnsic2hhcmluZ19kdXJhdGlvbiI6IjcyMDAiLCJpZF90b2tl" +
            "biI6eyJhY3IiOnsiZXNzZW50aWFsIjp0cnVlLCJ2YWx1ZXMiOlsidXJuOmNkcy5hdTpjZHI6MyJdfSwib3BlbmJhbmtpbmdfaW50ZW" +
            "50X2lkIjp7InZhbHVlIjoiZDMwMmI4ODgtNzM3Zi00YzQ5LTk4ZmUtNmVkZGU4OTk2ZDZlIiwiZXNzZW50aWFsIjp0cnVlfX0sInV" +
            "zZXJpbmZvIjp7ImdpdmVuX25hbWUiOm51bGwsImZhbWlseV9uYW1lIjpudWxsfX19.ozR7gArjVF8pRxyOxw-zu_pIM3WJo8fYmuUT" +
            "zhk0werT7taPUtP-TAPE-AL9oD1t5cgwvN_fXcH8T4c2T-V8g6IvF7MAq60A9lN9mYNzYmHJH9OkmUb6k1dUfxBqkDkrZvKC-FQUsY" +
            "MVIo5Y84RlXbkqL4TiODKUtqgyP3pzwG5Qc1ovu62nCbpoC2-bsmDQS9AK61o7Iy8b576tcyBp8J6H6iPBYhQvg3NzqXGbyxzfFQIR" +
            "8j9bpiPN3g9FfsImVoNeHO6lwqR3uVZVYqFn3s9L_JLRQdrrpp_9k6Tg7TTZyY59zSc3jAG7rxiZt0oExWsSU4dpkUyh6qSOkMFWJg";

    public static final String REQUEST_STRING = "eyJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IkR3TUtkV01tajdQV2ludm9x" +
            "ZlF5WFZ6eVo2USJ9.eyJhdWQiOiJodHRwczovL2xvY2FsaG9zdDo4MjQzL3Rva2VuIiwicmVzcG9uc2VfdHlwZSI6ImNvZGUgaWR" +
            "fdG9rZW4iLCJjbGllbnRfaWQiOiJpVEtPZnVxejQ2WTFIVlkyQkYwWjdKTTE4QXdhIiwicmVkaXJlY3RfdXJpIjoiaHR0cHM6Ly8" +
            "xMC4xMTAuNS4yMzI6ODAwMC90ZXN0L2EvYXBwMS9jYWxsYmFjayIsInNjb3BlIjoib3BlbmlkIGJhbms6YWNjb3VudHMuYmFzaWM6" +
            "cmVhZCBiYW5rOmFjY291bnRzLmRldGFpbDpyZWFkIGJhbms6dHJhbnNhY3Rpb25zOnJlYWQiLCJzdGF0ZSI6ImFmMGlmanNsZGtqIi" +
            "wibm9uY2UiOiJuLTBTNl9XekEyTWoiLCJjbGFpbXMiOnsic2hhcmluZ19kdXJhdGlvbiI6IjcyMDAiLCJpZF90b2tlbiI6eyJhY3Ii" +
            "OnsiZXNzZW50aWFsIjp0cnVlLCJ2YWx1ZXMiOlsidXJuOmNkcy5hdTpjZHI6MyJdfX0sInVzZXJpbmZvIjp7ImdpdmVuX25hbWUiOm" +
            "51bGwsImZhbWlseV9uYW1lIjpudWxsfX19.cnKvzjgiDWJ2JeRGL8ncTKB_pCxEynNHn6kzHSPBBXYRJ5e-WvPocTkvaDnwu1qSr" +
            "5lsJnFCNgYuNickzoIaTl9wUvl0rnK15iGVe0rSOCWIJ53eVphaV9uYtRfVHTN4HL4ecgdsREHhu6MyjYcqdgAeuv4g0robZGf" +
            "DDVCLr2Xb77f8yAr42xc6fBccAFnvZX33zVOHtFaY3S3j9RbQqRZjUxLycIgdVXGypRc2ESVKqJ9WgGxKG6fCUt2rDgqsobVj" +
            "8ekRAMyP2fGmYLoRAyycJ8JwU9uoRhGL6nqM6_uOYNG5a6xOsSs8i1Yvn4s7G6FUKQ_bmm4Gx2aJptzVA";

}
