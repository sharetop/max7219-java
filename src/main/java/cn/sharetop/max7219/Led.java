package cn.sharetop.max7219;

import java.io.IOException;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

/**
 * Matrix 7219 LED 显示
 * @author yancheng
 * 
 * 
 * */
public class Led {
	
	private static final boolean debug = false;
	
	public static class Constants{
		public static byte MAX7219_REG_NOOP = 0x0;
		public static byte MAX7219_REG_DIGIT0 = 0x1;
		public static byte MAX7219_REG_DIGIT1 = 0x2;
		public static byte MAX7219_REG_DIGIT2 = 0x3;
		public static byte MAX7219_REG_DIGIT3 = 0x4;
		public static byte MAX7219_REG_DIGIT4 = 0x5;
		public static byte MAX7219_REG_DIGIT5 = 0x6;
		public static byte MAX7219_REG_DIGIT6 = 0x7;
		public static byte MAX7219_REG_DIGIT7 = 0x8;
		public static byte MAX7219_REG_DECODEMODE = 0x9;
		public static byte MAX7219_REG_INTENSITY = 0xA;
		public static byte MAX7219_REG_SCANLIMIT = 0xB;
		public static byte MAX7219_REG_SHUTDOWN = 0xC;
		public static byte MAX7219_REG_DISPLAYTEST = 0xF;
	}
	
	protected static final short NUM_DIGITS = 8;
	
	//重叠几个屏幕（>=1）
	protected short cascaded = 1;
	//旋转方向（0,90,180,270）
	protected int orientation;
	//保存显示的字节数组（缓冲区）
	protected byte[] buffer;
	//SPI设备
	protected SpiDevice spi;
	
	public Led(short cascaded){
		this.orientation=0;
		this.cascaded = cascaded;
		this.buffer = new byte[NUM_DIGITS*this.cascaded];
		
		try {
			if (!debug) {
				this.spi = SpiFactory.getInstance(SpiChannel.CS0, SpiDevice.DEFAULT_SPI_SPEED,SpiDevice.DEFAULT_SPI_MODE);
				
				command(Constants.MAX7219_REG_SCANLIMIT, (byte) 0x7);
				command(Constants.MAX7219_REG_DECODEMODE, (byte) 0x0);
				command(Constants.MAX7219_REG_DISPLAYTEST, (byte) 0x0);
				//command(Constants.MAX7219_REG_SHUTDOWN, (byte) 0x1);
				
				this.brightness((byte)3);
				//this.clear();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 发送指令 data 到寄存器 register
	 * */
	public void command(byte register,byte data) throws Exception {
		
		int len = 2*this.cascaded;
		byte[] buf = new byte[len];
		
		for(int i=0;i<len;i+=2){
			buf[i]=register;
			buf[i+1]=data;
		}
		
		this._write(buf);
	}
	
	/**
	 * 清除屏幕
	 * */
	public void clear(){
		
		try{
			for(int i=0;i<this.cascaded;i++){
				for(short j=0;j<NUM_DIGITS;j++){
					this._setbyte(i, (short)(j+Constants.MAX7219_REG_DIGIT0), (byte)0x00);
				}
			}
			this.flush();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	/**
	 * 左右旋转
	 * */
	public void rotate_left(boolean redraw){
		byte t = this.buffer[NUM_DIGITS*this.cascaded-1];
		for(int i=NUM_DIGITS*this.cascaded-1;i>0;i--){
			this.buffer[i]=this.buffer[i-1];
		}
		this.buffer[0]=t;
		
		if(redraw)
			this.flush();
	}
	public void rotate_right(boolean redraw){
		byte t=this.buffer[0];
		for(int i=0;i<NUM_DIGITS*this.cascaded-1;i++){
			this.buffer[i]=this.buffer[i+1];
		}
		this.buffer[NUM_DIGITS*this.cascaded-1]=t;
		
		if(redraw)
			this.flush();
	}
	
	/**
	 * 左右移动
	 * */
	public void scroll_left(boolean redraw){
		for(int i=0;i<NUM_DIGITS*this.cascaded-1;i++){
			this.buffer[i]=this.buffer[i+1];
		}
		this.buffer[NUM_DIGITS*this.cascaded-1]=0x0;
		
		if(redraw)
			this.flush();
	}
	public void scroll_right(boolean redraw){
		for(int i=NUM_DIGITS*this.cascaded-1;i>0;i--){
			this.buffer[i]=this.buffer[i-1];
		}
		this.buffer[0]=0x0;
		
		if(redraw)
			this.flush();
	}
	
	/**
	 * 旋转
	 * */
	public void orientation(int angle){
		this.orientation(angle, true);
	}
	public void orientation(int angle,boolean redraw){
		if(angle!=0 && angle!=90 && angle!=180 && angle!=270) return;
		
		this.orientation = angle;
		if(redraw)
			this.flush();
	}
	/**
	 * 显示字符
	 * */
	public void letter(short deviceId,short asciiCode){
		this.letter(deviceId, asciiCode, Font.CP437_FONT,true);
	}
	public void letter(short deviceId,short asciiCode,short[][] font){
		this.letter(deviceId, asciiCode, font,true);
	}
	public void letter(short deviceId,short asciiCode,short[][] font,boolean redraw){
		short[] values = Font.value(font,asciiCode);
		
		short col = Constants.MAX7219_REG_DIGIT0;
		for(short value:values){
			if(col>Constants.MAX7219_REG_DIGIT7) return;
			
			this._setbyte(deviceId,col,(byte)(value&0xff));
			col+=1;
		}
		
		if(redraw)
			this.flush();
	}
	
	/**
	 * 显示字符串，并滚动
	 * */
	public void showMessage(String text){
		this.showMessage(text, Font.CP437_FONT);
	}
	public void showMessage(String text,short[][] font){
		for(int i=0;i<this.cascaded;i++)
			text +=' ';
		
		byte[] src = new byte[NUM_DIGITS*text.length()];
		
		for(int i=0;i<text.length();i++){
			char c = text.charAt(i);
			short[] values = Font.value(font,c);
			for(int j=0;j<values.length;j++){
				src[i*NUM_DIGITS+j]=(byte)(values[j]&0xff);
			}
		}
		
		for(int i=0;i<src.length;i++){
			try{
				Thread.sleep(60);
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			
			this.scroll_left(false);
			this.buffer[this.buffer.length-1]=src[i];
			
			this.flush();
			
		}
		
	}
	/**
	 * 将缓存区数据写入设备
	 * */
	public void flush(){
		try{
			byte[] buf = this.buffer;
			
			if(this.orientation>0){
				buf = this._rotate(buf);
			}
			
			for(short pos=0;pos<NUM_DIGITS;pos++){
				this._write(this._values(pos,buf));
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	/**
	 * 设置亮度
	 * intensity的范围为0<=?<16
	 * */
	public void brightness(byte intensity){
		try {
			if(intensity<0 || intensity>15) return;
			
			this.command(Constants.MAX7219_REG_INTENSITY, intensity);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 关闭设备
	 * */
	public void close(){
		try{
			this.clear();
			this.command(Constants.MAX7219_REG_SHUTDOWN, (byte)0x0);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * 打开设备
	 * */
	public void open(){
		try{
			this.command(Constants.MAX7219_REG_SHUTDOWN, (byte)0x1);
			this.clear();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//
	//
	//////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 将buf写入设备
	 * */
	private void _write(byte[] buf) throws Exception{
		if(!debug){
			//for(byte b:buf) System.out.print(b+",");
			//System.out.println("");
			
			this.spi.write(buf);
		}
		else{
			for(byte b:buf) System.out.print(b+",");
			System.out.println("");
		}
	}
	/**
	 * 根据屏幕多少，构造出待写入的字节数组
	 * 返回格式为 [position,data,position1,data1],（以两块屏幕为例） 
	 * */
	private byte[] _values(short position,byte[] buf) throws Exception {
		int len = 2*this.cascaded;
		byte[] ret = new byte[len];
		
		for(int i=0;i<this.cascaded;i++){
			ret[2*i]=(byte)((position+Constants.MAX7219_REG_DIGIT0)&0xff);
			ret[2*i+1]=buf[(i*NUM_DIGITS)+position];
			
		}
		return ret;
	}
	/**
	 * 写数据到显示缓冲区
	 * */
	private void _setbyte(int deviceId,short position,byte value){
		int offset = deviceId*NUM_DIGITS+position-Constants.MAX7219_REG_DIGIT0;
		this.buffer[offset]=value;
	}
	/**
	 * 8*8的数组旋转
	 * */
	private byte[] _rotate_8_8(byte[] buf){
		byte[] result = new byte[8];
    	for(int i=0;i<8;i++){ //输出结果的索引
    		short b = 0;
    		short t = (short)((0x01<<i)&0xff); //根据索引，计算一下需要取源的哪个bit
    		for(int j=0;j<8;j++){
    			int d = 7-i-j; //计算一下移位的个数，与i有关，有可能为负数
    			if(d>0)
    				b +=(short)((buf[j]&t)<<d);
    			else
    				b +=(short)((buf[j]&t)>>(-1*d));
    		}
    		result[i]=(byte)b;
    	}
    	
		return result;
	}
	/**
	 * 显示缓冲区旋转,每个屏幕单独旋转
	 * */
	private byte[] _rotate(byte[] buf){
		byte[] result = new byte[this.buffer.length];
		for(int i=0;i<this.cascaded*NUM_DIGITS;i+=NUM_DIGITS){
			byte[] tile = new byte[NUM_DIGITS];
			for(int j=0;j<NUM_DIGITS;j++){
				tile[j]=buf[i+j];
			}
			int k = this.orientation/90;
			for(int j=0;j<k;j++){
				tile = this._rotate_8_8(tile);
			}
			for(int j=0;j<NUM_DIGITS;j++){
				result[i+j]=tile[j];
			}
			
		}
		
		return result;
	}
	
}
