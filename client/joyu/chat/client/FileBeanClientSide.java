package joyu.chat.client;

public class FileBeanClientSide {
	private String fileName;
	private String fileKind;
	private String fileSize;
	private String fileFrom;
	private String fileCreatedTime;
	private String fileModifiedTime;

	public String toString() {
		return fileName + " " + fileKind + " " + fileSize + " " + fileFrom
				+ " " + fileCreatedTime + " " + fileModifiedTime + " ";
	}

	public int hashCode() {
		return fileName.hashCode();
	}

	public boolean equals(Object o) {
		FileBeanClientSide s = (FileBeanClientSide) o;
		return fileName.equals(s.fileName);
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