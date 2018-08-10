package com.parabit.parabeacon.app.tech.auth;

import android.content.Context;
import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.services.cognitoidentityprovider.model.InvalidParameterException;
import com.amazonaws.services.cognitoidentityprovider.model.LimitExceededException;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException;
import com.parabit.parabeacon.app.tech.R;
import com.parabit.parabeacon.app.tech.logging.ParabitLogConstants.*;
import com.parabit.parabeacon.app.tech.state.AppStateManager;
import com.parabit.parabeacon.app.tech.logging.ApplicationLogger;

import java.util.Map;

/**
 * Created by williamsnyder on 11/3/17.
 */

public class AuthManager {

    private static final String FEEDBACK_TOKEN_ATTR = "custom:feedback-api-key";
    private static final String FEEDBACK_URL_ATTR = "custom:feedback-api-url";
    private static final String FIRMWARE_TOKEN_ATTR = "custom:firmware-api-key";
    private static final String FIRMWARE_URL_ATTR = "custom:firmware-api-url";
    private static final String BEACON_TOKEN_ATTR = "custom:beacon-api-key";
    private static final String BEACON_URL_ATTR = "custom:beacon-api-url";
    private static final String LOG_TOKEN_ATTR = "custom:log-api-key";
    private static final String LOG_URL_ATTR = "custom:log-api-url";
    private static final String LOG_LEVEL_ATTR = "custom:log-api-level";
    private static final String APP_ID_ATTR = "custom:appId";
    private static String userPoolId;
    private static String clientId;
    private static String clientSecret;

    private static AuthManager instance;
    private  CognitoUserPool userPool;
    private CognitoUserSession currentSession;

    private Context context;
    private String feedbackURL;
    private String feedbackToken;
    private String firmwareURL;
    private String firmwareToken;
    private String beaconURL;
    private String beaconToken;
    private String logURL;
    private String logToken;
    private String appId;
    private String logLevel;
    private Map<String,String> attributes;
    private AppStateManager stateManager;

    private DefaultCognitoAuthenticationHandler cognitoAuthenticationHandler;
    private ForgotPasswordContinuation forgotPasswordContinuation;
    private NewPasswordContinuation newPasswordContinuation;
    private NewPasswordHandler newPasswordHandler;

    private AuthManager(Context context, AppStateManager stateManager) {
        this.context = context;
        this.stateManager = stateManager;
        cognitoAuthenticationHandler = new DefaultCognitoAuthenticationHandler();
    }

    public static AuthManager getInstance(Context context, AppStateManager stateManager) {
        if (instance == null) {
            instance = new AuthManager(context, stateManager);
        }

        return instance;
    }

    /**
     * return the user pool that is existed in AWS
     *
     * the userPoolId, clientId, clientSercret is hard coded
     * */
    private CognitoUserPool getUserPool() {
        if (userPool == null) {
            userPoolId = context.getString(R.string.auth_cognito_pool_id);
            clientId = context.getString(R.string.auth_cognito_app_id);
            clientSecret = context.getString(R.string.auth_cognito_app_secret);
            userPool = new CognitoUserPool(context, userPoolId,
                    clientId, clientSecret);
        }

        return userPool;
    }

    /**
     * every login has a session which has a time out
     * if the expand of the session > timeout, the user have to log in again
     * */
    public boolean isSessionValid() {
        long sessionBegin = stateManager.currentState().getSessionBegin();
        if (sessionBegin == 0) {
            return false;
        }
        long elapsedTime = System.currentTimeMillis() - sessionBegin;
        long sessionTimeout = stateManager.currentState().getSessionTimeout();
        return elapsedTime < sessionTimeout;
    }

    public boolean isSignedIn() {
        return  getUserPool().getCurrentUser() != null &&
                getUserPool().getCurrentUser().getUserId() != null &&
                attributes != null &&
                !attributes.isEmpty();
    }

    public void signOut(){
        getUserPool().getCurrentUser().signOut();
        if (attributes != null) {
            attributes.clear();
        }
        ApplicationLogger.logEvent(Events.LOGOUT);
        ApplicationLogger.setCurrentUsername(null);
    }

    public void setUserAttributes(Map<String,String> attributes) {
        this.attributes = attributes;

        if (attributes.containsKey(FEEDBACK_TOKEN_ATTR)) {
            feedbackToken = attributes.get(FEEDBACK_TOKEN_ATTR);
        }

        if (attributes.containsKey(FEEDBACK_URL_ATTR)) {
            feedbackURL = attributes.get(FEEDBACK_URL_ATTR);
        }

        if (attributes.containsKey(FIRMWARE_TOKEN_ATTR)) {
            firmwareToken = attributes.get(FIRMWARE_TOKEN_ATTR);
        }

        if (attributes.containsKey(FIRMWARE_URL_ATTR)) {
            firmwareURL = attributes.get(FIRMWARE_URL_ATTR);
        }

        if (attributes.containsKey(BEACON_TOKEN_ATTR)) {
            beaconToken = attributes.get(BEACON_TOKEN_ATTR);
        }

        if (attributes.containsKey(BEACON_URL_ATTR)) {
            beaconURL = attributes.get(BEACON_URL_ATTR);
        }

        if (attributes.containsKey(LOG_URL_ATTR)) {
            logURL = attributes.get(LOG_URL_ATTR);
        }


        if (attributes.containsKey(LOG_TOKEN_ATTR)) {
            logToken = attributes.get(LOG_TOKEN_ATTR);
        }

        if (attributes.containsKey(APP_ID_ATTR)) {
            appId = attributes.get(APP_ID_ATTR);
        }

        if (attributes.containsKey(LOG_LEVEL_ATTR)) {
            logLevel = attributes.get(LOG_LEVEL_ATTR);
            stateManager.currentState().setDebug("debug".equals(logLevel));
            stateManager.update(stateManager.currentState());
            boolean debug = stateManager.currentState().isDebugEnabled();
            ApplicationLogger.setDebug(debug);
        }


    }

    public String getUsername() {
       if (isSignedIn()) {
           return getUserPool().getCurrentUser().getUserId();
       }
       return null;
    }

    public String getFeedbackToken() {
        return feedbackToken;
    }

    public String getFeedbackURL() {
        return feedbackURL;
    }

    public String getFirmwareToken() {
        return firmwareToken;
    }

    public String getFirmwareURL() {
        return firmwareURL;
    }

    public String getBeaconToken() {
        return beaconToken;
    }

    public String getBeaconURL() {
        return beaconURL;
    }

    public String getLogURL() {
        return logURL;
    }

    public String getLogToken() {
        return logToken;
    }

    public String getAppId() {
        return appId;
    }

    public void authenticate(UserAuthHandler authHandler) {
        cognitoAuthenticationHandler.setAuthHandler(authHandler);
        /**
         * AWS
         * Returns a valid tokens for a the current user in the user pool
         *
         * the AuthenticationHandler(AWS) will do it in background and onSuccess will call when get the result
         *
         * TODO: ask the request step of teh auth
         * */
//        getUserPool().getCurrentUser().getSessionInBackground(cognitoAuthenticationHandler);
        CognitoUserPool userPool = getUserPool();
        CognitoUser user = userPool.getCurrentUser();
        user.getSessionInBackground(cognitoAuthenticationHandler);
    }

    public void setNewPasswordHandler(NewPasswordHandler newPasswordHandler) {
        this.newPasswordHandler = newPasswordHandler;
        cognitoAuthenticationHandler.setNewPasswordHandler(newPasswordHandler);
    }

    public void setNewPasswordContinuation(NewPasswordContinuation newPasswordContinuation) {
        this.newPasswordContinuation = newPasswordContinuation;
    }

    public void setNewPassword(String newPassword) throws Exception{
        if (this.newPasswordContinuation == null) {
            throw new Exception("New password not expected.");
        }

        newPasswordContinuation.setPassword(newPassword);
        newPasswordContinuation.continueTask();
    }

    /**
     * request reset password step: AWS side
     * get CognitoUser base on user name in UserPool
     * AWS will send a reset code to the email
     * add reset code to forgotPasswordContinuation
     * add new password ro forgotPasswordContinuation
     * forgotPasswordContinuation.continueTask();
     * success or fail
     * */
    public void requestPasswordReset(String username, final ResetPasswordHandler resetPasswordHandler) {
        CognitoUser user = getUserPool().getUser(username);

        user.forgotPasswordInBackground(new ForgotPasswordHandler() {
            @Override
            public void onSuccess() {
                ApplicationLogger.logEvent(Events.PWD_RESET_SUCCESS);
                resetPasswordHandler.onPasswordChangeSuccess();
            }

            @Override
            public void getResetCode(ForgotPasswordContinuation continuation) {
                /**
                 * login process => handle forget password
                 * after user intering the email, the will call and the the reset code will be sent to user's email
                 * */
                ApplicationLogger.logEvent(Events.PWD_RESET_CODE);
                forgotPasswordContinuation = continuation;
                resetPasswordHandler.onCodeSent();
            }

            @Override
            public void onFailure(Exception exception) {
                ApplicationLogger.logError(Events.PWD_RESET_FAILED, exception);
                String message = exception.getLocalizedMessage();
                if (exception instanceof InvalidParameterException) {
                    message = ((InvalidParameterException)exception).getErrorMessage();
                }
                else if (exception instanceof LimitExceededException) {
                    message = ((LimitExceededException)exception).getErrorMessage();
                }
                else if (exception instanceof UserNotFoundException) {
                    message = ((UserNotFoundException)exception).getErrorMessage();
                }
                else if (exception instanceof AmazonServiceException) {
                    message = ((AmazonServiceException)exception).getErrorMessage();
                }
                resetPasswordHandler.onPasswordChangeFailure(new Exception(message, exception));
            }
        });
    }

    /**
     * add the new password to forgotPasswordContinuation
     * now we have all the info, try to reset the password
     * */
    public void submitResetPassword(String password) {
        forgotPasswordContinuation.setPassword(password);
        forgotPasswordContinuation.continueTask();
    }

    /**
     * add the reset code to forgotPasswordContinuation
     * */
    public void setResetPassordVerification(String code) {
        forgotPasswordContinuation.setVerificationCode(code);
    }

    /**
     * DefaultCognitoAuthenticationHandler class
     * AuthenticationHandler API
     * */
    private class DefaultCognitoAuthenticationHandler implements AuthenticationHandler {

        private UserAuthHandler authHandler;
        private NewPasswordHandler passwordHandler;

        public DefaultCognitoAuthenticationHandler( ) {
        }

        public void setAuthHandler(UserAuthHandler authHandler) {
            this.authHandler = authHandler;
        }

        public void setNewPasswordHandler(NewPasswordHandler passwordHandler) {
            this.passwordHandler = passwordHandler;
        }

        public String getUsername() {
            if (authHandler == null) {
                return null;
            }
            return authHandler.getUsername();
        }

        public String getPassword() {
            if (authHandler == null) {
                return null;
            }
            return authHandler.getPassword();
        }

        @Override
        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
            currentSession = userSession;
            userPool.getCurrentUser().getDetailsInBackground(new GetDetailsHandler() {
                @Override
                public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                    Log.d("Cognito", cognitoUserDetails.toString());
                    /**
                     * authentication process:
                     * if the anthentication success, this call back function will call
                     *
                     * all the info the AWS send back to user
                     * */
                    setUserAttributes(cognitoUserDetails.getAttributes().getAttributes());

                    if (passwordHandler != null) {
                        ApplicationLogger.logError(Events.SET_PWD_SUCCESS);
                        passwordHandler.onNewPasswordSuccess();
                        return;
                    }
                    String email = cognitoUserDetails.getAttributes().getAttributes().get("email");
                    ApplicationLogger.setCurrentUsername(email);
                    ApplicationLogger.logEvent(Events.LOGIN_SUCCESS);
                    authHandler.onUserAuthenticationSuccess();
                }

                @Override
                public void onFailure(Exception exception) {
                    Log.d("Cognito", exception.getMessage());
                    ApplicationLogger.logError(Events.LOGIN_FAILED, exception);
                    if (passwordHandler != null) {
                        ApplicationLogger.logError(Events.SET_PWD_FAILURE);
                        passwordHandler.onNewPasswordFailure(exception);
                        return;
                    }
                    authHandler.onUserAuthenticationFailed(exception);
                }
            });
        }

        /**
         * login process | authentication process
         * the DefaultCognitoAuthenticationHandler add the user name and password to authenticationContinuation
         * and then try to sign in
         *
         * here we use the password and username to get the get the credentials
         *
         * add user name and password to authenticationContinuation
         * authenticationContinuation.continueTask()
         * */
        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String UserId) {
            // The API needs user sign-in credentials to continue
            AuthenticationDetails authenticationDetails = new AuthenticationDetails(getUsername(), getPassword(), null);

            // Pass the user sign-in credentials to the continuation
            authenticationContinuation.setAuthenticationDetails(authenticationDetails);

            // Allow the sign-in to continue
            authenticationContinuation.continueTask();
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation continuation) {

        }

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {
            Log.d("Cognito", continuation.getChallengeName());

            if ("NEW_PASSWORD_REQUIRED".equals(continuation.getChallengeName())) {
                authHandler.handleNewPasswordRequired(continuation);
            }


//            if ("NEW_PASSWORD_REQUIRED".equals(continuation.getChallengeName())){
//                Popup input dialog? or new screen?
//            NewPasswordContinuation newPasswordContinuation = (NewPasswordContinuation) continuation;
//            newPasswordContinuation.setPassword(getPassword());
//            continuation.continueTask();
//            }
        }

        @Override
        public void onFailure(Exception exception) {
            if (passwordHandler != null) {
                passwordHandler.onNewPasswordFailure(exception);
                return;
            }
            ApplicationLogger.logEvent(Events.LOGIN_FAILED);
            authHandler.onUserAuthenticationFailed(exception);
        }
    }

    public interface UserAuthHandler {

        void onUserAuthenticationSuccess();

        void onUserAuthenticationFailed(Exception excetpion);

        void handleNewPasswordRequired(ChallengeContinuation continuation);

        String getUsername();

        String getPassword();
    }

    public interface NewPasswordHandler {

        void onNewPasswordSuccess();

        void onNewPasswordFailure(Exception e);
    }

    public interface ResetPasswordHandler {

        void onCodeSent();

        void onPasswordChangeSuccess();

        void onPasswordChangeFailure(Exception e);
    }

}
