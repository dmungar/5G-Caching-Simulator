//I7500 Advanced Wireless
//D2D Caching Simulator
//David Ungar and Rene Miduan

import java.util.Random;
import java.util.ArrayList;
import java.io.PrintWriter;

public class Cache{
	public static void main(String[] args){
		
		CaseA();
		CaseB(MAXB1);
		CaseB(MAXB2);
		CaseB(MAXB3);
		CaseB(MAXB4);
		CaseB(MAXB5);
		CaseB(MAXB6);
		CaseB(MAXB10);
		CaseC(MAXC1);
		CaseC(MAXC2);
		CaseC(MAXC3);
		CaseC(MAXC4);
		CaseC(MAXC5);
		CaseC(MAXC6);
		CaseC(MAXC10);
		
		
	}
	
	
	//No Caching whatsoever.
	public static void CaseA(){
		int NUM_USERS = 5;
		Random rand = new Random();
		
		int time = 0;
		int delaySoFar = 0;
		int traffic = 0;
		
		//Simulate repeated random file requests from all the users by looping through all the users hundredes of times and having 
		//each request a random file. After each request check if the file is cached somewhere and record the delay and traffic load accordingly.
		for (int i = 0; i < NUM_SIMS; i++){
			for (int j = 0; j < NUM_USERS; j++){
				time++;
				int fileRequest = rand.nextInt(NUM_FILES);
				delaySoFar += (rand.nextInt(6) + 45);//delay btw 45-50 ms
				traffic++;
			}
		}
		
		double avgDelay = (double)delaySoFar/(NUM_SIMS*NUM_USERS);
		double trafficLoad = (double)traffic/(NUM_SIMS*NUM_USERS);
		System.out.println("Case A has average delay of " + avgDelay + " ms and traffic load of " + trafficLoad);
		
	}
	
	
	
	//Caching everywhere (EPC, RAN and user device) without D2D. Takes as argument the max # of files to be cached per node.
	public static void CaseB(int Max){
		int NUM_USERS = Max/10;//number of user devices
		//ArrayList of File Objects that are cached at the EPC
		ArrayList<File> EPC = new ArrayList<File>();
		//ArrayList of File Objects that are cached at the RAN
		ArrayList<File> RAN = new ArrayList<File>();
		//Create a 2D ArrayList to hold an ArrayList of Files cached at every user
		ArrayList<ArrayList<File>> userArr = new ArrayList<ArrayList<File>>(NUM_USERS);
		for(int a = 0; a < NUM_USERS; a++){
			userArr.add(new ArrayList<File>());
		}
		Random rand = new Random();
		
		int time = 0;
		int delaySoFar = 0;
		int traffic = 0;
		
		//Simulate repeated random file requests from all the users by looping through all the users hundredes of times and having 
		//each one request a random file. After each request check if the file is cached somewhere and record the delay and traffic load accordingly.
		for (int i = 0; i < NUM_SIMS; i++){
			for (int j = 0; j < NUM_USERS; j++){
				time++;
				int fileRequest = rand.nextInt(NUM_FILES);
				int foundAt = -1;
				//First check if the file is in the user device's cache
				for (int k = 0; k < userArr.get(j).size(); k++){
					if (userArr.get(j).get(k).getFileNum() == fileRequest){
						foundAt = k;
						break;
					}	
				}
				//If it is found in device cache...
				if (foundAt != -1){
					userArr.get(j).get(foundAt).setTime(time);//update the latest access of this file
				}
				//If it is not in device cache...
				else{
					//Check if the file is in the RAN cache
					for (int k = 0; k < RAN.size(); k++){
						if (RAN.get(k).getFileNum() == fileRequest){
							foundAt = k;
							break;
						}	
					}
					//If it is found in RAN cache...
					if (foundAt != -1){
						delaySoFar += (rand.nextInt(6) + 5);//between 5-10 ms of delay
						RAN.get(foundAt).setTime(time);//update the latest access of this file
					}
					//If not in RAN cache either...
					else{
						//Check if the file is in the EPC cache
						for (int k = 0; k < EPC.size(); k++){
							if (EPC.get(k).getFileNum() == fileRequest){
								foundAt = k;
								break;
							}	
						}
						//If it is found in EPC cache...
						if (foundAt != -1){
							delaySoFar += (rand.nextInt(6) + 25);//between 25-30 ms of delay
							EPC.get(foundAt).setTime(time);//update the latest access of this file
						}
						//If not in EPC either, it is not in cache at all
						else{
							traffic++;//Had to access from core
							delaySoFar += (rand.nextInt(6) + 45);//between 45-50 ms of delay
							
							//Since not cached, let's cache it! But where?
							if(userArr.get(j).size() < MAX_USER){//user device memory not full
								userArr.get(j).add(new File(fileRequest, time));
							}
							else if(RAN.size() < Max){//RAN memory not full
								RAN.add(new File(fileRequest, time));
							}
							else if(EPC.size() < Max){//EPC memory not full
								EPC.add(new File(fileRequest, time));
							}
							else{//No empty slots so must use LRU to replace a cached file
								int lruDevice = time + 1;
								int deviceIndex = -1;
								int lruRAN = time + 1;
								int RANIndex = -1;
								int lruEPC = time + 1;
								int EPCIndex = -1;
								
								for (int k = 0; k < userArr.get(j).size(); k++){//Find the lru in device cache
									if (userArr.get(j).get(k).getTime() < lruDevice){
										lruDevice = userArr.get(j).get(k).getTime();
										deviceIndex = k;
									}	
								}
								for (int k = 0; k < RAN.size(); k++){//Find the lru in RAN cache
									if (RAN.get(k).getTime() < lruRAN){
										lruRAN = RAN.get(k).getTime();
										RANIndex = k;
									}	
								}
								for (int k = 0; k < EPC.size(); k++){//Find the lru in EPC cache
									if (EPC.get(k).getTime() < lruEPC){
										lruEPC = EPC.get(k).getTime();
										EPCIndex = k;
									}	
								}
								
								if (lruDevice < lruRAN && lruDevice < lruEPC){//device cache had the lru
									userArr.get(j).remove(deviceIndex);
									userArr.get(j).add(new File(fileRequest, time));
								}
								else if(lruRAN < lruDevice && lruRAN < lruEPC){//RAN had the lru
									RAN.remove(RANIndex);
									RAN.add(new File(fileRequest, time));
								}
								else{//EPC had the lru
									EPC.remove(EPCIndex);
									EPC.add(new File(fileRequest, time));
								}
							}
						}	
					}	
				}
			}
		}
		double avgDelay = (double)delaySoFar/(NUM_SIMS*NUM_USERS);
		double trafficLoad = (double)traffic/(NUM_SIMS*NUM_USERS);
		System.out.println("Case B with max cache of " + Max*2 + " out of " + NUM_FILES + " total files has average delay of " + avgDelay + " ms and traffic load of " + trafficLoad);
		
	}
	
	//Caching with D2D
	public static void CaseC(int Max){
		int NUM_USERS = Max/10;//number of user devices such that the devices cache is 1/3 of the total cache size
		//ArrayList of File Objects that are cached at the EPC
		ArrayList<File> EPC = new ArrayList<File>();
		//ArrayList of File Objects that are cached at the RAN
		ArrayList<File> RAN = new ArrayList<File>();
		//Create a 2D ArrayList to hold an ArrayList of Files cached at every user
		ArrayList<ArrayList<File>> userArr = new ArrayList<ArrayList<File>>(NUM_USERS);
		for(int a = 0; a < NUM_USERS; a++){
			userArr.add(new ArrayList<File>());
		}
		Random rand = new Random();
		
		int time = 0;
		int delaySoFar = 0;
		int traffic = 0;
		
		//Simulate repeated random file requests from all the users by looping through all the users hundredes of times and having 
		//each one request a random file. After each request check if the file is cached somewhere and record the delay and traffic load accordingly.
		for (int i = 0; i < NUM_SIMS; i++){
			for (int j = 0; j < NUM_USERS; j++){
				time++;
				int fileRequest = rand.nextInt(NUM_FILES);
				int foundAt = -1;
				int userIndex = -1;
				//First check if the file is in the device caches
				for(int l = 0; l < userArr.size(); l++){
					for (int k = 0; k < userArr.get(l).size(); k++){
						if (userArr.get(l).get(k).getFileNum() == fileRequest){
							foundAt = k;
							userIndex = l;
							//break;
						}	
					}
				}
				
				//If it is found in device cache...
				if (foundAt != -1){
					userArr.get(userIndex).get(foundAt).setTime(time);//update the latest access of this file
					if(userIndex != j){//add delay only if accessing another device's cache
						delaySoFar += (rand.nextInt(6) + 5);//between 5-10 ms of delay
					}
				}
				//If it is not in device caches...
				else{
					//Check if the file is in the RAN cache
					for (int k = 0; k < RAN.size(); k++){
						if (RAN.get(k).getFileNum() == fileRequest){
							foundAt = k;
							break;
						}	
					}
					//If it is found in RAN cache...
					if (foundAt != -1){
						delaySoFar += (rand.nextInt(6) + 5);//between 5-10 ms of delay
						RAN.get(foundAt).setTime(time);//update the latest access of this file
					}
					//If not in RAN cache either...
					else{
						//Check if the file is in the EPC cache
						for (int k = 0; k < EPC.size(); k++){
							if (EPC.get(k).getFileNum() == fileRequest){
								foundAt = k;
								break;
							}	
						}
						//If it is found in EPC cache...
						if (foundAt != -1){
							delaySoFar += (rand.nextInt(6) + 25);//between 25-30 ms of delay
							EPC.get(foundAt).setTime(time);//update the latest access of this file
						}
						//If not in EPC either, it is not in cache at all
						else{
							traffic++;//Had to access from core
							delaySoFar += (rand.nextInt(6) + 45);//between 45-50 ms of delay
							
							//Since not cached, let's cache it! But where?
							int notFullUser = -1;
							//Let's check if there's room in a device
							for(int x = 0; x < userArr.size(); x++){
								if(userArr.get(x).size() < MAX_USER){
									notFullUser = x;
								}
							}
							if(notFullUser != -1){//device memory not full
								userArr.get(notFullUser).add(new File(fileRequest, time));
							}
							else if(RAN.size() < Max){//RAN memory not full
								RAN.add(new File(fileRequest, time));
							}
							else if(EPC.size() < Max){//EPC memory not full
								EPC.add(new File(fileRequest, time));
							}
							else{//No empty slots so must use LRU to replace a cached file
								int lruDevice = time + 1;
								int whichUser = -1;
								int deviceIndex = -1;
								int lruRAN = time + 1;
								int RANIndex = -1;
								int lruEPC = time + 1;
								int EPCIndex = -1;
								
								for(int x = 0; x < userArr.size(); x++){//Find the lru in all device caches
									for (int k = 0; k < userArr.get(x).size(); k++){
										if (userArr.get(x).get(k).getTime() < lruDevice){
											lruDevice = userArr.get(x).get(k).getTime();
											whichUser = x;
											deviceIndex = k;
										}	
									}
								}
								for (int k = 0; k < RAN.size(); k++){//Find the lru in RAN cache
									if (RAN.get(k).getTime() < lruRAN){
										lruRAN = RAN.get(k).getTime();
										RANIndex = k;
									}	
								}
								for (int k = 0; k < EPC.size(); k++){//Find the lru in EPC cache
									if (EPC.get(k).getTime() < lruEPC){
										lruEPC = EPC.get(k).getTime();
										EPCIndex = k;
									}	
								}
								
								if (lruDevice < lruRAN && lruDevice < lruEPC){//device cache had the lru
									userArr.get(whichUser).remove(deviceIndex);
									userArr.get(whichUser).add(new File(fileRequest, time));
								}
								else if(lruRAN < lruDevice && lruRAN < lruEPC){//RAN had the lru
									RAN.remove(RANIndex);
									RAN.add(new File(fileRequest, time));
								}
								else{//EPC had the lru
									EPC.remove(EPCIndex);
									EPC.add(new File(fileRequest, time));
								}
							}
						}	
					}	
				}
			}
		}
		double avgDelay = (double)delaySoFar/(NUM_SIMS*NUM_USERS);
		double trafficLoad = (double)traffic/(NUM_SIMS*NUM_USERS);
		System.out.println("Case C with max cache of " + Max*3 + " out of " + NUM_FILES + " total files has average delay of " + avgDelay + " ms and traffic load of " + trafficLoad);
		
	}
	
	
	
	
	//Object to represent a file in cache
	//Contains the file name and the latest time it was accessed
	public static class File{
		private int fileNum;
		private int latestAccess;
		//Constructor
		public File(int fileNum, int time){
			this.fileNum = fileNum;
			latestAccess = time;
		}
		//Get the file number of this object
		public int getFileNum(){
			return fileNum;
		}
		//Get the most recent access time of this File Object
		public int getTime(){
			return latestAccess;
		}
		//Set (update) the most recent access time
		public void setTime(int newTime){
			latestAccess = newTime;
		}	
	}
	
	
	//Set the parameters for the simulation
	public static final int NUM_FILES = 3000;//# of files in universe of simulation
	public static final int NUM_SIMS = 5000;//# of times the simulation will loop through all of the users
	public static final int MAX_USER = 10;//Max # of files to fit in each device's memory
	
	//Max files to be cached per node
	//Simulations 1-6 represent cache sizes between 10 and 60 percent of the total # of files
	public static final int MAXB1 = (NUM_FILES/10)/2;//10% of the total files, evenly split between EPC and RAN
	public static final int MAXC1 = (NUM_FILES/10)/3;//10% of the total files, evenly split between EPC and RAN and user devices
	
	public static final int MAXB2 = ((NUM_FILES*2)/10)/2;//20% of the total files, evenly split between EPC and RAN
	public static final int MAXC2 = ((NUM_FILES*2)/10)/3;
	
	public static final int MAXB3 = ((NUM_FILES*3)/10)/2;//30% of the total files, evenly split between EPC and RAN
	public static final int MAXC3 = ((NUM_FILES*3)/10)/3;
	
	public static final int MAXB4 = ((NUM_FILES*4)/10)/2;//40% of the total files, evenly split between EPC and RAN
	public static final int MAXC4 = ((NUM_FILES*4)/10)/3;
	
	public static final int MAXB5 = ((NUM_FILES*5)/10)/2;//50% of the total files, evenly split between EPC and RAN
	public static final int MAXC5 = ((NUM_FILES*5)/10)/3;
	
	public static final int MAXB6 = ((NUM_FILES*6)/10)/2;//60% of the total files, evenly split between EPC and RAN
	public static final int MAXC6 = ((NUM_FILES*6)/10)/3;
	
	public static final int MAXB10 = NUM_FILES/2;//100% of the total files, evenly split between EPC and RAN
	public static final int MAXC10 = NUM_FILES/3;
	
	
}