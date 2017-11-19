package odt.tool;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

/* 
 * There are several titles or suffixes that we do not allow
 * but there is not always agreement on which ones are not allowed.
 * This class will isolate those disagreements by using a separate
 * file that can change more often.
 */
public class TitleValidation {
	
	ArrayList<String> titleSuffixArray;
	
	/* This method initializes the suffix/title do-not-use list.
	 */
	public boolean initTitleSuffix( String libFilesPath ) {
		
		String titleLine;
		
		Path titlesFilePath = Paths.get(libFilesPath, "rmvtitles.txt");
		
		try {
			Scanner fileScanner = new Scanner(new File(titlesFilePath.toString()));
			
			titleSuffixArray = new ArrayList<>();
			
			/* while there are lines in the file */
			while ( fileScanner.hasNextLine() ) {
				
				/* get another line */
				titleLine = fileScanner.nextLine();
				
				/* If the line is not empty, put it in the array */
				if ( ! titleLine.equals("") ) {
					titleSuffixArray.add(titleLine);
				}
				
			} /* end of while there are lines in file */	
			
			fileScanner.close();
			
			return( true );
			
		} /* end of try to open file */
		
		catch ( Exception e ) {
			System.err.println("Title/Suffix validation exception: " + e.getMessage() );
		}
		
		return ( false );
		
	} /* end of checkTitleSuffix */
	
	/* This method will return whether a title or suffix
	 * is in the array of titles and suffixes that are
	 * not supposed to be used.
	 */
	public boolean checkTitleSuffix ( String titleOrSuffix ) {
		
		if ( titleSuffixArray.contains(titleOrSuffix) ) {
			return ( true );
		}
		else {
			return ( false );
		}

	} /* end of checkTitleSuffix */

} /* end of class TitleValidation */
