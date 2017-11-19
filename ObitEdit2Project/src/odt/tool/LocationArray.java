package odt.tool;
/* This class fills in the arrays of location abbreviations and publications */

import java.io.File;
import java.io.FileNotFoundException;
//import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class LocationArray {
	
	ArrayList<String> locArray;
	boolean removedCountry;
	String repairedLocation;

	/* This method will verify the validity of a location abbreviation.
	 * The argument is a string that may have a location abbreviation at the end of it,
	 * which may have up to three abbreviations of 2 or 3 capital letters.
	 * A string is returned that contains the abbreviation, i.e., the place code
	 * within the location string.
	 */
	public static String verifyLocation (String locationString, LocationArray locationArray) {
		String[] locParts, newParts;
		String penultimateCode = null;
		int i;
		int arraysize;
		boolean inList;
		
		locationArray.removedCountry = false;
		locationArray.repairedLocation = locationString;
		locParts = locationString.split(" ");
		newParts = locParts;
		
		/* If there's nothing in the string, we're done. */
		if (locationString.isEmpty()) {
			locationString = "Invalid";
			return(locationString);
		}
		
		arraysize = locParts.length;
		
		/* While there are more than three words in the string, shift everything up,
		 * until we have three words in the string.
		 */
		while ( arraysize > 3 ) {
			for (i=0; i<arraysize-1; i++) {
				newParts[i] = locParts[i+1];
			}
			locParts = newParts;
			arraysize--;
		}
		
		/* Check the string to see if it's a genuine location */
		do {
			locationString=locParts[0];
			
			/* put together the parts of the string */
			for (i=1; i<arraysize; i++) {
				locationString = locationString + " " + locParts[i];
			}
			
			/* If the string is in the list of recognized locations, 
			 * then return it if other things check out.
			 */
			inList = locationArray.locArray.contains(locationString);
			if ( inList ) {
				
				/* If we're down to one code to check, let's see if
				 * that's USA or CAN.  Locations that are state USA
				 * or province CAN will not be seen as valid.
				 * It should be just state or province.
				 */
				if ( arraysize == 1) {
					
					/* Check for USA or CAN */					
					if ( locationString.equals("USA") || locationString.equals("CAN") ) {
						
						/* See if the penultimate part was a state or
						 * province, in which case that's wrong.
						 * Just supposed to have state or province without country.
						 */
						inList = locationArray.locArray.contains(penultimateCode);
						if ( inList ) {	
							
							/* We have to recover the original string and remove
							 * the country.
							 */
							locParts = locationArray.repairedLocation.split(" ");
							locationArray.repairedLocation = locParts[0];
							if ( locParts.length > 1) {
								for ( int j=1; j<locParts.length-1; j++) {
									locationArray.repairedLocation = locationArray.repairedLocation + " " + locParts[j];
								}
							}
							
							/* Let the caller know we fixed the location
							 * and return the correct code.
							 */
							locationArray.removedCountry = true;
							return(penultimateCode);
							
						} /* end of the penultimate Code was valid */
						
					} /* end of if the USA or CAN */
					
				} /* end of if the arraysize is one */
				
				return (locationString);
				
			} /* end of if the location is in the list */
			
			else if (arraysize == 1) {
				/* If there are no more string parts to check, then it is not a location */
				return ("Invalid");
			}
			
			/* In case the last bit is USA or CAN, save the penultimate bit for later */
			penultimateCode = locParts[0];
			
			/* Shift everything over and try again */
			for (i=0; i<arraysize-1; i++) {
				newParts[i] = locParts[i+1];
			}
			locParts = newParts;
			arraysize--;

		} while (arraysize >= 1);
		
		return("Invalid");
		
	} /* end of VerifyLocation method */
	
	/* This method initializes the array of locations from the file of abbreviations */
	public static LocationArray initLocationArray(LocationArray abbreviationArray, String libFilesPath) 
	{
		String[] abbrevParts, locParts;
		abbreviationArray.locArray = new ArrayList<>();
		char symbol;
		String abbrevLine, location, firststr;
		int locationLen;
		
		try 
		{
			/* Get the file name path from the library folder */
			Path abbrevFilePath = Paths.get(libFilesPath, "abbrev.txt");			
			Scanner fileScanner = new Scanner(new File(abbrevFilePath.toString()));
			
			/* Find the first line of data, which comes after a line of dashes */
			do 
			{
				/* get the next line */
				abbrevLine = fileScanner.nextLine();

			} while (! abbrevLine.contains("-----"));
		
			/* A defunct location name will have this somewhere in the second string */
			String defunctLocation = "see"; 

			/* while there are lines in the file, keep going */
			while (fileScanner.hasNextLine()) 
			{			
				/* get the next line */
				abbrevLine = fileScanner.nextLine();
			
				/* split the line into parts delimited by a dash surrounded by spaces */
				abbrevParts = abbrevLine.split(" - ");
			
				/* if there are at least two parts, then it could be a valid location */
				if (abbrevParts.length > 1) 
				{				
					/* get the first delimited string on the line */
					firststr = abbrevParts[0].trim();
			
					/* get the first character on the line */
					symbol = firststr.charAt(0);
			
					/* if the first character is a space or a dash, skip that line */
					if (symbol != ' ' && symbol != '-') 
					{				
						/* get the next string on the line and its length */
						location = abbrevParts[1].trim();
						locationLen = location.length();
								
						/* If the first string does not indicate an unused location then
						 * we add it to the list
						 */
						if ( locationLen >= 2) 
						{
							locParts = location.split(" ");
							if ( ! locParts[0].equals(defunctLocation) ) 
							{
								abbreviationArray.locArray.add(location);
							}
						}
										
					} /* end of if the first character means a location */
				
				} /* end of if there are at least two parts */
			
			} /* end of while there are lines in the file */

			/* Close the resource(s) */
			fileScanner.close();			
		
		} /* end of try section */

		catch (FileNotFoundException fnf) 
		{
			/* If we were unable to open either of the files, then log the error. */
			System.err.println("FileNotFoundException: " + fnf.getMessage());
		}
		
		return(abbreviationArray);
		
	} /* end of initLocationArray */
	
	/* This method will fix a location code that is not all upper case letters. */
	public static String fixLowerCaseLocation ( String originalLocation, String correctLocationCode ) 
	{		
		String workingLocation;
		int stringIndex;
		
		/* take the original string and make it all upper case (including the city) */
		workingLocation = originalLocation.toUpperCase();
		
		/* get the index of the correct upper case location code within this upper case string */
		stringIndex = workingLocation.lastIndexOf(correctLocationCode);
		
		/* Use that index to get the correct (city) part of the original string. */
		workingLocation = originalLocation.substring(0, stringIndex);
		
		/* take the good part and add the correct location code */
		workingLocation = workingLocation + correctLocationCode;
		workingLocation = workingLocation.trim();
		
		/* that should do it */
		return ( workingLocation );
		
	} /* end of fixLowerCaseLocation */
	
} /* end of LocationArray class */