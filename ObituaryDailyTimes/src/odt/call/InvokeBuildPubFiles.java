package odt.call;

import odt.pub.BuildPubFiles;

/* This class will separate out the main call so that
 * BuildPubFiles can be called from here or from a GUI.
 */
public class InvokeBuildPubFiles {

	public static void main(String[] args) {
		
		
		BuildPubFiles.doBuildPubFiles( args );

	} /* end of main() */

} /* end of class InvokeObitEdit */
