
import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JComponent;
import java.awt.image.BufferedImage;
import java.awt.Polygon;
import java.util.List;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
public class Picture extends JComponent{
    public static BufferedImage background;
    Me me=new Me();//创建一个飞机;
    Bullet []BulletArray=new Bullet[0];//创建一个子弹数组
    UFO []UFOArray=new UFO[0];//创建一个ufo数组
    Bullet []UFOBulletArray=new Bullet[0];//
    asteroids []asteroidsArray=new asteroids[0];//创建一个陨石数组
    Balldisappear []ballArray=new Balldisappear[0];
    int menuflag=0;
    int score=0;
    int scoretemp=0;
    int ufobullettime=0;
    int asteroidstime=0;
    int ufoflag=1;
    int wudinum=0;
    int life=3;
    int highest=0;
    int startflag=0;
    int addasteroidsflag=0;
    int addasteroidsnum=1;
    int yy=-1000;
    int mehuanum=0;
    int Height=Windows.HEIGHT.getValue();
    int Width=Windows.WIDTH.getValue();
    File f;
    URI uri;
    URL url;
	
	private String playerName;
    public Picture(String playerName) {
    this.playerName = playerName;
}
	// Picture.java 類別最前面
    private ConcurrentHashMap<String,String> remoteAsteroids = new ConcurrentHashMap<>();

	// 網路同步資料
    private ConcurrentHashMap<String,String> otherPlayers = new ConcurrentHashMap<>();
	// 同步远端小行星状态：key = 小行星 ID， value = “x1,y1;x2,y2;...” 数据串
    private ConcurrentHashMap<String,String> asteroidMap = new ConcurrentHashMap<>();
    private List<String> chatBuffer = new ArrayList<>();
    private Map<String,Integer> scoreMap = new LinkedHashMap<>();
    private List<Bullet> remoteBullets = new ArrayList<>();
	
	private final ConcurrentHashMap<String, Integer> scores = new ConcurrentHashMap<>();
	
	
    static{
        try{
            background = ImageIO.read(Picture.class.getResource("background1.jpg"));
        }catch(Exception e){
            e.getStackTrace();
        }
    }
    /*void Music(){//注意，java只能播放无损音质，如.wav这种格式"E:\\Studying\\Programming\\untitled\\src\\Asteroids\\The XX-Intro.wav"
        try {
            f = new File("The XX-Intro.wav"); //绝对路径
            //uri = f.toURI();
            url = f.toURL(); //解析路径
            AudioClip aau;
            aau = Applet.newAudioClip(url);
            if(aau!=null)System.out.println("not null");
            aau.play();
            aau.loop();  //单曲循环
        } catch (Exception e)
        {
            e.printStackTrace();
            }

    }*/
    public void mehua(){
        mehuanum++;
       if(mehuanum<=150){
           me.y1[0]-=1;
           me.y1[1]-=1;
           me.y1[2]-=1;
       }
    }
	
	/** 當 Client 收到 asteroid:... 訊息時呼叫 
	public void updateAsteroids(Map<String,String> map) {
		remoteAsteroids.clear();
		remoteAsteroids.putAll(map);
		repaint();
	} */
	
	 public void updateScore(String name, int sc) {
        scores.put(name, sc);
    }
	
	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (startflag == 1) {
            if (menuflag == 0) {
                mehua();
                paintbackground(g);
                paintMe(g);
                paintBullet(g);
                paintScore(g);
                paintUFO(g);
                paintufobullet(g);
                paintasteroids(g);
				// 畫遠端同步的小行星 (紫)
				g.setColor(new Color(200, 0, 200, 180));
				for (String data : remoteAsteroids.values()) {
					asteroids a = asteroids.fromDataString(data);
					a.draw(g);
				}

                paintball(g);
                // 畫其他玩家
                paintOtherPlayers(g);
                // 畫遠端子彈
                paintRemoteBullets(g);
                // 畫聊天
                paintChat(g);
                // 畫排行榜
                //paintLeaderboard(g);
                if (life <= 0) paintover(g);
            } else {
                paintbackground(g);
                paintMenu(g);
            }
        } else {
            paintbackground(g);
            paintstart(g);
        }
    }


    public void paintMe(Graphics g){
        me.draw(g);
    }
	
	private void paintOtherPlayers(Graphics g) {
    g.setColor(Color.BLUE);
    for (String pos : otherPlayers.values()) {
        String[] xy = pos.split(",");
        int x = Integer.parseInt(xy[0]);
        int y = Integer.parseInt(xy[1]);
        g.fillOval(x - 10, y - 10, 20, 20);
    }
}

	
    public void paintufobullet(Graphics g){
        // 繪製所有小行星，跳過 null 元素以避免 NullPointerException
		for (asteroids a : asteroidsArray) {
			if (a != null) {
				a.draw(g);
			}
		}
    }
    public void paintasteroids(Graphics g){
        for(int i=0;i<asteroidsArray.length;i++){
            asteroidsArray[i].draw(g);
        }
    }
    public void paintbackground(Graphics g){
        yy++;
        if(yy>=0)yy=-Height;
        g.drawImage(background, 0, yy, null);
    }
    public void paintball(Graphics g){
        for(int i=0;i<ballArray.length;i++){
            if(ballArray[i].flag==0) {
                ballArray[i].draw(g);
            }else if(ballArray[i].flag==1){
                ballArray[i].drawline(g);
            }
        }
    }
	
	
    public void paintBullet(Graphics g){//画出子弹
        for(int i=0;i<BulletArray.length;i++){
            BulletArray[i].draw(g);
        }
    }
    public void paintMenu(Graphics g) {
        int menux = 550; // x坐标
        int menuy = 200; // y坐标
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 40); // 字体
        g.setColor(Color.red);
        g.setFont(font); // 设置字体
        g.drawString("Accelerate : ↑" , menux, menuy); // 画分数
        menuy += 80;
        g.drawString("Decelerate : ↓" , menux, menuy); // 画分数
        menuy += 80;
        g.drawString("Turn left : ←" , menux, menuy);
        menuy += 80;
        g.drawString("Turn right : →" ,menux, menuy); // 画命      }
        menuy += 80;
        g.drawString("Fire : Space" , menux, menuy); // 画分数
        menuy += 80;
        g.drawString("Moving to a random safe place : Shift" , menux, menuy); // 画分数
    }
     private void paintScore(Graphics g) {
        // 本地玩家也加入
        //scoreMap.put(playerName, score);

        //List<Map.Entry<String,Integer>> list = new ArrayList<>(scoreMap.entrySet());
        //list.sort((a,b) -> b.getValue() - a.getValue());

        // 字體設定
		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));

		// 1) 顯示本地玩家分數
		g.setColor(Color.WHITE);
		g.drawString("Score: " + score, 10, 40);

		// 2) 顯示生命值
		g.setColor(Color.RED);
		g.drawString("Life: " + life, 10, 80);

		// 3) 顯示選單提示
		g.drawString("MENU (ESC)", 300, 40);
    }

    public void paintover(Graphics g){
        int scorex = 400; // x坐标
        int scorey = 500; // y坐标
        int scorexx=400;
        int scoreyy=575;
        int scorexxx=400;
        int scoreyyy=650;
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 100); // 字体
        g.setColor(Color.WHITE);
        g.setFont(font); // 设置字体
        g.drawString("GAME OVER", scorex, scorey);
        Font font1 = new Font(Font.SANS_SERIF, Font.BOLD, 50); // 字体
        g.setFont(font1); // 设置字体
        g.drawString("Your score is "+score, scorexx, scoreyy);
        Font font2 = new Font(Font.SANS_SERIF, Font.BOLD, 50); // 字体
        g.setFont(font2); // 设置字体
        g.drawString("Please press 'B' to restart", scorexxx, scoreyyy);
    }
    public void paintstart(Graphics g){
        int scorex = 400; // x坐标
        int scorey = 400; // y坐标
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 150); // 字体
        g.setColor(Color.BLUE);
        g.setFont(font); // 设置字体
        g.drawString("Asteroids", scorex, scorey);
        scorey+=60;
        scorex+=150;
        Font font1 = new Font(Font.SANS_SERIF, Font.BOLD, 50); // 字体
        g.setColor(Color.BLUE);
        g.setFont(font1); // 设置字体
        g.drawString("Press 'S' to start", scorex, scorey); // 画命      }
    }
	
	/** 繪製其他玩家 
    private void paintOtherPlayers(Graphics g) {
        g.setColor(Color.BLUE);
        for (String pos : otherPlayers.values()) {
            String[] xy = pos.split(",");
            int x = Integer.parseInt(xy[0]);
            int y = Integer.parseInt(xy[1]);
            g.fillOval(x - 10, y - 10, 20, 20);
        }
    }*/
	
	/** 繪製遠端子彈 */
    private void paintRemoteBullets(Graphics g) {
        g.setColor(Color.RED);
        for (Bullet b : remoteBullets) {
            b.draw(g);
        }
    }
	
	/** 繪製聊天訊息 */
    private void paintChat(Graphics g) {
        g.setColor(Color.WHITE);
        int y = 20;
        for (String line : chatBuffer) {
            g.drawString(line, 10, y);
            y += 15;
        }
    }
	
	/** 繪製排行榜 
    private void paintLeaderboard(Graphics g) {
    // 将 scoreMap 按分数从高到低排序
    List<Map.Entry<String,Integer>> list = new ArrayList<>(scoreMap.entrySet());
    list.sort((a,b) -> b.getValue() - a.getValue());

    g.setColor(Color.YELLOW);
    int y = 40;
    g.drawString("排行榜", Width - 200, 20);

    String champion = null;
    int best = -1;
    for (Map.Entry<String,Integer> e : list) {
        String name = e.getKey();
        int sc = e.getValue();
        g.drawString(name + ": " + sc, Width - 200, y);
        if (sc > best) {
            best = sc;
            champion = name;
        }
        y += 20;
    }

    // 显示冠军
    if (champion != null) {
        g.setColor(Color.RED);
        g.drawString("冠軍：" + champion + " (" + best + ")", Width - 200, y + 20);
    }
}*/

	
	 // 更新方法，供 Client.listen 呼叫：
    public void updateOtherPlayers(Map<String, String> map) {
        otherPlayers.clear();
        otherPlayers.putAll(map);
        repaint();
    }
	/**
	 * 更新远端小行星状态
	 * @param map 来自 Client.listen 的 asteroidMap
	 */
	public void updateAsteroids(Map<String,String> map) {
		asteroidMap.clear();
		asteroidMap.putAll(map);
		// 根据 map 大小重建本地 asteroidsArray
		asteroidsArray = new asteroids[asteroidMap.size()];
		int idx = 0;
		for (String data : asteroidMap.values()) {
			asteroidsArray[idx++] = asteroids.fromDataString(data);
		}
		repaint();
	}
	
	public void appendChat(String txt) {
        chatBuffer.add(txt);
        if (chatBuffer.size() > 10) chatBuffer.remove(0);
        repaint();
    }
	
	
	public void spawnRemoteBullet(String who, int x, int y, int dir) {
        Bullet b = new Bullet(x, y, dir);
        remoteBullets.add(b);
        repaint();
    }

	
    public void paintUFO(Graphics g){
        for(int i=0;i<UFOArray.length;i++){
            UFOArray[i].draw(g);
        }
    }
    public void ballfly(){
        for(int i=0;i<ballArray.length;i++){
            if(ballArray[i].count>=150){
                Balldisappear temp = ballArray[i];
                ballArray[i] = ballArray[ballArray.length - 1];
                ballArray[ballArray.length - 1] = temp;
                ballArray = Arrays.copyOf(ballArray, ballArray.length - 1);
            }else ballArray[i].fly();
        }
    }
    public void BulletFly() {//飞机子弹飞行
        for(int i=0;i<BulletArray.length;i++) {
            int index1=-1;
            int index2=-1;
            for (int j = 0; j < UFOArray.length; j++) {
                if (!UFOArray[j].hit(new Polygon(new int[]{(int) (BulletArray[i].x + 2), (int) (BulletArray[i].x - 2), (int) (BulletArray[i].x), (int) (BulletArray[i].x)}, new int[]{(int) (BulletArray[i].y), (int) (BulletArray[i].y), (int) (BulletArray[i].y + 2), (int) (BulletArray[i].y - 2)}, 4))) {
                    index1 = i;
                    UFO temp = UFOArray[j];
                    UFOArray[j] = UFOArray[UFOArray.length - 1];
                    UFOArray[UFOArray.length - 1] = temp;
                    UFOArray = Arrays.copyOf(UFOArray, UFOArray.length - 1);
                    //j--;
                    for(int m=0;m<4;m++) {
                        ballArray = Arrays.copyOf(ballArray, ballArray.length + 1);
                        ballArray[ballArray.length - 1] = new Balldisappear(temp.cx, temp.cy);
                    }
                    score += 300;
					// 1) 更新本地排行榜数据
					updateScore(playerName, score);
					// 2) 通知其他客户端同步本地分数
					Client.sendScore(score);

                    scoretemp+=300;
                }
            }
            for (int k = 0; k < asteroidsArray.length; k++) {
                //double tempcx=0;double tempcy=0;
                if (!asteroidsArray[k].hit(new Polygon(new int[]{(int) (BulletArray[i].x + 2), (int) (BulletArray[i].x - 2), (int) (BulletArray[i].x), (int) (BulletArray[i].x)}, new int[]{(int) (BulletArray[i].y), (int) (BulletArray[i].y), (int) (BulletArray[i].y + 2), (int) (BulletArray[i].y - 2)}, 4))) {
                    index2 = i;
                    asteroids temp = asteroidsArray[k];
                    //tempcx=asteroidsArray[k].cx;tempcy=asteroidsArray[k].cy;
                    asteroidsArray[k] = asteroidsArray[asteroidsArray.length - 1];
                    asteroidsArray[asteroidsArray.length - 1] = temp;
                    asteroidsArray = Arrays.copyOf(asteroidsArray, asteroidsArray.length - 1);
                    for(int m=0;m<4;m++) {
                        ballArray = Arrays.copyOf(ballArray, ballArray.length + 1);
                        ballArray[ballArray.length - 1] = new Balldisappear(temp.cx, temp.cy);
                    }
                    //k--;
                    if(temp.asteroidsflag==3) {
                        asteroidsArray = Arrays.copyOf(asteroidsArray, asteroidsArray.length + 1);
                        asteroidsArray[asteroidsArray.length - 1] = new asteroidsm(temp.cx, temp.cy);
                        asteroidsArray = Arrays.copyOf(asteroidsArray, asteroidsArray.length + 1);
                        asteroidsArray[asteroidsArray.length - 1] = new asteroidsm(temp.cx, temp.cy);
                        score += 300;
						// 1) 更新本地排行榜数据
						updateScore(playerName, score);
						// 2) 通知其他客户端同步本地分数
						Client.sendScore(score);

                        scoretemp+=300;
                    }else if(temp.asteroidsflag==2){
                        asteroidsArray = Arrays.copyOf(asteroidsArray, asteroidsArray.length + 1);
                        asteroidsArray[asteroidsArray.length - 1] = new asteroidsl(temp.cx, temp.cy);
                        asteroidsArray = Arrays.copyOf(asteroidsArray, asteroidsArray.length + 1);
                        asteroidsArray[asteroidsArray.length - 1] = new asteroidsl(temp.cx, temp.cy);
                        score += 400;
						// 1) 更新本地排行榜数据
						updateScore(playerName, score);
						// 2) 通知其他客户端同步本地分数
						Client.sendScore(score);

                        scoretemp+=400;
                    }else if(temp.asteroidsflag==1){
                        //System.out.println("xiao");
                        score += 500;
						// 1) 更新本地排行榜数据
						updateScore(playerName, score);
						// 2) 通知其他客户端同步本地分数
						Client.sendScore(score);

                        scoretemp+=500;
                    }
                }
            }
            if (index1 != -1) {
                Bullet temp = BulletArray[index1];
                BulletArray[index1] = BulletArray[BulletArray.length - 1];
                BulletArray[BulletArray.length - 1] = temp;
                BulletArray = Arrays.copyOf(BulletArray, BulletArray.length - 1);
            } else if (index2 != -1 && index1 == -1) {
                Bullet temp = BulletArray[index2];
                BulletArray[index2] = BulletArray[BulletArray.length - 1];
                BulletArray[BulletArray.length - 1] = temp;
                BulletArray = Arrays.copyOf(BulletArray, BulletArray.length - 1);
            }
        }



        for(int i=0;i<BulletArray.length;i++) {
            if (BulletArray[i].count < 850) {
                BulletArray[i].fly();
            } else {
                Bullet temp = BulletArray[i];
                BulletArray[i] = BulletArray[BulletArray.length - 1];
                BulletArray[BulletArray.length - 1] = temp;
                BulletArray = Arrays.copyOf(BulletArray, BulletArray.length - 1);
                //i--;
            }
        }
    }

    public void UFOfly() {//UFO飞行
        for(int i=0;i<UFOArray.length;i++) {
            ufobullettime++;
            if (ufobullettime % 40 == 0) {
                UFOBulletArray = Arrays.copyOf(UFOBulletArray, UFOBulletArray.length + 1);
                UFOBulletArray[UFOBulletArray.length-1]=new Bullet(UFOArray[i].cx,UFOArray[i].cy,ufoflag);
                ufoflag = (ufoflag + 2) % 12;
            }
            if (UFOArray[i].hit(new Polygon(new int[]{(int) me.x1[0], (int) me.x1[1], (int) me.x1[2]}, new int[]{(int) me.y1[0], (int) me.y1[1], (int) me.y1[2]}, 3))) {
                UFOArray[i].fly();
            } else {
                UFO temp=UFOArray[i];
                UFOArray[i]=UFOArray[UFOArray.length-1];
                UFOArray[UFOArray.length-1]=temp;
                UFOArray = Arrays.copyOf(UFOArray, UFOArray.length - 1);
                for(int m=0;m<4;m++) {
                    ballArray = Arrays.copyOf(ballArray, ballArray.length + 1);
                    ballArray[ballArray.length - 1] = new Balldisappear(temp.cx, temp.cy);
                }
                for(int m=0;m<4;m++) {
                    ballArray = Arrays.copyOf(ballArray, ballArray.length + 1);
                    ballArray[ballArray.length - 1] = new Balldisappear(me.cx, me.cy);
                    ballArray[ballArray.length - 1].flag=1;
                    ballArray[ballArray.length - 1].linepoint();
                }
                // i--;
                life--;
                me=new Me();
                mehuanum=0;
                mehua();
                me.wudiflag=1;
            }
        }
    }
    public void ufobulletfly() {//ufo子弹飞行
        for(int i=0;i<UFOBulletArray.length;i++){
            if (UFOBulletArray[i].hit(new Polygon(new int[]{(int) me.x1[0], (int) me.x1[1], (int) me.x1[2]}, new int[]{(int) me.y1[0], (int) me.y1[1], (int) me.y1[2]}, 3))&&UFOBulletArray[i].count<1000) {
                UFOBulletArray[i].fly();
            } else if(UFOBulletArray[i].count>=1000){
                Bullet temp=UFOBulletArray[i];
                UFOBulletArray[i]=UFOBulletArray[UFOBulletArray.length-1];
                UFOBulletArray[UFOBulletArray.length-1]=temp;
                UFOBulletArray = Arrays.copyOf(UFOBulletArray, UFOBulletArray.length - 1);
                //i--;
            }else{
                Bullet temp=UFOBulletArray[i];
                UFOBulletArray[i]=UFOBulletArray[UFOBulletArray.length-1];
                UFOBulletArray[UFOBulletArray.length-1]=temp;
                UFOBulletArray = Arrays.copyOf(UFOBulletArray, UFOBulletArray.length - 1);
                // i--;
                for(int m=0;m<4;m++) {
                    ballArray = Arrays.copyOf(ballArray, ballArray.length + 1);
                    ballArray[ballArray.length - 1] = new Balldisappear(me.cx, me.cy);
                    ballArray[ballArray.length - 1].flag=1;
                    ballArray[ballArray.length - 1].linepoint();
                }
                life--;
                me=new Me();
                mehuanum=0;
                mehua();
                me.wudiflag=1;
            }
        }
    }

    public void asteroidsfly(){
        for(int i=0;i<asteroidsArray.length;i++){
            if (asteroidsArray[i].hit(new Polygon(new int[]{(int)me.x1[0],(int)me.x1[1],(int)me.x1[2]},new int[]{(int)me.y1[0],(int)me.y1[1],(int)me.y1[2]},3))) {
                asteroidsArray[i].fly();
            } else {
                asteroids temp=asteroidsArray[i];
                asteroidsArray[i]=asteroidsArray[asteroidsArray.length-1];
                asteroidsArray[asteroidsArray.length-1]=temp;
                asteroidsArray = Arrays.copyOf(asteroidsArray, asteroidsArray.length - 1);
                // i--;
                for(int m=0;m<4;m++) {
                    ballArray = Arrays.copyOf(ballArray, ballArray.length + 1);
                    ballArray[ballArray.length - 1] = new Balldisappear(temp.cx, temp.cy);
                }
                for(int m=0;m<4;m++) {
                    ballArray = Arrays.copyOf(ballArray, ballArray.length + 1);
                    ballArray[ballArray.length - 1] = new Balldisappear(me.cx, me.cy);
                    ballArray[ballArray.length - 1].flag=1;
                    ballArray[ballArray.length - 1].linepoint();
                }
                if(temp.asteroidsflag==3) {
                    asteroidsArray = Arrays.copyOf(asteroidsArray, asteroidsArray.length + 1);
                    asteroidsArray[asteroidsArray.length - 1] = new asteroidsm(temp.cx, temp.cy);
                    asteroidsArray = Arrays.copyOf(asteroidsArray, asteroidsArray.length + 1);
                    asteroidsArray[asteroidsArray.length - 1] = new asteroidsm(temp.cx, temp.cy);
                }else if(temp.asteroidsflag==2){
                    asteroidsArray = Arrays.copyOf(asteroidsArray, asteroidsArray.length + 1);
                    asteroidsArray[asteroidsArray.length - 1] = new asteroidsl(temp.cx, temp.cy);
                    asteroidsArray = Arrays.copyOf(asteroidsArray, asteroidsArray.length + 1);
                    asteroidsArray[asteroidsArray.length - 1] = new asteroidsl(temp.cx, temp.cy);
                }else if(temp.asteroidsflag==1){

                }
                life--;
                me=new Me();
                mehuanum=0;
                mehua();
                me.wudiflag=1;
            }
        }

    }


    public void addasteroids(){
            if (addasteroidsflag == 0) {
                asteroidsArray = Arrays.copyOf(asteroidsArray, addasteroidsnum);
                for (int i = 0; i < asteroidsArray.length; i++) {
                    asteroidsArray[i] = new asteroids();
                }
                addasteroidsflag = 1;
                addasteroidsnum += 1;
            } else {
                asteroidstime++;
                if (asteroidstime % 1500 == 0) {
                    UFOArray = Arrays.copyOf(UFOArray, UFOArray.length + 1);
                    UFOArray[UFOArray.length - 1] = new UFO();
                    //System.out.println(asteroidsArray.length);
                    // for(int i=0;i<asteroidsArray.length;i++){
                    //System.out.print(asteroidsArray[i].asteroidsflag+" ");
                    //}System.out.println("");
                }
            }
            if (asteroidsArray.length == 0 && UFOArray.length == 0) {
                addasteroidsflag = 0;
            }
    }

    public void addlife(){
        if(scoretemp>=10000){
            life++;
            scoretemp-=10000;
        }
    }

    public void shanxian(){
        int shanxianflag=0;
        double mecx=0;
        double mecy=0;
        while(true){
            shanxianflag=0;
            mecx=Math.random()*1400;
            mecy=Math.random()*750;
            for(int i=0;i<asteroidsArray.length;i++){
                if(!asteroidsArray[i].hit((new Polygon(new int[]{(int)mecx,(int)mecx-15,(int)mecx+15},new int[]{(int)mecy-25,(int)mecy+15,(int)mecy+15},3)))){
                    shanxianflag=1;
                }
            }
            for(int i=0;i<UFOArray.length;i++){
                if(!UFOArray[i].hit((new Polygon(new int[]{(int)mecx,(int)mecx-15,(int)mecx+15},new int[]{(int)mecy-25,(int)mecy+15,(int)mecy+15},3)))){
                    shanxianflag=1;
                }
            }
            for(int i=0;i<UFOBulletArray.length;i++){
                if(!UFOBulletArray[i].hit((new Polygon(new int[]{(int)mecx,(int)mecx-15,(int)mecx+15},new int[]{(int)mecy-25,(int)mecy+15,(int)mecy+15},3)))){
                    shanxianflag=1;
                }
            }
            if(shanxianflag==0)break;
        }
        me=new Me();
        me.cx=mecx;
        me.cy=mecy;
        me.x1[0]=me.cx;me.x1[1]=me.cx-15;me.x1[2]=me.cx+15;
        me.y1[0]=me.cy-25;me.y1[1]=me.cy+15;me.y1[2]=me.cy+15;
    }
	
	/** 重新初始化遊戲狀態，供按 B 重玩時呼叫 */
	public void resetGame() {
		BulletArray = new Bullet[0];
		UFOArray = new UFO[0];
		UFOBulletArray = new Bullet[0];
		asteroidsArray = new asteroids[0];
		ballArray = new Balldisappear[0];
		me = new Me();
		score = 0; scoretemp = 0;
		ufobullettime = 0; asteroidstime = 0; ufoflag = 1; wudinum = 0;
		life = 3; addasteroidsflag = 0; addasteroidsnum = 1;
		yy = -Height; mehuanum = 0;
		repaint();
	}



    public void action() {
    // 執行畫布的定時更新與網路同步
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
            // 只有在遊戲開始且不在選單時才執行邏輯
            if (startflag == 1 && menuflag == 0) {
                if (me.wudiflag == 1) {
                    // 無敵狀態下只更新特效
                    wudinum++;
                    addasteroids();
                    for (asteroids a : asteroidsArray) a.fly();
                    for (UFO u : UFOArray) u.fly();
                    for (Bullet b : UFOBulletArray) b.fly();
                    for (Bullet b : BulletArray) b.fly();
                    for (Balldisappear bd : ballArray) bd.fly();
                    if (wudinum >= 120 && life > 0) {
                        me.wudiflag = 0;
                        wudinum = 0;
                    }
                } else {
                    // 正常遊戲邏輯
                    addlife();
                    addasteroids();
                    me.fly();               // 玩家飛船移動
                    BulletFly();            // 玩家子彈更新
                    ufobulletfly();         // UFO 子彈更新
                    UFOfly();               // UFO 更新
                    ballfly();              // 爆炸特效更新
                    asteroidsfly();         // 小行星更新

                    // 每次更新都同步位置給伺服器
                    Client.sendUpdate(me.getCenterX(), me.getCenterY());
                }
            }
            // 無論何種狀態都重繪，保證畫面流暢
            repaint();
        }
    }, 0, 40);  // 每 40ms 執行一次 (約 25 FPS)
}

}

