package odt.call;

import java.io.PrintStream;
import java.util.Scanner;

import odt.ftool.ObitIssue;

public class InvokeObitIssue {

	public static void main(String[] args) {
		
		String fileToCheck;
		
		/* Get the name of the file to process (it will be in the "Check" folder */
		PrintStream consolePrint = new PrintStream( System.out );
		consolePrint.print("File to process: " );

		/* Ask the user for the name of the file to check */
		Scanner consoleScan = new Scanner( System.in );
		fileToCheck = consoleScan.nextLine();
		
		/* Close the resources */
		consoleScan.close();
		
		/* Call doObitEmail, passing along the arguments.
		 * The first one should be the name of the file
		 * to process.
		 */
		ObitIssue.doObitIssue(fileToCheck, args);

	} /* end of class InvokeObitEmail */

}
