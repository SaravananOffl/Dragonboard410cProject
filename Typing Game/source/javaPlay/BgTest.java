import java.awt.Container;  
import java.awt.FlowLayout;  
  
import javax.swing.ImageIcon;  
import javax.swing.JButton;  
import javax.swing.JFrame;  
import javax.swing.JLabel;  
import javax.swing.JPanel;  
import javax.swing.JPasswordField;  
import javax.swing.JTextField;  
  
public class BgTest extends JFrame{  
      
    public BgTest(){  
        init();  
    }  
  
    private void init() {  
          
        JLabel lbl1 = new JLabel("�û�����");  
        JLabel lbl2 = new JLabel("�ܡ��룺");  
        JTextField txt = new JTextField(10);  
        JPasswordField pasw = new JPasswordField(10);  
        pasw.setEchoChar('*');  
        JButton btn1 = new JButton("��¼");  
        JButton btn2 = new JButton("ȡ��");  
          
        ImageIcon img = new ImageIcon("backimage.jpg");  
        //Ҫ���õı���ͼƬ  
        JLabel imgLabel = new JLabel(img);  
        //������ͼ���ڱ�ǩ�  
        this.getLayeredPane().add(imgLabel, new Integer(Integer.MIN_VALUE));  
        //��������ǩ��ӵ�jfram��LayeredPane����  
        imgLabel.setBounds(0, 0, img.getIconWidth(), img.getIconHeight());  
        // ���ñ�����ǩ��λ��  
        Container contain = this.getContentPane();  
        ((JPanel) contain).setOpaque(false);   
        // �����������Ϊ͸������LayeredPane����еı�����ʾ������  
          
        contain.setLayout(new FlowLayout());  
        contain.add(lbl1);  
        contain.add(txt);  
        contain.add(lbl2);  
        contain.add(pasw);  
        contain.add(btn1);  
        contain.add(btn2);  
          
        this.setTitle("����ͼ����");  
        this.setSize(200, 200);//���ô����С  
        this.setLocation(600, 300);  
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
        this.setResizable(false);  
        this.setVisible(true);  
    }  
  
    public static void main(String[] args) {  
          
        new BgTest();   
    }  
}  