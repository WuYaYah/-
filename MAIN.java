// MAIN.java
import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * MAIN - 遊戲啟動類別 (檔名請為 MAIN.java)
 * 功能：
 * 1. 讓玩家輸入名稱並連線 Server
 * 2. 建立遊戲視窗與 Picture 畫布
 * 3. 註冊 MeListener 取得鍵盤與滑鼠操作
 * 4. 啟動遊戲主迴圈與 Client 端接收 Thread
 */
public class MAIN {
    public static void main(String[] args) {
        // 1. 玩家輸入名稱
        String playerName = JOptionPane.showInputDialog(null, "輸入玩家名稱：");
        if (playerName == null || playerName.trim().isEmpty()) {
            System.out.println("玩家名稱不可為空，程序終止。");
            return;
        }

        // 2. 連線伺服器
        try {
            Client.connect();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "無法連線到伺服器：" + e.getMessage());
            return;
        }
        // 取得通訊用 PrintWriter
        PrintWriter out = Client.getWriter();

        // 3. 建立遊戲主視窗
        JFrame window = new JFrame("Network Asteroids - " + playerName);
        window.setSize(Windows.WIDTH.getValue(), Windows.HEIGHT.getValue());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);

        // 4. 建立遊戲畫布 Picture，並確保可接收鍵盤焦點
        Picture pic = new Picture(playerName);
        pic.setFocusable(true);
        pic.requestFocusInWindow();
        window.add(pic);

        // 5. 註冊輸入監聽到畫布上 (鍵盤 + 滑鼠)
        MeListener ml = new MeListener(pic, out, playerName);
        pic.addKeyListener(ml);
        pic.addMouseListener(ml);

        window.setVisible(true);
		
		// 6. 啟動遊戲主迴圈
        pic.action();

        // 7. 啟動 Client 背景執行緒，接收 Server 廣播
        Client.listen(pic, playerName);
		
		
      
        

        
    }
}
