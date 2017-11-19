package odt.tool;

/* This is an enumeration for the general categories of errors reported:
 * Error - a probable error that needs fixing
 * Automatically corrected - it was an error, but has been fixed
 * Warning - not necessarily an error but the user might want to examine it.
 */
public enum ErrorCategory {
		ERROR, 
		AUTOCORRECT, 
		SPACING,
		WARNING
}
