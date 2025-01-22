package com.messaging.rcs.jwt;

import java.util.Arrays;
import java.util.List;

public class Constants {
	public static final String defaultDateFormat = "yyyy-MM-dd";
	public static final String defaultDateTimeFormat = "yyyy-MM-dd HH:mm:ss";
	public static final String defaultTimezone = "Asia/Kolkata";
	public static final long ACCESS_TOKEN_VALIDITY_SECONDS = 5 * 60 * 60;
	public static final String SIGNING_KEY = "timesmobile@123";
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String HEADER_STRING = "Authorization";
	public static final List<String> Lead_Info_detail_MAPPING_SCHEMA = Arrays.asList("USERNAME", "CAMPAIGNNAME",
			"IPADDRESS", "USER2", "ACCOUNTNAME");
	public static final Integer Lead_Info_detail_MAPPING_SCHEMA_SCHEMA_LENGTH = Lead_Info_detail_MAPPING_SCHEMA.size();
	public static final String REQUIRED_COLUMN_MSG = "Given File Does Not Exist Required Column ::";

	public static final int REQUEST_SUCCESS = 200;

	public static final int RECORD_EXISTS = 201;
	public static final String RECORD_ALREADY_EXISTS = "Record Already Exists.";
	public static final int RECORD_NOT_EXISTS = 202;
	public static final String RECORD_NOT_AVAILABLE = "Record Doesn't Exists.";

	public static final int REQUEST_FAILED = 400;
	public static final int NOT_FOUND = 404;

	public static final int INVALID_REQUEST = 401;

	public static final int USER_DISABLED = 402;

	public static final int USER_SESSION_OUT = 403;

	public static final int USER_REGISTERED_ALREADY = 405;

	public static final int BLOCKED_USER = 406;

	public static final int INVALID_OLD_PASSWORD = 407;

	public static final int MAX_LIMIT_EXCEEDED = 408;

	public static final int SQL_FAILED = 409;

	public static final int DELETION_FAILED = 410;

	public static final int INVALID_TOEKN_ID = 411;

	public static final int MISSING_MANDATORY_PARAMS = 412;

	public static final int CONTAINER_SCAN_FAILED = 413;

	public static final int MAIL_NOT_SENT_CODE = 414;

	public static final int TOKEN_TIME_OUT_CODE = 415;

	public static final int EMAIL_ID_NOT_FOUND_CODE = 416;

	public static final List<String> SMS_BLACKLIST_COLUMN = Arrays.asList("msisdn");

	public static final Integer SMS_BLACKLIST_COLUMN_LENGTH = SMS_BLACKLIST_COLUMN.size();
	public static final String SUCCESS_MSG = "Success";
	public static final String SUCCESSADD_MSG = "Added Successfully";
	public static final String FAIL_MSG = "Failed";
	public static final String DEFAULT_ERROR = "Error";
	public static final String USER_NOT_ACTIVE = "Account Is Not Active.";
	public static final String USER_NOT_AVAILABLE = "Invalid User Name";
	public static final String USER_DOSE_NOT_AVAILABLE = "User Dose Not Exists.";
	public static final String DELETE_MSG = "Records Deleted Successfully.";
	public static final String PASSWORD_VALIDATION = "(?=.*[0-9])(?=.*[a-zA-Z]).{8,}";
	public static final String PASSWORD_MSG = "Password Not Valid(min 8 and max 15)for Ex: Aa123@ascg";
	public static final String MOBILE_MSG = "Mobile Number Not Valid For Ex:: 1234567890";
	public static final String EMAIL_MSG = "Email Should Be Contains Capital Letter and Small Letter ,Special Character and Numeric Number.";

	public static final String FILE_NOT_UPLOAD = "Could not upload the file";
	public static final String FILE_UPLOAD_SUCCESSFULLY = "Uploaded the file successfully";
	public static final String FILE_CANNOT_EMPTY = "File Cannot Empty.";
	public static final String DATA_NOT_FOUND = "Data Not Found.";

	public static final String INVALID_FORMAT = "Invalid Format";

	public static final String PAISA_BAZAR_TRANS = "paisabazaar";
	public static final String PAISA_BAZAR_PRO = "paisabazaarpro";
	public static final String BLACKLIST_MOBILE_VALIDATOR_MSG = "Valid No";
	public static final String BLACKLIST_MOBILE_INVALID_MSG = "In Valid No";

}
