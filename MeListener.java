

import java.awt.event.*;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * MeListener - 處理玩家輸入（鍵盤 + 滑鼠）
 * 並將玩家操作結果同步至伺服器
 */
public class MeListener implements KeyListener, MouseListener {
    private Picture picture;
    private PrintWriter out;    // 用於發送訊息到伺服器
    private String playerName;  // 玩家名稱

    /**
     * 建構子
     * @param picture 遊戲畫面物件
     * @param out     與伺服器通訊的 PrintWriter
     * @param playerName 玩家識別名稱
     */
    public MeListener(Picture picture, PrintWriter out, String playerName) {
        this.picture = picture;
        this.out = out;
        this.playerName = playerName;
    }

    // ----------- MouseListener methods -----------
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // ---------- KeyListener methods ------------
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        // 1. 按 S 開始遊戲
        if (code == KeyEvent.VK_S) {
            picture.startflag = 1;
            return;
        }
        // 2. 按 ESC 切換選單
        if (picture.startflag == 1 && code == KeyEvent.VK_ESCAPE) {
            picture.menuflag = (picture.menuflag + 1) % 2;
            return;
        }
        // 3. 遊戲中操作
        if (picture.startflag == 1 && picture.mehuanum > 150) {
            if (picture.me.wudiflag != 1 && picture.menuflag == 0) {
                switch (code) {
                    case KeyEvent.VK_LEFT:
                        picture.me.rotateLeft();
                        break;
                    case KeyEvent.VK_RIGHT:
                        picture.me.rotateRight();
                        break;
                    case KeyEvent.VK_UP:
                        picture.me.addspeed();
                        break;
                    case KeyEvent.VK_DOWN:
                        picture.me.minusspeed();
                        break;
                    case KeyEvent.VK_SPACE:
                        // 玩家射擊並通知伺服器
                        picture.BulletArray = Arrays.copyOf(
                            picture.BulletArray, picture.BulletArray.length + 1);
                        picture.BulletArray[picture.BulletArray.length - 1] =
                            new Bullet(
                                picture.me.getCenterX() - 2,
                                picture.me.getCenterY() - 2,
                                picture.me.flag
                            );
                        out.println("bullet:" + playerName + ":"
                            + picture.me.getCenterX() + "," + picture.me.getCenterY()
                            + ":" + picture.me.flag);
                        break;
                    case KeyEvent.VK_SHIFT:
                        picture.shanxian();
                        break;
                }
                // 同步玩家位置
                int x = picture.me.getCenterX();
                int y = picture.me.getCenterY();
                out.println("update:" + x + "," + y);
                picture.repaint();
            }
            // 4. 死亡重置
            if (picture.life <= 0 && code == KeyEvent.VK_B) {
                if (picture.score > picture.highest) {
                    picture.highest = picture.score;
                }
                picture.resetGame();
            }
        }
    }
}
