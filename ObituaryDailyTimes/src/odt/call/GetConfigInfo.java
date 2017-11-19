package odt.call;

import java.io.PrintStream;
import java.util.Scanner;

import odt.pub.ConfigFile;
import odt.tool.DirectorySetup;

public class GetConfigInfo {

	public static void main(String[] args) {
		
		/* Set up the directory structure */
		DirectorySetup dirSetup = new DirectorySetup();
		
		/* Set up the ODT directory structure.  If it fails, we're done. */
		if ( ! dirSetup.isSetupComplete() ) {			
			return;
		}

		/* Get the name of the FTP host */
		PrintStream consolePrint = new PrintStream( System.out );
		consolePrint.print("Enter FTP host: " );		
		
		Scanner consoleScan = new Scanner( System.in );
		String host = consoleScan.nextLine();
		
		/* get user name */
		consolePrint.print("Enter FTP user name: " );		
		String username = consoleScan.nextLine();
	
		consolePrint.print("Enter FTP password: " );		
		String password = consoleScan.nextLine();
		
		if ( ConfigFile.storeConfig(host, username, password) ) {
			consolePrint.print("Config file values stored" );
		}
		else {
			consolePrint.print("Oops, there was a problem." );
		}
		
		consoleScan.close();
		consolePrint.close();
	}

}
