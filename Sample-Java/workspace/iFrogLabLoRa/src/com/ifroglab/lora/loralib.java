package com.ifroglab.lora;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class loralib {
	boolean debug=true;
	public SerialPort comPort;
	public SerialPort mSerialPort[];  //COM Port 資料
	//public SerialPort mLoRaSerialPort[];  //COM Port 資料
	private ArrayList<SerialPort> mLoRaSerialPort; // = new ArrayList<SerialPort>();
	public ArrayList<String> mLoRaSerialPortString; // = new ArrayList<SerialPort>();
	
	public static String OS = null;   //作業系統
	// 用戶的設定
	private String mSerialPortDescriptivePortName;
	private int mBautRate=115200;
	public byte m_TXRX;
	public byte m_Freq1;
	public byte m_Freq2;
	public byte m_Freq3;
	public byte m_Power;
	

	public class loraDevice {
		public byte  id[]={0,0,0,0};                // 設備ID 
		public long  m_UniqueNumber;                // 設備ID HEX
		public  byte dataChip; 						//顯示Chip 資訊
		public  byte dataFW_Ver; 							//顯示FW_Ver 資訊
		public String DescriptivePortName;          // 設備 USB 名稱 
	}
	public loraDevice mCurrentLoRa;

	
	public loralib() {
		mLoRaSerialPort = new ArrayList<SerialPort>();
		mLoRaSerialPortString = new ArrayList<String>();
		if(OS == null) { OS = System.getProperty("os.name"); }
		mCurrentLoRa=new loraDevice();
	}

	
	public boolean CheckPIDVID(SerialPort iSerialPort){
	   //TODO 還沒有判斷Vendor ID 和 Product ID 
	   if(OS.startsWith("Windows")==true){              		   // windows
			// https://stackoverflow.com/questions/6362775/getting-device-driver-information-related-to-a-com-port
	   }
	   return true;
	}

	public void serial_serialEvent_Close() {
		comPort.removeDataListener();
	}
	public void serial_serialEvent_Open() {
		serial_serialEvent_Close();
		comPort.addDataListener(new SerialPortDataListener() {
			   @Override
			   public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
			   @Override
			   public void serialEvent(SerialPortEvent event)
			   {
			      if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
			         return;
			      byte[] newData = new byte[comPort.bytesAvailable()];
			      int numRead = comPort.readBytes(newData, newData.length);
			      System.out.println("--------------------Read " + numRead + " bytes.");
			   }
			});
    }
	public  ArrayList<String> serial_allPorts() {
	    mLoRaSerialPort.clear();                                                           //清除原本的資料
	    mLoRaSerialPortString.clear();                                                     //清除原本的資料
		mSerialPort=SerialPort.getCommPorts();                                             // 找所有COM PORTs
		int length=mSerialPort.length;
		// 顯示所有COM Port設備
		while (length>0){
		    System.out.println("SerialPort: " + mSerialPort[length-1].getDescriptivePortName()+",PATH:"+mSerialPort[length-1].getSystemPortName());
		    System.out.println("  ") ;
		    
		    // 找iFrogLab LoRa USB 設備
		    if(OS.startsWith("Mac")==true){  //  Mac OS X
		    	 if(mSerialPort[length-1].getDescriptivePortName().startsWith("USB-UART LP (Dial-In)")==true){   //用USB的名稱來判斷
			    	// if(mSerialPort[length-1].getDescriptivePortName().startsWith("USB-UART LP")==true){   //用USB的名稱來判斷
				    	 if(CheckPIDVID(mSerialPort[length-1])==true){
				    		 mLoRaSerialPort.add(0,mSerialPort[length-1]);
				    		 mLoRaSerialPortString.add(0,mSerialPort[length-1].getSystemPortName());
			    		 }
			    	// }
		    	 }
			}else if((OS.startsWith("Linux")==true) || (OS.startsWith("Windows")==true) ) { 
				 if(mSerialPort[length-1].getDescriptivePortName().startsWith("USB-UART LP")==true){   //用USB的名稱來判斷
				    	// if(mSerialPort[length-1].getDescriptivePortName().startsWith("USB-UART LP")==true){   //用USB的名稱來判斷
					    	 if(CheckPIDVID(mSerialPort[length-1])==true){
					    		 mLoRaSerialPort.add(0,mSerialPort[length-1]);
					    		 mLoRaSerialPortString.add(0,mSerialPort[length-1].getSystemPortName());
				    		 }
				    	// }
			    	 }
			
			}
		    length=length-1;
		}
		return mLoRaSerialPortString;
	}
	public void SerialPort_setSerialPort(String iSerialPortDescriptivePortName) {
		mSerialPortDescriptivePortName=iSerialPortDescriptivePortName;
		//FunLora_setup();
	}
	public void ReadMode(byte Freq1,byte Freq2,byte Freq3,byte Power){
		  m_TXRX=0x03;
		  Setup(m_TXRX,Freq1,Freq2,Freq3,Power);
		  //delay(40);
	}
	//          WriteMode(byte Freq1,byte Freq2,byte Freq3,byte Power){ TXRX,0x01,0x65,0x6c,0x3
	public void WriteMode(byte Freq1,byte Freq2,byte Freq3,byte Power){
		  m_TXRX=0x02;
		  Setup(m_TXRX,Freq1,Freq2,Freq3,Power);
	}
	public byte[]write(byte[] array1){
		ArrayList<Byte> readBuffer = new ArrayList<Byte>();
		try {
			if(comPort!=null && comPort.isOpen()==true){		
				if(debug) 	System.out.println("COM Port->"+FunBytesToHex(array1)); 
			    comPort.writeBytes(array1, array1.length);
			    TimeUnit.MILLISECONDS.sleep(7);
				try {
				   int i=0;
				   int totalnum=0;
				   while (i<6)
				   {
					  int lenComPort=comPort.bytesAvailable();
				      if(lenComPort>0){
					      byte[] readBuffer2 = new byte[lenComPort];
					      int numRead2 = comPort.readBytes(readBuffer2, readBuffer2.length);
					      totalnum=totalnum+numRead2;
					      System.out.println("Read " + numRead2 + " bytes.");
					      for(int j=0;j<readBuffer2.length;j++){
					    	  readBuffer.add(readBuffer2[j]);
					      }
					      //if(totalnum>=ResultLen) break;
				      }else{
						  TimeUnit.MILLISECONDS.sleep(1);
						 // sleep(0.001);
				      }
				      i++;
				   }
				} catch (Exception e) { e.printStackTrace(); }
			}
		} catch ( Exception e1 ){
	      e1.printStackTrace();
	    }
		if(readBuffer.size()==0) return null;
		byte[] newData =FunToByteArray(readBuffer);

		if(debug) 	System.out.println("COM Port<-"+FunBytesToHex(newData)); 
		return newData;
	}
	public byte[] Setup(byte TXRX,byte Freq1,byte Freq2,byte Freq3,byte Power){
	  byte[] data=GetChipIDAll();
	  if(data.length<10) return null;
	  if(debug){
		  mCurrentLoRa.dataChip=data[3];							//顯示Chip 資訊
		  System.out.println(" Chip:"+FunByteToHex(mCurrentLoRa.dataChip)); 
		  mCurrentLoRa.dataFW_Ver=data[4];							//顯示FW_Ver 資訊
		  System.out.println(" FW_Ver:"+FunByteToHex(mCurrentLoRa.dataFW_Ver)); 
		  mCurrentLoRa.m_UniqueNumber=data[5]*0x1000000+ data[6]*0x10000+ data[7]*0x100+data[8]*0x1;
		  System.out.println(" UniqueNumber:"+mCurrentLoRa.m_UniqueNumber); 
	  }
	  // byte t1[] = {0xc1,0x03,0x05,TXRX,0x01,0x65,0x6c,0x3};
	  m_TXRX=TXRX;
	  m_Freq1=Freq1;
	  m_Freq2=Freq2;
	  m_Freq3=Freq3;
	  m_Power=Power;
	  byte CRC=0;
	  byte t1[] = {(byte)0xc1,0x03,0x05,TXRX,Freq1,Freq2,Freq3,Power,CRC};
	  CRC=Fun_CRC2(t1,8);
	  t1[8] = CRC;
	  byte[] data2=write(t1);
	  return data2;
	 }
    // 送byte 到　Chip 上
    public byte[] FunLora_ChipSendByte(byte[] array1){
    	  byte[] data2=write(array1);
    	  return data2;
    }
	// Chip 編號
	public int GetChipID(){
	  byte[] tDeviceData=GetChipIDAll();
		if(tDeviceData.length>9){
			if(tDeviceData[0]==(byte)0x80  && tDeviceData[1]==(byte)0x80 ){	// 確認資料正確
				return tDeviceData[3];
			}
		}
		return 0;
	}
	// 韌體版本
	public int GetFirmwareVersion(){
	    byte[] tDeviceData=GetChipIDAll();
		if(tDeviceData.length>9){
			if(tDeviceData[0]==(byte)0x80  && tDeviceData[1]==(byte)0x80 ){	// 確認資料正確
				return tDeviceData[4];
			}
		}
		return 0;
	}
	// 回傳這設備的ID
	public String GetDeviceID(){
		byte[] tDeviceData=GetChipIDAll();
		if(tDeviceData.length>9){
			// 確認資料正確
			if(tDeviceData[0]==(byte)0x80  &&
			   tDeviceData[1]==(byte)0x80 ){
				mCurrentLoRa.id[0]=tDeviceData[5];
				mCurrentLoRa.id[1]=tDeviceData[6];
				mCurrentLoRa.id[2]=tDeviceData[7];
				mCurrentLoRa.id[3]=tDeviceData[8];
				return FunBytesToHex(mCurrentLoRa.id);
			}
		}
		return "";
	}
	
	private byte Fun_CRC2(byte[] data,int CRCIndex){
	    	byte crc=0;
	    	for (int i=0;i<CRCIndex;i++){
	    		byte t1=data[i];
	    		crc=(byte) (crc^t1);
	    	}
	    	return crc;
	}
    private byte Fun_CRC(byte[] data){
    	return Fun_CRC2(data,data.length);
    }
    private int PortNameToIndex(String iSerialPortDescriptivePortName){
       return 1;
    }

	public  byte[] FunLora_Serial(byte[] array1,int CRCNum,int ResultLen){
		ArrayList<Byte> readBuffer = new ArrayList<Byte>();
		try{
			//List<byte> readBuffer2; // = new ArrayList<SerialPort>();
			int numRead=0;
			if(comPort!=null) comPort.closePort();
			if(comPort==null || comPort.isOpen()==false){
				comPort = SerialPort.getCommPort(mSerialPortDescriptivePortName); //iSerialPortDescriptivePortName);
				comPort.setBaudRate(115200);
				comPort.openPort();
				//comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 1000);
				comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
			}
		    //byte[] array1={(byte) 0x80,0x00,0x00,0};
		    //array1[3]=Fun_CRC(array1);
			int t1=array1.length;
		    array1[t1-1]=Fun_CRC(array1);
		    if(debug) 	System.out.println(FunBytesToHex(array1)); 
		    comPort.writeBytes(array1, array1.length);
		    //TimeUnit.SECONDS.sleep(1);
		    TimeUnit.MILLISECONDS.sleep(7);
			try {
			   int i=0;
			   int totalnum=0;
			   while (i<5)
			   {
			      if(comPort.bytesAvailable()>0){
				      byte[] readBuffer2 = new byte[comPort.bytesAvailable()];
				      int numRead2 = comPort.readBytes(readBuffer2, readBuffer2.length);
				      totalnum=totalnum+numRead2;
				      System.out.println("Read " + numRead2 + " bytes.");
				      for(int j=0;j<readBuffer2.length;j++){
				    	  readBuffer.add(readBuffer2[j]);
				      }
				      if(totalnum>=ResultLen) break;
			      }else{
					  TimeUnit.MILLISECONDS.sleep(1);
			      }
			      i++;
			   }
			} catch (Exception e) { e.printStackTrace(); }
			 // byte[] newData =FunToByteArray(readBuffer);
		     // System.out.println(FunBytesToHex(newData));	
		    //System.out.println("close ");
			//comPort.closePort();
			//comPort=null;
		} catch ( Exception e1 ){
            e1.printStackTrace();
        }
		byte[] newData =FunToByteArray(readBuffer);
		return newData;
	}
	public byte[] GetChipIDAll(){ 
		//String iSerialPortDescriptivePortName){
		
		//byte[] newData=
		byte[] array1={(byte) 0x80,0x00,0x00,0};
		byte[] newData =FunLora_Serial(array1,3,10);
		//byte[] newData =FunToByteArray(readBuffer);
	    System.out.println(FunBytesToHex(newData));	
		
	    
		return newData;
	}

    		  
    // 寫入
	public byte[] FunLora_5_write16bytesArray(byte[] data_array){
		int len=2+1+data_array.length+1;
		byte[] CMD_Data = new byte[len];
		CMD_Data[0]=(byte)0xc1;
		CMD_Data[1]=(byte)0x05;
		CMD_Data[2]=(byte)data_array.length;
		for(int i=0;i<data_array.length;i++){
			CMD_Data[3+i]=(byte)data_array[i];
		}
		CMD_Data[len-1]=0;
		CMD_Data[len-1]=Fun_CRC(CMD_Data);
		
		byte[] data2=FunLora_ChipSendByte(CMD_Data); 
		return data2;
	}
	public byte[] FunLora_6_readPureData(){
		int len=4;
		byte[] CMD_Data = new byte[len];
		CMD_Data[0]=(byte)0xc1;
		CMD_Data[1]=(byte)0x06;
		CMD_Data[2]=(byte)0x00;
		CMD_Data[len-1]=Fun_CRC(CMD_Data);
		
		byte[] data2=FunLora_ChipSendByte(CMD_Data); 
		return data2;
	}
	public byte[] FunLora_7_counter(){
		int len=4;
		byte[] CMD_Data = new byte[len];
		CMD_Data[0]=(byte)0xc1;
		CMD_Data[1]=(byte)0x07;
		CMD_Data[2]=(byte)0x00;
		CMD_Data[len-1]=Fun_CRC(CMD_Data);
	
		//return null;
	   	byte[] data2=FunLora_ChipSendByte(CMD_Data); 
		 return data2;
	}
	
/*
    # 讀取LoRa 傳過來的資料去掉CRC, RSSI 等別的資料
def FunLora_6_readPureData(self):
   array1=[0xC1,0x6,0x0,0]
   array1[3]=self.Fun_CRC(array1)
   data=self.FunLora_ChipSendByte(array1)
   data2=[]
   t_len=len(data)
   t_DataLen=0
   i=0
   if(t_len>6):
      if(data[1].encode('hex')=="86"):
         t_DataLen=ord(data[2])
         if(t_DataLen>2 and t_DataLen<=18):
           for i in range(3,t_DataLen-2+2+1):
             try:
                 data2.append(ord(data[i]))
                 #if self.debug == True:
                 #   print data[i]
             except:
                 print("except")
                 break
   return data2   
	*/
	
	public void FunLora_initByName(String iSerialPortDescriptivePortName){
		try{
			
			//SerialPort comPort = SerialPort.getCommPorts()[0];
			if(comPort!=null) comPort.closePort();
			comPort = SerialPort.getCommPort(iSerialPortDescriptivePortName);
			comPort.setBaudRate(115200);
			comPort.openPort();
			//comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 1000);
			comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
			


		    byte[] array1={(byte) 0x80,0x00,0x00,0};
		    array1[3]=Fun_CRC(array1);
		    if(debug) 	System.out.println(FunBytesToHex(array1)); 
		    int numRead = comPort.writeBytes(array1, array1.length);
		    //TimeUnit.SECONDS.sleep(1);
		    TimeUnit.MILLISECONDS.sleep(7);
		    
		      
			try {
				//SerialPort comPort = SerialPort.getCommPorts()[0];
				//comPort.openPort();
				comPort.addDataListener(new SerialPortDataListener() {
				   @Override
				   public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
				   @Override
					public void serialEvent(SerialPortEvent event) {
						 if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
					         return;
					      byte[] newData = new byte[comPort.bytesAvailable()];
					      int numRead = comPort.readBytes(newData, newData.length);
					      System.out.print("Read " + numRead + " bytes.");
					      System.out.println(FunBytesToHex(newData));
					    
					}
				});
			} catch (Exception e) { 
				e.printStackTrace(); 
				comPort.closePort();
			}
			
/*
	        print(i_portPath)
	        self.ser = serial.Serial(i_portPath, 115200, timeout=3)    
	        return self.ser
	      except SerialException:
	        print("port already open")
	*/        
		} catch ( Exception e1 ){
            e1.printStackTrace();
        }
		
	}
	public void FunLora_close(){
		try{
			if(comPort!=null) comPort.closePort();
			comPort=null;
		} catch ( Exception e1 ){
            e1.printStackTrace();
        }
	}
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String FunBytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	public static String FunByteToHex(byte ibyte) {
		byte[] bytes={ibyte};
	    return FunBytesToHex(bytes);
	}
	public static byte[] FunToByteArray(List<Byte> in) {
	    final int n = in.size();
	    byte ret[] = new byte[n];
	    for (int i = 0; i < n; i++) {
	        ret[i] = in.get(i);
	    }
	    return ret;
	}
	

}
