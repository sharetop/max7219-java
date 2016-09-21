import java.io.IOException;

import cn.sharetop.max7219.Led;

/**
 * 微雪的RPi LED Matrix 
 * @author yancheng
 * 
 * */
public class MainDemo {

	public static void main(String[] args){
		
		//有两块(8*8)的屏幕
		Led c = new Led((short)2);
		
		//打开设备
		c.open();
		
		//旋转270度，缺省两个屏幕是上下排列，我需要的是左右排
		c.orientation(270);
		
		//DEMO1: 输出两个字母
		//c.letter((short)0, (short)'Y',false);
		//c.letter((short)1, (short)'C',false);
		//c.flush();
		
		//DEMO2: 输出两个汉字，比较CHOU
		//c.letter((short)0, (short)0,Font.CHN_FONT,false);
		//c.letter((short)1, (short)1,Font.CHN_FONT,false);
		//c.flush();
		
		//DEMO3: 输出一串字母
		c.showMessage("Hello 0123456789$");
		
		try {
			System.in.read();
			c.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
