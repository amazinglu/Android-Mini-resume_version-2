package com.parabit.mmrbt.api;

/**
 * Created by williamsnyder on 2/21/18.
 */

public interface RegistrationHandler {

    // TODO log: add onAuth, on AuthError and registerError function

    void onRegistered();

    void onError(String s);
}
