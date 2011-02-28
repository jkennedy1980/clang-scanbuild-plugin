package jenkins.plugins.clangscanbuild.history;

public class ClangScanBuildBug {

	public String reportFile;
	public String sourceFile;
	public String bugType;
	public String bugDescription;
	public String bugCategory;
	public boolean newBug;
	
	public boolean isNewBug() {
		return newBug;
	}
	public void setNewBug(boolean newBug) {
		this.newBug = newBug;
	}
	public String getBugCategory() {
		return bugCategory;
	}
	public void setBugCategory(String bugCategory) {
		this.bugCategory = bugCategory;
	}
	public String getBugType() {
		return bugType;
	}
	public void setBugType(String bugType) {
		this.bugType = bugType;
	}
	public String getBugDescription() {
		return bugDescription;
	}
	public void setBugDescription(String bugDescription) {
		this.bugDescription = bugDescription;
	}
	public String getReportFile() {
		return reportFile;
	}
	public void setReportFile(String reportFile) {
		this.reportFile = reportFile;
	}
	public String getSourceFile() {
		return sourceFile;
	}
	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((bugCategory == null) ? 0 : bugCategory.hashCode());
		result = prime * result
				+ ((bugDescription == null) ? 0 : bugDescription.hashCode());
		result = prime * result + ((bugType == null) ? 0 : bugType.hashCode());
		result = prime * result
				+ ((reportFile == null) ? 0 : reportFile.hashCode());
		result = prime * result
				+ ((sourceFile == null) ? 0 : sourceFile.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClangScanBuildBug other = (ClangScanBuildBug) obj;
		if (bugCategory == null) {
			if (other.bugCategory != null)
				return false;
		} else if (!bugCategory.equals(other.bugCategory))
			return false;
		if (bugDescription == null) {
			if (other.bugDescription != null)
				return false;
		} else if (!bugDescription.equals(other.bugDescription))
			return false;
		if (bugType == null) {
			if (other.bugType != null)
				return false;
		} else if (!bugType.equals(other.bugType))
			return false;
		if (reportFile == null) {
			if (other.reportFile != null)
				return false;
		} else if (!reportFile.equals(other.reportFile))
			return false;
		if (sourceFile == null) {
			if (other.sourceFile != null)
				return false;
		} else if (!sourceFile.equals(other.sourceFile))
			return false;
		return true;
	}
	
}
