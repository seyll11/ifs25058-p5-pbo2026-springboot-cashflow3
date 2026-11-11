package org.delcom.ifs00000springbootcashflow.domain.exception;

/** Checked exception dasar untuk semua kegagalan aturan domain. */
public class DomainException extends Exception {
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
