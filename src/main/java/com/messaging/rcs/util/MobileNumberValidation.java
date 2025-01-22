package com.messaging.rcs.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MobileNumberValidation {
	// function to check if the mobile number is valid or not
	public static boolean isValidMobileNo(String str) {
		// (0/91): number starts with (0/91)
		// [7-9]: starting of the number may contain a digit between 0 to 9
		// [0-9]: then contains digits 0 to 9
		Pattern ptrn = Pattern.compile("(0|91)?[6-9][0-9]{9}");
		// the matcher() method creates a matcher that will match the given input
		// against this pattern
		Matcher match = ptrn.matcher(str);
		// returns a boolean value
		return (match.find() && match.group().equals(str));
	}

	// driver code
	public static void main(String args[]) {
		// mobile number to check
		String str = "918057393132";
		// method calling
		if (isValidMobileNo(str))
			System.out.println("It is a valid mobile number.");
		else
			System.out.println("Entered mobile number is invalid.");
	}

}
