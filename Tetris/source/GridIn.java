


//�ڲ��߼���GridIn��

public class GridIn{
	
	int[] grid_oldx;
	int[] grid_oldy;//��¼����������һ������
	int[] grid_x;
	int[] grid_y;//��ǰ�����������ֵ��ȡ�������
	int grid_type;//��������7��:1-T,2-Z,3-Z',4-L,5-L',6-I,7-O
	int grid_angle;//������ת�Ƕ�����4��:1-0��,2-90��,3-180��,4-270��
	boolean can=true;//�Ƿ�ɲ���,���Ƿ�������䵽�ײ�,�򲻿ɲ���
	
	//******************************************************************
	public GridIn(){
		
		grid_oldx=new int[4];
		grid_oldy=new int[4];
		grid_x=new int[4];
		grid_y=new int[4];
		grid_type=1;
		grid_angle=1;
	}
	
	//*************************************************************************
	public boolean rotate(boolean[][] position){
		
		
		
		
		int init_rotatex[][][]={
			{{1,0,1,-1},  {1,0,-1,-1},  {-1,0,-1,1},  {-1,0,1,1}},  //1-T
			{{1,0,1,0},   {1,0,-1,-2},  {0,1,0,1},    {-2,-1,0,1}}, //2-Z	
			{{2,1,0,-1},  {0,-1,0,-1},  {-1,0,1,2},   {-1,0,-1,0}}, //3-Z'		    
			{{-1,0,1,0},  {1,0,-1,-2},  {1,0,-1,0},   {-1,0,1,2}},  //4-L
			{{2,1,0,-1},  {0,-1,0,1},   {-2,-1,0,1},  {0,1,0,-1}},  //5-L'
			{{-1,0,1,2},  {1,0,-1,-2},  {2,1,0,-1},   {-2,-1,0,1}}, //6-I
			{{0,1,0,-1},  {1,0,-1,0},   {0,-1,0,1},   {-1,0,1,0}}   //7-O
		};
		
		int init_rotatey[][][]={
			{{1,0,-1,-1}, {0,1,0,2},    {-2,-1,0,0},  {1,0,1,-1}},  //1-T
			{{1,0,-1,-2}, {0,1,0,1},    {-2,-1,0,1},  {1,0,1,0}},   //2-Z
			{{0,-1,0,-1}, {-1,0,1,2},   {-1,0,-1,0},  {2,1,0,-1}},  //3-Z'
			{{2,1,0,-1},  {0,-1,-2,-1}, {-1,0,1,2},   {-1,0,1,0}},  //4-L
			{{0,-1,0,1},  {-2,-1,0,1},  {1,2,1,0},    {1,0,-1,-2}}, //5-L' 
			{{3,2,1,0},   {0,-1,-2,-3}, {0,1,2,3},    {-3,-2,-1,0}},//6-I  
			{{1,0,-1,0},  {0,-1,0,1},   {-1,0,1,0},   {0,1,0,-1}}   //7-O 
		};
		
		int maxx,maxy,minx;
		int[] grid_tempx=new int[4];
		int[] grid_tempy=new int[4];;//�м����������
		
		for(int i=0;i<4;i++){//��ʼ��Ҫ��ת�Ժ��ֵ
			grid_tempx[i]=this.grid_x[i]+
			  init_rotatex[this.grid_type-1][this.grid_angle-1][i];
			grid_tempy[i]=this.grid_y[i]+
			  init_rotatey[this.grid_type-1][this.grid_angle-1][i];
		}
		
		maxx=10;maxy=24;minx=1;
		
		//�����ת���и��ӵ�x����С��1�����10,����Ӧ���ƻ�����
		for(int i=0;i<4;i++){
			if(grid_tempx[i]>maxx){
				maxx=grid_tempx[i];
			}
		}
		
		for(int i=0;i<4;i++){
			grid_tempx[i]=grid_tempx[i]-(maxx-10);
		}
		
		for(int i=0;i<4;i++){
			if(grid_tempx[i]<minx){
				minx=grid_tempx[i];
			}
		}
		
		for(int i=0;i<4;i++){
			grid_tempx[i]=grid_tempx[i]-(minx-1);
		}
		
		//�����ת���и����������20,����Ӧ����
		for(int i=0;i<4;i++){
			if(grid_tempy[i]>maxy){
				maxy=grid_tempy[i];
			}
		}
		
		for(int i=0;i<4;i++){
			grid_tempy[i]=grid_tempy[i]-(maxy-24);
		}
		
		for(int i=0;i<4;i++){//�����ת���з�������������λ�ó�ͻ,������ת
			if((position[grid_tempy[i]-1][grid_tempx[i]-1]==true)&&
			   ((grid_tempx[i]==grid_x[0])&&(grid_tempy[i]==grid_y[0])||
			    (grid_tempx[i]==grid_x[1])&&(grid_tempy[i]==grid_y[1])||
			    (grid_tempx[i]==grid_x[2])&&(grid_tempy[i]==grid_y[2])||
			    (grid_tempx[i]==grid_x[3])&&(grid_tempy[i]==grid_y[3]))
			    ==false){
			    	return false;
			    }
		}
		
		//�޸ķ������
		for(int i=0;i<4;i++){
			grid_oldx[i]=grid_x[i];
			grid_oldy[i]=grid_y[i];
			grid_x[i]=grid_tempx[i];
			grid_y[i]=grid_tempy[i];
		}
		
		grid_angle=grid_angle%4+1;
		
		for(int i=0;i<4;i++){//������תǰ��λ������
			if(grid_oldx[i]>=1&&grid_oldx[i]<=10&&
			   grid_oldy[i]>=1&&grid_oldy[i]<=24){
			   	 position[grid_oldy[i]-1][grid_oldx[i]-1]=false;
			   }
		}
		
		for(int i=0;i<4;i++){
			if(grid_x[i]>=1&&grid_x[i]<=10&&
			   grid_y[i]>=1&&grid_y[i]<=24){
			   	 position[grid_y[i]-1][grid_x[i]-1]=true;
			   }
		}
		
		return true;
	}//rotate()����
	
	
	//**********************************************************************
	public boolean moveLeft(boolean[][] position){//�����������һ�� 
		
		int[] grid_tempx=new int[4];
		int[] grid_tempy=new int[4];//���ƺ�Ĵ���ֵ
		
		for(int i=0;i<4;i++){
			grid_tempx[i]=grid_x[i]-1;
			grid_tempy[i]=grid_y[i];
		}
		
		for(int i=0;i<4;i++){//���ƺ���x����С��1,���账��
			if(grid_tempx[i]<1){
				return false;
			}
		}
		
		for(int i=0;i<4;i++){//������ƺ��з�������������λ�ó�ͻ,������ת
			if((position[grid_tempy[i]-1][grid_tempx[i]-1]==true)&&
			   ((grid_tempx[i]==grid_x[0])&&(grid_tempy[i]==grid_y[0])||
			    (grid_tempx[i]==grid_x[1])&&(grid_tempy[i]==grid_y[1])||
			    (grid_tempx[i]==grid_x[2])&&(grid_tempy[i]==grid_y[2])||
			    (grid_tempx[i]==grid_x[3])&&(grid_tempy[i]==grid_y[3]))
			    ==false){
			    	return false;
			    }
		}
		
		//�޸ķ������
		for(int i=0;i<4;i++){
			grid_oldx[i]=grid_x[i];
			grid_oldy[i]=grid_y[i];
			grid_x[i]=grid_tempx[i];
			grid_y[i]=grid_tempy[i];
		}	
		
		for(int i=0;i<4;i++){//��������ǰ��λ������
			if(grid_oldx[i]>=1&&grid_oldx[i]<=10&&
			   grid_oldy[i]>=1&&grid_oldy[i]<=24){
			   	 position[grid_oldy[i]-1][grid_oldx[i]-1]=false;
			   }
		}
		
		for(int i=0;i<4;i++){
			if(grid_x[i]>=1&&grid_x[i]<=10&&
			   grid_y[i]>=1&&grid_y[i]<=24){
			   	 position[grid_y[i]-1][grid_x[i]-1]=true;
			   }
		}
		
		return true;
	}//moveLeft()����
	
	//**********************************************************************
	public boolean moveRight(boolean[][] position){//�����������һ�� 
		
		int[] grid_tempx=new int[4];
		int[] grid_tempy=new int[4];//���ƺ�Ĵ���ֵ
		
		for(int i=0;i<4;i++){
			grid_tempx[i]=grid_x[i]+1;
			grid_tempy[i]=grid_y[i];
		}
		
		for(int i=0;i<4;i++){//���ƺ���x�������10,���账��
			if(grid_tempx[i]>10){
				return false;
			}
		}
		
		for(int i=0;i<4;i++){//������ƺ��з�������������λ�ó�ͻ,������ת
			if((position[grid_tempy[i]-1][grid_tempx[i]-1]==true)&&
			   ((grid_tempx[i]==grid_x[0])&&(grid_tempy[i]==grid_y[0])||
			    (grid_tempx[i]==grid_x[1])&&(grid_tempy[i]==grid_y[1])||
			    (grid_tempx[i]==grid_x[2])&&(grid_tempy[i]==grid_y[2])||
			    (grid_tempx[i]==grid_x[3])&&(grid_tempy[i]==grid_y[3]))
			    ==false){
			    	return false;
			    }
		}
		
		//�޸ķ������
		for(int i=0;i<4;i++){
			grid_oldx[i]=grid_x[i];
			grid_oldy[i]=grid_y[i];
			grid_x[i]=grid_tempx[i];
			grid_y[i]=grid_tempy[i];
		}	
		
		for(int i=0;i<4;i++){//��������ǰ��λ������
			if(grid_oldx[i]>=1&&grid_oldx[i]<=10&&
			   grid_oldy[i]>=1&&grid_oldy[i]<=24){
			   	 position[grid_oldy[i]-1][grid_oldx[i]-1]=false;
			   }
		}
		
		for(int i=0;i<4;i++){
			if(grid_x[i]>=1&&grid_x[i]<=10&&
			   grid_y[i]>=1&&grid_y[i]<=24){
			   	 position[grid_y[i]-1][grid_x[i]-1]=true;
			   }
		}
		
		return true;
	}//moveLeft()����
	
	//*****************************************************************
	public boolean moveDown(boolean[][] position){
		
		int[] grid_tempx=new int[4];
		int[] grid_tempy=new int[4];//���ƺ�Ĵ���ֵ
		
		for(int i=0;i<4;i++){
			grid_tempx[i]=grid_x[i];
			grid_tempy[i]=grid_y[i]+1;
		}
		
		for(int i=0;i<4;i++){//���ƺ���x����С��1,���账��
			if(grid_tempy[i]>24){
				return false;
			}
		}
		
		for(int i=0;i<4;i++){//������ƺ��з�������������λ�ó�ͻ,������ת
			if((position[grid_tempy[i]-1][grid_tempx[i]-1]==true)&&
			   ((grid_tempx[i]==grid_x[0])&&(grid_tempy[i]==grid_y[0])||
			    (grid_tempx[i]==grid_x[1])&&(grid_tempy[i]==grid_y[1])||
			    (grid_tempx[i]==grid_x[2])&&(grid_tempy[i]==grid_y[2])||
			    (grid_tempx[i]==grid_x[3])&&(grid_tempy[i]==grid_y[3]))
			    ==false){
			    	return false;
			    }
		}
		
		//�޸ķ������
		for(int i=0;i<4;i++){
			grid_oldx[i]=grid_x[i];
			grid_oldy[i]=grid_y[i];
			grid_x[i]=grid_tempx[i];
			grid_y[i]=grid_tempy[i];
		}	
		
		for(int i=0;i<4;i++){//��������ǰ��λ������
			if(grid_oldx[i]>=1&&grid_oldx[i]<=10&&
			   grid_oldy[i]>=1&&grid_oldy[i]<=24){
			   	 position[grid_oldy[i]-1][grid_oldx[i]-1]=false;
			   }
		}
		
		for(int i=0;i<4;i++){
			
			if(grid_x[i]>=1&&grid_x[i]<=10&&
			   grid_y[i]>=1&&grid_y[i]<=24){
			   	 position[grid_y[i]-1][grid_x[i]-1]=true;
			}
			  
		}
		
		return true;
	}//moveDown()����
}