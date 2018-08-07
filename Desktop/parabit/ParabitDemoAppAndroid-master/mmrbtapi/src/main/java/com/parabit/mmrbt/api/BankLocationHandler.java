package com.parabit.mmrbt.api;

public interface BankLocationHandler {

    void onResult(BankLocation bankLocation);

    void onError(String s);

}
