package odt.tool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PunctuationChecks {

	private boolean commaExists;
	private String checkField;
		
	/* This method checks for the correct number of commas in a field.
	 * It returns whether the line should be displayed.
	 */
	public boolean commaCheck ( String fieldString, ErrorField fieldType, ErrorLog errorLog )
	{
		int countCommas, charIndex;
		boolean displayLine = false;
		StringBuilder builderLine;
		Pattern matchPattern;
		Matcher patternMatcher;
		
		errorLog.setErrorField(fieldType);
		
		/* assume need no comma */
		setCommaExists(false);
		setCheckField(fieldString);
		
		/* Count the commas in the field */
		countCommas = fieldString.length() - fieldString.replaceAll(",","").length();
		
		/* If there are no commas, then we don't need to do much else.
		 * If this is the name field, we expect a comma, so that's an error.
		 * Otherwise, we just return happy.
		 */
		if ( countCommas < 1 ) 
		{
			if ( fieldType == ErrorField.NAME )
			{
				errorLog.setErrCategory(ErrorCategory.ERROR);
				errorLog = errorLog.logError("No commas", errorLog);
				
				displayLine = true;
			}
			
			/* Save the string that was being checked */
			setCheckField(fieldString);
			
			return (displayLine);

		} /* end of if there are no commas */

		setCommaExists(true);
		
		/* If there is a space before the comma, then report it and remove any spaces. */
		if ( fieldString.contains(" ,") ) 
		{
			errorLog.setErrCategory(ErrorCategory.SPACING);
			errorLog = errorLog.logError("Space before comma", errorLog);
			
			fieldString = fieldString.replaceAll("[ ]+,", ",");
			displayLine = true;
		}
		
		/* If we got here, there is at least one comma.
		 * Let's make sure that any comma has a space after it.
		 */
				
		/* Pattern for comma without a space after it */
		matchPattern = Pattern.compile(",\\w");
		patternMatcher = matchPattern.matcher(fieldString);
		
		/* While there are commas without spaces after them,
		 * add space after each.
		 */
		boolean noSpace = false;
		
		while ( patternMatcher.find() )
		{
			noSpace = true;
			
			charIndex = patternMatcher.start();
			builderLine = new StringBuilder();
			builderLine.append(fieldString);
			
			/* Add a space after the comma */
			builderLine.insert(charIndex+1, ' ');
			fieldString = builderLine.toString();
			patternMatcher = matchPattern.matcher(fieldString);			
			
		} /* end while there are commas without following spaces */
		
		/* If there was no space after a comma, report that error. */
		if ( noSpace )
		{
			/* Report the error as corrected */
			errorLog.setErrCategory(ErrorCategory.AUTOCORRECT); 
			errorLog.logError("No space after comma", errorLog);
			
			displayLine = true;

		} /* end of if no spaces after any commas */

		/* If we got here, there are commas and if it is not the
		 * name field, there should not be any commas.  
		 * Remove the comma(s), report the error, and finish.
		 */
		if ( fieldType != ErrorField.NAME )
		{
			fieldString = fieldString.replaceAll(",","");
			setCheckField(fieldString);
			
			/* Report that error. */
			displayLine = true;
			
			/* Report the error as corrected */
			errorLog.setErrCategory(ErrorCategory.AUTOCORRECT); 
			errorLog.logError("Disallowed comma(s)", errorLog);

			return( displayLine );

		}

		/* If we have one comma in the name field, everything is fine and
		 * we are finished. 
		 */
		if ( countCommas == 1 )
		{
			if ( fieldType == ErrorField.NAME )
			{
				setCheckField(fieldString);
				
				return (displayLine);
			}

		} /* end of if there is one comma */
		
		/* There should only be one comma in the name field.
		 */
		if ( countCommas > 1 ) 
		{		
			/* If we got here, then the name field has too many commas.
			 * While there are too many commas, get rid of the 
			 * last one in the name field.
			 */
			while ( countCommas > 1 ) 
			{
				/* Delete the last comma in the field */
				charIndex = fieldString.lastIndexOf(',');
				
				builderLine = new StringBuilder();	
				builderLine.append(fieldString);
				builderLine.deleteCharAt(charIndex);
				
				fieldString = builderLine.toString();

				/* count the commas again */
				countCommas = fieldString.length() - fieldString.replaceAll(",","").length();
				
			} /* end while there are too many commas */
			
			setCheckField(fieldString);
			displayLine = true;
			
			/* Now report the error as corrected */
			errorLog.setErrCategory(ErrorCategory.AUTOCORRECT); 
			errorLog = errorLog.logError("Too many commas", errorLog);
			
		} /* if there is more than one comma */
		
		return(displayLine);

	} /* end of commaCheck */
	
	/* This method checks for hyphens in a field and make sure there are
	 * no spaces around the hyphen.
	 * It returns whether the line should be displayed.
	 */
	public boolean hyphenCheck ( String fieldString, ErrorField fieldType, ErrorLog errorLog )
	{
		boolean displayLine = false;
		
		/* If there is a hyphen in the field, check for spaces around it and remove
		 * any that we find.
		 */
		if ( fieldString.contains("-") )
		{
			/* If there is a space before the hyphen, then report it 
			 * and remove any spaces.
			 */
			if ( fieldString.contains(" -") ) 
			{
				errorLog.setErrCategory(ErrorCategory.SPACING);
				errorLog = errorLog.logError("Space before hyphen", errorLog);
				
				fieldString = fieldString.replaceAll("[ ]+-", "-");
				displayLine = true;
			}
			
			/* If there is a space after the hyphen, then report it 
			 * and remove any spaces.
			 */
			if ( fieldString.contains("- ") ) 
			{
				errorLog.setErrCategory(ErrorCategory.SPACING);
				errorLog = errorLog.logError("Space after hyphen", errorLog);
				
				fieldString = fieldString.replaceAll("-[ ]+", "-");
				displayLine = true;
			}
		}
		
		setCheckField(fieldString);		
		
		return(displayLine);
		
	} /* end of hyphenCheck */

	public boolean isCommaExists() {
		return commaExists;
	}

	public void setCommaExists(boolean commaExists) {
		this.commaExists = commaExists;
	}

	public String getCheckField() {
		return checkField;
	}

	public void setCheckField(String checkField) {
		this.checkField = checkField;
	}

	
}
