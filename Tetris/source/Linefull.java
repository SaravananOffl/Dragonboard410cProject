


//������
public class Linefull{
	
	boolean[] lineFullRow;//��¼�������ڵ�����
	int full;//���е�����
	
	public Linefull(){
		
		full=0;
		lineFullRow=new boolean[24];
		for(int i=0;i<24;i++){
			lineFullRow[i]=false;
		}
	}
	
	public void setFullRow(int row){
		
		if(row>=0&&row<24){
			this.lineFullRow[row]=true;
		}
	}
	
	public void freshLine(){//ˢ�²���ʼ������
	
	    full=0;
		for(int i=0;i<24;i++){
			lineFullRow[i]=false;
		}
	}
}