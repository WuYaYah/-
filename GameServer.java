// GameServer.java
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;


/**
 * GameServer - 多人連線伺服器主程式
 * 支援 3 人以上連線，使用 CachePool 快取機制，
 * 接收各類型訊息(update/chat/score/bullet)，並廣播給所有 Client。
 */
public class GameServer {
    /** 伺服器監聽埠號 */
    private static final int PORT = 12345;
    /** 同步管理所有連線執行緒 */
    public static final List<ClientHandler> clients =
        Collections.synchronizedList(new ArrayList<>());
    /** 快取機制實例 */
    public static final CachePool cache = new CachePool();
	
	public static final Map<String,Integer> scoreMap =
        Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("▶ 伺服器啟動，等待玩家連線（埠號 " + PORT + "）...");

        ExecutorService exec = Executors.newCachedThreadPool();
        while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket);
            clients.add(handler);
            handler.start();
        }
    }

    /**
     * 廣播訊息給所有 Client
     * @param message 完整訊息字串（已含前綴，如 "pos:Alice:400,300"）
     */
    public static void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }
}

/**
 * ClientHandler - 處理單一 Client 連線與訊息收發
 */
class ClientHandler extends Thread {
    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String name = "";

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // 建立輸入/輸出管道
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 1. 讀取玩家名稱
            name = in.readLine();
            System.out.println("➤ 玩家「" + name + "」已加入，當前在線：" + GameServer.clients.size());
			
			// 1.5 把所有已存在的分數發給新連線的 client
            //    cache 裡 key = "<player>.score"
            //for (Map.Entry<String,Integer> e : GameServer.scoreMap.entrySet()) {
            //out.println("score:" + e.getKey() + ":" + e.getValue());
       // }
			//ameServer.clients.add(this);
			
			synchronized (GameServer.scoreMap) {
                for (Map.Entry<String,Integer> e : GameServer.scoreMap.entrySet()) {
                    out.println("score:" + e.getKey() + ":" + e.getValue());
                }
            }
			
			GameServer.scoreMap.putIfAbsent(name, 0);               // 如果还没记录，就初始化为 0
            GameServer.broadcast("score:" + name + ":0");
			GameServer.clients.add(this);

            // 2. 迴圈接收來自此 Client 的所有訊息
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("update:")) {
                    // 位置更新，格式 update:x,y
                    String pos = line.substring(7);
                    GameServer.cache.set(name + ".pos", pos);
                    GameServer.broadcast("pos:" + name + ":" + pos);

                } else if (line.startsWith("chat:")) {
                    // 文字聊天，格式 chat:Hello world
                    GameServer.broadcast("chat:" + name + ":" + line.substring(5));

                } else if (line.startsWith("score:")) {
                    // 分數同步，格式 score:123
                    String[] parts = line.split(":", 3);
					if (parts.length == 3) {
						String player = parts[1];
						// 這裡直接用 parts[2] 來解析分數
						int val = Integer.parseInt(parts[2]);

						// 把最新分數寫入全局 scoreMap
						GameServer.scoreMap.put(player, val);
						// （如果你還在用 cache 存分數，也一併更新）
						GameServer.cache.set(player + ".score", val);

						// 再廣播給所有在線 client
						GameServer.broadcast("score:" + player + ":" + val);
					}

                } else if (line.startsWith("bullet:")) {
                    // 子彈同步，格式 bullet:x,y:dir
                    GameServer.broadcast("bullet:" + name + ":" + line.substring(7));
					
					

                } else {
                    // 其他類型訊息，都當作原樣廣播
                    GameServer.broadcast(line);
                }
            }
        } catch (IOException e) {
            System.out.println("✖ 玩家「" + name + "」連線中斷。");
        } finally {
            // 清理資源
            try { socket.close(); } catch (IOException ignored) {}
            GameServer.clients.remove(this);
            System.out.println("✖ 玩家「" + name + "」已離線，當前在線：" + GameServer.clients.size());
        }
    }

    /** 發送訊息給此 Client */
    public void sendMessage(String msg) {
        if (out != null) out.println(msg);
    }
}
