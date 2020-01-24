package joyu.chat.server;

public class FileBeanServerSide {
	private String fileName;
	private String fileKind;
	private String fileSize;
	private String fileFrom;
	private String fileCreatedTime;
	private final String systemSigleQuote;
	private String fileModifiedTime;
	private final String systemInfo;

	public FileBeanServerSide(String systemInfo, String systemSigleQuote) {
		this.systemInfo = systemInfo;
		this.systemSigleQuote = systemSigleQuote;
	}

	public String toString() {
		fileName = fileName.replace(systemSigleQuote, "'");
		return fileName + systemInfo + fileKind + systemInfo + fileSize
				+ systemInfo + fileFrom + systemInfo + fileCreatedTime
				+ systemInfo + fileModifiedTime + systemInfo;
	}

	public String getFileCreatedTime() {
		return fileCreatedTime;
	}

	public void setFileCreatedTime(String fileCreatedTime) {
		this.fileCreatedTime = fileCreatedTime;
	}

	public String getFileFrom() {
		return fileFrom;
	}

	public void setFileFrom(String fileFrom) {
		this.fileFrom = fileFrom;
	}

	public String getFileKind() {
		return fileKind;
	}

	public void setFileKind(String fileKind) {
		this.fileKind = fileKind;
	}

	public String getFileModifiedTime() {
		return fileModifiedTime;
	}

	public void setFileModifiedTime(String fileModifiedTime) {
		this.fileModifiedTime = fileModifiedTime;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}
}