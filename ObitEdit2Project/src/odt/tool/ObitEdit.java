package odt.tool;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.io.*;
import java.lang.StringBuilder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import odt.gui.ObitEditGui;


/* This is the class in charge of checking the index entries. 
 */
public class ObitEdit {	
	
	public static ErrorLog doObitEdit ( String fileToCheck, ErrorLog obitErrors ) {
		
		TagnameArray tagnamesArray = new TagnameArray();
		LocationArray locationsArray = new LocationArray();
		PublicationMap pubsMap = new PublicationMap();

		/* Get the path for ObitEdit files */
		DirectorySetup odtPaths = new DirectorySetup();
		
		odtPaths = odtPaths.getDirectorySetup( odtPaths );		
		
		/* Set up corrected file path -- for location of corrected files */
		Path correctedFilePath = Paths.get(DirectorySetup.odtFilePath.toString(), "Correct");
		if ( ! correctedFilePath.toFile().isDirectory() ) {
			/* try to make the folder */
			correctedFilePath.toFile().mkdir();
			
			if ( ! correctedFilePath.toFile().isDirectory() ) {
				/* We can't use the folder we're used to, so default to the user's
				 * current folder.
				 */
				System.err.println("Unable to use ODT check files path: " + correctedFilePath.toString() );
				System.err.println("Defaulting to current folder");
				correctedFilePath = Paths.get(System.getProperty("user.dir") );
			}
		}
		
		/* Set up check file path -- for location of files to be checked */
		Path checkFilePath = Paths.get(DirectorySetup.odtFilePath.toString(), "Check");
		if ( ! checkFilePath.toFile().isDirectory() ) {
			/* try to make the folder */
			checkFilePath.toFile().mkdir();
			
			if ( ! checkFilePath.toFile().isDirectory() ) {
				/* We can't use the folder we're used to, so default to the user's
				 * current folder.
				 */
				System.err.println("Unable to use ODT check files path: " + checkFilePath.toString() );
				System.err.println("Defaulting to current folder");
				checkFilePath = Paths.get(System.getProperty("user.dir") );
			}
		}
		
		/* Set up report file path */
		Path reportFilePath = Paths.get(DirectorySetup.odtFilePath.toString(), "Reports");
		if ( ! reportFilePath.toFile().isDirectory() ) {
			/* try to make the folder */
			reportFilePath.toFile().mkdir();
			
			if ( ! reportFilePath.toFile().isDirectory() ) {
				/* We can't use the folder we're used to, so default to the user's
				 * current folder.
				 */
				System.err.println("Unable to use ODT reports path: " + reportFilePath.toString() );
				System.err.println("Defaulting to current folder");
				reportFilePath = Paths.get(System.getProperty("user.dir") );
			}
		}	
		
		/* Add on the file name */
		reportFilePath = Paths.get(reportFilePath.toString(), "ObitEditRpt.txt");
		String reportFile = reportFilePath.toString();
		obitErrors.rptFileName = reportFile;
		
		/* fill in the location array */
		locationsArray = LocationArray.initLocationArray(locationsArray, DirectorySetup.libFilesPath.toString() );
		
		if (locationsArray.locArray.size() <= 0) {
			/* then we have a problem */
			System.err.println("ObitEdit: Unable to process abbreviations list.");
			return( obitErrors );
		}
		
		/* fill in the publications map */
		pubsMap = PublicationMap.initPublicationMap(locationsArray, DirectorySetup.libFilesPath.toString() );
		
		if ( pubsMap.publicationsMap.isEmpty() ) {
			/* we have a problem */
			System.err.println("ObitEdit: Unable to process publication list.");
			return( obitErrors );
		}
		
		/* fill in the Tagname array */
		tagnamesArray = TagnameArray.initTagArray(tagnamesArray, locationsArray, DirectorySetup.libFilesPath.toString() );

		if (tagnamesArray.tagArray.size() <= 0) {
			/* then we have a problem */
			System.err.println("ObitEdit: Unable to process tagname list.");

			return( obitErrors );
		}
		
		/*  Retrieve line separator dependent on OS. */
		String newLine = System.getProperty("line.separator");

		try {
			
			/* If the input File does not exist, we're done */
			checkFilePath = Paths.get(checkFilePath.toString(), fileToCheck);
			String checkFileIn = checkFilePath.toString();
			if ( !checkFilePath.toFile().exists() ) {
				System.err.println("File does not exist: " + checkFileIn);
				
				return( obitErrors );
			}

			/* For displaying purposes, echo a blank line */
			System.out.println();
			
			/* Set up the path to the output file.
			 * Append "_c" to the file name.
			 */
			String correctdFileNm;
			int dotIndex = fileToCheck.indexOf('.');
			if ( dotIndex > 0 ) {
				correctdFileNm = fileToCheck.substring(0, dotIndex) + "_c" + fileToCheck.substring(dotIndex);
			}
			else {
				correctdFileNm = fileToCheck + "_c";
			}

			correctedFilePath = Paths.get(correctedFilePath.toString(), correctdFileNm);
			String checkFileOut = correctedFilePath.toString();	
			obitErrors.correctedFileName = checkFileOut;
					
			/* Open the requested file to check the fields in each line */
			Scanner fileScanner = new Scanner(new BufferedReader(new FileReader(checkFileIn) ) );
			PrintStream fileWriter = new PrintStream(new File(checkFileOut) );			
			PrintStream reportWriter = new PrintStream(new File(reportFile) );
			obitErrors.rptStream = reportWriter;
			String reportDate, reportTime;
			
			/* Get some information to put in the report */
			Date date = Calendar.getInstance().getTime();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d");
			reportDate = dateFormat.format(date);
			dateFormat = new SimpleDateFormat("k:mm");
			reportTime = dateFormat.format(date);
			
			/* Get the version number for this */
			String versionNumber = ObitEditGui.class.getPackage().getImplementationVersion();

			/* Start report log with the header */
			reportWriter.println(reportDate + "\tObituary Daily Times - Edit Report\t" + reportTime);
			reportWriter.println("\t\t  ObitEdit2 Version " + versionNumber);
			reportWriter.println();
			reportWriter.println("Checking file " + checkFileIn);
			reportWriter.println("Corrected file: " + checkFileOut);
			reportWriter.println();
			
			reportWriter.println("TOTALS");
			reportWriter.printf("%-16s: %8d%n","Tagnames", tagnamesArray.tagArray.size() );
			reportWriter.printf("%-16s: %8d%n","Publications", pubsMap.publicationsMap.size() );
			reportWriter.println("");
			
			/* Output for console */
			System.out.println("-----------------------------------");
			System.out.println("Checking file " + checkFileIn);
			System.out.println(reportDate + "\t" + reportTime);
			
			/* for system err box */
			dateFormat = new SimpleDateFormat("k:mm:ss");
			reportTime = dateFormat.format(date);
			System.err.println("-----------------------------------");
			System.err.println("Checking begins " + reportDate + " -- " + reportTime );

			StringBuilder builderLine = new StringBuilder();
			String tagname, originalLine; 
			String submissionLine;
			String reassembledLine;
			String[] submissionFields;
			int countChars, charIndex, i;
			boolean displayLine, canParse;
			String deathPlaceCode;
			Pattern matchPattern;
			Matcher myMatcher;
			int countLeftParens;
			int countRightParens;
			boolean isDuplicate;
			boolean displayCheck;

			Set<String> inputLines = new HashSet<String>();
			ArrayList<String> dupLines = new ArrayList<>();
			PunctuationChecks punctChk = new PunctuationChecks();
			
			/* Start the count of records */
			obitErrors.recordNum = 0;
			
			/* Go through the file line-by-line. */
			while ( fileScanner.hasNextLine() ) {
				
				/* Get the line and trim spaces */
				originalLine = fileScanner.nextLine().trim();
				
				int countNonPrintBefore = 0;
				int countNonPrintAfter = 0;
				
				countNonPrintBefore = originalLine.length() - originalLine.replaceAll("%","").length();
				
				/* Replace all non-printable characters with a percent sign.
				 * Any percent signs will be reported later.
				 */
				originalLine = originalLine.replaceAll("[^\\p{Print}]", "%");
				
				countNonPrintAfter = originalLine.length() - originalLine.replaceAll("%","").length();
				
				/* initialize the error log */
				obitErrors = ErrorLog.initRptLineErrorLog(obitErrors);
				
				/* set some default values */
				submissionLine = originalLine;
				obitErrors.rptOriginal = false;
				displayLine = false;
				canParse = true;
				boolean hasCommas = true;
				tagname = null;				
				isDuplicate = false;
				
				/* check for record level errors before parsing */
				if ( submissionLine.length() <= 0 ) {
					/* nothing here to parse */
					canParse = false;
				}
				else {
					/* Check if this is a duplicate line */
					if ( inputLines.contains(originalLine) ) {
						/* this line is a duplicate and we needn't go any further with it */
						canParse = false;
						isDuplicate = true;
						dupLines.add(originalLine);
						obitErrors.incrDupCount();
					}
					else {
						inputLines.add(originalLine);
						
						/* keep track of how many records we've processed */
						obitErrors.recordNum++;  
					}

				} /* end of else the line is not blank */				
				
				/* BEGIN RECORD LEVEL ERROR CHECKS */
				
				obitErrors.setErrorField(ErrorField.RECORD);
				
				/* There should be 5 semicolons in the line, 
				 * not an elegant counting method, but it's short and 
				 * Java doesn't supply anything better.
				 */
				countChars = submissionLine.length() - submissionLine.replace(";","").length();
				if (countChars < 5 && submissionLine.length() > 0 ) {
					
					/* Report the error.  If there are less than five, we can't parse this line. */
					obitErrors.setErrCategory(ErrorCategory.ERROR);
					obitErrors = obitErrors.logError("Too few semicolons", obitErrors);	
					canParse = false;
				}
				else if (countChars > 5) {
					
					/* report that there are too many semicolons */
					obitErrors.setErrCategory(ErrorCategory.ERROR);
					obitErrors = obitErrors.logError("Too many semicolons", obitErrors);
					
					/* If the extra semicolon is at the end of the line, report the error, and 
					 * we might be able to continue if fixing that brings the number of semicolons to
					 * the right number.
					 */
					if ( submissionLine.endsWith(";") ) {
						obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
						obitErrors = obitErrors.logError("Ends with semicolon", obitErrors);
						
						/* Get the index of that last semicolon and try replacing it. */
						charIndex = submissionLine.lastIndexOf(";");						
						submissionLine = submissionLine.substring(0,charIndex);
						displayLine = true;
						
						/* count the semicolons again */
						countChars = submissionLine.length() - submissionLine.replace(";","").length();
						
						/* If we still have too many semicolons, we cannot parse the line */
						if ( countChars > 5 ) {
							canParse = false;
						}		
						
					} /* end of if the line ends with a semicolon */					
					
					else {
						/* The line ends with some other character, so we have no confidence 
						 * in parsing this line with the wrong number of semicolons.
						 */
						canParse = false;
					}
					
				} /* end of else if there are too many semicolons */			
				
				/* If the line ends with a semicolon, then there's a problem. */
				if ( submissionLine.endsWith(";") ) {
					obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
					obitErrors = obitErrors.logError("Ends with semicolon", obitErrors);
					
					/* Get the index of that last semicolon and try replacing it. */
					charIndex = submissionLine.lastIndexOf(";");						
					submissionLine = submissionLine.substring(0,charIndex);
					displayLine = true;
					
					/* count the semicolons again */
					countChars = submissionLine.length() - submissionLine.replace(";","").length();
					
					/* If we have the wrong number of semicolons, we cannot parse the line */
					if ( (canParse == true) && (countChars > 5 || countChars > 5) ) {
						obitErrors.setErrCategory(ErrorCategory.ERROR);
						obitErrors = obitErrors.logError("Wrong number of semicolons; cannot parse line", obitErrors);

						canParse = false;
					}		
					
				} /* end of if the line ends with a semicolon */
				
				/* there should be no colons in the line */
				if ( submissionLine.indexOf(':') >= 0 ) {
					
					/* Report we found at least one colon */
					obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
					obitErrors = obitErrors.logError("Found colon(s)", obitErrors);
					
					/* We assume the user meant to use a semicolon, so replace the colons. */
					submissionLine = submissionLine.replace(":",";");
					displayLine = true;
					
					/* Maybe that fix gave us enough semicolons to parse.  Let's count them again
					 * and if there are enough, then we can parse this line.
					 * Otherwise, if we made too many semicolons by the replacement, we cannot 
					 * parse this line.
					 */
					countChars = submissionLine.length() - submissionLine.replace(";","").length();
					if (countChars == 5) {						
						canParse = true;
					}
					else if (countChars > 5) {
						canParse = false;
					}

				} /* end of if there are semicolons in the line */				
				
				/*
				 * If there are percent signs in the line that were not there before
				 * non-printable characters were replaced, then report the error,
				 * don't do any parsing and display the line for the user.
				 */
				if ( (countNonPrintBefore != countNonPrintAfter) && (countNonPrintBefore == 0) ) {
					obitErrors.setErrCategory(ErrorCategory.ERROR);
					obitErrors = obitErrors.logError("Unprintable characters in line - replaced with '%'", obitErrors);	
					canParse = false;
				}

				/* Now we need some heavy-duty matching */
				
				/* there should NOT be a space before each semicolon after a letter */
				matchPattern = Pattern.compile("\\w ;");
				myMatcher = matchPattern.matcher(submissionLine);

				if ( myMatcher.find() ) {				
					obitErrors.setErrCategory(ErrorCategory.SPACING);
					obitErrors = obitErrors.logError("Space before semicolon", obitErrors);						
						
					charIndex = myMatcher.start();
						
					/* remove space(s) before semicolons */
					while ( charIndex >= 0 ) 
					{
						builderLine = new StringBuilder();
						builderLine.append(submissionLine);
						builderLine.deleteCharAt(charIndex+1);
							
						submissionLine = builderLine.toString();
						myMatcher = matchPattern.matcher(submissionLine);
							
						if ( myMatcher.find() ) {
							charIndex = myMatcher.start();
						}
						else {
							charIndex = -1;
						}						
							
					} /* end of while we find the pattern */
					
					displayLine = true;
					
				} /* end of if there is a space before a semicolon */

				/* there should be a space after each semicolon */
				matchPattern = Pattern.compile(";[^ ]");
				myMatcher = matchPattern.matcher(submissionLine);
				builderLine = new StringBuilder();

				if ( myMatcher.find() ) {				
					obitErrors.setErrCategory(ErrorCategory.SPACING);
					obitErrors = obitErrors.logError("Missing space after semicolon", obitErrors);
					
					charIndex = myMatcher.start();
					while ( charIndex >= 0 ) 
					{
						builderLine = new StringBuilder();
						builderLine.append(submissionLine);
						builderLine.insert(charIndex+1,' ');
						
						submissionLine = builderLine.toString();
						myMatcher = matchPattern.matcher(submissionLine);
						
						if ( myMatcher.find() ) {
							charIndex = myMatcher.start();
						}
						else {
							charIndex = -1;
						}
												
					} /* end of while find semicolon without space after it */
					
					displayLine = true;
					
				} /* end of if found no space after semicolon */

				/* If we can parse the line, let's do it and get the tagname */
				if ( canParse == true ) {
					
					
					/* Parse the string into its fields */
					submissionFields = submissionLine.split(";");
					if ( submissionFields.length < 6 ) {
						obitErrors = obitErrors.logError("Error in parsing - blank tagname", obitErrors);
						tagname = "";
					}
					else {
						tagname = submissionFields[5].trim();
					}
					
					obitErrors.setErrorField(ErrorField.TAGNAME);
					
					/* Check for commas */
					punctChk = new PunctuationChecks();
					displayCheck = punctChk.commaCheck(tagname, ErrorField.TAGNAME, obitErrors);
					
					/* If we have not already decided to display the line, then
					 * set it to whatever the comma check decided.
					 */
					if ( ! displayLine )
					{
						displayLine = displayCheck;
					}
					
					/* Get the resulting tagname string */
					tagname = punctChk.getCheckField();
					
					/* Check for any parentheses or brackets. They shouldn't be there */
					if ( ValidateFields.checkParentheses(tagname) ) {
						obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
						obitErrors = obitErrors.logError("Invalid parentheses", obitErrors);
						tagname = tagname.replaceAll("\\)", "");
						tagname = tagname.replaceAll("\\(", "");
						displayLine = true;
					}
						
					if ( ValidateFields.checkBrackets(tagname) ) {
						obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
						obitErrors = obitErrors.logError("Invalid '[' or ']'", obitErrors);
						tagname = tagname.replaceAll("\\]", "");
						tagname = tagname.replaceAll("\\[", "");
						displayLine = true;
					}

					/* If the tagname is valid, then put it in the error log for counting.
					 * Otherwise, report an invalid tagname.
					 */
					if ( TagnameArray.verifyTagname(tagname, tagnamesArray.tagArray) == true ) {
						obitErrors.tagname = tagname;
					}
					else {
						obitErrors.tagname = null;
						obitErrors.setErrCategory(ErrorCategory.ERROR);
						obitErrors = obitErrors.logError("Invalid or unknown tagname", obitErrors);
					}					
					
					if ( submissionFields.length == 6 ) {
						/* put the tagname back in case anything was changed */
						submissionFields[5] = " " + tagname;
					}					
					
					/* Put the string back together from its parts */
					reassembledLine = submissionFields[0];
					for ( i=1; i<submissionFields.length; i++ ) {
						reassembledLine = reassembledLine + ";" + submissionFields[i];
					}
					submissionLine = reassembledLine;
					
				} /* end of if we can parse the line */
				
				obitErrors.setErrorField(ErrorField.RECORD);
								
				/* Look for disallowed characters.  Check for periods or carets. */
				if ( (submissionLine.indexOf('.') >=1) || (submissionLine.indexOf('^') >=1) ) {
					obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
					obitErrors = obitErrors.logError("Found '.' or '^'", obitErrors);

					/* Try to fix the problem.  Remove the character, but preserve
					 * spaces that are there, and if none, add a space.
					 */
					if (submissionLine.indexOf('.') >=1) {
						submissionLine = submissionLine.replaceAll("\\. "," ");
						submissionLine = submissionLine.replaceAll(" \\.", " ");
						submissionLine = submissionLine.replaceAll("\\.;", ";");
						submissionLine = submissionLine.replaceAll("\\.", " ");
						displayLine = true;
					}
					
					if (submissionLine.indexOf('^') >=1) {
						submissionLine = submissionLine.replaceAll("\\^ "," ");
						submissionLine = submissionLine.replaceAll(" \\^", " ");
						submissionLine = submissionLine.replaceAll("\\^;", ";");
						submissionLine = submissionLine.replaceAll("\\^"," ");
						displayLine = true;
					}					
					
				} /* end of if there is a period or caret */
				
				/* If the first character of the string is a space, report it and delete it. */
				if ( submissionLine.length() > 0 ) {
					if ( submissionLine.charAt(0) == ' ') {
						obitErrors.setErrCategory(ErrorCategory.SPACING);
						obitErrors = obitErrors.logError("First character a space", obitErrors);
					
						/* get the whole line, minus blanks at either end */
						submissionLine = submissionLine.trim();
						displayLine = true;
					
					} /* end of if there is a space at the beginning of the line */
					
				} /* end of if the line is non-empty */
				
				/* If there are any braces in the line, those are not valid characters. */
				if ( (submissionLine.indexOf('{') >= 0) || (submissionLine.indexOf('}') >=0) ) {
					obitErrors.setErrCategory(ErrorCategory.ERROR);
					obitErrors = obitErrors.logError("Invalid '{' or '}'", obitErrors);
				}
				
				/* If there are multiple consecutive spaces in the line, that is not valid */
				if ( submissionLine.contains("  ") ) {
					obitErrors.setErrCategory(ErrorCategory.SPACING);
					obitErrors = obitErrors.logError("Multiple spaces", obitErrors);
					
					/* Fix those spaces so just one remains */
					submissionLine = submissionLine.replaceAll("[ ]+", " ");
					displayLine = true;
				}

				/* If there is a space before or after the '>', then report it and remove any spaces. */
				if ( submissionLine.contains(" >") || submissionLine.contains("> ") ) {
					obitErrors.setErrCategory(ErrorCategory.SPACING);
					obitErrors = obitErrors.logError("Space before or after '>'", obitErrors);
					
					submissionLine = submissionLine.replaceAll("[ ]+>", ">");
					submissionLine = submissionLine.replaceAll(">[ ]+", ">");
					displayLine = true;
				}

				/* Look for double open parentheses and delete one of them */
				matchPattern = Pattern.compile("\\(\\(");
				myMatcher = matchPattern.matcher(submissionLine);

				if ( myMatcher.find() ) {				
					obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
					obitErrors = obitErrors.logError("Found '(('", obitErrors);
					displayLine = true;
					
					charIndex = myMatcher.start();
					while ( charIndex >= 0 ) 
					{
						builderLine = new StringBuilder();
						builderLine.append(submissionLine);
						
						/* Delete one of the parenthesis. */
						builderLine.deleteCharAt(charIndex);
						
						submissionLine = builderLine.toString();
						myMatcher = matchPattern.matcher(submissionLine);
						
						if ( myMatcher.find() ) {
							charIndex = myMatcher.start();
						}
						else {
							charIndex = -1;
						}						
						
					} /* end of while find double open parenthesis */
					
				} /* end of if there double open parenthesis */

				/* Look for double closing parentheses and delete one of them */
				matchPattern = Pattern.compile("\\)\\)");
				myMatcher = matchPattern.matcher(submissionLine);

				if ( myMatcher.find() ) {				
					obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
					obitErrors = obitErrors.logError("Found '))'", obitErrors);
					displayLine = true;
									
					charIndex = myMatcher.start();
					while ( charIndex >= 0 ) 
					{
						builderLine = new StringBuilder();
						builderLine.append(submissionLine);
						
						/* Delete one of the parenthesis. */
						builderLine.deleteCharAt(charIndex);
						
						submissionLine = builderLine.toString();
						myMatcher = matchPattern.matcher(submissionLine);
						
						if ( myMatcher.find() ) {
							charIndex = myMatcher.start();
						}
						else {
							charIndex = -1;
						}
												
					} /* end of while find double closing parenthesis */
					
				} /* end of if there double closing parenthesis */

				/* If there is no space before a '(' then report it and add the space */
				matchPattern = Pattern.compile("[^ ]\\(");
				myMatcher = matchPattern.matcher(submissionLine);

				if ( myMatcher.find() ) {				
					obitErrors.setErrCategory(ErrorCategory.SPACING);
					obitErrors = obitErrors.logError("No space in front of '('", obitErrors);
					displayLine = true;
										
					charIndex = myMatcher.start();
					while ( charIndex >= 0 ) 
					{
						builderLine = new StringBuilder();
						builderLine.append(submissionLine);
						
						/* Add spaces in front of any parentheses and then keep checking
						 * until all necessary spaces have been added.
						 */
						builderLine.insert(charIndex+1,' ');
						
						submissionLine = builderLine.toString();
						myMatcher = matchPattern.matcher(submissionLine);
						
						if ( myMatcher.find() ) {
							charIndex = myMatcher.start();
						}
						else {
							charIndex = -1;
						}						
						
					} /* end of while find no space before parenthesis */
					
				} /* end of if there are no spaces in front of parenthesis */
				
				/* If there is space after a '(' (not followed by a closing one)
				 * then report it and delete the space.
				 */
				matchPattern = Pattern.compile("\\([ ][^)]");
				myMatcher = matchPattern.matcher(submissionLine);

				if ( myMatcher.find() ) 
				{				
					obitErrors.setErrCategory(ErrorCategory.SPACING);
					obitErrors = obitErrors.logError("Disallowed space after '('", obitErrors);
					displayLine = true;
															
					/* Keep going through the line and removing spaces after
					 * any parenthesis.
					 */
					charIndex = myMatcher.start();
					while ( charIndex >= 0 ) 
					{
						builderLine = new StringBuilder();
						builderLine.append(submissionLine);
						
						/* Delete the space after the parenthesis. */
						builderLine.deleteCharAt(charIndex+1);
						
						submissionLine = builderLine.toString();
						myMatcher = matchPattern.matcher(submissionLine);
						
						if ( myMatcher.find() ) {
							charIndex = myMatcher.start();
						}
						else {
							charIndex = -1;
						}						
						
					} /* end of while find no space after parenthesis */
					
				} /* end of if there are spaces after parenthesis */

				/* If there is space before a ')' (not preceded by an opening one),
				 * then report it and delete the space.
				 */
				matchPattern = Pattern.compile("[^(][ ]\\)");
				myMatcher = matchPattern.matcher(submissionLine);

				if ( myMatcher.find() ) 
				{				
					obitErrors.setErrCategory(ErrorCategory.SPACING);
					obitErrors = obitErrors.logError("Disallowed space before ')'", obitErrors);
					displayLine = true;
										
					/* Keep going through the line and removing spaces before
					 * any parenthesis.
					 */
					charIndex = myMatcher.start();
					while ( charIndex >= 0 ) 
					{
						builderLine = new StringBuilder();
						builderLine.append(submissionLine);
						
						/* Delete the space after the parenthesis. */
						builderLine.deleteCharAt(charIndex+1);
						
						submissionLine = builderLine.toString();
						myMatcher = matchPattern.matcher(submissionLine);
						
						if ( myMatcher.find() ) {
							charIndex = myMatcher.start();
						}
						else {
							charIndex = -1;
						}
												
					} /* end of while find no space before parenthesis */
					
				} /* end of if there are spaces before closing parenthesis */

				/* If there is no space before a '[' then report it and add the space */
				matchPattern = Pattern.compile("[^ ]\\[");
				myMatcher = matchPattern.matcher(submissionLine);

				if ( myMatcher.find() ) 
				{				
					obitErrors.setErrCategory(ErrorCategory.SPACING);
					obitErrors = obitErrors.logError("No space in front of '['", obitErrors);
					displayLine = true;					
					
					charIndex = myMatcher.start();
					while ( charIndex >= 0 ) 
					{
						builderLine = new StringBuilder();
						builderLine.append(submissionLine);
						
						/* Add spaces in front of the bracket and then keep checking
						 * until all necessary spaces have been added.
						 */
						builderLine.insert(charIndex+1,' ');
						
						submissionLine = builderLine.toString();
						myMatcher = matchPattern.matcher(submissionLine);
						
						if ( myMatcher.find() ) {
							charIndex = myMatcher.start();
						}
						else {
							charIndex = -1;
						}
												
					} /* end of while find no space before bracket */
					
				} /* end of if there are no spaces in front of bracket */

				/* If there is space after a '[' then report it and delete the space */
				matchPattern = Pattern.compile("\\[[ ]");
				myMatcher = matchPattern.matcher(submissionLine);

				if ( myMatcher.find() ) 
				{				
					obitErrors.setErrCategory(ErrorCategory.SPACING);
					obitErrors = obitErrors.logError("Disallowed space after '['", obitErrors);
					displayLine = true;					
					
					/* Keep going through the line and removing spaces after
					 * any bracket.
					 */
					charIndex = myMatcher.start();
					while ( charIndex >= 0 ) 
					{
						builderLine = new StringBuilder();
						builderLine.append(submissionLine);
						
						/* Delete the space after the bracket. */
						builderLine.deleteCharAt(charIndex+1);
						
						submissionLine = builderLine.toString();
						myMatcher = matchPattern.matcher(submissionLine);
						
						if ( myMatcher.find() ) {
							charIndex = myMatcher.start();
						}
						else {
							charIndex = -1;
						}
												
					} /* end of while find a space after bracket */
					
				} /* end of if there are spaces after bracket */

				/* If there is space before a ']' then report it and delete the space */
				matchPattern = Pattern.compile("[ ]\\]");
				myMatcher = matchPattern.matcher(submissionLine);

				if ( myMatcher.find() ) 
				{				
					obitErrors.setErrCategory(ErrorCategory.SPACING);
					obitErrors = obitErrors.logError("Disallowed space before ']'", obitErrors);
					displayLine = true;					
					
					/* Keep going through the line and removing spaces before
					 * any bracket.
					 */
					charIndex = myMatcher.start();
					while ( charIndex >= 0 ) 
					{
						builderLine = new StringBuilder();
						builderLine.append(submissionLine);
						
						/* Delete the space before the bracket. */
						builderLine.deleteCharAt(charIndex);
						
						submissionLine = builderLine.toString();
						myMatcher = matchPattern.matcher(submissionLine);
						
						if ( myMatcher.find() ) {
							charIndex = myMatcher.start();
						}
						else {
							charIndex = -1;
						}
												
					} /* end of while find a space before closing bracket */
					
				} /* end of if there are spaces before closing bracket */

				/* We should only be using simple ASCII codes, but some people are using
				 * left and right quotes.
				 */
				if ( submissionLine.indexOf('\u201C') >= 0 || submissionLine.indexOf('\u201D') >= 0 ) {
					
					/* Report we found at least one disallowed quote */
					obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
					obitErrors = obitErrors.logError("Found disallowed left or right quote(s)", obitErrors);
					
					/* We replace any disallowed quotes with regular ones. */
					submissionLine = submissionLine.replace("\u201C","\"");
					submissionLine = submissionLine.replace("\u201D","\"");
					displayLine = true;					

				} /* end of if there are non-ASCII quotes in the line */
				
				/* look for disallowed single quotes as well */
				if ( submissionLine.indexOf('\u2018') >= 0 || submissionLine.indexOf('\u2019') >= 0 
						|| submissionLine.indexOf('\u0060') >= 0 || submissionLine.indexOf('\u00B4') >= 0) {
					
					/* Report we found at least one disallowed quote */
					obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
					obitErrors = obitErrors.logError("Found disallowed left or right single quote(s)", obitErrors);
					
					/* We replace any disallowed quotes with regular ones. */
					submissionLine = submissionLine.replace("\u2018","'");
					submissionLine = submissionLine.replace("\u2019","'");
					submissionLine = submissionLine.replace("\u0060","'");
					submissionLine = submissionLine.replace("\u00B4","'");
					displayLine = true;					

				} /* end of if there are non-ASCII single quotes in the line */
				
				/* Let's count how many quotes there are, if any. */
				int countQuotes;
				countQuotes = submissionLine.length() - submissionLine.replaceAll("\\\"","").length();

				/* If we have quotes, let's see if they are balanced */
				if ( countQuotes > 0 ) {
					
					/* If there are an even number, then they are balanced */
					if ( (countQuotes % 2) != 0 ) {
						/* unbalanced quotes */
						obitErrors.setErrCategory(ErrorCategory.ERROR);
						obitErrors = obitErrors.logError("Unbalanced quotes", obitErrors);
					}
				}

				/* Check for quotes right next to each other */
				matchPattern = Pattern.compile("\"\"");
				myMatcher = matchPattern.matcher(submissionLine);

				if ( myMatcher.find() ) {				
					obitErrors.setErrCategory(ErrorCategory.ERROR);
					obitErrors = obitErrors.logError("Quotes right next to each other", obitErrors);	
					
					/* Try to fix this.
					 * If the two quotes are after a letter and before a 
					 * semicolon, then remove one of the quotes.
					 * If there are letters on either side of the two quotes, then
					 * put a space between them.
					 * If there is a space, two quotes, and a letter, then replace
					 * the two quotes with one.
					 * Otherwise, if the quotes are unbalanced, replace the two
					 * quotes with one.
					 */
					
					/* Check for a letter, two quotes, a semicolon */
					matchPattern = Pattern.compile("\\w\"\";");
					myMatcher = matchPattern.matcher(submissionLine);

					/* Keep replacing the two quotes with a single quote as long
					 * as we find a match.
					 */
					if ( myMatcher.find() ) 
					{						
						charIndex = myMatcher.start();
						while ( charIndex >= 0 ) 
						{
							builderLine = new StringBuilder();
							builderLine.append(submissionLine);
							
							/* Delete one of the quotes. */
							builderLine.deleteCharAt(charIndex+1);
							
							submissionLine = builderLine.toString();
							myMatcher = matchPattern.matcher(submissionLine);
							
							if ( myMatcher.find() ) {
								charIndex = myMatcher.start();
							}
							else {
								charIndex = -1;
							}
														
						} /* end of while find a letter, two quotes then semicolon */
						
						/* display the line */
						displayLine = true;
					
					} /* end of if a letter, two quotes, semicolon */

					/* Check for two quotes with no spaces around them */
					matchPattern = Pattern.compile("\\w\"\"\\w");
					myMatcher = matchPattern.matcher(submissionLine);

					/* Keep replacing it with a space in between as long as we
					 * find a match.
					 */
					if ( myMatcher.find() ) 
					{						
						charIndex = myMatcher.start();
						while ( charIndex >= 0 ) 
						{
							builderLine = new StringBuilder();
							builderLine.append(submissionLine);
							
							/* Add space in front of the matched quotes then keep
							 * checking until all necessary spaces have been added.
							 */
							builderLine.insert(charIndex+2,' ');
							
							submissionLine = builderLine.toString();
							myMatcher = matchPattern.matcher(submissionLine);
							
							if ( myMatcher.find() ) {
								charIndex = myMatcher.start();
							}
							else {
								charIndex = -1;
							}							
							
						} /* end of while find no space between quotes */
						
						/* display the line */
						displayLine = true;
					
					} /* end of if no space between or beside quotes */

					/* Check for two quotes with a space before, no space after */
					matchPattern = Pattern.compile("[ ]\"\"\\w");
					myMatcher = matchPattern.matcher(submissionLine);

					/* Keep replacing it with a single quote as long as we
					 * find a match.
					 */
					if ( myMatcher.find() ) 
					{						
						charIndex = myMatcher.start();
						while ( charIndex >= 0 ) 
						{
							builderLine = new StringBuilder();
							builderLine.append(submissionLine);
							
							/* Delete one of the quotes. */
							builderLine.deleteCharAt(charIndex+1);
							
							submissionLine = builderLine.toString();
							myMatcher = matchPattern.matcher(submissionLine);
							
							if ( myMatcher.find() ) {
								charIndex = myMatcher.start();
							}
							else {
								charIndex = -1;
							}							
							
						} /* end of while find a space, two quotes then a letter */
						
						/* display the line */
						displayLine = true;
					
					} /* end of if a space, two quotes, a letter */

					/* If there are still unbalanced quotes, replace any remaining
					 * two quotes next to each other with a single quote.
					 */
					countQuotes = submissionLine.length() - submissionLine.replaceAll("\\\"","").length();

					if ( countQuotes > 0 ) {
						
						submissionLine = submissionLine.replace("\"\"","\"");
						displayLine = true;
					}
					
				} /* end of quotes found right next to each other */
				
				/* Check for quotes with spaces on both sides of them */
				matchPattern = Pattern.compile("[ ]\"[ ]");
				myMatcher = matchPattern.matcher(submissionLine);

				/* If we find it, just report it, because there's no way to
				 * tell what it should be.
				 */
				if ( myMatcher.find() ) {				
					obitErrors.setErrCategory(ErrorCategory.ERROR);
					obitErrors = obitErrors.logError("Spaces before and after double quote", obitErrors);					
				}
				
				/* Check for single quotes with spaces on both sides of them */
				matchPattern = Pattern.compile("[ ]\'[ ]");
				myMatcher = matchPattern.matcher(submissionLine);

				/* If we find it, just report it, because there's no way to
				 * tell what it should be.
				 */
				if ( myMatcher.find() ) {				
					obitErrors.setErrCategory(ErrorCategory.ERROR);
					obitErrors = obitErrors.logError("Spaces before and after single quote", obitErrors);					
				}
				
				/* If there is a space before a quote-semicolon,
				 * then report it and delete the space.
				 */
				matchPattern = Pattern.compile("[ ]\";");
				myMatcher = matchPattern.matcher(submissionLine);

				if ( myMatcher.find() ) 
				{				
					obitErrors.setErrCategory(ErrorCategory.SPACING);
					obitErrors = obitErrors.logError("Space before quote and semicolon", obitErrors);
					
					charIndex = myMatcher.start();
					builderLine = new StringBuilder();
					builderLine.append(submissionLine);
					
					/* Delete the space in front the quote */
					builderLine.deleteCharAt(charIndex);					
					submissionLine = builderLine.toString();
					myMatcher = matchPattern.matcher(submissionLine);			
						
					displayLine = true;
					
				} /* end of if there a space in front of quote-semicolon */
				
				/* Check for quotes with no spaces around them */
				matchPattern = Pattern.compile("\\w\"\\w");
				myMatcher = matchPattern.matcher(submissionLine);

				/* Just report it if we find it, because we cannot tell what
				 * it should be.
				 */
				if ( myMatcher.find() ) {				
					obitErrors.setErrCategory(ErrorCategory.ERROR);
					obitErrors = obitErrors.logError("No space before or after quote", obitErrors);					
				}				
				
				/* END RECORD LEVEL ERRORS */
				
				/* If we were able to parse the line, we can continue with the rest of
				 * the error checks.
				 */				
				if (canParse == true) {
					
					/* parse the line into fields */
					submissionFields = submissionLine.split("; ");
					
					if ( submissionFields.length < 6 ) {
						canParse = false;
						obitErrors.setErrCategory(ErrorCategory.ERROR);
						obitErrors = obitErrors.logError("Problem parsing fields", obitErrors);
					}
				}
				
				if ( canParse == true ) {
					
					/* parse the line into fields */
					submissionFields = submissionLine.split("; ");
					
					/* BEGIN NAME FIELD CHECKS */
					obitErrors.setErrorField(ErrorField.NAME);

					String nameString;
					nameString = submissionFields[0].trim();
					
					String[] nameParts;
					String restOfTheName = "";
					String firstName = "";
					String surName = "";
					String maybeAge = ""; /* we may find numbers in odd places */
					
					displayCheck = punctChk.commaCheck(nameString, ErrorField.NAME, obitErrors);
					
					if ( ! displayLine )
					{
						displayLine = displayCheck;
					}
					
					hasCommas = punctChk.isCommaExists();
					nameString = punctChk.getCheckField();
					
					/* If the name string has commas, then we can parse it because
					 * the punctuation check will have made sure there is only one.
					 */
					if ( hasCommas ) 
					{
						/* We can start to split the name. */
						nameParts = nameString.split(", ");
						surName = nameParts[0];
						
						/* If there are not enough parts, then it's an error and
						 * we cannot parse the line.
						 */
						if ( nameParts.length < 2 ) {
							obitErrors.setErrCategory(ErrorCategory.ERROR);
							obitErrors = obitErrors.logError("No first name given", obitErrors);
							hasCommas = false;
						}
						else {
							restOfTheName = nameParts[1];
						}							
						
					} /* if there are commas in the name field */
				
					/* Count the slashes, there should not be any */
					countChars = nameString.length() - nameString.replaceAll("\\/","").length();
					if ( countChars > 0 )
					{
						obitErrors.setErrCategory(ErrorCategory.ERROR);
						obitErrors = obitErrors.logError("Slashes are not allowed in name", obitErrors);
					}
					
					/* This object is used to check the various names */
					NameValidation validateName = new NameValidation();
					validateName.libFilesPath = DirectorySetup.libFilesPath.toString();
					
					/* If there is a comma in the name field we can check some
					 * things about the surname.
					 */
					if ( hasCommas ) {
						
						/* if the surname is not null, we can check it for problems */
						if ( ! surName.equals("") ) {
							obitErrors.setErrorField(ErrorField.SURNAME);
							
							/* look for any non-ASCII characters */
							int countNonAscii = 0;
							countNonAscii = surName.length() - surName.replaceAll("[^\\p{ASCII}]","").length();
							
							/* If we found any non-ASCII characters in the surname,
							 * report it.
							 */
							if ( countNonAscii > 0 ) {
								obitErrors.setErrCategory(ErrorCategory.ERROR);
								obitErrors = obitErrors.logError("Non-ASCII characters found", obitErrors);
							}
							
							validateName.nameToCheck = surName;
							surName = NameValidation.checkLastName(obitErrors, validateName);
							
							if ( validateName.corrected ) {
								displayLine = true;
							}
							
						} /* end of if the surname is not null */
						
						else {
							obitErrors.setErrCategory(ErrorCategory.ERROR);
							obitErrors = obitErrors.logError("Empty surname", obitErrors);
						}

					} /* end of if there are commas */
					
					/* If the rest of the name is not null, we have to look for
					 * more fields, e.g., first name, maiden name.
					 */
					if ( hasCommas && ! restOfTheName.equals("") ) {	
						
						obitErrors.setErrorField(ErrorField.NAME);
						
						/* Let's count how many parentheses there are, if any. */
						countLeftParens = restOfTheName.length() - restOfTheName.replaceAll("\\(","").length();
						countRightParens = restOfTheName.length() - restOfTheName.replaceAll("\\)","").length();

						/* If we have a left parenthesis, let's see if they are balanced and whatnot */
						if ( countLeftParens > 0 || countRightParens > 0 ) {
							
							if ( countLeftParens != countRightParens ) {
								/* unbalanced parentheses */
								obitErrors.setErrCategory(ErrorCategory.ERROR);
								obitErrors = obitErrors.logError("Unbalanced parentheses", obitErrors);
							}
							
							if ( countLeftParens > 1 || countRightParens > 1 ) {
								/* too many parentheses */
								obitErrors.setErrCategory(ErrorCategory.ERROR);

								/* If there are two open in a row, we can fix that */
								if ( countLeftParens > 1 ) {
									restOfTheName = restOfTheName.replaceAll("\\(\\(", "(");
									countLeftParens = restOfTheName.length() - restOfTheName.replaceAll("\\(","").length();
									if ( countLeftParens == 1 ) {
										/* We fixed it */
										obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
										displayLine = true;
									}
								}
								
								/* If there are two closing in a row, we can fix that */
								if ( countRightParens > 1 ) {
									restOfTheName = restOfTheName.replaceAll("\\)\\)", ")");
									countLeftParens = restOfTheName.length() - restOfTheName.replaceAll("\\)","").length();
									if ( countRightParens == 1 ) 
									{
										/* We fixed it */
										obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
										displayLine = true;
									}
								}	
								obitErrors = obitErrors.logError("Too many parentheses", obitErrors);
							}
							if ( countLeftParens == countRightParens ) {
								/* we have balanced parentheses -- is there anything between them? */
								matchPattern = Pattern.compile("\\(\\)");
								myMatcher = matchPattern.matcher(restOfTheName);

								/* If there is no space between the parentheses, then
								 * report it and add it.
								 */
								if ( myMatcher.find() ) 
								{
									/* There's supposed to at least be a space between the parentheses */
									obitErrors.setErrCategory(ErrorCategory.SPACING);
									obitErrors = obitErrors.logError("No space between parentheses", obitErrors);
									
									/* add the space */																		
									charIndex = myMatcher.start();
									
									builderLine = new StringBuilder();
									builderLine.append(restOfTheName);									
									builderLine.insert(charIndex+1,' ');
									
									restOfTheName = builderLine.toString();	
									displayLine = true;
									
								} /* end of if there's nothing between the parentheses */
															
							} /* end of else there are balanced parentheses */											
							
						} /* end of if there are parentheses */
												
						/* Let's count how many brackets there are, if any. */
						int countLeftBrackets, countRightBrackets;
						countLeftBrackets = restOfTheName.length() - restOfTheName.replaceAll("\\[","").length();
						countRightBrackets = restOfTheName.length() - restOfTheName.replaceAll("\\]","").length();

						/* If we have left brackets, let's see if they are balanced */
						if ( countLeftBrackets > 0 || countRightBrackets > 0 ) 
						{							
							if ( countLeftBrackets != countRightBrackets ) 
							{
								/* unbalanced brackets */
								obitErrors.setErrCategory(ErrorCategory.ERROR);
								obitErrors = obitErrors.logError("Unbalanced '[' or ']'", obitErrors);
								displayLine = true;
							}
						}
						
						/* Let's count how many quotes there are in the name, if any. */
						countQuotes = restOfTheName.length() - restOfTheName.replaceAll("\\\"","").length();

						/* If we have left brackets, let's see if they are balanced */
						if ( countQuotes > 0 ) {
							
							/* If there are an even number, then they are balanced */
							if ( (countQuotes % 2) != 0 ) {
								/* unbalanced quotes */
								obitErrors.setErrCategory(ErrorCategory.ERROR);
								obitErrors = obitErrors.logError("Unbalanced quotes", obitErrors);
							}
						}
						
						/* Now we have to get all the rest of the parts of the name.  The
						 * easiest way is to go character by character.
						 */
						String maidenName = "";
						
						/* There can be more than one other name or nick name, so we use
						 * arrays to keep track of them.
						 */
						ArrayList<String> otherNamesArray = new ArrayList<>();
						ArrayList<String> nickNamesArray = new ArrayList<>();
						String otherName = "";
						String nickName = "";

						/* Use these booleans to keep track of where we are
						 * in the name field.
						 * Assume we start getting the first name.
						 */
						boolean gettingFirstName = true;
						boolean gettingMaidenName = false;
						boolean gettingOtherName = false;
						boolean gettingNickName = false;
						boolean noMoreParens = false;
						boolean noMoreBrackets = false;
						boolean noMoreQuotes = false;
						boolean missIncluded = false;
						char workingChar; 
		
						/* Start looking at the first name */
						obitErrors.setErrorField(ErrorField.FIRSTNAME);

						for ( i=0; i<restOfTheName.length(); i++ ) 
						{							
							workingChar = restOfTheName.charAt(i);
							
							/* If we're working on the first name, keep adding characters as long
							 * as we keep seeing characters for a name.
							 */
							if ( gettingFirstName ) {
								if (Character.isLetter(workingChar) || workingChar == ' ' || workingChar == '\'' || workingChar == '-' ) {
									
									/* If it is a non-ASCII character, report it and delete it. */
									if ( ! StringUtils.isPureAscii(Character.toString(workingChar) ) ) {
										obitErrors.setErrCategory(ErrorCategory.ERROR);
										obitErrors = obitErrors.logError("Disallowed non-ASCII character deleted: " + workingChar, obitErrors);
										displayLine = true;
									}
									else {
										firstName = firstName + Character.toString(workingChar);
									}									
								}
								else if ( workingChar == '(' ) {
									gettingFirstName = false;
									gettingNickName = false;
									noMoreQuotes = true;
									
									gettingMaidenName = true;	
									obitErrors.setErrorField(ErrorField.MAIDENNAME);
								}
								else if ( workingChar == '[' ) {
									gettingFirstName = false;
									gettingMaidenName = false;
									noMoreParens = true;
									gettingNickName = false;
									noMoreQuotes = true;
									
									gettingOtherName = true;
									otherName = "";
									obitErrors.setErrorField(ErrorField.OTHERNAME);
								}
								else if ( workingChar == '{' ) {
									/* People seem to use these sometimes instead
									 * of square brackets.  We'll handle it.
									 */
									obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
									obitErrors = obitErrors.logError("Unexpected '{'", obitErrors);
									displayLine = true;

									gettingFirstName = false;
									gettingMaidenName = false;
									noMoreParens = true;
									gettingNickName = false;
									noMoreQuotes = true;
									
									gettingOtherName = true;
									otherName = "";
									obitErrors.setErrorField(ErrorField.OTHERNAME);
								}
								else if (workingChar == '"') {
									gettingFirstName = false;
									gettingNickName = true;
									nickName = "";
									obitErrors.setErrorField(ErrorField.NICKNAME);
								}
								else if ( Character.isDigit(workingChar) ) {
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									obitErrors = obitErrors.logError("Found number", obitErrors);
									
									/* Numbers are not supposed to be there, but
									 * it may be a misplaced age.  In any event,
									 * we will be removing it from the field.
									 * So we'll make sure we print the "corrected"
									 * line, and save the number, in case it is
									 * an age.
									 */
									displayLine = true;
									maybeAge = maybeAge + workingChar;
								}
								else {
									
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									
									/* If it is a non-ASCII character, report it and delete it. */
									if ( ! StringUtils.isPureAscii(Character.toString(workingChar) ) ) {										
										obitErrors = obitErrors.logError("Disallowed non-ASCII character deleted", obitErrors);
									}
									else {										
										obitErrors = obitErrors.logError("Unexpected character deleted: " + workingChar, obitErrors);
									}
									
									/* It isn't exactly corrected, but it will
									 * not appear in the line, so we'll make sure
									 * it is printed for the use to see what happened
									 * to the unexpected character.
									 */
									displayLine = true;
								}
								
								/* We'll wait until later to validate the first name, in case
								 * we had some weird stray parts where they're not supposed
								 * to be							
								 */
								
							} /* end of if getting first name */
							
							else if ( gettingMaidenName ) {
								if (Character.isLetter(workingChar) || workingChar == ' ' || workingChar == '\'' || workingChar == '-' ) {
									
									/* If it is a non-ASCII character, report it and delete it. */
									if ( ! StringUtils.isPureAscii(Character.toString(workingChar) ) ) {
										obitErrors.setErrCategory(ErrorCategory.ERROR);
										obitErrors = obitErrors.logError("Disallowed non-ASCII character deleted: " + workingChar, obitErrors);
										displayLine = true;
									}
									else {
										maidenName = maidenName + Character.toString(workingChar);
									}
								}
								else if ( workingChar == ')' ) {
									gettingMaidenName = false;	
									noMoreParens = true;
									obitErrors.setErrorField(ErrorField.NAME);
								}
								else if ( workingChar == '[' ) {					
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									obitErrors = obitErrors.logError("Unexpected open bracket", obitErrors);
									
									/* It would appear that we are now getting an other name */
									gettingMaidenName = false;	
									noMoreParens = true;
									gettingOtherName = true;
									otherName = "";			
									obitErrors.setErrorField(ErrorField.OTHERNAME);
								}
								else if ( workingChar == ']' ) {		
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									obitErrors = obitErrors.logError("Unexpected closing bracket", obitErrors);
								}
								else if (workingChar == '"') {						
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									obitErrors = obitErrors.logError("Unexpected quote", obitErrors);
									
									/* It would appear that we are now getting a nick name */
									gettingMaidenName = false;	
									noMoreParens = true;
									gettingNickName = true;
									nickName = "";
									obitErrors.setErrorField(ErrorField.NICKNAME);
								}
								else if ( workingChar == '?' ) {
									obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
									obitErrors = obitErrors.logError("Unexpected question mark", obitErrors);

									/* they meant a space */
									maidenName = maidenName + " ";
									displayLine = true;
								}
								else if ( Character.isDigit(workingChar) ) {
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									obitErrors = obitErrors.logError("Found number", obitErrors);
									
									/* Not supposed to have a number here but
									 * perhaps it's a misplaced age.  We'll save
									 * it and say that it's "corrected" so the line
									 * will be printed in its altered state.
									 */
									maybeAge = maybeAge + workingChar;
									displayLine = true;
								}
								else 
								{									
									obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
									
									/* If it is a non-ASCII character, report it and delete it. */
									if ( ! StringUtils.isPureAscii(Character.toString(workingChar) ) ) {										
										obitErrors = obitErrors.logError("Disallowed non-ASCII character deleted: " + workingChar, obitErrors);
									}
									else {										
										obitErrors = obitErrors.logError("Unexpected character deleted: " + workingChar, obitErrors);
									}
									
									/* It wasn't exactly corrected, but we want to print the
									 * character so that the user will see what happened to
									 * the unexpected character since it won't be saved.
									 */
									displayLine = true;
								}
								
								/* If we are no longer getting characters for the maiden name, 
								 * we can check it for validity.
								 */
								if ( ! gettingMaidenName ) {									
									
									/* If there is an actual maiden name to check, then check it */
									if ( ! maidenName.matches("[ ]+") ) {
										validateName.nameToCheck = maidenName.trim();
										obitErrors.setErrorField(ErrorField.MAIDENNAME);
										maidenName = NameValidation.checkLastName(obitErrors, validateName);
										
										if ( validateName.corrected ) 
										{
											displayLine = true;
										}
									}									

								} /* end of if no longer getting first name */							
								
							} /* end of else getting the maiden name */
							
							else if ( gettingOtherName ) 
							{
								if (Character.isLetter(workingChar) || workingChar == ' ' || workingChar == '\'' || workingChar == '-' ) {
									
									/* If it is a non-ASCII character, report it and delete it. */
									if ( ! StringUtils.isPureAscii(Character.toString(workingChar) ) ) 
									{
										obitErrors.setErrCategory(ErrorCategory.ERROR);
										obitErrors = obitErrors.logError("Disallowed non-ASCII character deleted: " + workingChar, obitErrors);
										displayLine = true;
									}
									else {
										otherName = otherName + Character.toString(workingChar);
									}
								}
								else if ( workingChar == ']' ) 
								{
									gettingOtherName = false;
									obitErrors.setErrorField(ErrorField.NAME);
									
									/* there might be more Other names, so we can keep looking for brackets */
								}
								else if ( workingChar == '}' ) 
								{
									/* Some people use these instead of square brackets
									 * even though they aren't supposed to.  We'll handle
									 * it the same but report it.
									 */									
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									obitErrors = obitErrors.logError("Unexpected '}'", obitErrors);
									displayLine = true;
									
									gettingOtherName = false;
									/* there might be more Other names, so we can keep looking for brackets */
								}
								else if ( workingChar == ')' || workingChar == '(' ) 
								{					
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									obitErrors = obitErrors.logError("Unexpected parenthesis", obitErrors);
									
									/* we might be collecting an unusual other name, 
									 * so turn off gettingOtherName
									 */
									gettingOtherName = false;
								}
								else if (workingChar == '"') 
								{							
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									obitErrors = obitErrors.logError("Unexpected quote", obitErrors);
									
									/* Apparently we're now looking for a nick name.
									 * The submission must have things in the wrong order
									 * or unbalanced delimiters
									 */
									gettingOtherName = false;
									noMoreBrackets = true;
									gettingNickName = true;
									nickName="";
									obitErrors.setErrorField(ErrorField.NICKNAME);
								}
								else if ( Character.isDigit(workingChar) ) 
								{
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									obitErrors = obitErrors.logError("Found number", obitErrors);
									
									/* We don't expect numbers in this field, but they
									 * may have put the age in the wrong place.  We'll save
									 * this digit and check later.  We'll also print the
									 * "corrected" line so the user will see what happened.
									 */
									maybeAge = maybeAge + workingChar;
									displayLine = true;
								}
								else 
								{									
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									
									/* If it is a non-ASCII character, report it and delete it. */
									if ( ! StringUtils.isPureAscii(Character.toString(workingChar) ) ) 
									{										
										obitErrors = obitErrors.logError("Disallowed non-ASCII character deleted: " + workingChar, obitErrors);
									}
									else {											
										obitErrors = obitErrors.logError("Unexpected character deleted: " + workingChar, obitErrors);
									}
									
									/* We didn't really correct it, but the character will not
									 * be saved so we'll print the line so the user can see
									 * what happened to the unexpected character.
									 */
									displayLine = true;
								}
								
								/* If we are no longer getting the other name, we can check it.
								 * Then we add it to the array of other names.
								 */
								if ( ! gettingOtherName ) 
								{
									validateName.nameToCheck = otherName.trim();

									/* If the name is not null, then check it */
									if ( !validateName.nameToCheck.equals("") ) 
									{
										obitErrors.setErrorField(ErrorField.OTHERNAME);
										otherName = NameValidation.checkLastName(obitErrors, validateName);			
										otherNamesArray.add(otherName);
										
										if ( validateName.corrected ) {
											displayLine = true;
										}									
									}									
									
								} /* end of no longer getting other name */
								
							} /* end of else if getting other name */
							
							else if ( gettingNickName ) 
							{
								if (Character.isLetter(workingChar) || workingChar == ' ' || workingChar == '\'' || workingChar == '-' ) 
								{									
									/* If it is a non-ASCII character, report it and delete it. */
									if ( ! StringUtils.isPureAscii(Character.toString(workingChar) ) ) 
									{
										obitErrors.setErrCategory(ErrorCategory.ERROR);
										obitErrors = obitErrors.logError("Disallowed non-ASCII character deleted: " + workingChar, obitErrors);
										displayLine = true;
									}
									else 
									{
										nickName = nickName + Character.toString(workingChar);
									}
								}
								else if (workingChar == '"') 
								{
									gettingNickName = false;
									obitErrors.setErrorField(ErrorField.NAME);
									
									/* there can be multiple nicknames */
								}
								else if ( workingChar == '(' ) 
								{					
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									obitErrors = obitErrors.logError("Unexpected open parenthesis", obitErrors);
									
									/* Let's figure that the closing quote is missing
									 * and now we're doing the maiden name
									 */
									gettingNickName = false;
									noMoreQuotes = true;
									gettingMaidenName = true;
									obitErrors.setErrorField(ErrorField.MAIDENNAME);
								}
								else if ( workingChar == ')' ) 
								{	
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									obitErrors = obitErrors.logError("Unexpected closing parenthesis", obitErrors);
									
									/* assume we are no longer getting nick name */
									gettingNickName = false;
									noMoreQuotes = true;
								}
								else if ( workingChar == '[' ) 
								{					
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									obitErrors = obitErrors.logError("Unexpected open bracket", obitErrors);
									
									/* Let's figure that the closing quote is missing
									 * and now we're doing an other name
									 */
									gettingNickName = false;
									noMoreQuotes = true;
									gettingOtherName = true;
									otherName = "";
									obitErrors.setErrorField(ErrorField.OTHERNAME);
								}
								else if ( workingChar == ']' ) 
								{	
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									obitErrors = obitErrors.logError("Unexpected closing bracket", obitErrors);
									
									/* Let's figure that the closing quote is missing
									 * and now we're no longer doing the nick name.
									 */
									gettingNickName = false;
									noMoreQuotes = true;
									obitErrors.setErrorField(ErrorField.NAME);
								}
								else if ( Character.isDigit(workingChar) ) 
								{
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									obitErrors = obitErrors.logError("Found number", obitErrors);
									
									/* We don't expect a number, but the age may be in 
									 * the wrong place.  Save the number and print the
									 * line.
									 */
									maybeAge = maybeAge + workingChar;
									displayLine = true;
								}
								else 
								{
									obitErrors.setErrCategory(ErrorCategory.ERROR);

									if ( ! StringUtils.isPureAscii(Character.toString(workingChar) ) ) 
									{
										obitErrors = obitErrors.logError("Non-ASCII character deleted: " + workingChar, obitErrors);
									}
									else 
									{
										obitErrors = obitErrors.logError("Unexpected character deleted: " + workingChar, obitErrors);
									}									
									
									/* We didn't "correct" it, but print the line so
									 * the user can see what happened.
									 */
									displayLine = true;
								}
								
								/* If we are no longer getting characters for this nick name, 
								 * we can check it for validity.
								 */
								if ( ! gettingNickName ) 
								{
									validateName.nameToCheck = nickName.trim();
									validateName.nameType = FirstNameType.NICK;
									
									/* If there's actually something in the nickname,
									 * check it and add it to the array.
									 */
									if ( ! nickName.equals("") ) 
									{
										obitErrors.setErrorField(ErrorField.NICKNAME);
										nickName = NameValidation.checkFirstName(obitErrors, validateName);
										nickNamesArray.add(nickName);
										
										if ( validateName.corrected ) 
										{
											displayLine = true;
										}
									}									
									
								} /* end of if no longer getting nick name */
								
							} /* end of else if getting nick name */
							
							/* Now we check for transitions to the next type of name to check */
							else if ( workingChar == '(' ) {
								
								/* If we don't expect a maiden name at this point, 
								 * we should report it.
								 */
								if ( noMoreParens ) {
									obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
									obitErrors = obitErrors.logError("Unexpected open parenthesis", obitErrors);
									
									/* We'll do our best to put the line
									 * back together in the right order.
									 */
									displayLine = true;
									
								} /* end of if we weren't expecting parentheses */	

								/* If we have a maiden name already, then we'll
								 * assume that this is another other name with
								 * the wrong kind of delimiters.
								 */
								if ( ! maidenName.equals("") ) 
								{
									/* We should not be getting another maiden name -- you can only
									 * have one maiden name.  So we'll report it and switch to gathering
									 * "other name."
									 */										
									gettingMaidenName = false;
									gettingOtherName = true;
									otherName = "";

									obitErrors.setErrorField(ErrorField.OTHERNAME);
									
									/* We'll do our best to put the line
									 * back together in the right order.
									 */
									displayLine = true;
								}
																
								else 
								{									
									/* We start gathering a maiden name */
									gettingMaidenName = true;
									obitErrors.setErrorField(ErrorField.MAIDENNAME);

									/* we shouldn't expect for a nick name after this */
									noMoreQuotes = true;				
								}

							} /* end of else it's an open parenthesis */
							
							else if ( workingChar == '[' ) 
							{								
								/* If we were not expecting this, that means that
								 * the fields are out of order, so we'll report an 
								 * error.  Our processing will presumably fix the
								 * problem of order.
								 */
								if ( noMoreBrackets ) 
								{
									obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
									obitErrors = obitErrors.logError("Unexpected open bracket", obitErrors);
									displayLine = true;
								}
								
								/* We are now getting an Other name.
								 * set things up for doing that.
								 */
								gettingOtherName = true;
								otherName = "";
								obitErrors.setErrorField(ErrorField.OTHERNAME);
								noMoreQuotes = true;
								
							} /* end of else if opening bracket */
							
							else if ( workingChar == '"' ) 
							{								
								/* If we weren't expecting a nick name field at this
								 * point, then things are out of order.  Report the error.
								 * Our processing will presumably put things back in order.
								 */
								if ( noMoreQuotes ) 
								{
									obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
									obitErrors = obitErrors.logError("Unexpected open quote", obitErrors);
									displayLine = true;
								}

								/* We're getting a nick name now, so set things
								 * up for that processing.
								 */
								gettingNickName = true;
								noMoreQuotes = false;
								nickName = "";
								obitErrors.setErrorField(ErrorField.NICKNAME);
								
							} /* end of else it's an opening quote */
							
							else if ( ! StringUtils.isPureAscii(Character.toString(workingChar) ) ) 
							{								
								/* Not supposed to have any non-ASCII characters at all. */
								obitErrors.setErrCategory(ErrorCategory.ERROR);
								obitErrors = obitErrors.logError("Non-ASCII character deleted: " + workingChar, obitErrors);
								
								/* It wasn't exactly a correction, but we'll print the line
								 * so the user can see what it looks like.
								 */
								displayLine = true;
							}
							
							else if ( Character.isLetter(workingChar) || workingChar == '\'' || workingChar == '-') 
							{								
								/* we didn't expect to see any name characters that are not surrounded by delimiters */
								/* Let's go back to gathering the first name */
								obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
								obitErrors = obitErrors.logError("Unexpected name character", obitErrors);
								displayLine = true;								

								/* Process this as a first name section */
								gettingFirstName = true;			
								firstName = firstName + " " + Character.toString(workingChar);
								obitErrors.setErrorField(ErrorField.FIRSTNAME);
								
							} /* end of else if it's a name character */
							
							else if ( Character.isDigit(workingChar) ) 
							{								
								/* We don't expect numbers in a name.
								 * Perhaps it's a misplaced age field.
								 * Save the number and report it, and
								 * print the line.
								 */
								obitErrors.setErrCategory(ErrorCategory.ERROR);
								obitErrors = obitErrors.logError("Found number", obitErrors);
								maybeAge = maybeAge + workingChar;
								displayLine = true;
								
							} /* end of else a digit found */
							
							else if ( workingChar == '{' ) 
							{								
								/* Some people use curly braces by mistake.
								 * Treat it like square brackets, but report the
								 * error.
								 */
								obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
								obitErrors = obitErrors.logError("Unexpected '{'", obitErrors);
								displayLine = true;
								
								gettingOtherName = true;
								otherName = "";
								obitErrors.setErrorField(ErrorField.OTHERNAME);
								noMoreQuotes = true;
								
							} /* end of else if a curly brace */
							
							else if ( workingChar != ' ' ) 
							{								
								/* We don't fix this exactly, but we don't save
								 * unexpected characters, so print the line to
								 * let the user see what happened.
								 */
								obitErrors.setErrCategory(ErrorCategory.ERROR);
								obitErrors = obitErrors.logError("Unexpected character deleted: " + workingChar, obitErrors);
								displayLine = true;
								
							} /* end of else if unexpected character */				
							
						} /* end of loop through characters in rest of name */
						
						/* Now we can validate the first name, if there was
						 * a comma so we could parse it.
						 */
						if ( hasCommas ) 
						{							
							/* Clean up any extra spaces that may have been
							 * added along the way.
							 */
							firstName = firstName.replaceAll("[ ]+", " ");
							validateName.nameToCheck = firstName.trim();
							validateName.nameType = FirstNameType.FIRST;
							obitErrors.setErrorField(ErrorField.FIRSTNAME);
							firstName = NameValidation.checkFirstName(obitErrors, validateName);
							missIncluded = validateName.missIncluded;
							
							if ( validateName.corrected ) 
							{
								displayLine = true;
							}							
							
						} /* end of if there was a comma */						

						/* Put the rest of the name back together, in case we fixed anything */
						restOfTheName = firstName.trim();
						
						/* Get the nick name(s), if any */
						if ( ! nickNamesArray.isEmpty() ) 
						{
							for ( i=0; i<nickNamesArray.size(); i++ ) 
							{
								restOfTheName = restOfTheName + " \"" + nickNamesArray.get(i).trim() + "\"";
							}
						}
						
						/* Get the maiden name if there is one */
						if ( ! maidenName.equals("") ) 
						{							
							/* If the maiden name is spaces, we want
							 * to keep it that way, but otherwise, we
							 * want to trim spaces.
							 */
							if ( !maidenName.matches("[ ]+") ) 
							{
								maidenName = maidenName.trim();
							}
							else {
								/* make sure it's just one space */
								maidenName = " ";
							}
							
							restOfTheName = restOfTheName + " (" + maidenName + ")";
							
							/* If they also had "miss" in the first name, that doesn't make
							 * sense.
							 */
							if ( missIncluded ) 
							{
								obitErrors.setErrCategory(ErrorCategory.ERROR);
								obitErrors = obitErrors.logError("Maiden name and 'miss' in same record", obitErrors);
							}
							
						} /* end of if there is a maiden name */
						
						/* Get the other name(s), if any */
						if ( ! otherNamesArray.isEmpty() ) 
						{
							for ( i=0; i<otherNamesArray.size(); i++ ) 
							{
								restOfTheName = restOfTheName + " [" + otherNamesArray.get(i).trim() + "]";
							}
						}						
						
						/* Put the whole name back together in case we fixed anything */
						nameString = surName + ", " + restOfTheName.trim();		
						
						/* clean up any multiple spaces that may have been created */
						nameString = nameString.replaceAll("[ ]+", " ");
						
					} /* end of if the first name chunk is not null */

					/* Put the nameString back into the line */
					submissionFields[0] = nameString.trim();
					
					/* END OF NAME FIELD CHECKS */
				
					/* BEGIN AGE FIELD CHECKS */
					
					String ageString;
					
					ageString = submissionFields[1].trim();
					obitErrors.setErrorField(ErrorField.AGE);
					
					/* If the ageString is non-empty then perform some checks */
					if ( ! ageString.equals("") ) {
						
						/* Check for commas */
						punctChk = new PunctuationChecks();
						displayCheck = punctChk.commaCheck(ageString, ErrorField.AGE, obitErrors);
						
						/* If we have not already decided to display the line, then
						 * set it to whatever the comma check decided.
						 */
						if ( ! displayLine )
						{
							displayLine = displayCheck;
						}
						
						/* Get the resulting age string */
						ageString = punctChk.getCheckField();
						
						/* there shouldn't be parentheses or brackets in this field */
						if ( ValidateFields.checkParentheses(ageString) ) {
							obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
							obitErrors = obitErrors.logError("Invalid parentheses", obitErrors);
							ageString = ageString.replaceAll("\\)", "");
							ageString = ageString.replaceAll("\\(", "");
							displayLine = true;
						}
					
						if ( ValidateFields.checkBrackets(ageString) ) {
							obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
							obitErrors = obitErrors.logError("Invalid '[' or ']'", obitErrors);
							ageString = ageString.replaceAll("\\]", "");
							ageString = ageString.replaceAll("\\[", "");
							displayLine = true;
						}

						/* If it is numeric, then check whether it's out of range */
						if ( ageString.matches("[0-9]+") ) 
						{
							/* now we need it as a number to check on its validity */
							int age = Integer.parseInt(ageString);
							
							/* If the age is greater than 120, we are suspicious. */
							if ( age > 120 ) {
								obitErrors.setErrCategory(ErrorCategory.WARNING);
								obitErrors = obitErrors.logError("Age > 120?", obitErrors);
							}		
							
							/* If there were stray characters in other fields that
							 * match the age in the age string, they have been removed
							 * from that other string and that's a good correction.
							 */
							if ( ! maybeAge.equals("") && maybeAge.equals(ageString) ) {
								displayLine = true;
							}
							
						} /* end of if it's a string of numeric characters */
						else 
						{
							/* it's not numeric characters */
							obitErrors.setErrCategory(ErrorCategory.ERROR);
							obitErrors = obitErrors.logError("Non-numeric", obitErrors);						

							/* Sometimes people put some sort of information in the field
							 * to indicate that a baby died.  They're supposed to just put
							 * zero.  Let's check for that and fix it if we find it.
							 */
							if ( ageString.contains("infant") || ageString.contains("baby") || ageString.contains("month") || ageString.contains("child") ) 
							{
								ageString = "0";
							}
						
							/* If the string now equals zero, we fixed it, so report this error */
							if ( ageString.equals("0") ) 
							{
								obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
								obitErrors = obitErrors.logError("'infant' or the like found", obitErrors);
								displayLine = true;
							}
							
						} /* end of else the age string is not a number */

					} /* end of if the field is not null */
					
					else if ( ! maybeAge.equals("") ) 
					{
						/* maybe they put the age in the wrong field */
						obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
						obitErrors = obitErrors.logError("Possible age found in other field", obitErrors);

						ageString = maybeAge;
						displayLine = true;
					}
					
					/* save the string in case we fixed anything */
					submissionFields[1] = ageString;
					
					/* END OF AGE FIELD CHECKS */
					
					/* BEGIN LOCATION FIELD CHECKS */
					
					/* get the location from the line and assume the location will be valid */
					String locationString = submissionFields[2].trim();
					deathPlaceCode = "";
					obitErrors.setErrorField(ErrorField.LOCATION);
					
					/* if the field is not empty, then we can do some checks */
					if ( ! locationString.equals("") ) 
					{						
						/* there shouldn't be parentheses or brackets in this field */
						if ( ValidateFields.checkParentheses(locationString) ) 
						{
							obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
							obitErrors = obitErrors.logError("Invalid parentheses", obitErrors);
							locationString = locationString.replaceAll("\\)", "");
							locationString = locationString.replaceAll("\\(", "");
							displayLine = true;
						}

						if ( ValidateFields.checkBrackets(locationString) ) 
						{
							obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
							obitErrors = obitErrors.logError("Invalid '[' or ']'", obitErrors);
							locationString = locationString.replaceAll("\\]", "");
							locationString = locationString.replaceAll("\\[", "");
							displayLine = true;
						}
						
						/* If the word "County" is in the place, it's not supposed to be
						 * there.  It should be "Co" so change it if we find it.
						 */
						if ( locationString.contains("County") ) 
						{
							obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
							obitErrors = obitErrors.logError("'County' found; should be 'Co'", obitErrors);

							locationString = locationString.replaceAll("County", "Co");
							displayLine = true;
						}

						/* If the word "Township" is in the place, it should be "Twp" 
						 * so change it if we find it.
						 */
						if ( locationString.contains("Township") ) {
							obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
							obitErrors = obitErrors.logError("'Township' found; should be 'Twp'", obitErrors);

							locationString = locationString.replace("Township", "Twp");
							displayLine = true;
						}

						/* Check for commas */
						punctChk = new PunctuationChecks();
						displayCheck = punctChk.commaCheck(locationString, ErrorField.LOCATION, obitErrors);
						
						/* If we have not already decided to display the line, then
						 * set it to whatever the comma check decided.
						 */
						if ( ! displayLine )
						{
							displayLine = displayCheck;
						}
						
						/* Get the resulting location string */
						locationString = punctChk.getCheckField();
						
						/* Count the greater than signs, there should be just one */
						countChars = locationString.length() - locationString.replaceAll(">","").length();
						if (countChars > 1) 
						{							
							/* attempt to fix multiple '>' with just one */
							locationString = locationString.replaceAll("[>]+",">");				
							countChars = locationString.length() - locationString.replaceAll(">","").length();
							
							/* did we fix it? */
							if ( countChars == 1 ) 
							{
								obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
								displayLine = true;
							}
							else 
							{
								obitErrors.setErrCategory(ErrorCategory.ERROR);
							}
							
							obitErrors = obitErrors.logError("Too many '>'", obitErrors);
							
						} /* end of if too many '>' */
						
						if ( countChars == 1 ) 
						{							
							/* we need to parse the line at the '>' and look at the parts */
							String[] locParts = locationString.split(">");
							
							/* if there is something after the ">", see if it's a valid location */
							if ( ! locationString.endsWith(">") ) 
							{
								deathPlaceCode = LocationArray.verifyLocation(locParts[1],locationsArray);
								
								/* If the death place code came up invalid, report it,  then check for lower
								 * case letters in code.
								 */
								if ( deathPlaceCode.equals("Invalid") ) 
								{
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									obitErrors = obitErrors.logError("Invalid death place code", obitErrors);
									displayLine = true;
									
									/* let's try making the string uppercase and check again */
									deathPlaceCode = LocationArray.verifyLocation(locParts[1].toUpperCase(),locationsArray);
									
									/* if it's valid now, then there were lowercase letters in there */
									if ( deathPlaceCode != "Invalid" ) {
										obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
										obitErrors = obitErrors.logError("Lowercase in death place code", obitErrors);
										
										/* Fix the location code in the string */
										locParts[1] = LocationArray.fixLowerCaseLocation(locParts[1], deathPlaceCode);
									}
									
									else 
									{
										/* it is invalid, but we already reported it, so just set it to null */
										deathPlaceCode = "";											
									}
									
								} /* if death place code is invalid */
								
								else if ( locationsArray.removedCountry ) 
								{
									/* If the location was changed by the verify method,
									 * then something was wrong, so save it.
									 */
									obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
									obitErrors = obitErrors.logError("Invalid country with death place code", obitErrors);
									locParts[1] = locationsArray.repairedLocation;
									displayLine = true;
								}
								
							} /* end of if the death place is not blank */
							
							else 
							{								
								/* There shouldn't be a birth place without a death place */
								obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
								obitErrors = obitErrors.logError("Birth place without death place", obitErrors);
								
								/* remove the birth place */
								locParts[0] = "";
								locationString = "";
								displayLine = true;
								
							} /* end of if the death place blank */
							
							String birthPlaceCode = "";
							
							/* if the birth place is not null, then check if it's valid */
							if ( ! locParts[0].equals("") ) 
							{
								birthPlaceCode = LocationArray.verifyLocation(locParts[0],locationsArray);
								
								if ( birthPlaceCode.equals("Invalid") ) 
								{
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									obitErrors = obitErrors.logError("Invalid birth place code", obitErrors);
									
									/* let's try making the string uppercase and check again */
									birthPlaceCode = LocationArray.verifyLocation(locParts[0].toUpperCase(),locationsArray);
									
									/* if it's valid now, then there were lowercase letters in there */
									if ( birthPlaceCode != "Invalid" ) 
									{
										obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
										obitErrors = obitErrors.logError("Lowercase in birth place code", obitErrors);
										
										/* Fix the location code in the string */
										locParts[0] = LocationArray.fixLowerCaseLocation(locParts[0], birthPlaceCode);
										displayLine = true;
									}
									else 
									{
										/* it is invalid, but we already reported it, so just set it to null */
										birthPlaceCode = "";											
									}
										
								} /* end of if birth place code is invalid */		
								
								else if ( locationsArray.removedCountry ) 
								{
									/* If the birthPlaceCode was changed by the verify
									 * method, then report it, and say it was corrected.
									 */
									obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
									obitErrors = obitErrors.logError("Invalid country with birth place code", obitErrors);
									locParts[0] = locationsArray.repairedLocation;
									displayLine = true;
								}
							
							} /* end of if the birth place is not null */
							
							/* If after all of that, we have a non-null death and birth place codes,
							 * we need to find out if the place codes are equal.  If they are, get rid
							 * of the birth place.
							 */
							if ( ! birthPlaceCode.equals("") && ! deathPlaceCode.equals("") ) 
							{								
								/* If they are equal, report it and put the death place in the
								 * location string.
								 */
								if ( birthPlaceCode.equals(deathPlaceCode) ) 
								{
									obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
									obitErrors = obitErrors.logError("Birth place code = death place code", obitErrors);
									displayLine = true;

									locationString = locParts[1].trim();
								}
								else 
								{
									/* put the string back together in case there were corrections */
									locationString = locParts[0].trim() + ">" + locParts[1].trim(); 
								}
								
							} /* end of if the birth and death codes are not both null */							
							
						} /* end of else there is one '>' in the location field */
						
						else if ( countChars == 0 ) 
						{							
							/* there are no > symbols, so there is just a death place */
							deathPlaceCode = LocationArray.verifyLocation(locationString,locationsArray);
							
							/* If the death place code came up invalid, report it, but then
							 * set the place code to null;
							 */
							if ( deathPlaceCode.equals("Invalid") ) 
							{
								obitErrors.setErrCategory(ErrorCategory.ERROR);
								obitErrors = obitErrors.logError("Invalid death place code", obitErrors);
								displayLine = true;
								
								/* let's try making the string uppercase and check again */
								deathPlaceCode = LocationArray.verifyLocation(locationString.toUpperCase(),locationsArray);
								
								/* if it's valid now, then there were lowercase letters in there */
								if ( deathPlaceCode != "Invalid" ) 
								{
									obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
									obitErrors = obitErrors.logError("Lowercase in death place code", obitErrors);
									
									/* Fix the location code in the string */
									locationString = LocationArray.fixLowerCaseLocation(locationString, deathPlaceCode);
								}
								else 
								{
									/* it is invalid, but we already reported it, so just set it to null */
									deathPlaceCode = "";											
								}

							} /* end of if the death place is invalid */
							
						} /* end of else there are no > symbols */								

						/* put the location string back, in case it has any fixes in it */
						submissionFields[2] = locationString.trim();
						
					} /* end of if location field is not empty */
					
					/* END LOCATION FIELD CHECKS */

					/* BEGIN PUBLICATION FIELD CHECKS */
					
					String publicationString;
					String publication, subPubLocation;
					String[] pubParts;
					
					obitErrors.setErrorField(ErrorField.PUBLICATION);					
					publicationString = submissionFields[3].trim();

					/* Check for commas */
					punctChk = new PunctuationChecks();
					displayCheck = punctChk.commaCheck(publicationString, ErrorField.PUBLICATION, obitErrors);
					
					/* If we have not already decided to display the line, then
					 * set it to whatever the comma check decided.
					 */
					if ( ! displayLine )
					{
						displayLine = displayCheck;
					}
					
					/* Get the resulting publication string */
					publicationString = punctChk.getCheckField();
					
					/* there shouldn't be any brackets in this field */
					if ( ValidateFields.checkBrackets(publicationString) ) {
						obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
						obitErrors = obitErrors.logError("Invalid '[' or ']'", obitErrors);
						publicationString = publicationString.replaceAll("\\]", "");
						publicationString = publicationString.replaceAll("\\[", "");
						displayLine = true;
					}

					/* Start by assuming that there's just a publication, nothing else.
					 * This field is not supposed to be null.
					 */
					publication = publicationString;
					subPubLocation = "";					
					
					/* Let's count how many parentheses there are, if any. */
					countLeftParens = publicationString.length() - publicationString.replaceAll("\\(","").length();
					countRightParens = publicationString.length() - publicationString.replaceAll("\\)","").length();
					
					/* if there are more than one of either kind, that's too many */
					if ( countLeftParens > 1 || countRightParens > 1 ) 
					{
						obitErrors.setErrCategory(ErrorCategory.ERROR);
						obitErrors = obitErrors.logError("Too many parentheses", obitErrors);
					}
					
					/* if they are unbalanced, that's just wrong */
					else if ( countLeftParens != countRightParens ) 
					{
						obitErrors.setErrCategory(ErrorCategory.ERROR);
						obitErrors = obitErrors.logError("Unbalanced parentheses", obitErrors);
					}
					
					/* if there is just one left, they must match so let's do some checks for spaces */
					else if ( countLeftParens == 1 && countRightParens == 1 ) 
					{						
						/* If there is a space before a ')' then report it and delete the space */
						matchPattern = Pattern.compile("[ ]\\)");
						myMatcher = matchPattern.matcher(publicationString);						

						if ( myMatcher.find() ) 
						{				
							obitErrors.setErrCategory(ErrorCategory.SPACING);
							obitErrors = obitErrors.logError("Space before ')'", obitErrors);
							
							charIndex = myMatcher.start();
							builderLine = new StringBuilder();
							builderLine.append(publicationString);
								
							/* Delete the space in front of the parenthesis. */
							builderLine.deleteCharAt(charIndex);
								
							publicationString = builderLine.toString();
							displayLine = true;
							
						} /* end of if there are is a space before closing parenthesis */		

						/* If there is a space after a '(' then report it and delete the space */
						matchPattern = Pattern.compile("\\([ ]");
						myMatcher = matchPattern.matcher(publicationString);						

						if ( myMatcher.find() ) 
						{				
							obitErrors.setErrCategory(ErrorCategory.SPACING);
							obitErrors = obitErrors.logError("Space after '('", obitErrors);
							
							charIndex = myMatcher.start();
							builderLine = new StringBuilder();
							builderLine.append(publicationString);
								
							/* Delete the space after the parenthesis. */
							builderLine.deleteCharAt(charIndex+1);
								
							publicationString = builderLine.toString();
							displayLine = true;
							
						} /* end of if there is a space after opening parenthesis */	
						
						/* So we must have a place code with the publication, so
						 * let's get the place code and publication separated.
						 */
						pubParts = publicationString.split("\\(");
						publication = pubParts[0].trim();
						subPubLocation = pubParts[1].trim();
						pubParts = subPubLocation.split("\\)");
						
						if ( pubParts.length >= 1 ) 
						{
							subPubLocation = pubParts[0].trim();
						}
						else 
						{
							/* must have been nothing between those parentheses */
							obitErrors.setErrCategory(ErrorCategory.ERROR);
							obitErrors = obitErrors.logError("Empty parentheses", obitErrors);
							displayLine = true;
						}
						
					} /* end of else there is one set of parentheses */

					/* is this a valid publication? */
					boolean validPub = PublicationMap.checkPublication(pubsMap, publication);
					if ( ! validPub ) 
					{						
						/* They might have put a location with the publication
						 * abbreviation and not put parentheses around it.
						 * We'll try the verify location method and see if
						 * there's a location in there.
						 */
						String testForLoc = LocationArray.verifyLocation(publication,locationsArray);
						
						/* If we found a location code in there, let's put it in parentheses. */
						if ( testForLoc != "Invalid" ) 
						{							
							publication = PublicationMap.fixPubLocation(publication, testForLoc);
							
							/* If the publication is now found, we found and fixed the problem */
							if ( PublicationMap.checkPublication(pubsMap, publication) ) {
								
								/* report it */
								obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
								obitErrors = obitErrors.logError("Publication location missing parentheses", obitErrors);
								
								/* Put the location in the right strings. */
								subPubLocation = testForLoc;

								/* Now it's a valid publication and we corrected it. */
								validPub = true;
								displayLine = true;	
								
							} /* if we found a valid publication */		
							
						} /* if we found a location in the publication name */
						
					} /* end of if not a valid publication */
					
					/* Now we can report this if we weren't able to fix it above. */
					if ( ! validPub ) 
					{
						/* this is not a valid publication */
						obitErrors.setErrCategory(ErrorCategory.ERROR);
						obitErrors = obitErrors.logError("Invalid or unknown publication", obitErrors);
						displayLine = true;
					}
					
					if ( validPub ) 
					{
						/* It's a valid publication, so let's check some other things */
						
						/* get the location of this publication */
						String pubPlaceCode = PublicationMap.getPubLocation(pubsMap, publication);
						
						/* If the submitted pub location code is non-empty but is not
						 * the same as the publication's actual place, then the 
						 * submitter used the wrong code.
						 */
						if ( ! subPubLocation.equals("")  && ! subPubLocation.equals(pubPlaceCode) ) 
						{
							obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
							obitErrors = obitErrors.logError("Incorrect place code", obitErrors);
							
							subPubLocation = pubPlaceCode;							
							displayLine = true;
						}

						/* If the death place code is different than the
						 * publication's place code, then the publication field
						 * should include the place code.
						 * So if the submitted publication field has a blank
						 * location code, report and fix it.
						 */
						if ( ! deathPlaceCode.equals(pubPlaceCode) && subPubLocation.equals("") )
						{
							obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
							obitErrors = obitErrors.logError("Missing place code", obitErrors);
							
							subPubLocation = pubPlaceCode;
							displayLine = true;
						}
						
						/* If the death place code is the same as the publication
						 * location code, then the publication field should not
						 * include a location.
						 * So if it does, then make it blank and report the error.
						 */
						if ( deathPlaceCode.equals(pubPlaceCode) && ! subPubLocation.equals("") )
						{
							obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
							obitErrors = obitErrors.logError("Unnecessary place code", obitErrors);
							
							subPubLocation = "";
							displayLine = true;							
						}
					
						/* Put the string together, depending on whether there's
						 * a publication place code.
						 */
						if ( subPubLocation.equals("") ) 
						{
							publicationString = publication;
						}
						else {
							publicationString = publication + " (" + subPubLocation + ")";
						}						
						
					} /* end of it is a valid publication */					
					
					/* put the publication string back in case there were corrections */
					submissionFields[3] = publicationString;
					
					/* END PUBLICATION FIELD CHECKS */
					
					/* BEGIN DATE FIELD CHECKS */
				
					String dateString;
					
					dateString = submissionFields[4].trim();
					obitErrors.setErrorField(ErrorField.DATE);
									
					/* Check for things that we can correct before we verify whether it's
					 * a good date or not.
					 */
					
					/* Check for commas */
					punctChk = new PunctuationChecks();
					displayCheck = punctChk.commaCheck(dateString, ErrorField.DATE, obitErrors);
					
					/* If we have not already decided to display the line, then
					 * set it to whatever the comma check decided.
					 */
					if ( ! displayLine )
					{
						displayLine = displayCheck;
					}
					
					/* Get the resulting publication string */
					dateString = punctChk.getCheckField();
					
					/* We should only be using simple ASCII codes, but some people are using
					 * UNICODE dashes.
					 */
					if ( dateString.indexOf('\u2013') >= 0 || dateString.indexOf('\u2014') >= 0 ) 
					{						
						/* Report we found at least one disallowed quote */
						obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
						obitErrors = obitErrors.logError("Found disallowed dash(es)", obitErrors);
						
						/* We replace any disallowed quotes with regular ones. */
						dateString = dateString.replace("\u2013","-");
						dateString = dateString.replace("\u2014","-");
						displayLine = true;					

					} /* end of if there are non-ASCII dashes in the line */
							
					/* there shouldn't be parentheses or brackets in this field */
					if ( ValidateFields.checkParentheses(dateString) ) 
					{
						obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
						obitErrors = obitErrors.logError("Invalid parentheses", obitErrors);
						dateString = dateString.replaceAll("\\)", "");
						dateString = dateString.replaceAll("\\(", "");
						displayLine = true;
					}

					if ( ValidateFields.checkBrackets(dateString) ) 
					{
						obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
						obitErrors = obitErrors.logError("Invalid '[' or ']'", obitErrors);
						dateString = dateString.replaceAll("\\]", "");
						dateString = dateString.replaceAll("\\[", "");
						displayLine = true;						
					}
					
					/* We can check for some other common problems and fix them
					 * before trying to validate the date.
					 * Check for any spaces first.
					 */
					if ( dateString.contains(" ") ) 
					{
						/* there should be no spaces at all */						
						obitErrors.setErrCategory(ErrorCategory.SPACING);
						obitErrors = obitErrors.logError("Space(s) found", obitErrors);
						
						/* get rid of any spaces */
						dateString = dateString.replaceAll("[ ]+", "");
						displayLine = true;
					}
					
					/* If there is a dash followed by a zero and a number, then fix it. 
					 * There are no leading zeros allowed.
					 */
					matchPattern = Pattern.compile("-0\\d");
					myMatcher = matchPattern.matcher(dateString);

					if ( myMatcher.find() ) 
					{
						/* there should be no zeros after a dash */						
						obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);
						obitErrors = obitErrors.logError("Leading zero found", obitErrors);
						
						/* get rid of any zeros */
						dateString = dateString.replaceAll("-0", "-");
						displayLine = true;
					}
										
					/* Now check if what we have left is a valid date */
					boolean dateCorrected;
					DateValidation dateValidateClass = new DateValidation();
					dateValidateClass.submittedDate = dateString;

					dateValidateClass = DateValidation.checkDateField(obitErrors, dateValidateClass);
					
					/* Do basic format checks of date field */
					dateCorrected = dateValidateClass.corrected;
					
					/* If the date format was corrected, remember that */
					if (dateCorrected) 
					{
						displayLine = true;
						
						/* get the corrected date string */
						dateString = dateValidateClass.submittedDate;
					}
					
					/* If it was a non-null date but invalid, let's see if we 
					 * can figure out how it's invalid.
					 */
					if ( dateValidateClass.badDate ) {						
								
						/* Let's check how many dashes there are.  There should be two. */
						countChars = dateString.length() - dateString.replace("-","").length();
						if ( countChars != 2 ) {
								
							obitErrors.setErrCategory(ErrorCategory.ERROR);
							if ( countChars < 2 ) 
							{
								obitErrors = obitErrors.logError("Too few dashes", obitErrors);
							}
							else {
								obitErrors = obitErrors.logError("Too many dashes", obitErrors);
							}
						}
						else 
						{
							/* There are the right number of dashes, so we can parse the date
							 * and check the validity of its parts.
							 */
							String[] dateParts = dateString.split("-");
							boolean checkDay = false;
							boolean checkMonth = false;								
								
							/* Check for leading zeroes in the month or day -- not supposed to be there */
							if ( dateParts.length > 1 ) 
							{
								if ( dateParts[1].length() > 0 ) 
								{
									if ( dateParts[1].length() > 1 && dateParts[1].charAt(0) == '0' ) 
									{
										obitErrors.setErrCategory(ErrorCategory.ERROR);
										obitErrors = obitErrors.logError("Leading zero in day", obitErrors);
									}
									checkDay = true;
								}
							}
							if ( dateParts.length > 2 ) 
							{
								if ( dateParts[2].length() > 0 ) 
								{
									if ( dateParts[2].length() > 1 && dateParts[2].charAt(0) == '0' ) 
									{
										obitErrors.setErrCategory(ErrorCategory.ERROR);
										obitErrors = obitErrors.logError("Leading zero in month", obitErrors);
									}
									checkMonth = true;
								}
							}
								
							/* if the month field is okay to check */
							if ( checkMonth == true ) 
							{								
								/* If the month is a number */
								if ( dateParts[1].matches("[0-9]+") ) 
								{									
									/* Check for valid month */
									int month = Integer.parseInt(dateParts[1]);
									if ( month < 1 || month > 12 ) 
									{
										obitErrors.setErrCategory(ErrorCategory.ERROR);
										obitErrors = obitErrors.logError("Invalid month", obitErrors);
									}
									
									if ( checkDay == true ) 
									{
										/* If the day is a number */
										if ( dateParts[2].matches("[0-9]+") ) 
										{
											/* Check for valid day */
											int day = Integer.parseInt(dateParts[2]);
											if ( day < 1 ) 
											{
												obitErrors.setErrCategory(ErrorCategory.ERROR);
												obitErrors = obitErrors.logError("Invalid day", obitErrors);
											}
											else 
											{
												switch (month) 
												{
												case 1:
												case 3:
												case 5:
												case 7:
												case 8:
												case 10:
												case 12:
													if ( day > 31 ) 
													{
														obitErrors.setErrCategory(ErrorCategory.ERROR);
														obitErrors = obitErrors.logError("Invalid day", obitErrors);
													}
													break;
												case 4:
												case 6:
												case 9:
												case 11:
													if ( day > 30 ) 
													{
														obitErrors.setErrCategory(ErrorCategory.ERROR);
														obitErrors = obitErrors.logError("Invalid day", obitErrors);
													}
													break;
												case 2:
													if ( day > 29 ) 
													{
														obitErrors.setErrCategory(ErrorCategory.ERROR);
														obitErrors = obitErrors.logError("Invalid day", obitErrors);
													}
													break;
														
												} /* end of switch on month */
													
											} /* end of else day is greater than one */
												
										} /* end of if day is a number */
										
									} /* end of if we can check the day */	
									
								} /* end of if month is a number */
								
							} /* end of if we can check the month field */
									
							/* If the year is a number */
							if ( dateParts[0].matches("[0-9]+") ) 
							{										
								/* it should be four digits */
								if ( dateParts[0].length() != 4 ) 
								{
									obitErrors.setErrCategory(ErrorCategory.ERROR);
									obitErrors = obitErrors.logError("Incorrect number of digits in year", obitErrors);
								}
								else 
								{
									int year = Integer.parseInt(dateParts[0]);
										
									/* arbitrarily say that if the year is less than 1700, it's too old */
									if ( year < 1700 ) 
									{
										obitErrors.setErrCategory(ErrorCategory.ERROR);
										obitErrors = obitErrors.logError("Year too far in the past", obitErrors);
									}
											
								} /* end of else it's a four-digit number year */
									
							} /* end of if there are only numbers in the year part */
							else 
							{
								/* it can't be valid if it's not four digits */
								obitErrors.setErrCategory(ErrorCategory.ERROR);
								obitErrors = obitErrors.logError("Incorrect number of digits in year", obitErrors);
							}
						} /* end of else there are two dashes */								
					
					} /* end of if it's bad date field */
					
					/* put the date string back in the line in case something has been corrected */
					submissionFields[4] = dateString;
					
					/* END OF DATE FIELD CHECKS */
					
					/* Put the string back together from its parts */
					reassembledLine = submissionFields[0];
					for ( i=1; i<submissionFields.length; i++ ) 
					{
						reassembledLine = reassembledLine + "; " + submissionFields[i];
					}
					submissionLine = reassembledLine;

				} /* end of if line can be parsed */
				
				/* If we need to report the original lines, then store them. */
				if ( obitErrors.rptOriginal == true ) 
				{					
					/* Store the errors reported in the appropriate place */
					obitErrors = ErrorLog.storeErrRptLines(obitErrors, originalLine);
				}

				/* If the line was corrected, then store it too */
				if ( displayLine == true )
				{					
					/* Store the corrected line for later retrieval */
					obitErrors = ErrorLog.storeCorrectedLine(obitErrors, submissionLine);
				}

				/* Don't write the output if the line is empty or is a duplicate */
				if ( ! submissionLine.trim().isEmpty() && ! isDuplicate ) 
				{					
					/* write the line (perhaps fixed) to the output file */
					fileWriter.println(submissionLine);
				}
				
			} /* end of while there is a next line */
			
			reportWriter.println();
			reportWriter.printf("%-16s: %8d%n", "Records", obitErrors.recordNum);
			reportWriter.printf("%-16s: %8d%n", "Duplicates", obitErrors.getDupCount() );
			reportWriter.printf("%-16s: %8d%n", "Errors", obitErrors.errorTotal);
		
			reportWriter.printf("%nCATEGORY TOTALS%n");
			Set<Entry<ErrorCategory, Integer>> enumSet= obitErrors.errCategoryCounter.entrySet();
			for (Entry<ErrorCategory, Integer> entry:enumSet) 
			{
				reportWriter.printf("%-16s: %8d%n",entry.getKey(),entry.getValue() );
			}			
			
			reportWriter.println(newLine + "FIELD TOTALS");
			Set<Entry<ErrorField, Integer>> enumSet2= obitErrors.errFieldCount.entrySet();
			for (Entry<ErrorField, Integer> entry:enumSet2) 
			{
				reportWriter.printf("%-16s: %8d%n",entry.getKey(),entry.getValue() );
			}			
			
			reportWriter.println(newLine + "TAGNAME TOTALS");
			Set<Entry<String, Integer>> hashSet= obitErrors.tagErrsMap.entrySet();
			for (Entry<String, Integer> entry:hashSet) 
			{
				reportWriter.printf("%-16s: %8d%n",entry.getKey(),entry.getValue() );
			}			
			
			/* Report all of the error lines, in order of highest-ranking
			 * error category.
			 */
			ErrorLog.dumpErrRptArrays(obitErrors);
			
			/* Report deleted duplicate lines, if any */
			if ( obitErrors.getDupCount() > 0 ) 
			{
				reportWriter.println();
				reportWriter.println("** Duplicate line(s) removed: **");
				for ( String line : dupLines ) 
				{
					reportWriter.println(line);
				}
			}
			
			/* System error notice - get the current time */
			date = Calendar.getInstance().getTime();
			dateFormat = new SimpleDateFormat("k:mm:ss");
			reportTime = dateFormat.format(date);

			System.err.println("Checking complete " + reportTime);
			
			/* Close any open resources */
		
			if (fileScanner!=null) {
				fileScanner.close();
			}
			
			if (fileWriter!=null) {
				fileWriter.close();
			}
			
			if (reportWriter!=null) {
				reportWriter.close();
			}

		} /* end of try file from user */
		
		catch (FileNotFoundException fnf) 
		{
			System.err.println("ObitEdit: File not found" + fnf.getMessage() );
		}
		
		return( obitErrors );
		
/*		System.exit(0); /* This was to prevent a weird eclipse error */

	} /* end of main */

} /* end of class ObitEdit */
