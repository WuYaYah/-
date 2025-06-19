// Client.java
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;
import java.io.PrintWriter;
/**
 * Client - 負責與 GameServer 建立 TCP 連線，並提供以下功能：
 * 1. 傳送玩家名稱（識別 ID）
 * 2. 傳送各類型訊息：位置 update、聊天 chat、分數 score、子彈 bullet
 * 3. 在背景執行緒接收來自 Server 的所有廣播，並更新 Picture 畫面
 */
public class Client {
    public static final String SERVER_IP = "localhost";
    public static final int    PORT      = 12345;
    //static PrintWriter out;
    // Socket & I/O
    private static Socket           socket;
    private static BufferedReader   in;
    private static PrintWriter      out;

    // 存放其他玩家的最新位置：key=playerName, value="x,y"
    public static ConcurrentHashMap<String, String> otherPlayers = new ConcurrentHashMap<>();

    /**
     * 建立與伺服器的連線，同時傳送玩家名稱
     * @param name 玩家識別名稱
     */
    public static void connect() throws IOException {
		socket = new Socket(SERVER_IP, PORT);
		in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out    = new PrintWriter(socket.getOutputStream(), true);
		// 不再在这里 out.println(name);
	}
	public static void sendName(String name) {
		out.println(name);
	}
	
	public static void sendPosition(String playerName, int x, int y) {
        if (out != null) {
            out.println("pos:" + playerName + ":" + x + "," + y);
        }
    }

    /** 傳送位置更新給伺服器，格式: "update:x,y" */
    public static void sendUpdate(int x, int y) {
        out.println("update:" + x + "," + y);
    }

    /** 傳送聊天訊息給伺服器，格式: "chat:Hello world" */
    public static void sendChat(String msg) {
        out.println("chat:" + msg);
    }

    /** 傳送分數更新給伺服器，格式: "score:123" */
    public static void sendScore(int score) {
        out.println("score:" + score);
    }

    /**
     * 傳送子彈同步事件，格式: "bullet:x,y:dir"
     * @param x   子彈初始 X
     * @param y   子彈初始 Y
     * @param dir 子彈方向旗標
     */
    public static void sendBullet(int x, int y, int dir) {
        out.println("bullet:" + x + "," + y + ":" + dir);
    }
	
	public static PrintWriter getWriter() {
    return out;
}

	public static void sendAsteroid(String id, String data) {
		out.println("asteroid:" + id + ":" + data);
	}

	
	
    /**
     * 在背景執行緒持續接收伺服器廣播，
     * 根據前綴(pos/chat/score/bullet)呼叫 Picture 的對應更新
     * @param pic       遊戲畫面管理物件
     * @param playerName 我自己的玩家名稱（用來過濾 self）
     */
    public static void listen(Picture pic, String playerName) {
		out.println(playerName);
		new Thread(() -> {
			try {
				String msg;
				while ((msg = in.readLine()) != null) {
					// 忽略空或不含 ":" 的消息
					if (msg == null || !msg.contains(":")) {
						continue;
					}

					// 只做最多 3 段拆分，避免过度分割
					String[] parts = msg.split(":", 3);
					String cmd = parts[0];

					switch (cmd) {
						case "pos":
							// 期望 parts = ["pos", "name", "x,y"]
							if (parts.length == 3 && !parts[1].equals(playerName)) {
								otherPlayers.put(parts[1], parts[2]);
								pic.updateOtherPlayers(otherPlayers);
							}
							break;
						case "chat":
							// 期望 parts = ["chat", "name", "message"]
							if (parts.length == 3) {
								pic.appendChat(parts[1] + ": " + parts[2]);
							}
							break;
						case "score":
							// 期望 parts = ["score", "name", "123"]
							if (parts.length == 3) {
								//String name = parts[1];
								//int sc      = Integer.parseInt(parts[2]);
								pic.updateScore(parts[1], Integer.parseInt(parts[2]));
                                pic.repaint();
							}
							break;
						case "bullet":
							// 期望 parts = ["bullet", "name", "x,y:dir"]
							if (parts.length == 3) {
								String[] sub = parts[2].split(":", 2);
								if (sub.length == 2) {
									String[] xy = sub[0].split(",", 2);
									int x = Integer.parseInt(xy[0]);
									int y = Integer.parseInt(xy[1]);
									int dir = Integer.parseInt(sub[1]);
									pic.spawnRemoteBullet(parts[1], x, y, dir);
								}
							}
							break;
						case "asteroid":
							// 期望 parts = ["asteroid", "id", "dataString"]
							if (parts.length == 3) {
								Map<String,String> m = new HashMap<>();
								m.put(parts[1], parts[2]);
								pic.updateAsteroids(m);
							}
							break;							
							
						default:
							// 其他消息直接忽略或日志
							break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}, "Text-Listener").start();
	}

}
