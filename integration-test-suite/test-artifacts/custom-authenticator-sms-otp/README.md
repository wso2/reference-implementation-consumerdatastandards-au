#Introduction to custom-authenticator-sms-otp

This is a custom authenticator use only for testing purpose of Second Factor Authentication.
Custom authenticator was written using the default SMS authenticator (identity-outbound-auth-sms-otp). 

In this authenticator, 
- User prompt to enter a sample sms-otp code after completing the Basic Auth step.
- The sms-otp code can be any code from the list ["123456", "111222", "333444", "555666"].
- Mobile Number step is removed from Custom authenticator.

#How to Deploy Custom SMS OTP Authenticator. 
1. Build the identity-outbound-auth-sms-otp/component/authenticator module. org.wso2.carbon.extension.identity.authenticator.smsotp.connector jar 
will be generated in custom-authenticator-sms-otp/identity-outbound-auth-sms-otp/component/authenticator/target folder.

2. Replace the org.wso2.carbon.extension.identity.authenticator.smsotp.connector jar in 
<IAM_HOME>/repository/components/dropins folder with the generated jar.

3. Restart the OB-IAM server.
