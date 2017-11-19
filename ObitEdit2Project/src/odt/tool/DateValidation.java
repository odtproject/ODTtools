package odt.tool;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/* This class will contain stuff to validate the date field */
public class DateValidation  {
	
	String submittedDate;
	boolean badDate;
	boolean corrected;
	
	public static DateValidation checkDateField (ErrorLog obitErrors, DateValidation dateToValidate) {

		SimpleDateFormat dateFormat;
		
		Date todayDate = Calendar.getInstance().getTime();
		dateToValidate.badDate = false;
		dateToValidate.corrected = false;
	
		/* If the date field is empty, that's not valid */
		if ( dateToValidate.submittedDate.equals("") ) 
		{
			obitErrors.setErrCategory(ErrorCategory.ERROR);						
			obitErrors = obitErrors.logError("Empty field", obitErrors);
		}
		else 
		{
			/* Check for the proper date format */
			Date testDate = null;
		
			/* try parsing the string using the expected format */
			try {
				dateFormat = new SimpleDateFormat("yyyy-M-d");
				dateFormat.setLenient(false);
				testDate = dateFormat.parse(dateToValidate.submittedDate);
			
				/* If the parsed date does not match the date entered, it's not valid */
				if ( !dateFormat.format(testDate).equals(dateToValidate.submittedDate) ) 
				{				
					obitErrors.setErrCategory(ErrorCategory.ERROR);						
					obitErrors = obitErrors.logError("Invalid format", obitErrors);
					dateToValidate.badDate = true;
				}
				
				/* if the date is later than today, that's suspicious */
				if ( testDate.compareTo(todayDate) > 0 ) 
				{
					obitErrors.setErrCategory(ErrorCategory.ERROR);						
					obitErrors = obitErrors.logError("Date in the future", obitErrors);
				}

			} /* end of try to parse the date string with expected format */
		
			catch (ParseException e1) 
			{				
				/* the date field couldn't be parsed */
				obitErrors.setErrCategory(ErrorCategory.ERROR);						
				obitErrors = obitErrors.logError("Invalid format", obitErrors);
				dateToValidate.badDate = true;
				
				/* try parsing it using an older format */
				try 
				{
					dateFormat = new SimpleDateFormat("d-M-yyyy");
					dateFormat.setLenient(false);
					testDate = dateFormat.parse(dateToValidate.submittedDate);
					
					/* If we're here then it was using an older format */
					obitErrors.setErrCategory(ErrorCategory.AUTOCORRECT);						
					obitErrors = obitErrors.logError("Incorrect format", obitErrors);
					
					/* We want to convert it to the right format.  However, they
					 * might have been using two-digit years, so we have to
					 * check for that first.
					 */
					Calendar cal = Calendar.getInstance();
					cal.setTime(testDate);
					int year = cal.get(Calendar.YEAR);		
					
					/* If the year is less than 100, they were using 2-digit years.
					 * Add 2000 to the year and see if that fixes it.
					 */
					if ( year < 100 ) 
					{
						cal.add(Calendar.YEAR, 2000);
						testDate = cal.getTime();
						
						/* If the resulting date turns out to be in the future, 
						 * we need to correct the date to get the right century.
						 */
						if ( testDate.compareTo(todayDate) > 0 ) 
						{
							cal.add(Calendar.YEAR, -100);
							testDate = cal.getTime();
							dateFormat = new SimpleDateFormat("d-M-yy");
							dateFormat.set2DigitYearStart(testDate);
							dateFormat.setLenient(false);
							testDate = dateFormat.parse(dateToValidate.submittedDate);
						}

					} /* end of if the year is less than 100 */

					/* Now convert the date to the right format. */
					dateFormat = new SimpleDateFormat("yyyy-M-d");
					dateToValidate.submittedDate = dateFormat.format(testDate);	
					
					/* We've managed to fix it, so we can say the date is good */
					dateToValidate.badDate = false;
					dateToValidate.corrected = true;

				} /* end of try to parse the date string with an older format */
			
				catch (ParseException e2) 
				{					
					/* that's the way it goes */
/*					System.err.println("checkDateString: Second parse failed");*/
					
				} /* end of catch second parse exception */

			} /* end of first catch of parse exception */
			
		} /* end of else the date string has something in it */

		return(dateToValidate);
		
	} /* end of checkDateString method */
	
} /* end of class DateValidation */
