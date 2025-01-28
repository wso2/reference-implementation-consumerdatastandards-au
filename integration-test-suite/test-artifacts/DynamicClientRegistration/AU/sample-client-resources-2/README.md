#Using Sample Resources

Following configs can be used in test-config.xml to use sample SSA and keystores for DCR tests.

    <DCR>
        <SSAPath>Path.To.Directory/ssa.txt</SSAPath>
        <!-- SSA SoftwareId -->
        <SoftwareId>SP3</SoftwareId>
        <!-- SSA Redirect Uri -->
        <RedirectUri>https://www.google.com/redirects/redirect1</RedirectUri>
    </DCR>

SoftwareId of App Config 2 - SP3

Use signing.jks in 'signing-keystore' directory as the Application Keystore, and transport.jks in 'transport-keystore'
directory as the Transport Keystore.

Sample Keystore information:

- Signing Kid = cIYo-5zX4OTWZpHrmmiZDVxACJM

- Signing keystore alias = signing

- Signing keystore password = wso2carbon

- Transport Kid - 41uFt7QcUzeWoEWO3slUAGfXk8Y

- Transport keystore alias = transport

- Transport keystore password = wso2carbon