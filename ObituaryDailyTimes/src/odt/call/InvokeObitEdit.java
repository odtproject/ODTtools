package odt.call;

import java.io.PrintStream;
import java.util.Scanner;
import odt.tool.*;

/* This class will separate out the main call so that
 * ObitEdit can be called from here or from a GUI.
 */
public class InvokeObitEdit {

	public static void main(String[] args) {
		
		String fileToCheck;
		
		/* Get the name of the file to check */
		PrintStream consolePrint = new PrintStream( System.out );
		consolePrint.print("File to check: " );

		/* Ask the user for the name of the file to check */
		Scanner consoleScan = new Scanner( System.in );
		fileToCheck = consoleScan.nextLine();
	
		/* Call ObitEdit and pass along any arguments there might be.
		 * The first one should be the name of the file to process.
		 */
		ErrorLog obitErrors = new ErrorLog();

		ObitEdit.doObitEdit( fileToCheck, obitErrors );

		/* Close the resources */
		consoleScan.close();

	} /* end of main() */

} /* end of class InvokeObitEdit */
