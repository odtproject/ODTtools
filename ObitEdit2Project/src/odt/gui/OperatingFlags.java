package odt.gui;

/*
 * These flags govern the reporting of certain types of
 * errors.
 */
public class OperatingFlags {

	private boolean suppressWarnings;
	private boolean suppressSpacingCorr;
	private boolean suppressAutoCorrect;

	public OperatingFlags () {
		suppressWarnings = false;
		suppressSpacingCorr = false;
		suppressAutoCorrect = false;
	}

	public boolean isSuppressWarnings() {
		return suppressWarnings;
	}

	public void setSuppressWarnings(boolean reportWarnings) {
		this.suppressWarnings = reportWarnings;
	}

	public boolean isSuppressSpacingCorr() {
		return suppressSpacingCorr;
	}

	public void setSuppressSpacingCorr(boolean reportSpacingCorr) {
		this.suppressSpacingCorr = reportSpacingCorr;
	}

	public boolean isSuppressAutoCorrect() {
		return suppressAutoCorrect;
	}

	public void setSuppressAutoCorrect(boolean reportAutoCorrect) {
		this.suppressAutoCorrect = reportAutoCorrect;
	}
	
}
