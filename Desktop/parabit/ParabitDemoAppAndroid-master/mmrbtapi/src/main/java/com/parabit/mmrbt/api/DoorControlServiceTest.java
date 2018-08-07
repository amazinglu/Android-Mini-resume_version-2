package com.parabit.mmrbt.api;

import com.github.cheergoivan.totp.TOTPAuthenticator;

/**
 * Created by williamsnyder on 9/9/17.
 */

public class DoorControlServiceTest {
    public static void main(String[] args) {

        System.out.println(getToken("e6ca4592-76cf-4f9a-b201-1df3cc1822d6"));
//        String url = "https://api.parabit.com/dev-control/";
//        String key = "EPQQ4BC18y24iij8WJL6y2ABp9PQUt1O7HuOeZEr";
//        ParabitDoorManager doorManager = ParabitDoorManager.getInstance(url, key);
//        UnlockCommand unlockCommand = new UnlockCommand();
//
//        String deviceId = "2d4f56f2-8685-4df9-ac17-33737c5aaf89";
//        String token = getToken(deviceId);
//
//        unlockCommand.setDeviceId(deviceId);
//        unlockCommand.setToken(token);
//        doorManager.unlock(unlockCommand, new Callback<UnlockCommandResult>() {
//            @Override
//            public void onResponse(Call<UnlockCommandResult> call, Response<UnlockCommandResult> response) {
//                System.out.println("Is unlocked? --- " + response.body().isUnlocked());
//            }
//
//            @Override
//            public void onFailure(Call<UnlockCommandResult> call, Throwable t) {
//                System.out.println(t.getLocalizedMessage());
//            }
//        });
    }

    public static String getToken(String deviceId) {
//        String secret = Base32.encode("858501b9-0fcc-4937-ae92-5b1b7ce52aa3".getBytes());
//        System.out.println("Secret: " + secret);
//        Totp totp = new Totp("HA2TQNJQGFRDSLJQMZRWGLJUHEZTOLLBMU4TELJVMIYWEN3DMU2TEYLBGM======");
//        String token = totp.now();
//        System.out.println("Token is " + token);
//
//        System.out.println(TOTPHelper.generate(secret.getBytes()));

        TOTPAuthenticator auth = TOTPAuthenticator.builder().build();
        String totp = auth.generateTOTP("GU3GCMZUMU3WGLLGGE4DMLJUGRSTMLLCMFQWILLEGQ3WEYZXGI4TGYZRMY======".getBytes());
        return totp;
    }
}

