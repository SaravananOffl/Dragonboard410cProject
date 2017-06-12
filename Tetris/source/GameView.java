


//�û����� GameView
import java.awt.*;
import java.applet.*;
import java.awt.event.*;

public class GameView extends Applet implements KeyListener,Runnable{
	
	Panel panel;
	Panel panel2;
	GridIn gridIn;//�ڲ������߼���
	GridIn gridIn2=null;//�ڲ������߼���,��ʾ��
	Cgame cgame;//���������
	Linefull line;//������
	GridBlock[][] gridBlock;//20*10ƽ�̵Ŀ�Ƭ����С��������
	GridBlock[][] gridBlock2;//4*4ƽ�̵Ŀ�Ƭ����С��������,��ʾ��
	Thread thread;//�߳�,������Ϸ����
	int delay=500;//�����߳�ͣ��ʱ��
	int su=500;//������Ϸ�ٶ�
	int fang=0;//�����־
	int gameInt=1;//��¼��Ϸ�ȼ�
	String gameMessage="ף���ɹ���";	
	boolean diFlag=false;//������鵽��ײ�ʱ���ƶ���,
	               //�˷��黹������,��ת�ƶ���,diFlag=true;
	int count=0;//��¼����
	int sp=1;//��¼�ȼ��������
	
	Label message1;//��ʾ��ǰ��Ϸ�ȼ�****************************
	Label message2;//��ʾ��ǰ��Ϣ
	TextField text;//��ʾ����
	boolean stop=false;//������󵽴�ײ�ʱ��true
	int m=0;
	Button button;
	
	int mm=0;//������־
	private AudioClip sound1;
	private AudioClip sound2;
    private AudioClip sound3;
    private AudioClip sound4;
	
	public void init(){
		
		sound1=getAudioClip(getDocumentBase(),"�����ļ�/SOUND8.WAV");	//��ת,����������
		sound2=getAudioClip(getDocumentBase(),"�����ļ�/SOUND8.WAV");  //ʧ��ʱ
		sound3=getAudioClip(getDocumentBase(),"�����ļ�/SOUND104.WAV");//����ײ�
		sound4=getAudioClip(getDocumentBase(),"�����ļ�/SOUND53.WAV"); //�÷�
		
		this.setLayout(null);
		cgame=new Cgame();  
		line=new Linefull();
		text=new TextField("�÷�:"+count);
		
		message1=new Label();
		this.add(message1);
		message1.setBounds(260,150,100,20);
		
		message2=new Label();
		this.add(message2);
		message2.setBounds(260,170,100,20);
		
		this.add(text);
		text.setBounds(50,10,80,22);
		text.setBackground(Color.YELLOW);

		panel=new Panel();	
		this.add(panel);
		panel.setBounds(50,50,200,400);
		panel.setLayout(new GridLayout(20,10,0,0));
		
		panel2=new Panel();	
		this.add(panel2);
		panel2.setBounds(260,50,80,80);
		panel2.setLayout(new GridLayout(4,4,0,0));
		
		//**************************************
	
		gridBlock=new GridBlock[20][10];
		
		for(int i=0;i<20;i++){
			for(int j=0;j<10;j++){
				gridBlock[i][j]=new GridBlock();
				panel.add(gridBlock[i][j]);
				gridBlock[i][j].showBlockLabel();
			}
		}
		
		gridBlock2=new GridBlock[4][4];//��ʾ��
		
		for(int i=0;i<4;i++){
			for(int j=0;j<4;j++){
				gridBlock2[i][j]=new GridBlock();
				panel2.add(gridBlock2[i][j]);
				gridBlock2[i][j].showBlockLabel();
			}
		}
		
		button=new Button("���¿�ʼ");
		this.add(button);
		button.setBackground(Color.MAGENTA);
		button.setBounds(200,5,80,30);
		button.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent event){
					initGame();
                }

			}
		);
		
		button.addKeyListener(this);
	    button.requestFocus();
		
	}
	
	public void initGame(){//�����Ի���
	    for(int i=0;i<24;i++){
	         for(int j=0;j<10;j++){
   	             cgame.position[i][j]=false;
	         }
        }
	    gridIn=cgame.selectGrid();
	    cgame.reset_position(gridIn);
	    cgame.reset_position2(gridIn2);
	    count=0;
	  
	}
	
	public void soundStart(){
		
		if(mm==1){//��ת
			mm=0;
			sound1.play();
		}
		else if(mm==2){//����������
			mm=0;
			sound2.play();
		}
		else if(mm==3){//����ײ�
			mm=0;
			sound3.play();
		}
		else if(mm==4){//�÷�
			mm=0;
			sound4.play();
		}
		else{
			mm=0;
		}
		
	}
	
	public void start(){
		if(thread==null)	
		    thread=new Thread(this);	
		thread.start();
	}
	
	public void stop(){
		thread=null;//�̹߳���
	}
	
	
	
	public void paint(Graphics g){
		button.requestFocus();
		g.drawRect(48,48,203,403);
		text.setText("�÷�:"+count*200);
		message1.setText("��Ϸ�ȼ�Ϊ��"+gameInt);
		message2.setText("��Ϸ�ٶ�Ϊ"+(550-delay)/5);//****************************
	}
	
	public void run(){
		while(thread!=null){
			repaint();
			try{
				Thread.sleep(delay);
			}
			catch(InterruptedException e){}
			
			
			for(int i=0;i<20;i++){
				for(int j=0;j<10;j++){
					if(cgame.position[i+4][j]==true){
						gridBlock[i][j].showBlockButton();
					}
					else{
						gridBlock[i][j].showBlockLabel();
					}
				}
			}
			
	
			if(stop==false&&diFlag==false){
				
				count=count+line.full;//�ӷ�	
				if(gridIn2!=null){
					gridIn=gridIn2;
					
				}
				else{		
					gridIn=cgame.selectGrid();//�õ��·������
					
				}
				gridIn2=cgame.selectGrid();//�õ��·������
				cgame.reset_position(gridIn);
				cgame.reset_position2(gridIn2);
			}
			
	        line.freshLine();//ˢ������
			for(int i=0;i<4;i++){
				for(int j=0;j<4;j++){
					if(cgame.position2[i][j]==true){
						gridBlock2[i][j].showBlockButton();
					}
					else{
						gridBlock2[i][j].showBlockLabel();
					}
				}
			}
			
			stop=gridIn.moveDown(cgame.position);//�����ƶ��ɹ�����true
			cgame.setFullLine(line);//��������
			
			if(stop==false){//����ײ�
				
				mm=3;//���ŵ���ײ�������
				this.soundStart();
				
				if(cgame.freshGame(line)==true){//ɾ�����гɹ�
				    mm=4;
				    this.soundStart();
				}
			    
			    diFlag=false;	
			}	
			    
			if(stop==false&&cgame.isGameFail()==true){
			    this.initGame();
			}  //�����Ϸʧ��
	        
	        //�˴�������Ϸ�ȼ�����Ϸ��������ٶ�
	        if(count/25>sp){
	            sp=sp+1;
	        	count=count;
	        	if(su<=50){
	        		su=50;
	        	}
	        	else{
	        		su=su-50;
	        	}
	        	
	        	delay=su;
	        	gameInt=gameInt+1;	
	        }
	               
		}//while
		
	}//run
	
	public void keyPressed(KeyEvent e){
		
		mm=2;
		this.soundStart();	
		if(e.getKeyCode()==KeyEvent.VK_UP||e.getKeyCode()==KeyEvent.VK_W){
			//���ϣ��������ת
			gridIn.rotate(cgame.position);
		}
		else if(e.getKeyCode()==KeyEvent.VK_LEFT||e.getKeyCode()==KeyEvent.VK_A){
			//�������������
			gridIn.moveLeft(cgame.position);
			
			if(stop==false){
			    diFlag=true;	
			}	
		}
		else if(e.getKeyCode()==KeyEvent.VK_RIGHT||e.getKeyCode()==KeyEvent.VK_D){
			//���ң����������
		    gridIn.moveRight(cgame.position);
		    if(stop==false){
			    diFlag=true;	
			}	
		}
		else if(e.getKeyCode()==KeyEvent.VK_DOWN||e.getKeyCode()==KeyEvent.VK_S){
			//���£�������������
		    delay=30;
		}
		else{}
	}
	  
	public void keyTyped(KeyEvent e){}
	public void keyReleased(KeyEvent e){
	    delay=su;
	    mm=0;
	}
}

