


//��GridIn��������,�ӹ�;������Ϸ�Ŀ�ʼ,����,�÷�
public class Cgame{
	
	boolean[][] position;//��ŷſ�λ�õ��������
	boolean[][] position2;//��ʾ�򷽿�����
	int[][][] init_selectx={//��ʼ������
	    	{{4,5,5,6},  {5,5,6,5},  {6,5,5,4},  {5,5,4,5}},  //1-T
	    	{{4,5,5,6},  {5,5,6,6},  {6,5,5,4},  {6,6,5,5}},  //1-Z
	    	{{4,5,5,6},  {6,6,5,5},  {6,5,5,4},  {5,5,6,6}},  //1-Z'
	    	{{5,5,5,6},  {4,5,6,6},  {6,6,6,5},  {6,5,4,4}},  //1-L
	    	{{4,5,5,5},  {6,6,5,4},  {6,5,5,5},  {4,4,5,6}},  //1-L'
	    	{{5,5,5,5},  {4,5,6,7},  {5,5,5,5},  {7,6,5,4}},  //1-I  
	    	{{5,5,6,6},  {5,6,6,5},  {6,6,5,5},  {6,5,5,4}}   //1-O
	    };
	    
    int[][][] init_selecty={
	    	{{3,3,4,3},  {4,3,3,2},  {4,4,3,4},  {2,3,3,4}},  //1-T
	    	{{3,3,4,4},  {4,3,3,2},  {4,4,3,3},  {2,3,3,4}},  //1-Z
	    	{{4,4,3,3},  {4,3,3,2},  {3,3,4,4},  {2,3,3,4}},  //1-Z'
	    	{{2,3,4,4},  {4,4,4,3},  {4,3,2,2},  {3,3,3,4}},  //1-L
	    	{{4,4,3,2},  {4,3,3,3},  {2,2,3,4},  {3,4,4,4}},  //1-L'
	    	{{1,2,3,4},  {4,4,4,4},  {4,3,2,1},  {4,4,4,4}},  //1-I
	    	{{3,4,4,3},  {4,4,3,3},  {4,3,3,4},  {3,3,4,4}}   //1-O
	    };
	
	public Cgame(){
		
		position=new boolean[24][10];
		position2=new boolean[4][4];
	}
	
	public void startGame(GridIn grid){};//��ʼ��Ϸ
	public void endGame(GridIn grid){};//������Ϸ
	
	public boolean isGameFail(){//�����Ϸʧ��,����true
		
		boolean game=false;
		
		for(int j=0;j<10;j++){
			if(position[4][j]==true){
				game=true;
			}
		}
		
		return game;
	}
	
	public void setFullLine(Linefull line){//ÿ��ѭ������һ��,��������line������
	
	    int flag;//��¼ÿ��С�������
	    
	    for(int i=0;i<24;i++){
	    	flag=0;       
	    	for(int j=0;j<10;j++){
	    		if(position[i][j]==true){
	    			flag++;
	    		}
	    		
	    		if(flag>=10){//����Ϊ����
	    			line.setFullRow(i);
	    			line.full=0;
	    			for(int t=0;t<24;t++){
	    				if(line.lineFullRow[t]==true){
	    				line.full=line.full+1;//�ӷ�	
	    				}
	    			}
	    		}
	    	}
	    }
	}
	
	public boolean freshGame(Linefull line){//ɾ������,ˢ�½���
		    boolean boo=false;
			if(line.lineFullRow[0]==true){//����һ��Ϊ����
				for(int p1=0;p1<10;p1++){
					position[0][p1]=false;
				}
				
				boo=true;
			}
			else{//���в��ǵ�һ��
			    for(int t=1;t<24;t++){
			    	if(line.lineFullRow[t]==true){//����(t+1)��Ϊ����
			    	    for(int at=t;at>=1;at--){//ɾ����(t+1)��
			    	    	for(int ai=0;ai<10;ai++){
			    	    		position[at][ai]=position[at-1][ai];
			    	    	}
			    	    }
			    	    
			    	    boo=true;
			    	}
			    }
			}
			
			return boo;	
	}
	
	public GridIn selectGrid(){//���ѡȡ���󲢷ŵ�position[][]��
	    	    	    
	    GridIn grid=new GridIn();
 	    grid.grid_type=(int)(Math.random()*7)+1; //ȡ7����ʽ�е�һ��	  
  	    grid.grid_angle=(int)(Math.random()*4)+1; //ȡ4����ʽ�е�һ��    	    	   
	    return grid;
	}
	
	public void reset_position(GridIn grid){
		for(int i=0;i<4;i++){
	   	    grid.grid_x[i]=init_selectx[grid.grid_type-1][grid.grid_angle-1][i];
	    	grid.grid_y[i]=init_selecty[grid.grid_type-1][grid.grid_angle-1][i];
	    	position[grid.grid_y[i]-1][grid.grid_x[i]-1]=true;
	    }
	}
	
	public void reset_position2(GridIn grid){
		
		for(int i1=0;i1<4;i1++){
	    	for(int j=0;j<4;j++){
	    		position2[i1][j]=false;
	    	}
	   	}	
	   	for(int i=0;i<4;i++){
	    	grid.grid_x[i]=init_selectx[grid.grid_type-1][grid.grid_angle-1][i];
	    	grid.grid_y[i]=init_selecty[grid.grid_type-1][grid.grid_angle-1][i]; 
	    	position2[grid.grid_y[i]-1][grid.grid_x[i]-4]=true;
	    }	
	}
	
}

