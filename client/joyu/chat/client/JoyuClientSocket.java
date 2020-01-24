package joyu.chat.client;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class JoyuClientSocket {
	private Socket socket = null;
	private DataOutputStream dos = null;
	private DataInputStream dis = null;

	public JoyuClientSocket(String ip, int port) throws Exception {
		try {
			socket = new Socket(ip, port);
			dis = new DataInputStream(new BufferedInputStream(
					socket.getInputStream()));
			dos = new DataOutputStream(socket.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
			if (socket != null)
				socket.close();
			throw e;
		}
	}

	public synchronized void sendString(String sendMessage) throws Exception {
		try {
			dos.writeUTF(sendMessage);
			dos.flush();
		} catch (Exception e) {
			e.printStackTrace();
			if (dos != null)
				dos.close();
			throw e;
		}
	}

	public synchronized void sendBits(byte[] buf, int start, int length)
			throws Exception {
		try {
			dos.write(buf, start, length);
			dos.flush();
		} catch (Exception e) {
			e.printStackTrace();
			if (dos != null)
				dos.close();
			throw e;
		}
	}

	public String readString() throws Exception {
		try {
			return dis.readUTF();
		} catch (Exception e) {
			e.printStackTrace();
			if (dis != null)
				dis.close();
			throw e;
		} finally {
		}
	}

	public void shutDownConnection() {
		try {
			if (dos != null)
				dos.close();
			if (dis != null)
				dis.close();
			if (socket != null)
				socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Socket getSocket() {
		return socket;
	}
}