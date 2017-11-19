package odt.call;

import odt.pub.ConfigFile;
import odt.tool.DirectorySetup;

public class ReadConfigInfo {

	public static void main(String[] args) {
		
		/* Set up the directory structure */
		DirectorySetup dirSetup = new DirectorySetup();
		
		/* Set up the ODT directory structure.  If it fails, we're done. */
		if ( ! dirSetup.isSetupComplete() ) {			
			return;
		}

		/* try to read the FTP config file; if it fails, we're done */
		if ( ! ConfigFile.readConfig() ) {
			System.err.println("Could not read config file");
			return;
		}

		/* get the FTP login information */
		System.out.println("host = " + ConfigFile.getFTPhost());
		System.out.println("user = " + ConfigFile.getFTPuser());
		System.out.println("pw = " + ConfigFile.getFTPpasswd());
	}
}
