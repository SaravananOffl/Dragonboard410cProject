//1.дһ�� ������Ϸ
//Ҫ��
//(1)��swing��awtʵ��GUI
//(2)���߳�
//(3)�¼�ģ�� 
//(4)����Ҫͬʱ����10�������ĸ���Ӽ������룬����ɹ���ӷ֣������򲻼Ƿ�


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Toolkit.*;

public class MyPritGame
{
	 
	public static void main(String[] args)
	{
		MyFrame mf=new MyFrame();
	//	mf.requestFocus();
		mf.setVisible(true);
	}
}

class MyFrame extends JFrame 
{
	MyPanel my[]=new MyPanel[10];
	Toolkit kit=Toolkit.getDefaultToolkit();
	JPanel jp=new JPanel();

	MySecondPanel mysp=new MySecondPanel(this);
	private int mark;//����
	public MyFrame()
	{
		setTitle("������Ϸ");
		setSize(400,400);
		this.setLocation(200,100);
		this.setResizable(false);
		Image img=kit.getImage("icon7.gif");
		this.setIconImage(img);
		mark=0;
		
	//this.getContentPane().add(jb1,BorderLayout.SOUTH);
	//this.getContentPane().setLayout(new GridLayout(1,10));
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(jp);
		
		this.getContentPane().add(mysp,BorderLayout.SOUTH);
		jp.setLayout(new GridLayout(1,10));
		for(int i=0;i<10;i++)
		{
			my[i]=new MyPanel();
			jp.add(my[i]);
			my[i].setBackground(Color.white);
		}
	
		this.addKeyListener(new KeyAdapter()
		{
			public void keyTyped(KeyEvent k)
			{
				char getchar=k.getKeyChar();
				for(int i=0;i<10;i++)
				{
					if(String.valueOf(getchar).equals(my[i].ch))
					{
						mark+=1;
						mysp.jt.requestFocus();
					//	mysp.jt.setText("");
						mysp.jt.setText(String.valueOf(mark));
						requestFocus();
					//	System.out.println(mark);
						my[i].ch="";
						my[i].y=10;
						my[i].ch=my[i].s[(int)(Math.random()*26)];
						my[i].repaint();
					}
					
				}
				
			}
		});
	}
	
}

class MyPanel extends  JPanel implements Runnable
{
	int y;
	Thread t;
	
	String ch;
	int num;
	String[] s={"a","b","c","d","e","f","g","h","i","j",
				"k","l","m","n","o","p","q","r","s","t",
				"u","v","w","x","y","z"};
	Color[] c={Color.black,Color.blue,Color.cyan,
				Color.green,Color.darkGray,Color.magenta,
				Color.orange,Color.pink,Color.red,Color.yellow};
	public MyPanel()
	{
		ch=s[(int)(Math.random()*26)];
		num=(int)Math.random()*10;
		t=new Thread(this);
		y=10;
		t.start();
	}
	public void run()
	{
		while(true)
		{
			if(MySecondPanel.flag)
			{
				try
				{
					Thread.sleep(1000);
				}catch(InterruptedException e){}
				repaint();
			}
		}
	}
	public void paint(Graphics g)
	{  
		int num=(int)(Math.random()*10);
		super.paintComponent(g);
		g.setColor(c[num]);
		g.drawString(ch,10,y);
		y=y+10+num;
		if(y>this.getSize().getHeight())
		{
			y=10;
			ch=s[(int)(Math.random()*26)];
		}
	}
}

class MySecondPanel extends JPanel
{
	JButton jb1=new JButton("��ʼ");
	JButton jb2=new JButton("ȡ��");
	JLabel jl=new JLabel("������");
	JTextField jt=new JTextField(5);
	
	static boolean flag=false;
	MyFrame mm;
	public MySecondPanel(MyFrame m)
	{
		this.add(jb1);
		this.add(jb2);
		this.add(jl);
		this.add(jt);
		mm=m;
		jt.setText("0");
		jb1.addActionListener(new MyEvent());
		jb2.addActionListener(new MyEvent());
	}
	private class MyEvent implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if(e.getActionCommand()=="ȡ��")
			{
				System.exit(0);
			}
			try
			{
				mm.requestFocus();
			}catch(NullPointerException n){}
			flag=!flag;
			
			if(flag)
				jb1.setText("��ͣ");
			else
				jb1.setText("��ʼ");
		}
	}
}
