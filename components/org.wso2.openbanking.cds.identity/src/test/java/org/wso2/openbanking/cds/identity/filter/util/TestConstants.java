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
package org.wso2.openbanking.cds.identity.filter.util;

/**
 * Test Constants class.
 */
public class TestConstants {
    public static final String TARGET_STREAM = "targetStream";
    public static final String CERTIFICATE_HEADER = "x-wso2-mutual-auth-cert";
    public static final String CERTIFICATE_CONTENT = "-----BEGIN CERTIFICATE-----" +
            "MIIFODCCBCCgAwIBAgIEWcWGxDANBgkqhkiG9w0BAQsFADBTMQswCQYDVQQGEwJH" +
            "QjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxLjAsBgNVBAMTJU9wZW5CYW5raW5nIFBy" +
            "ZS1Qcm9kdWN0aW9uIElzc3VpbmcgQ0EwHhcNMTkwNTE2MDg0NDQ2WhcNMjAwNjE2" +
            "MDkxNDQ2WjBhMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxGzAZ" +
            "BgNVBAsTEjAwMTU4MDAwMDFIUVFyWkFBWDEfMB0GA1UEAxMWc0Zna2k3Mk9pcXda" +
            "TkZPWmc2T2FqaTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANoVwx4E" +
            "iWnQs89lj8vKSy/xTbZU2AHS9tFNz7wVa+rkpFyLVPtQW8AthG4hlfrBYMne7/P9" +
            "c1Fi/q+n7eomWvJJo44GV44GJhegM6yyRaIcQdpxe9x9G4twWK4cY+VU3TfE6Dbd" +
            "DdmAt7ai4KFbbpB33N8RwXoeGZdwxZFNPmfaoZZbz5p9+aSMQf1UyExcdlPXah77" +
            "PDZDwAnyy5kYXUPS59S78+p4twqZXyZu9hd+Su5Zod5UObRJ4F5LQzZPS1+KzBje" +
            "JM0o8qoRRZTZkLNnmmQw503KXp/LCLrSbFU2ZLGy3bQpKFFc5I6tZiy67ELNzLWo" +
            "DzngEbApwhX+jtsCAwEAAaOCAgQwggIAMA4GA1UdDwEB/wQEAwIHgDAgBgNVHSUB" +
            "Af8EFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwgeAGA1UdIASB2DCB1TCB0gYLKwYB" +
            "BAGodYEGAWQwgcIwKgYIKwYBBQUHAgEWHmh0dHA6Ly9vYi50cnVzdGlzLmNvbS9w" +
            "b2xpY2llczCBkwYIKwYBBQUHAgIwgYYMgYNVc2Ugb2YgdGhpcyBDZXJ0aWZpY2F0" +
            "ZSBjb25zdGl0dXRlcyBhY2NlcHRhbmNlIG9mIHRoZSBPcGVuQmFua2luZyBSb290" +
            "IENBIENlcnRpZmljYXRpb24gUG9saWNpZXMgYW5kIENlcnRpZmljYXRlIFByYWN0" +
            "aWNlIFN0YXRlbWVudDBtBggrBgEFBQcBAQRhMF8wJgYIKwYBBQUHMAGGGmh0dHA6" +
            "Ly9vYi50cnVzdGlzLmNvbS9vY3NwMDUGCCsGAQUFBzAChilodHRwOi8vb2IudHJ1" +
            "c3Rpcy5jb20vb2JfcHBfaXNzdWluZ2NhLmNydDA6BgNVHR8EMzAxMC+gLaArhilo" +
            "dHRwOi8vb2IudHJ1c3Rpcy5jb20vb2JfcHBfaXNzdWluZ2NhLmNybDAfBgNVHSME" +
            "GDAWgBRQc5HGIXLTd/T+ABIGgVx5eW4/UDAdBgNVHQ4EFgQU5eqvEZ6ZdQS5bq/X" +
            "dzP5XY/fUXUwDQYJKoZIhvcNAQELBQADggEBAIg8bd/bIh241ewS79lXU058VjCu" +
            "JC+4QtcI2XiGV3dBpg10V6Kb6E/h8Gru04uVZW1JK52ivVb5NYs6r8txRsTBIaA8" +
            "Cr03LJqEftclL9NbkPZnpEkUfqCBfujNQF8XWaQgXIIA+io1UzV1TG3K9XCa/w2S" +
            "sTANKfF8qK5kRsy6z9OGPUE+Oi3DUt+E9p5LCq6n5Bkp9YRGmyYRPs8JMkJmq3sf" +
            "wtXOy27LE4exJRuZsF1CA78ObaRytuE3DJcnIRdhOcjWieS/MxZD7bzuuAPu5ySX" +
            "i2/qxT3AlWtHtxrz0mKSC3rlgYAHCzCAHoASWKpf5tnB3TodPVZ6DYOu7oI=" +
            "-----END CERTIFICATE-----";
    public static final String CLIENT_ASSERTION = "eyJraWQiOiJqeVJVY3l0MWtWQ2xjSXZsVWxjRHVrVlozdFUiLCJhbGciOiJQUzI1" +
            "NiJ9.eyJzdWIiOiJpWXBSbTY0YjJ2bXZtS0RoZEw2S1pEOXo2ZmNhIiwiYXVkIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTQ0My9vYXV0a" +
            "DIvdG9rZW4iLCJpc3MiOiJpWXBSbTY0YjJ2bXZtS0RoZEw2S1pEOXo2ZmNhIiwiZXhwIjoxNjEwNjMxNDEyLCJpYXQiOjE2MTA2MDE" +
            "0MTIsImp0aSI6IjE2MTA2MDE0MTI5MDAifQ.tmMTlCL-VABhFTA6QQ6UPvUydKuzynidepAa8oZGEBfVyAsiW5IF01NKYD0ynpXXJC" +
            "Q6hcbWK0FEGity67p6DeI9LT-xAnaKwZY7H8rbuxWye2vhanM0jVa1vggsmwWYyOR4k55ety9lP1MkcGZpaK48qoaqsX_X7GCSGXzq" +
            "BncTEPYfCpVUQtS4ctwoCl06TFbY2Lfm9E24z1rfmU9xPc7au6LpKRLMMHQ8QXuc-FhnWdgEFv_3tAai2ovVmrqEfwj6Z6Ew5bFeI9" +
            "jtCR4TSol47hzDwldx5rH7m2OPUx66yEtGrM7UU62fC-4nxplZ69fjlHN4KQ62PxEaCQs0_A";

    public static final String CLIENT_ASSERTION_NO_HEADER =
            "eyJraWQiOiJqeVJVY3l0MWtWQ2xjSXZsVWxjRHVrVlozdFUiLCJhbGciOiJQUzI1" +
            "NiJ.eyJzdWIiOiJpWXBSbTY0YjJ2bXZtS0RoZEw2S1pEOXo2ZmNhIiwiYXVkIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTQ0My9vYXV0a" +
            "DIvdG9rZW4iLCJpc3MiOiJpWXBSbTY0YjJ2bXZtS0RoZEw2S1pEOXo2ZmNhIiwiZXhwIjoxNjEwNjMxNDEyLCJpYXQiOjE2MTA2MDE" +
            "0MTIsImp0aSI6IjE2MTA2MDE0MTI5MDAifQ.tmMTlCL-VABhFTA6QQ6UPvUydKuzynidepAa8oZGEBfVyAsiW5IF01NKYD0ynpXXJC" +
            "Q6hcbWK0FEGity67p6DeI9LT-xAnaKwZY7H8rbuxWye2vhanM0jVa1vggsmwWYyOR4k55ety9lP1MkcGZpaK48qoaqsX_X7GCSGXzq" +
            "BncTEPYfCpVUQtS4ctwoCl06TFbY2Lfm9E24z1rfmU9xPc7au6LpKRLMMHQ8QXuc-FhnWdgEFv_3tAai2ovVmrqEfwj6Z6Ew5bFeI9" +
            "jtCR4TSol47hzDwldx5rH7m2OPUx66yEtGrM7UU62fC-4nxplZ69fjlHN4KQ62PxEaCQs0_A";

    public static final String EXTERNAL_TRAFFIC_HEADER = "X-External-Traffic";
    public static final String SESSION_DATA_KEY = "sessionDataKey";

}
