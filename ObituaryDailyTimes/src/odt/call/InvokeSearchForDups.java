package odt.call;

import java.io.PrintStream;
import java.util.Scanner;

import odt.ftool.SearchForDups;

/* This class will separate out the main call so that
 * ObitEdit can be called from here or from a GUI.
 */
public class InvokeSearchForDups {

	public static void main(String[] args) {
		
		String folderToCheck;
		
		/* Get the name of the file to check */
		PrintStream consolePrint = new PrintStream( System.out );
		consolePrint.print("File to check: " );

		/* Ask the user for the name of the folder to check */
		Scanner consoleScan = new Scanner( System.in );
		System.out.print("Directory for files to check against: ");
		folderToCheck = consoleScan.nextLine();
		
		/* Close the resources */
		consoleScan.close();
	
		/* Call ObitEdit and pass along any arguments there might be.
		 * The first one should be the name of the file to process.
		 */
		SearchForDups.doSearchForDups( folderToCheck, args );

	} /* end of main() */

} /* end of class InvokeObitEdit */
