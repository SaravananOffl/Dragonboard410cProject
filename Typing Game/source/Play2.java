
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.applet.*;
import java.net.URL;

class Syn{
	int date;
}
class GamePanel extends JPanel implements Runnable{
	static String str="synchronized";
	//Syn syn=new Syn();
	static int allCharCount;//��һ�������Frame����ʾ���ַ���
	static int MaxCharCount=5;//�����ַ�����
	static int MaxBallCount=3;//��Ļ�ǵ�����������
	static boolean GameOver=false;//��Ϸ�Ƿ����
	private int hasBallCount;//��Ļ�����ڲ�������
	private static int speed;//�ٶ�
	private static int baseSpeed;//�����ٶ�
	private static int start_pos;//��ʼλ��
	private static int stupSpeed=1;//�ٶ�����
	private static int maxSpeed=12;//����ٶ�
	private boolean isDown=false;//�Ƿ��������
	static Image img;
	static AudioClip mySong;
	static AudioClip chomp;//���α��Ե�����
	static AudioClip dangerMusic;//Σ����ʾ��
	static AudioClip gameOver;//��Ϸ������
	static int dangerDistance=100;//��ʾ����
	private boolean flag=false;//�Ƿ��ڿ���ʾ������
	private static int reshTime;//ˢ��ʱ��
	private char int_char;//�ַ�
	private int pos;//λ��
	private Thread thread=null;
	private  int showAllCount;//����ܹ���ʾ���ַ���
	private  int hitsCount;//��������ַ���
	boolean imageflag=false;
	private int this_width;
	private int this_heigth;
	private static Font this_font=new Font("Arial",Font.BOLD,20);
	public static Random random=new Random();
	public static void reSetStaticCount(){
		GamePanel.allCharCount=0;
		GamePanel.GameOver=false;
	}
	public static void setMusic(){
		img=Toolkit.getDefaultToolkit().getImage("explode.gif");
		try{
			Class this_class;
			this_class=Class.forName("GamePanel");
			URL this_url_over=this_class.getResource("gameover.wav");
			gameOver=Applet.newAudioClip(this_url_over);
			URL this_url=this_class.getResource("firework.au");
			mySong=Applet.newAudioClip(this_url);
			URL this_url_chomp=this_class.getResource("chomp.au");
			chomp=Applet.newAudioClip(this_url_chomp);
			URL this_url_danger=this_class.getResource("danger.au");
			dangerMusic=Applet.newAudioClip(this_url_danger);
		}catch(Exception e){
			e.printStackTrace();
		}	
	}
	public GamePanel(String title,int reshTime,int speed){
		super();
		this.setName(title);
		this.reshTime=reshTime*100;
		this.speed=speed;
		this.baseSpeed=speed;
		this.hasBallCount=GamePanel.MaxBallCount;
		start_pos=5;
		this.setFont(GamePanel.this_font);
	}
	       
	public GamePanel(String title){
		this(title,1,2);
	}
	public void run(){
		while(true){
				System.out.println(Thread.currentThread().getName()+"������");
				this.setPostion();
				this.repaint();
				try{
					Thread.sleep(reshTime);
				}
				catch(Exception e){
					e.printStackTrace();
				}		
		}
	}
	public void Down(boolean isDown){
		this.isDown=isDown;
	}
	public boolean getDown(){
		return this.isDown;
	}
	public void start(){
		if(thread!=null){
			return;
		}
		this.hitsCount=0;
		this.showAllCount=0;
		this.setRandomChar();
		thread=new Thread(this);
		thread.start();
		
	}
	public void resume() {
		if(thread!=null){
			//thread.resume();
		}
	}
	public void suspend(){
		if(thread!=null){
			//thread.suspend();
		}
		
	}
	public void stop(){
		if(thread!=null){
			this.int_char=32;//�ո�
			this.pos=0;
			this.hasBallCount=this.MaxBallCount;
			//thread.stop();
			thread=null;
			this.repaint();
			System.out.println("�̹߳ر�:");
			if(thread==null){
				System.out.println("thread=null");
			}
		}
	}
	public int getPos(){
		return this.pos;
	}
	public char getChar(){
		return this.int_char;
	}
	public boolean hasNomarl(){
		if(pos>=start_pos&&pos<=this.getHeight()){
			return true;
		}
		return false;
	}
	public static boolean addSpeed(int level){
		if((GamePanel.baseSpeed+GamePanel.stupSpeed*level)>GamePanel.maxSpeed){
			return false;
		}
		GamePanel.speed=GamePanel.baseSpeed+GamePanel.stupSpeed*level;
		return true;
	}
	public int getShowAllCount(){
		return this.showAllCount;
	}
	public int getHitsCount(){
		return this.hitsCount;
	}
	private void setPostion(){
		if(GamePanel.GameOver){
			this.Reset();//��Ϸ����
			return;
		}
		System.out.println("�ַ���:"+GamePanel.allCharCount);
		if(this.canDown()){
			pos=pos+speed;
			if(pos>=start_pos&&flag==false){
				this.showAllCount++;
				flag=true;
			}
			if(this.IsDanger()){
				chomp.play();
				if(--this.hasBallCount<1){
					GamePanel.GameOver=true;
				}
				this.Reset();
			}
		}
	}
	private boolean IsDanger(){
		if((pos>=(this.getHeight()-this_heigth*this.hasBallCount-10))&&flag==true){
			return true;
		}
		return false;
	}
	private boolean canDown(){
		synchronized(GamePanel.str){
			if(this.pos!=0&&!this.imageflag&&this.isDown){
				return true;
			}
			if(this.pos==0&&GamePanel.allCharCount<MaxCharCount&&this.isDown){
				GamePanel.allCharCount++;
				System.out.println("Char : "+String.valueOf((char)this.int_char)+" : pos"+this.pos+" allCharCount:"+GamePanel.allCharCount);
				return true;
			}
	
			return false;
		}
	}
	public void Reset(){
		if(GamePanel.allCharCount>0)
			GamePanel.allCharCount--;
		this.pos=0;
		flag=false;
		this.setRandomChar();
	   	//System.out.println("�ַ���:"+GamePanel.allCharCount);
	}
	private void setRandomChar(){
		int temp;
		while(true){
			temp=random.nextInt(255);
			if((48<=temp&&temp<=57)||(temp>=65&&temp<=90)||(temp>=97&&temp<=122)){
				this.int_char=(char)temp;
				break;
			}
		}
	}
	private void explode(Graphics g){
	
	}
	private void paintGlobule(Graphics g){
		
	}
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		if(this.hasNomarl()){
			g.setColor(new Color(random.nextInt(255),random.nextInt(255),random.nextInt(255)));
			if(this.imageflag){
				g.drawImage(img,(this.getWidth()/2-10),pos-10,20,20,null);
				mySong.play();
				this.hitsCount++;
				this.Reset();
				imageflag=false;
			}else{
				g.drawString(String.valueOf((char)this.int_char),this.getWidth()/2,pos);
			}
			
		}
		//������
		
		g.setColor(Color.GREEN);
		int this_x,this_y;
		this_width=this.getWidth()-10;
		this_heigth=15;
		this_x=(this.getWidth()-this_width)/2;
		for(int i=0;i<this.hasBallCount;i++){
			this_y=this.getHeight()-this_heigth*(i+1)-10;
			if((this_y-this.pos)<=GamePanel.dangerDistance){
				dangerMusic.play();
				g.setColor(new Color(random.nextInt(255),random.nextInt(255),random.nextInt(255)));
			}
			g.fill3DRect(this_x,this_y,this_width,this_heigth,true);
		}
		
		
			
	}
}
	
	
public class Play extends JFrame{
	Container c=null;
	GamePanel[] games;
	JLabel[] labels;
	JButton jbtn_start;
	JButton	jbtn_set;
	JButton jbtn_pause;
	JButton	jbtn_exit;
	private long startTime;//��ʼ��Ϸʱ��
	private long currentTime;//��ǰʱ��
	private URL this_url=null;//����������ʾ����ַ
	private AudioClip errSound=null;//����������ʾ��
	private AudioClip backSound=null;
	static AudioClip startMusic;//��Ϸ��ʼ��
	static AudioClip addLevel;//���������
	static AudioClip subLevel;//��ҽ�����
	int curLevel;//��ҵ�ǰ�ļ���
	int rate;	//����
	int time;	//ʱ��
	int mark;	//�Ƿ�
	int level;	//����
	int errCount;//����
	int baseMark=20;//ϵͳĬ��ֵ�������ڴ�ֵʱ�������ٶ�
	private boolean IsStarted=false;//�Ƿ�ʼ��Ϸ
	int showCount;	//��ʾ�ַ�����
	int attackCount;//��������
	Thread thread_start=null;//��ʼ�����߳�
	Thread thread_computer=null;//�����߳�
	Thread thread_gameover=null;	
	public Play(String title,int showCount){
		super(title);
		//super();
		this.showCount=showCount;
		//this.setSize(90*showCount,700);	
		Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		
		this.setSize(screenSize.width,screenSize.height-80);	
		
		Dimension frameSize=this.getSize();
		
		
        // �����������Ϊ͸������LayeredPane����еı�����ʾ������  
		//contain.setLayout(new FlowLayout()); 
		
		this.setLocation((screenSize.width-frameSize.width)/2,(screenSize.height-frameSize.height)/2);
		//this.setBackground(Color.BLACK);
		this.setResizable(false);
		this.Init();
		this.setVisible(true);
		try{
			Class this_class=Class.forName("Play");
			this_url=this_class.getResource("errsound.au");
			errSound=Applet.newAudioClip(this_url);
			this_url=this_class.getResource("backsound.wav");
			backSound=Applet.newAudioClip(this_url);
			this_url=this_class.getResource("start.wav");
			startMusic=Applet.newAudioClip(this_url);
			this_url=this_class.getResource("addlevel.wav");
			addLevel=Applet.newAudioClip(this_url);
			this_url=this_class.getResource("sublevel.wav");
			subLevel=Applet.newAudioClip(this_url);
		}catch(Exception e){
		}
		Play.startMusic.play();
		
	}
	private class StartThread extends Thread{
		public void run(){	
			if(!IsStarted){
				Play.this.IsStarted=true;
				started();
			}
		}
	}
	private class ComputerThread extends Thread{
		public void run(){
			while(true){//��������ٶ�,ʱ��,����
				try{
					this.sleep(1000);
				}catch(Exception e){
					
				}
				Play.this.computer();
			}
		}
	}
	public void threadStart(){
		Play.startMusic.stop();
		thread_start=new StartThread();
		//System.out.println("StartThread-------------------------");
		this.backSound.loop();//���ű�������
		thread_start.start();
	}
	public synchronized void resume(){
		for(int i=0;i<showCount;i++){
			if(games[i]!=null){
				games[i].resume();
			}
		}
		
	}
	public synchronized void stop(){
		this.IsStarted=!this.IsStarted;
		if(thread_start!=null){
			this.backSound.stop();
			//thread_start.stop();
			//thread_computer.stop();
			thread_start=null;
			thread_computer=null;
		}
		for(int i=0;i<showCount;i++){
			if(games[i]!=null){
				games[i].Down(false);
				System.out.println("�̹߳ر�:"+i);
				games[i].stop();
			}
		}
		GamePanel.reSetStaticCount();
		
	}
	public synchronized void suspend(){
		for(int i=0;i<showCount;i++){
			if(games[i]!=null){
				games[i].suspend();
			}
		}
	}
	private void Init(){
		//��ʼ��
		GamePanel.setMusic();
		games=new GamePanel[showCount];
		for(int i=0;i<showCount;i++){ 
			games[i]=new GamePanel("�߳�:"+i);
		}
		       
		c=this.getContentPane();
		c.setLayout(new BorderLayout());
		((JPanel) c).setOpaque(false); 
		//c.setLayout(new FlowLayout());
		int i=0;
		labels=new JLabel[10];
		labels[i++]=new JLabel("�ٶ�:");
		labels[i++]=new JLabel("0/0  ");
		labels[i++]=new JLabel("����:");
		labels[i++]=new JLabel("0    ");
		labels[i++]=new JLabel("ʱ��:");
		labels[i++]=new JLabel("0    ");
		labels[i++]=new JLabel("����:");
		labels[i++]=new JLabel("0    ");
		labels[i++]=new JLabel("����:");
		labels[i++]=new JLabel("0    ");
		JPanel south_Panel=new JPanel();
		JPanel center_Panel=new JPanel();
		for(int j=0;j<labels.length;j++){
			south_Panel.add(labels[j]);
		}
		i=0;
		jbtn_start=new JButton("��ʼ");
		jbtn_set=new JButton("����");
		jbtn_pause=new JButton("��ͣ");
		jbtn_exit=new JButton("�˳�");
		south_Panel.add(jbtn_start);
		south_Panel.add(jbtn_set);
		south_Panel.add(jbtn_pause);
		south_Panel.add(jbtn_exit);
		c.add(south_Panel,BorderLayout.SOUTH);
		center_Panel.setLayout(new GridLayout(1,showCount));
		for(i=0;i<showCount;i++){
			center_Panel.add(games[i]);
		}
		c.add(center_Panel,BorderLayout.CENTER);
		OnClickAdapter onClickListener=new OnClickAdapter();
		jbtn_start.addActionListener(onClickListener);
		jbtn_set.addActionListener(onClickListener);
		jbtn_pause.addActionListener(onClickListener);
		jbtn_exit.addActionListener(onClickListener);	
		this.addKeyListener(new KeyCheckAdaoter());
		//this.enableEvents(AWTEvent.WINDOW_EVENT_MASK);
	}
	  //Overridden so we can exit when window is closed
//	protected void processWindowEvent(WindowEvent e) {
	//   super.processWindowEvent(e);
	 //  if (e.getID() == WindowEvent.WINDOW_CLOSING) {
	   //		System.exit(0);
	 //  }
	//}
	public void started(){
		//System.out.println("started-------------------------");
		this.curLevel=1;
		thread_gameover=null;
		startTime=System.currentTimeMillis();//��ȡ��Ϸ��ʼʱ��
		thread_computer=new ComputerThread();
		thread_computer.start();
		int j=0;
		int[] arrposflag=new int[showCount];	
		for(int i=0;i<showCount;i++){
			arrposflag[i]=0;//��ʼ�����߳�
			games[i].start();
		
			//System.out.println("showCount-------------------------"+String.valueOf(i));
		}
		GamePanel temp=null;
		for(int i=0;i<showCount;i++){
			while(true){
				j=GamePanel.random.nextInt(showCount);
				if(arrposflag[j]==0){
					arrposflag[j]=1;
					if(temp!=null){
						try{
							Thread.sleep(1500);
						}catch(Exception e){
						}	
					}
					//System.out.println("games[j]-------------------------"+games[j]);
					temp=games[j];
					games[j].Down(true);
					break;	
				}
			}
		}
	}
	private class GameOverThread extends Thread{
		public void run(){
			Play.this.backSound.stop();
			GamePanel.gameOver.play();
			Play.this.stop();
			jbtn_start.setText("��ʼ");
			JDialog dialog=new JDialog();
			dialog.setSize(200,100);
			dialog.setLocation((Play.this.getWidth()-dialog.getWidth())/2,
				(Play.this.getHeight()-dialog.getHeight())/2);
			dialog.setVisible(true);
		}
	}
	private void computer(){
		//�����ٶ�
		if(GamePanel.GameOver){
			if(thread_gameover==null){
				thread_gameover=new GameOverThread();
				thread_gameover.start();
			}
			return;
		}
		int this_rate=0;
		int this_hits=0;
		for(int i=0;i<showCount;i++){
			this_rate+=games[i].getShowAllCount();
			this_hits+=games[i].getHitsCount();
		}
		this.labels[1].setText(Integer.toString(this_hits)+"/"+Integer.toString(this_rate));
		//����ʱ��
		
		this.currentTime=System.currentTimeMillis();
		int thisTime=(int)((this.currentTime-this.startTime)/1000);
		this.labels[5].setText(Integer.toString(thisTime)+"��");
		
		//�������
		this.labels[3].setText(Integer.toString(this.errCount));
		
		//�������
		this.mark=this_hits*60/thisTime;
		this.labels[7].setText(Integer.toString(this.mark));
		
		//���㼶��
		//System.out.println("���㼶��");
		this.level=(this.mark-this.baseMark)/10;//10���ַ�Ϊһ��
		if(this.level<=0||this_hits<this.baseMark){
			this.level=1;
		}
		if(this.level>this.curLevel){
			Play.addLevel.play();
		}else if(this.level<this.curLevel){
			Play.subLevel.play();
		}
		this.curLevel=this.level;
		GamePanel.addSpeed(this.level);
		this.labels[9].setText(Integer.toString(this.level));
	}
	private void reComputer(){
		this.labels[1].setText("0/0  ");
		this.labels[3].setText("0    ");
		this.labels[5].setText("0    ");
		this.labels[7].setText("0    ");
		this.labels[9].setText("0    ");
	}
	private class KeyCheckAdaoter extends KeyAdapter{
		public void keyTyped(KeyEvent e){
			char keychar;
			keychar=e.getKeyChar();
			for(int i=0;i<showCount;i++){
				if (keychar==games[i].getChar()&&games[i].hasNomarl()){
					games[i].imageflag=true;
					return;
				}
			}
			//20160930
			Play.this.errSound.play();
			Play.this.errCount++;//����İ���
		}	
	}
	private class OnClickAdapter implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if(e.getSource()==jbtn_start){
				if(jbtn_start.getText().equals("��ʼ")){
					jbtn_start.setText("ֹͣ");
					Play.this.threadStart();
				}else{
					jbtn_start.setText("��ʼ");
					Play.this.stop();
				}
				Play.this.requestFocus(true);
			}else if(e.getSource()==jbtn_set){
				Play.this.requestFocus(true);
			}else if(e.getSource()==jbtn_pause){
				if(jbtn_pause.getText().equals("��ͣ")){
					jbtn_pause.setText("�ָ�");
					Play.this.suspend();
				}else{
					jbtn_pause.setText("��ͣ");
					Play.this.resume();
				}
				
				Play.this.requestFocus(true);
			}else if(e.getSource()==jbtn_exit){
				Play.this.requestFocus(true);
				System.exit(0);
			}
		}	
	}
	public static void main(String [] args){
		Play this_play=new Play("Qualcomm key Game-Dragonboard 410c:V1.0",12);
	}
}