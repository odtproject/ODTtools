package odt.tool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* This class contains information and methods for validating name fields */
public class NameValidation {
	
	/* fields in this class */
	String nameToCheck;
	FirstNameType nameType;
	boolean missIncluded;
	boolean corrected;
	String libFilesPath;
	
	/* This method will validate a first name
	 * The arguments are the error log, a copy of a variable of
	 * this class, and the type of name field.
	 * It returns the field to check, which may have been
	 * corrected.
	 */
	public static String checkFirstName ( ErrorLog errorLog, NameValidation nameToValidate ) 
	{		
		boolean nameValid = true;
		String workingName;
		String firstNamePattern;
		String[] workingParts;
		PunctuationChecks punctChk;
		boolean displayCheck = false;
		TitleValidation titleValidation = new TitleValidation();
		boolean titleCheckAble;
		titleCheckAble = titleValidation.initTitleSuffix(nameToValidate.libFilesPath);
		
		/* assumptions */
		nameToValidate.corrected = false;
		nameToValidate.missIncluded = false;
		
		/* Get the name to validate */
		workingName = nameToValidate.nameToCheck;
		
		/* Check and fix any hyphen issues */
		punctChk = new PunctuationChecks();
		displayCheck = punctChk.hyphenCheck(workingName, errorLog.getErrorField(), errorLog);
		
		/* Set corrected to whatever the comma check decided. */
		nameToValidate.corrected = displayCheck;
		
		/* Get the resulting name field string */
		workingName = punctChk.getCheckField();
		
		/* In case there is more than one name in the string, we 
		 * need to split it up into parts and look at each string 
		 * individually.
		 */
		workingParts = workingName.split(" ");
		firstNamePattern = "[A-Z](([a-z]*'[A-Za-z])?|([a-z]*-[A-Z][a-z])?|([a-z]+[A-z][a-z])?)[a-z]*";
		
		int numParts = workingParts.length;
		int i;
		
		for ( i=0; i<numParts; i++ ) 
		{			
			/* check for a name ending with a single quote */
			if ( workingParts[i].endsWith("\'") ) 
			{				
				errorLog.setErrCategory(ErrorCategory.AUTOCORRECT);
				errorLog.logError("Removed ending single quote", errorLog);

				int charIndex = workingParts[i].lastIndexOf("\'");						
				workingParts[i] = workingParts[i].substring(0,charIndex);
				nameToValidate.corrected = true;
				
			} /* end of check for ending single quote */
			
			if ( workingParts[i].startsWith("\'") ) 
			{				
				/* it starts with a single quote */
				
				errorLog.setErrCategory(ErrorCategory.AUTOCORRECT);
				errorLog.logError("Removed starting single quote", errorLog);

				workingParts[i] = workingParts[i].substring(1);
				nameToValidate.corrected = true;
				
			} /* end of check for starting single quote */
			
			nameValid = workingParts[i].matches(firstNamePattern);
			
			/* If the name is not a valid format, let's see if we
			 * can figure out why and maybe fix it.
			 */
			if ( ! nameValid && ! workingParts[i].equals("") ) 
			{				
				/* look for various expected values */
				switch( workingParts[i] ) 
				{
				case "miss":
					/* this is "miss" and that's okay */
					nameToValidate.missIncluded = true;
					break;

				case "the":
				case "and":
				case "de":
				case "with":
				case "du":
				case "da":
				case "of":
				case "del":
				case "do":
				case "des":
				case "from":
				case "on":
					
					/* these are all acceptable */
					break;
					
				case "ms":
					
					/* They probably meant "miss". */
					errorLog.setErrCategory(ErrorCategory.AUTOCORRECT);
					errorLog.logError("'ms' changed to 'miss' ", errorLog);		
					nameToValidate.corrected = true;
					
					/* change it "miss" */
					workingParts[i] = "miss";
					nameToValidate.missIncluded = true;
					break;
					
				case "aka":
					
					/* This is not allowed, they're supposed to put "aka" names in
					 * quotes.  But we can't know which part goes in quotes, so we
					 * can only report it, not fix it.
					 */
					errorLog.setErrCategory(ErrorCategory.ERROR);
					errorLog.logError("Disallowed abbreviation 'aka'; use quotes instead", errorLog);
					break;
					
				default:
					
					/* If it's not "miss" or other words which are valid, 
					 * then check for other things.
					 */
					char firstLetter, secondLetter;
					firstLetter = workingParts[i].charAt(0);
					
					/* If there's a second letter, then save it.
					 * Otherwise, set the second letter to something
					 * that won't cause an error.
					 */
					if ( workingParts[i].length() > 1 ) 
					{
						secondLetter = workingParts[i].charAt(1);
					}
					else {
						secondLetter = '\0';
					}

					/* If the first character is lower case and not followed by a single quote,
					 * then we'll try to fix it, and report it.
					 */
					if ( Character.isLowerCase(firstLetter) && Character.compare(secondLetter, '\'') != 0 ) 
					{						
						/* try to fix it */
						firstLetter = Character.toUpperCase(firstLetter);
						workingParts[i] = Character.toString(firstLetter) + workingParts[i].substring(1);								
			
						/* If it now matches the expected pattern, we're good */
						if ( workingParts[i].matches(firstNamePattern) ) 
						{							
							/* now it's okay so say it's valid and we corrected it */
							nameToValidate.corrected = true;
							errorLog.setErrCategory(ErrorCategory.AUTOCORRECT);
							errorLog.logError("Lowercase first letter", errorLog);
							
							/* Now that we've corrected the problem, check to see
							 * if it's a disallowed abbreviation.
							 */
							if ( titleCheckAble ) 
							{							
								/* If this part was found in the disallowed title/suffix list 
								 * and we're checking a first name, then report and correct it.
								 */
								if ( titleValidation.checkTitleSuffix(workingParts[i]) && nameToValidate.nameType == FirstNameType.FIRST ) 
								{
									errorLog.setErrCategory(ErrorCategory.AUTOCORRECT);
									errorLog.logError("Disallowed abbreviation: " + workingParts[i], errorLog);
									workingParts[i] = "";
									nameToValidate.corrected = true;
								}
							}
						}
						else 
						{
							/* It still doesn't match the pattern, so report
							 * it.
							 */
							errorLog.setErrCategory(ErrorCategory.ERROR);
							errorLog.logError("Unexpected character pattern", errorLog);
						}
						
					} /* end of if the first letter is lowercase */
					
					/* Check if it's all upper case */
					else if ( workingParts[i].matches("[A-Z][A-Z]+") ) 
					{						
						/* Check for allowable all uppercase suffixes */
						switch ( workingParts[i] ) 
						{
						case "II":
						case "III":
						case "IV":
						case "VII":
						case "VIII":
						case "IX":
						case "XI":
						case "XII":
						case "XIII":
							/* These are all okay */
							break;
							
						default:
							/* If we are able to check the title/suffix against our list, then check it.
							 * Otherwise, report it's an uppercase part we didn't expect.
							 */
							if ( titleCheckAble ) {
								
								/* If this part was found in the disallowed title/suffix list 
								 * and we're checking a first name, then report and correct it.
								 * Otherwise, report it as an uppercase part we didn't expect.
								 */
								if ( titleValidation.checkTitleSuffix(workingParts[i]) && nameToValidate.nameType == FirstNameType.FIRST ) 
								{
									errorLog.setErrCategory(ErrorCategory.AUTOCORRECT);
									errorLog.logError("Disallowed abbreviation: " + workingParts[i], errorLog);
									workingParts[i] = "";
									nameToValidate.corrected = true;
								}
								else 
								{
									errorLog.setErrCategory(ErrorCategory.WARNING);
									errorLog.logError("Found all uppercase part: " + workingParts[i], errorLog);
								}
							}
							else 
							{
								errorLog.setErrCategory(ErrorCategory.WARNING);
								errorLog.logError("Found all uppercase part: " + workingParts[i], errorLog);
							}							
							
						} /* end of switch on this part */
						
					} /* end of else if all uppercase */
					
					/* What if it's mixed case? */
					else if ( workingParts[i].matches("[A-Z]+[a-z]+[A-Z]+") ) 
					{
						/* If we can check this against our disallowed title/suffix
						 * list then do it.
						 */
						if ( titleCheckAble ) 
						{							
							/* If this part was found in the disallowed title/suffix list 
							 * and we're checking a first name, then report and correct it.
							 */
							if ( titleValidation.checkTitleSuffix(workingParts[i]) && nameToValidate.nameType == FirstNameType.FIRST ) 
							{
								errorLog.setErrCategory(ErrorCategory.AUTOCORRECT);
								errorLog.logError("Disallowed abbreviation: " + workingParts[i], errorLog);
								workingParts[i] = "";
								nameToValidate.corrected = true;
							}
						}

					} /* end of else it's mixed case */
					
					else 
					{						
						/* Let's check for numbers */
						Pattern matchPattern;
						Matcher myMatcher;

						/* Check for numbers in the name */
						matchPattern = Pattern.compile("[0-9]+");
						myMatcher = matchPattern.matcher(workingParts[i]);

						if ( myMatcher.find() ) {
							/* the name should never contain numbers */
							errorLog.setErrCategory(ErrorCategory.ERROR);
							errorLog.logError("Found number(s): " + workingParts[i], errorLog);
						}
						
						if ( nameToValidate.nameType != FirstNameType.NICK ) 
						{
							errorLog.setErrCategory(ErrorCategory.WARNING);
							errorLog.logError("Unusual format: " + workingParts[i], errorLog);
						}

					} /* end of else something's wrong, we don't know what */

					break;
				} /* end of switch on workingParts[i] */
				
				if (workingParts[i].equals("miss"))
				{
					nameToValidate.missIncluded = true;
				}
				
			} /* end of if name has invalid format */
			
			else 
			{			
				String correctName;
				
				/* The name may basically be valid, but there might
				 * be some oddities.
				 */
				switch ( workingParts[i] ) 
				{
				case "Iii":
				case "Ii":
				case "Iv":
				case "Vii":
				case "Viii":
				case "Ix":
				case "Xii":
				case "Xiii":
					errorLog.setErrCategory(ErrorCategory.AUTOCORRECT);
					errorLog.logError("Found sequence should be uppercase: " + workingParts[i], errorLog);
					nameToValidate.corrected = true;
					correctName = workingParts[i].toUpperCase();
					workingParts[i] = correctName;
					break;

				case "Aka":
					if ( nameToValidate.nameType == FirstNameType.FIRST ) 
					{
						errorLog.setErrCategory(ErrorCategory.ERROR);
						errorLog.logError("Disallowed abbreviation aka; use quotes instead", errorLog);
					}
					
					break;					
					
				case "Ms":
					if ( nameToValidate.nameType == FirstNameType.FIRST ) 
					{
						errorLog.setErrCategory(ErrorCategory.WARNING);
						errorLog.logError("'Ms' should only indicate unclear gender or marital status", errorLog);
						
						if ( i == 0 ) 
						{
							errorLog.setErrCategory(ErrorCategory.ERROR);
							errorLog.logError("Title should be at end of first name: " + workingParts[i], errorLog);
						}
					}
					break;

				case "Mr":
					if ( nameToValidate.nameType == FirstNameType.FIRST ) 
					{
						errorLog.setErrCategory(ErrorCategory.WARNING);
						errorLog.logError("'Mr' should only indicate unclear gender", errorLog);
						
						if ( i == 0 ) 
						{
							errorLog.setErrCategory(ErrorCategory.ERROR);
							errorLog.logError("Title should be at end of first name: " + workingParts[i], errorLog);
						}
					}
					break;
					
				case "Mrs":
					if ( nameToValidate.nameType == FirstNameType.FIRST ) 
					{
						errorLog.setErrCategory(ErrorCategory.WARNING);
						errorLog.logError("'Mrs' should only indicate unclear gender or woman's name not given", errorLog);
						
						if ( i == 0 ) 
						{
							errorLog.setErrCategory(ErrorCategory.ERROR);
							errorLog.logError("Title should be at end of first name: " + workingParts[i], errorLog);
						}
					}
					break;
					
				case "Fr":
					if ( nameToValidate.nameType == FirstNameType.FIRST ) 
					{
						/* Spell "Father" instead */
						errorLog.setErrCategory(ErrorCategory.AUTOCORRECT);
						errorLog.logError("Disallowed abbreviation 'Fr'; changed to 'Father'", errorLog);
						workingParts[i] = "Father";
						nameToValidate.corrected = true;

						/* If not the last part of the name string, it should be */
						if ( i == 0 ) 
						{
							errorLog.setErrCategory(ErrorCategory.ERROR);
							errorLog.logError("Title should be at end of first name: " + workingParts[i], errorLog);
						}
					}
					break;					
					
				case "Miss":
					
					if ( nameToValidate.nameType == FirstNameType.FIRST ) 
					{
						/* It should be all lowercase. */
						errorLog.setErrCategory(ErrorCategory.AUTOCORRECT);
						errorLog.logError("Found 'Miss'; should be 'miss'", errorLog);		
						workingParts[i] = "miss";
						
						nameToValidate.corrected = true;
						nameToValidate.missIncluded = true;
						
						/* If not the last part of the name string, it should be */
						if ( i== 0 ) {
							errorLog.setErrCategory(ErrorCategory.ERROR);
							errorLog.logError("Title should be at end of first name: " + workingParts[i], errorLog);
						}
					}
					
					break;				
					
				case "Dr":
				case "Rev":
				case "Sr":
				case "Jr":
				case "Father":
				case "Sister":
				case "Soeur":
					
					/* These titles should not be at the beginning of the first name. */
					if ( nameToValidate.nameType == FirstNameType.FIRST ) 
					{
						/* If at the start of the name string, it should not be, 
						 * but we don't know enough to fix it.
						 */
						if ( i == 0 ) 
						{
							errorLog.setErrCategory(ErrorCategory.ERROR);
							errorLog.logError("Title/suffix should be at end of first name: " + workingParts[i], errorLog);
						}
					}
					
					break;

				default:
					/* If we can check this against our disallowed title/suffix
					 * list then do it.
					 */
					if ( titleCheckAble ) 
					{						
						/* If this part was found in the disallowed title/suffix list 
						 * and we're checking a first name, then report and correct it.
						 */
						if ( titleValidation.checkTitleSuffix(workingParts[i]) && nameToValidate.nameType == FirstNameType.FIRST ) 
						{
							errorLog.setErrCategory(ErrorCategory.AUTOCORRECT);
							errorLog.logError("Disallowed abbreviation: " + workingParts[i], errorLog);
							workingParts[i] = "";
							nameToValidate.corrected = true;
						}
					}

					break;

				} /* end of switch on this part of name */				
				
			} /* end of else name has valid format */	

		} /* end of loop through name parts */
		
		/* Put the string back together */
		workingName = workingParts[0];
		for ( i=1; i<numParts; i++ ) 
		{
			workingName = workingName + " " + workingParts[i];
		}
				
		return(workingName);
		
	} /* end of checkFirstName */
	
	/* This method will validate a last name */
	public static String checkLastName ( ErrorLog errorLog, NameValidation nameToValidate ) 
	{		
		String workingName;
		String[] workingParts;
		Pattern matchPattern;
		Matcher myMatcher;
		boolean nameValid;
		String lastNamePattern;
		boolean lowerCase = false;
		PunctuationChecks punctChk;
		boolean displayCheck = false;
		NamePrefix namePrefix = new NamePrefix();
		NamePrefix.initPrefixMaps(namePrefix);
		
		/* assume it's not going to be corrected */
		nameToValidate.corrected = false;
		
		/* get the name to validate */
		workingName = nameToValidate.nameToCheck;
		
		/* Check and fix any hyphen issues */
		punctChk = new PunctuationChecks();
		displayCheck = punctChk.hyphenCheck(workingName, errorLog.getErrorField(), errorLog);
		
		/* Set corrected to whatever the comma check decided. */
		nameToValidate.corrected = displayCheck;
		
		/* Get the resulting name field string */
		workingName = punctChk.getCheckField();
		
		/* It should match this pattern */
		lastNamePattern = "[A-Z]+('[A-Z])*(-[A-Z]+)*('[A-Z])*";
		
		/* First do some single quote checks.
		 * Check for a name ending with a single quote.
		 */
		if ( workingName.endsWith("\'") ) 
		{			
			errorLog.setErrCategory(ErrorCategory.AUTOCORRECT);
			errorLog.logError("Removed ending single quote", errorLog);

			int charIndex = workingName.lastIndexOf("\'");						
			workingName = workingName.substring(0,charIndex);
			nameToValidate.corrected = true;
			
		} /* end of check for ending single quote */
		
		/* Check for a name starting with a single quote */
		if ( workingName.startsWith("\'") ) 
		{			
			/* it starts with a single quote */			
			errorLog.setErrCategory(ErrorCategory.AUTOCORRECT);
			errorLog.logError("Removed starting single quote", errorLog);

			workingName = workingName.substring(1);
			nameToValidate.corrected = true;
			
		} /* end of check for starting single quote */

		/* Now check to see if it matches the standard pattern */
		nameValid = workingName.matches(lastNamePattern);
		
		/* If the name is valid, our work here is done */
		if ( nameValid ) 
		{
			/* Even if it is valid, it might have non-ASCII characters */
			if ( ! StringUtils.isPureAscii(workingName) ) 
			{
				/* the name should never contain non-ASCII characters */
				errorLog.setErrCategory(ErrorCategory.ERROR);
				errorLog.logError("Found disallowed non-ASCII character(s)", errorLog);	
			}
			
			return (workingName);
		}		
		
		/* Obviously something is amiss, so let's find out what it
		 * is and see if we can fix it.  There may be multiple
		 * problems, so we'll try everything.
		 */		
		if ( ! StringUtils.isPureAscii(workingName) ) 
		{
			/* the name should never contain non-ASCII characters */
			errorLog.setErrCategory(ErrorCategory.ERROR);
			errorLog.logError("Found disallowed non-ASCII characters", errorLog);	
		}
		
		/* Check for numbers in the name field */
		matchPattern = Pattern.compile("[0-9]");
		myMatcher = matchPattern.matcher(workingName);

		if ( myMatcher.find() ) 
		{			
			/* the name should never contain numbers */
			errorLog.setErrCategory(ErrorCategory.ERROR);
			errorLog.logError("Found number(s)", errorLog);	

		} /* end of if we found numbers */

		/* Check for any lowercase letters in the name field */
		matchPattern = Pattern.compile("[a-z]");
		myMatcher = matchPattern.matcher(workingName);

		if ( myMatcher.find() ) 
		{			
			/* The name is not supposed to have lowercase letters,
			 * although we will remember it for now, because there 
			 * are exceptions.
			 */
			lowerCase = true;

		} /* end of if we found lowercase letters */

		/* In case there is more than one name in the string, we 
		 * need to split it up into parts and look at each string 
		 * individually.
		 */
		workingParts = workingName.split(" ");
		
		int numParts = workingParts.length;
		int i;
		
		/* If there is more than one part to the name, then there
		 * is a space in there and we give a warning about that.
		 */
		if ( numParts > 1 ) 
		{
			/* report the warning */
			errorLog.setErrCategory(ErrorCategory.WARNING);
			errorLog.logError("Embedded space", errorLog);			
			
		} /* end of if there are more than one part, meaning a space in the name */

		/* Loop through the parts of this name and see if we find any special
		 * cases like Van, Mc, De, etc.
		 */
		for ( i=0; i<numParts; i++ ) 
		{	
			/* If this bit is non-zero length, and does not
			 * match the expected pattern, let's try to figure
			 * out what's wrong.
			 */
			if ( workingParts[i].length() > 0 && ! workingParts[i].matches(lastNamePattern) ) 
			{				
				char firstLetter;
				firstLetter = workingParts[i].charAt(0);
				
				/* The first character should never be lowercase, so
				 * if it is, make it uppercase.
				 */
				if ( Character.isLowerCase(firstLetter) && ! workingParts[i].equals("und") ) 
				{					
					firstLetter = Character.toUpperCase(firstLetter);
					workingParts[i] = Character.toString(firstLetter) + workingParts[i].substring(1);
					
					errorLog.setErrCategory(ErrorCategory.AUTOCORRECT);
					errorLog.logError("First letter lowercase", errorLog);					

					nameToValidate.corrected = true;
					
				} /* end of if the first letter is lowercase */			
			
				/* If any prefix is a lone string, that's not right.
				 * Fix the prefix if necessary and indicate that it's by itself.  
				 * It's supposed to be next to the item following it.
				 */
				String correctPrefix = "";
				boolean isPrefix = false;
				
				/* First see if this is a a capitalized prefix.
				 * If so, the method will convert it to the expected
				 * mixed case version.
				 */
				namePrefix.prefix = workingParts[i];				
				correctPrefix = NamePrefix.checkCapPrefix(namePrefix);

				/* If it found the capitalized version and converted it
				 * then we have a prefix.
				 * Otherwise, we need to check if it is an accepted
				 * mixed case prefix.
				 */
				if ( correctPrefix == null ) 
				{
					isPrefix = NamePrefix.checkProperPrefix(namePrefix);
					if ( isPrefix ) 
					{
						correctPrefix = workingParts[i];
					}
				}
				else 
				{
					isPrefix = true;
				}
				
				/* If this is the last name in the name field, then it can't be
				 * a prefix, it would be a name that looks like a prefix.
				 * Set it to not being a prefix so we don't get stuck in a loop!
				 */
				if ( i == numParts-1 ) 
				{
					isPrefix = false;
				}
				
				/* If it is a prefix, we need to put it together with
				 * the string right after it and shift everything over.
				 */
				String reassembledString;
				if ( isPrefix ) 
				{
					errorLog.setErrCategory(ErrorCategory.AUTOCORRECT);
					errorLog.logError("Detached prefix found", errorLog);					

					/* put the correct prefix in the string we're working on */
					workingParts[i] = correctPrefix;
					
					/* start with the first field and reassemble the string */
					reassembledString = workingParts[0].trim();
					int j;
					for ( j=1; j<numParts; j++ ) 
					{						
						if ( j == (i+1) ) 
						{
							reassembledString = reassembledString + workingParts[j].trim();
						}
						else 
						{
							reassembledString = reassembledString + " " + workingParts[j].trim();
						}
						 
					} /* end of loop through reassembling the string */
					
					/* Since we fixed something, let's remember that.
					 * Then split the string again and save the number of
					 * parts.
					 */
					nameToValidate.corrected = true;
					workingParts = reassembledString.split(" ");
					numParts = workingParts.length;
					
					/* Decrement the counter to make up 
					 * for shifting everything.
					 */
					if ( (i >= 0) && (numParts > 1) ) 
					{
						i--;		
					}						
					
				} /* end of if we have a lone prefix */	
				
				else if ( lowerCase ) 
				{					
					/* If we found lower case letters earlier and it wasn't
					 * the detached prefix thing, let's see if it's an attached
					 * prefix, and if so, we won't give an error.
					 */
					int wLength = workingParts[i].length();
					int position;
					String back = null;
					isPrefix = false;
					
					/* We check for prefixes based on their length.
					 * We'll loop through the various possible lengths
					 * and see if any of the prefixes match.
					 */
					for ( position = wLength; position>=2; position-- ) 
					{					
						/* Get the front part and the back part. 
						 * If the front part is a prefix, then break out
						 * and we'll check the back part.
						 */
						namePrefix.prefix = workingParts[i].substring(0, position);
						back = workingParts[i].substring(position,wLength);
						isPrefix = NamePrefix.checkProperPrefix(namePrefix);
						
						if ( isPrefix ) 
						{
							break;
						}
						
					} /* end of loop through positions in the front of the name */					

					/* If we found a prefix at the front of the name, check the back
					 * to see if it's all caps.
					 */
					if ( isPrefix ) 
					{						
						/* Check for any lowercase letters in the non-prefix part */
						matchPattern = Pattern.compile("[a-z]");
						myMatcher = matchPattern.matcher(back);

						if ( myMatcher.find() ) 
						{							
							/* The prefix is okay, but we found lower case letters
							 * in the part that should be upper case.  We'll still
							 * make it a warning because names are all over the place.
							 * This way, we don't report errors for exceptions we
							 * know about.
							 */
							errorLog.setErrCategory(ErrorCategory.WARNING);
							errorLog.logError("Lowercase letters found", errorLog);							

						} /* end of if we found lowercase letters */

					} /* end of if we found an attached prefix */
					
					else if ( workingParts[i].contains("-") ) 
					{						
						/* Maybe the lowercase bit is in the second
						 * half of a hyphenated name. 
						 */
						String[] hyphenStringParts;
						
						hyphenStringParts = workingParts[i].split("-");
						wLength = hyphenStringParts[1].length();
						back = null;
						isPrefix = false;
						
						/* We check for prefixes based on their length.
						 * We'll loop through the various possible lengths
						 * and see if any of the prefixes match.
						 */
						for ( position = wLength; position>=2; position-- ) 
						{							
							/* Get the front part and the back part. 
							 * If the front part is a prefix, then break out
							 * and we'll check the back part.
							 */
							namePrefix.prefix = hyphenStringParts[1].substring(0, position);
							back = hyphenStringParts[1].substring(position,wLength);
							isPrefix = NamePrefix.checkProperPrefix(namePrefix);
							
							if ( isPrefix ) 
							{
								break;
							}
							
						} /* end of loop through positions in the front of the name */					

						/* If we found a prefix at the front of the name, check the back
						 * to see if it's all caps.
						 */
						if ( isPrefix ) 
						{							
							/* Check for any lowercase letters in the non-prefix part */
							matchPattern = Pattern.compile("[a-z]");
							myMatcher = matchPattern.matcher(back);

							if ( myMatcher.find() ) 
							{								
								/* The prefix is okay, but we found lower case letters
								 * in the part that should be upper case.  We'll still
								 * make it a warning because names are all over the place.
								 * This way, we don't report errors for exceptions we
								 * know about.
								 */
								errorLog.setErrCategory(ErrorCategory.WARNING);
								errorLog.logError("Lowercase letters found", errorLog);							

							} /* end of if we found lowercase letters */

						} /* end of if we found an attached prefix */
					}
						
					else 
					{
						/* We've tried everything to find a reasonable 
						 * explanation for finding lowercase letters.
						 * It's time to report it.
						 */
						matchPattern = Pattern.compile("[a-z]");
						myMatcher = matchPattern.matcher(workingParts[i]);

						if ( myMatcher.find() ) 
						{
							errorLog.setErrCategory(ErrorCategory.WARNING);
							errorLog.logError("Lowercase letters found", errorLog);							
						}
						
					} /* end of else we found no attached prefix */

				} /* end of else if found lowercase earlier */
										
			} /* end of if name has invalid format */
			
			else 
			{				
				/* We need to check if this has a detached prefix
				 * in an otherwise valid format.
				 */
				String correctPrefix = "";
				boolean isPrefix = false;
				
				/* First see if this is a a capitalized prefix.
				 * If so, the method will convert it to the expected
				 * mixed case version.
				 */
				namePrefix.prefix = workingParts[i];				
				correctPrefix = NamePrefix.checkCapPrefix(namePrefix);
				
				/* If it found the capitalized version and converted it
				 * then we have a prefix.
				 * Otherwise, we need to check if it is an accepted
				 * mixed case prefix.
				 */
				if ( correctPrefix == null ) 
				{
					isPrefix = NamePrefix.checkProperPrefix(namePrefix);
					if ( isPrefix ) 
					{
						correctPrefix = workingParts[i];
					}
				}
				else 
				{
					isPrefix = true;
				}
				
				/* If this is the last name in the name field, then it can't be
				 * a prefix, it would be a name that looks like a prefix.
				 * Set it to not being a prefix so we don't get stuck in a loop!
				 */
				if ( i == numParts-1 ) 
				{
					isPrefix = false;
				}
				
				/* If it is a prefix, we need to put it together with
				 * the string right after it and shift everything over.
				 */
				String reassembledString;
				if ( isPrefix ) 
				{					
					errorLog.setErrCategory(ErrorCategory.AUTOCORRECT);
					errorLog.logError("Detached prefix found", errorLog);					

					/* put the correct prefix in the string we're working on */
					workingParts[i] = correctPrefix;
					
					/* start with the first field and reassemble the string */
					reassembledString = workingParts[0].trim();
					int j;
					for ( j=1; j<numParts; j++ ) 
					{						
						if ( j == (i+1) ) 
						{
							reassembledString = reassembledString + workingParts[j].trim();
						}
						else 
						{
							reassembledString = reassembledString + " " + workingParts[j].trim();
						}
						 
					} /* end of loop through reassembling the string */

					/* Since we fixed something, let's remember that.
					 * Then split the string again and save the number of
					 * parts.
					 */
					nameToValidate.corrected = true;
					workingParts = reassembledString.split(" ");
					numParts = workingParts.length;
					
					/* Decrement the counter to make up 
					 * for shifting everything.
					 */
					if ( (i >= 0) && (numParts > 1) ) 
					{
						i--;
					}			
					
				} /* end of if this is a detached prefix in uppercase */

			} /* end of else format is valid for this part */

		} /* end of loop through parts of string */		
						
		/* Put the string back together */
		workingName = workingParts[0];
		for ( i=1; i<numParts; i++ ) 
		{
			workingName = workingName + " " + workingParts[i];
		}
		
		return(workingName);
		
	} /* end of checkLastName */
	
	/* This method will find the first index of an upper case letter in a string */
	public int firstIndexOfUCL( String stringToCheck ) 
	{        
	    for( int i=0; i<stringToCheck.length(); i++ ) 
	    {	    	
	        if(Character.isUpperCase(stringToCheck.charAt(i))) 
	        {
	            return i;
	        }
	    }
	    return -1;
	    
	} /* end of firstIndexOfUCL */

} /* end of NameValidation class */
