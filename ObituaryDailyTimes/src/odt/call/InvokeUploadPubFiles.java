package odt.call;

import odt.pub.BuildPubFiles;
import odt.tool.DirectorySetup;

/* This class will separate out the main call so that
 * BuildPubFiles can be called from here or from a GUI.
 */
public class InvokeUploadPubFiles {

	public static void main(String[] args) {
		
		/* Set up the directory structure */
		DirectorySetup dirSetup = new DirectorySetup();
		
		/* Set up the ODT directory structure.  If it fails, we're done. */
		if ( ! dirSetup.isSetupComplete() ) {			
			return;
		}
		
		BuildPubFiles.uploadPubFiles(dirSetup);

	} /* end of main() */

} /* end of class InvokeUploadPubFiles */
