package com.parabit.parabeacon.app.tech.logging;

/**
 * Created by williamsnyder on 12/21/17.
 */

public final class ParabitLogConstants {

    public static final class Keys {
        public static final String USERNAME = "username";
        public static final String SERIAL_NUMBER = "serialNumber";
        public static final String CURRENT_FIRMWARE = "currentFirmware";
        public static final String ADV_INTERVAL = "advInterval";
        public static final String TX_POWER = "txPower";
        public static final String NEW_VALUE = "newValue";
        public static final String OLD_VALUE = "oldValue";
        public static final String NEW_FIRMWARE = "newFirmware";
        public static final String ERROR_MESSAGE = "errorMessage";
        public static final String REQUEST_DURATION = "requestDuration";
        public static final String ENVIRONMENT =  "environment";
    }

    public static final class Events {
        public static final String APP_LAUNCHED = "APP_LAUNCHED";
        public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
        public static final String LOGIN_FAILED = "LOGIN_FAILED";
        public static final String LOGOUT = "LOGOUT";
        public static final String BEACON_SCAN = "BEACON_SCAN";
        public static final String UNLOCK_ATTEMPT = "UNLOCK_ATTEMPT";
        public static final String BEACON_TAP = "BEACON_TAP";
        public static final String ADV_CHANGED = "ADV_CHANGED";
        public static final String TX_CHANGED = "TX_CHANGED";
        public static final String UPDATE_CHECK = "UPDATE_CHECK";
        public static final String UPDATE_SUCCESS = "UPDATE_SUCCESS";
        public static final String UPDATE_FAILED = "UPDATE_FAILED";
        public static final String BEACON_DISCONNECTED = "BEACON_DISCONNECTED";

        public static final String PWD_RESET_STARTED = "PWD_RESET_STARTED";
        public static final String PWD_RESET_CODE = "PWD_RESET_CODE";
        public static final String PWD_RESET_SET = "PWD_RESET_SET";
        public static final String PWD_RESET_SUCCESS = "PWD_RESET_SUCCESS";
        public static final String PWD_RESET_FAILED = "PWD_RESET_FAILED";
        public static final String SET_PWD_SUCCESS = "SET_PWD_SUCCESS";
        public static final String SET_PWD_FAILURE = "SET_PWD_FAILURE";
        public static final String MENU_ABOUT = "MENU_ABOUT";
        public static final String MENU_HELP = "MENU_HELP";
        public static final String MENU_REPORT_PROBLEM = "MENU_REPORT_PROBLEM";
        public static final String REPORT_PROBLEM_SUCCESS = "REPORT_PROBLEM_SUCCESS";
        public static final String REPORT_PROBLEM_FAILED = "REPORT_PROBLEM_FAILED";
        public static final String MENU_FEEDBACK = "MENU_FEEDBACK";
        public static final String FEEDBACK_SUCCESS = "FEEDBACK_SUCCESS";
        public static final String FEEDBACK_FAILED = "FEEDBACK_FAILED";

        public static final String UNLOCK_WRITE_FAILED = "UNLOCK_WRITE_FAILED";
        public static final String API_FAILURE = "API_FAILURE";
        public static final String FW_UNKNOWN = "FW_UNKNOWN";
        public static final String SN_UNKNOWN = "SN_UNKNOWN";

        public static final String ERROR_READING_FIRMWARE = "ERROR_READING_FIRMWARE";
        public static final String UNLOCK_CLOUD_RESPONSE_FAILURE = "UNLOCK_CLOUD_RESPONSE_FAILURE";
        public static final String UNLOCK_CODE_NOT_FOUND = "UNLOCK_CODE_NOT_FOUND";
        public static final String DEBUG_DFU_CONNECTING = "DEBUG_DFU_CONNECTING";
        public static final String DEBUG_DFU_ERROR = "DEBUG_DFU_ERROR";
        public static final String DEBUG_DFU_ABORTED = "DEBUG_DFU_ABORTED";
        public static final String DEBUG_DFU_COMPLETE = "DEBUG_DFU_COMPLETE";
        public static final String DEBUG_DFU_DISCONNECTING = "DEBUG_DFU_DISCONNECTING";
        public static final String DEBUG_DFU_VALIDATING = "DEBUG_DFU_VALIDATING";
        public static final String DEBUG_DFU_ENABLING = "DEBUG_DFU_ENABLING";
        public static final String DEBUG_DFU_STARTING = "DEBUG_DFU_STARTING";
        public static final String DEBUG_DFU_END_DOWNLOAD = "DEBUG_DFU_END_DOWNLOAD";
        public static final String DEBUG_DFU_START_TRANSFER = "DEBUG_DFU_START_TRANSFER";
        public static final String DEBUG_DFU_ERROR_DOWNLOAD = "DEBUG_DFU_ERROR_TRANSFER";
        public static final String LOG_SERVICE_UNAVAILABLE = "LOG_SERVICE_UNAVAILABLE";
        public static final String FIRMWARE_NOT_FOUND = "FIRMWARE_NOT_FOUND";
        public static final String BEACON_INFO_SUCCESS = "BEACON_INFO_SUCCESS";
        public static final String BEACON_INFO_FAILURE = "BEACON_INFO_FAILED";
        public static final String SESSION_TIMEOUT = "SESSION_TIMEOUT";
    }
}
