package org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions;

public class AccountMetadataException extends Exception {

    public AccountMetadataException(String message) {
        super(message);
    }

    public AccountMetadataException(String message, Throwable cause) {
        super(message, cause);
    }
}
