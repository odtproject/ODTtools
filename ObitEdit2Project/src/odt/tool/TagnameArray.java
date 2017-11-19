package odt.tool;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

/* This class is for keeping track of all the tagnames for contributors */
public class TagnameArray {
	
	ArrayList<String> tagArray = new ArrayList<>();

	/* This method initializes the array of tagnames */
	public static TagnameArray initTagArray(TagnameArray tagNames, LocationArray locsArray, String libFilesPath) {
		
		char symbol;
		String tagnameLine, tagname;
		String[] tagParts;
		
		try {
			
			/* Get the file name path from the library folder */
			java.nio.file.Path tagsFilePath = Paths.get(libFilesPath, "tagname.txt");			
			Scanner diskScanner = new Scanner(new File(tagsFilePath.toString()));
		
			/* Find the first line of data, which comes after a line of dashes */
			do {
				/* get the next line */
				tagnameLine = diskScanner.nextLine();

			} while (! tagnameLine.contains("-----"));

			/* skip the line of dashes */
			tagnameLine = diskScanner.nextLine();

			/* while there are lines in the file, keep processing */
			while (diskScanner.hasNextLine()) {
				
				/* get the line */
				tagnameLine = diskScanner.nextLine();
				tagParts = tagnameLine.split(" ");
			
				/* get the first word on the line */
				tagname = tagParts[0];
			
				/* get the first character on the line */
				symbol = tagname.charAt(0);
			
				/* If the line is longer than one character and the
				 * first character is a lower case letter, then
				 * add it to the array.
				 */
				if (tagParts.length > 1 && symbol >= 'a' && symbol <= 'z') {
					
					tagNames.tagArray.add(tagname);				
					
				} /* end of if this line has good information */
				
				else if (tagParts.length <= 1) {
					System.err.println("initTagArray: bad line: " + tagnameLine);
					System.err.println("in " + tagsFilePath.toString());
							
				}
			
			} /* end of while there are lines in the file */
		
			/* The diskScanner is still open, close it. */
			if(diskScanner!=null) {
				diskScanner.close();
			}
		}
		catch (FileNotFoundException fnf) {
			/* If we were unable to open the file, then log the error. */
			System.err.println("initTagArray file not found: " + fnf.getMessage());
		}
		
		return(tagNames);
		
	} /* end of initTagArray */
	
	/* This method verifies that a tagname is in the tagname array. */
	public static boolean verifyTagname (String tagname, ArrayList<String> tagArray) {
		
		if (tagArray.contains(tagname)) {
			return true;
		}
		else {
			return false;
		}

	} /* end of VerifyTagname */

} /* end of class */
