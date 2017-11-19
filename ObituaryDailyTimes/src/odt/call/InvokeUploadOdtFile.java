package odt.call;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

import odt.pub.UploadOdtFile;

/* This class will separate out the main call so that
 * UploadOdtFile can be called from here or from a GUI.
 */
public class InvokeUploadOdtFile {

	public static void main(String[] args) {
		
		ArrayList<String> filesToUpload = new ArrayList<>();
		
		/* Get the name of the file to upload */
		PrintStream consolePrint = new PrintStream( System.out );
		consolePrint.print("File to upload (full path name): " );
		
		/* Ask the user for the name of the file to upload */
		Scanner consoleScan = new Scanner( System.in );
		String fileInput = consoleScan.nextLine();
		filesToUpload.add(fileInput);
		
		/* Try to upload the file(s).  It will report any failure */
		if ( UploadOdtFile.uploadOdtFiles( filesToUpload ) ) {
			System.out.println("Success!");
		}
		else {
			System.err.println("Oops!");
		}
		
		/* Close the resources */
		consoleScan.close();

	} /* end of main() */

} /* end of class InvokeUploadOdtFile */
