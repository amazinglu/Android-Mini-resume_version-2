package com.parabit.mmrbt.api;

/**
 * Created by williamsnyder on 2/21/18.
 */

public interface UnlockHandler {
    void onResult(boolean unlocked);

    void onError(String s);
}
