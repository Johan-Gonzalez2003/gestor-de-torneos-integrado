package com.torneos.common;

public class AccountLockedException extends RuntimeException {

    public AccountLockedException() {
        super("Demasiados intentos fallidos. Intente de nuevo mas tarde");
    }
}
