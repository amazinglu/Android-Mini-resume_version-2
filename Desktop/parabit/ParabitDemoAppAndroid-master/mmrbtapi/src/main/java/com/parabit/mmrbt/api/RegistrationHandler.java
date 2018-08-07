package com.parabit.mmrbt.api;

/**
 * Created by williamsnyder on 2/21/18.
 */

public interface RegistrationHandler {
    void onRegistered();

    void onError(String s);
}
