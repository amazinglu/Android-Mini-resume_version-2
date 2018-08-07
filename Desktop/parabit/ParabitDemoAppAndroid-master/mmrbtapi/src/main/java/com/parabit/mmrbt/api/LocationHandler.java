package com.parabit.mmrbt.api;

import java.util.List;

/**
 * Created by williamsnyder on 2/21/18.
 */

public interface LocationHandler {

    void onSuccess(List<BankLocation> locations);

    void onError(String s);
}
