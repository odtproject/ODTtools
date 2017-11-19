package odt.tool;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

import odt.gui.OperatingFlags;

/* This class will contain everything for recording errors. */
public class ErrorLog 
{	
	
	int errorTotal;		/* total number of errors for this run of the tool */
	int recordNum;	/* current record number (for logError call) */
	int dupCount;  /* count of duplicate lines */
	String tagname;	/* current tagname (for logError call) */
	private ErrorField errorField; /* field in which the error was found */
	boolean rptOriginal; /* report the original input line */
	PrintStream rptStream; /* the stream to write the report to */
	public String rptFileName; /* name and path of the output file */
	public String correctedFileName; /* name and path of the corrected file */

	HashMap<Integer, String> errorMsg;	/* error message for each type of error */
	HashMap<Integer, Integer> errorCount;	/* error count for each type of error */
	
	/* a collection of the error counts for each tagname */
	public HashMap<String, Integer> tagErrsMap = new HashMap<>();	
	
	/* count of errors for each type of field */
	public EnumMap<ErrorField, Integer> errFieldCount; 
	
	/* a report string for the field where the error occurred */
	EnumMap<ErrorField, String> errFieldString;	
	
	/* The category of the error, correctable or warning */
	private ErrorCategory errCategory;
	
	/* counters and report strings for each category of error */
	public EnumMap<ErrorCategory, Integer> errCategoryCounter; 
	EnumMap<ErrorCategory, String> errCategoryString;
	
	/* for separating the error reports for later */
	ArrayList<String> errorRptLines;
	ArrayList<String> correctedRptLines;
	ArrayList<String> warningRptLines;
	ArrayList<String> spacingRptLines;
	
	/* The errors reported for the line being processed */
	ArrayList<String> rptLineErrorLines;
	
	/* error category for the line being processed */	
	ErrorCategory rptLineCategory; 	
	
	/* Flags for what should be reported */
	public OperatingFlags suppressFlags;
	
	/* constructor */
	public ErrorLog() {
		errorTotal = 0;
		recordNum = 0;
		dupCount = 0;  
		tagname = null;
		errorField = ErrorField.RECORD;
		errorMsg = new HashMap<>();
		errorCount = new HashMap<>();
		errCategoryCounter = new EnumMap<ErrorCategory, Integer>(ErrorCategory.class);
		errCategoryString = new EnumMap<ErrorCategory, String>(ErrorCategory.class);
		errFieldCount = new EnumMap<ErrorField, Integer>(ErrorField.class);
		errFieldString = new EnumMap<ErrorField, String>(ErrorField.class);
		errorRptLines = new ArrayList<>();
		correctedRptLines = new ArrayList<>();
		warningRptLines = new ArrayList<>();
		spacingRptLines = new ArrayList<>();
		
		/* Initialize the strings that will print what field the error
		 * was found in.
		 */
		errFieldString.put(ErrorField.RECORD, "Record: ");
		errFieldString.put(ErrorField.NAME, "Name field: ");
		errFieldString.put(ErrorField.SURNAME, "Surname: ");
		errFieldString.put(ErrorField.FIRSTNAME, "First Name: ");
		errFieldString.put(ErrorField.MAIDENNAME, "Maiden Name: ");
		errFieldString.put(ErrorField.NICKNAME, "Nickname: ");
		errFieldString.put(ErrorField.OTHERNAME, "Other Name: ");
		errFieldString.put(ErrorField.AGE, "Age field: ");
		errFieldString.put(ErrorField.LOCATION, "Location field: ");
		errFieldString.put(ErrorField.PUBLICATION, "Publication field: ");
		errFieldString.put(ErrorField.DATE, "Date field: ");
		errFieldString.put(ErrorField.TAGNAME, "Tagname field: ");
		
		/* Initialize the strings that will print what the category of 
		 * error was.
		 */
		errCategoryString.put(ErrorCategory.ERROR, "Error in ");
		errCategoryString.put(ErrorCategory.AUTOCORRECT, "Corrected Error in ");
		errCategoryString.put(ErrorCategory.WARNING, "Warning for ");
		errCategoryString.put(ErrorCategory.SPACING, "Spacing error corrected in ");
		
		/* Initialize report flags */
		suppressFlags = new OperatingFlags();
				
	} /* end of ErrorLog constructor  */

	/* this method reports and records a specified error in the errorLog */
	public ErrorLog logError (String errorMessage, ErrorLog errorLog) {
	
		int oldCounter;
		ErrorCategory category;
		ErrorField field;
		String message;
		boolean skipMessage = false;
		
		/* If this is a spacing error and we are supposed to suppress reporting
		 * those errors, then set skipMessage flag.
		 */
		if ( errorLog.errCategory.equals(ErrorCategory.SPACING) && ( errorLog.suppressFlags.isSuppressSpacingCorr()) ) {
			
			skipMessage = true;
		}

		/* If this is a warnings and we are supposed to suppress them,
		 * then set skipMessage flag.
		 */
		if ( errorLog.errCategory.equals(ErrorCategory.WARNING) && ( errorLog.suppressFlags.isSuppressWarnings()) ) {
			
			skipMessage = true;
		}

		/* If we are not skipping this message, then put it together and store it. */
		if ( !skipMessage ) {
			
			/* put together the error message */	
			message = "** " + errorLog.errCategoryString.get(errorLog.errCategory);
			message = message + errorLog.errFieldString.get(errorLog.errorField);
			message = message + errorMessage + " **";
			
			/* Store the message for later retrieval */
			errorLog.rptLineErrorLines.add(message);
		}
		
		/* Set the error category for this line to the highest between the existing
		 * category for the line and the category for the error being logged.
		 */
		if ( (errorLog.errCategory.equals(ErrorCategory.ERROR)) || (errorLog.rptLineCategory.equals(ErrorCategory.ERROR)) ) {
			errorLog.rptLineCategory = ErrorCategory.ERROR;
		}
		else if ( (errorLog.errCategory.equals(ErrorCategory.AUTOCORRECT)) || (errorLog.rptLineCategory.equals(ErrorCategory.AUTOCORRECT)) ) {
			errorLog.rptLineCategory = ErrorCategory.AUTOCORRECT;
		}
		else if ( (errorLog.errCategory.equals(ErrorCategory.SPACING)) || (errorLog.rptLineCategory.equals(ErrorCategory.SPACING)) ) {
			errorLog.rptLineCategory = ErrorCategory.SPACING;
		}
		else {
			errorLog.rptLineCategory = ErrorCategory.WARNING;
		}
		
		/* increment the error counter for this category of error */
		category = errorLog.errCategory;
		if ( errorLog.errCategoryCounter.containsKey(category) ) {
			oldCounter = errorLog.errCategoryCounter.get(category);
			errorLog.errCategoryCounter.put(category, (oldCounter+1));
		}
		else {
			errorLog.errCategoryCounter.put(category, 1);
		}		

		/* increment the error counter for this field */
		field = errorLog.errorField;
		if ( errorLog.errFieldCount.containsKey(field) ) {
			oldCounter = errorLog.errFieldCount.get(field);
			errorLog.errFieldCount.put(field, (oldCounter+1));
		}
		else {
			errorLog.errFieldCount.put(field, 1);
		}		
		
		/* Increment the error counter for this tagname, 
		 * if there is a counter for it.
		 * Otherwise, initialize a counter if there is a valid tagname.
		 */
		if ( errorLog.tagErrsMap.containsKey(errorLog.tagname) ) {
			oldCounter = errorLog.tagErrsMap.get(errorLog.tagname);
			errorLog.tagErrsMap.put(errorLog.tagname, (oldCounter+1));
		}
		else if ( errorLog.tagname != null ) {
			errorLog.tagErrsMap.put(errorLog.tagname, 1);
		}
	
		/* increment total number of errors for this run of tool */
		errorLog.errorTotal++;
		
		/* the original submitted line will need to be printed after it's processed */
		errorLog.rptOriginal = true;
		
		return(errorLog);

	} /* end of logError() */
	
	/* This method will get the number of records from the error log */
	public static int getNumRecords(ErrorLog obitErrors) {
		
		return(obitErrors.recordNum);
		
	} /* end of getNumRecords */
	
	/* This method will get the number of records from the error log */
	public static int getNumErrors(ErrorLog obitErrors) {
		
		return(obitErrors.errorTotal);
		
	} /* end of getNumRecords */
	
	/* This method will initialize the error storage for the current line
	 * being processed.
	 */
	public static ErrorLog initRptLineErrorLog ( ErrorLog errorLog ) {
		
		errorLog.rptLineCategory = ErrorCategory.WARNING;
		errorLog.rptLineErrorLines = new ArrayList<>();
		
		return( errorLog );
		
	} /* end of initRptLineErrCategory */
	
	/* This method puts all of the reported errors for the line
	 * being processed into the appropriate array, depending on the error
	 * category.
	 */
	public static ErrorLog storeErrRptLines( ErrorLog errorLog, String origLine ) {
		int i;
		
		/*  Retrieve line separator dependent on OS. */
		String newLine = System.getProperty("line.separator");
		
		String origRptLine;
		
		origRptLine = String.format(" Record # %04d",errorLog.recordNum) + newLine;
		origRptLine = origRptLine + origLine;
		
		/* Depending on the highest error category for this line,
		 * put the reported errors into the appropriate list to be
		 * reported at the end.
		 */
		switch ( errorLog.rptLineCategory ) {
		case ERROR:
			errorLog.errorRptLines.add(newLine);
			for ( i=0; i<errorLog.rptLineErrorLines.size(); i++ ) {
				errorLog.errorRptLines.add(errorLog.rptLineErrorLines.get(i));
			}
			errorLog.errorRptLines.add(origRptLine);
			break;
			
		case AUTOCORRECT:
			errorLog.correctedRptLines.add(newLine);
			for ( i=0; i<errorLog.rptLineErrorLines.size(); i++ ) {
				errorLog.correctedRptLines.add(errorLog.rptLineErrorLines.get(i));
			}
			errorLog.correctedRptLines.add(origRptLine);
			break;
			
		case WARNING:
			errorLog.warningRptLines.add(newLine);
			for ( i=0; i<errorLog.rptLineErrorLines.size(); i++ ) {
				errorLog.warningRptLines.add(errorLog.rptLineErrorLines.get(i));
			}
			errorLog.warningRptLines.add(origRptLine);
			break;
			
		case SPACING:
			errorLog.spacingRptLines.add(newLine);
			for ( i=0; i<errorLog.rptLineErrorLines.size(); i++ ) {
				errorLog.spacingRptLines.add(errorLog.rptLineErrorLines.get(i));
			}
			errorLog.spacingRptLines.add(origRptLine);
			break;
			
		default:
			System.err.println("storErrRptLines: unrecognized category: " + errorLog.rptLineCategory.toString());
			break;
		} /* end of switch on report line category */
		
		return ( errorLog );
		
	} /* end of storeErrRptLines */
	
	/* This method puts the corrected line into the appropriate
	 * list for later reporting.
	 */
	public static ErrorLog storeCorrectedLine( ErrorLog errorLog, String correctedLine ) {
		
		String correctedRptLine;
		
		/*  Retrieve line separator dependent on OS. */
		String newLine = System.getProperty("line.separator");
		
		correctedRptLine = newLine + "-- Changed entry:" + newLine;
		correctedRptLine = correctedRptLine + correctedLine;
		
		switch ( errorLog.rptLineCategory ) {
		case ERROR:			
			errorLog.errorRptLines.add(correctedRptLine);
			break;
			
		case AUTOCORRECT:
			errorLog.correctedRptLines.add(correctedRptLine);
			break;
			
		case WARNING:
			errorLog.warningRptLines.add(correctedRptLine);
			break;
			
		case SPACING:
			errorLog.spacingRptLines.add(correctedRptLine);
			break;
			
		default:
			System.err.println("storeCorrectedLine: unrecognized category: " + errorLog.rptLineCategory.toString());
			break;
		} /* end of switch on report line category */

		return( errorLog );
		
	} /* end of storeCorrectedLine */
	
	/* This method dumps all the error reporting for the file
	 * being processed.
	 */
	public static void dumpErrRptArrays( ErrorLog errorLog ) {
		int i;
		
		for ( i=0; i<errorLog.errorRptLines.size(); i++ ) {
			errorLog.rptStream.println(errorLog.errorRptLines.get(i));
		}

		for ( i=0; i<errorLog.correctedRptLines.size(); i++ ) {
			errorLog.rptStream.println(errorLog.correctedRptLines.get(i));
		}
		
		/* If spacing errors are not suppressed, then print them */
		if ( ! errorLog.suppressFlags.isSuppressSpacingCorr() ) {
			for ( i=0; i<errorLog.spacingRptLines.size(); i++ ) {
				errorLog.rptStream.println(errorLog.spacingRptLines.get(i));
			}
		}

		/* If warnings are not suppressed, then print them */
		if ( ! errorLog.suppressFlags.isSuppressWarnings() ) {
			for ( i=0; i<errorLog.warningRptLines.size(); i++ ) {
				errorLog.rptStream.println(errorLog.warningRptLines.get(i));
			}

		}

		return;
		
	} /* end of dumpErrRptArrays */
	
	public int getDupCount() {
		return dupCount;
	}

	public void setDupCount(int dupCount) {
		this.dupCount = dupCount;
	}
	
	public void incrDupCount() {
		this.dupCount++;
	}

	public ErrorField getErrorField() {
		return errorField;
	}

	public void setErrorField(ErrorField errorField) {
		this.errorField = errorField;
	}

	public ErrorCategory getErrCategory() {
		return errCategory;
	}

	public void setErrCategory(ErrorCategory errCategory) {
		this.errCategory = errCategory;
	}

	
} /* end of ErrorLog class */
