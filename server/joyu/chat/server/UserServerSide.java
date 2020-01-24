package joyu.chat.server;

public class UserServerSide {

	private String username;
	private String realname;
	private String sex;
	private String email;
	private String phone;
	private String address;
	private final String systemInfo;

	public UserServerSide(String systemInfo) {
		this.systemInfo = systemInfo;

	}

	public String toString() {
		return username + systemInfo + realname + systemInfo + sex + systemInfo
				+ email + systemInfo + phone + systemInfo + address
				+ systemInfo;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}