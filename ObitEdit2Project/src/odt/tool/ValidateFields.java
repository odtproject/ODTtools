package odt.tool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidateFields {

	/*
	 * This method checks for parentheses in the field passed to it.
	 */
	public static boolean checkParentheses (String fieldToCheck) {
		Pattern matchPattern;
		Matcher myMatcher;

		/* Check for parentheses in this field */
		matchPattern = Pattern.compile("\\(");
		myMatcher = matchPattern.matcher(fieldToCheck);

		if ( myMatcher.find() ) {				
			return(true);
		}
		else {
			matchPattern = Pattern.compile("\\)");
			myMatcher = matchPattern.matcher(fieldToCheck);

			if ( myMatcher.find() ) {				
				return(true);
			}	
		}
		
		return(false);
		
	}/*end of checkParentheses */

	public static boolean checkBrackets (String fieldToCheck) {
		Pattern matchPattern;
		Matcher myMatcher;

		/* Check for parentheses in this field */
		matchPattern = Pattern.compile("\\[");
		myMatcher = matchPattern.matcher(fieldToCheck);

		if ( myMatcher.find() ) {				
			return(true);
		}
		else {
			matchPattern = Pattern.compile("\\]");
			myMatcher = matchPattern.matcher(fieldToCheck);

			if ( myMatcher.find() ) {				
				return(true);
			}	
		}
		
		return(false);
		
	}/*end of checkParentheses */

} /* end of ValidateFields class */
