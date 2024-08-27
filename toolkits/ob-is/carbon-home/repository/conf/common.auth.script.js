var psuChannel = 'Online Banking';

function onLoginRequest(context) {
    publishAuthData(context, "AuthenticationAttempted", {'psuChannel': psuChannel});
    OBAuthenticationWorker(context, { stage: "started" }, "publishAbandonedConsentFlowData");
    doLogin(context);
}

var doLogin = function(context) {
    executeStep(1, {
        onSuccess: function (context) {
            //identifier-first success
            publishAuthData(context, "AuthenticationSuccessful", {'psuChannel': psuChannel});
            OBAuthenticationWorker(context, { stage: "userIdentified" }, "publishAbandonedConsentFlowData");
            OTPFlow(context);
        },
        onFail: function (context) { //identifier-first fail
            publishAuthData(context, "AuthenticationFailed", {'psuChannel': psuChannel});
            doLogin(context);
        }
    });
};

var OTPFlow = function(context) {
    executeStep(2, {
        //OTP-authentication
        onSuccess: function (context) {
            context.selectedAcr = "urn:cds.au:cdr:2";
            publishAuthData(context, "AuthenticationSuccessful", {'psuChannel': psuChannel});
            OBAuthenticationWorker(context, { stage: "userAuthenticated" }, "publishAbandonedConsentFlowData");
        },
        onFail: function (context) {
            publishAuthData(context, "AuthenticationFailed", {'psuChannel': psuChannel});
            OTPFlow(context);
        }
    });
};
