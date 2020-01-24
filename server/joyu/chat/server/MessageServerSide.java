package joyu.chat.server;

public class MessageServerSide {
	private String username;
	private String thetime;
	private String message;
	private final String systemSigleQuote;

	public MessageServerSide(String systemSigleQuote) {
		this.systemSigleQuote = systemSigleQuote;
	}

	public String toString() {
		message = message.replace(systemSigleQuote, "'");
		if (username.equalsIgnoreCase("sysInfomation")) {
			return message;
		} else {
			return username + " [" + thetime + "]:\n" + message;
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getThetime() {
		return thetime;
	}

	public void setThetime(String thetime) {
		this.thetime = thetime;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}