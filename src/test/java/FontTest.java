import org.junit.*;

import cn.sharetop.max7219.Font;

public class FontTest {

	 @Test 
	 public void testDefaultValue() {
		short[] font = Font.value((short)'A');
		Assert.assertArrayEquals(new short[]{0x7C, 0x7E, 0x13, 0x13, 0x7E, 0x7C, 0x00, 0x00}, font);
	 }
	 @Test
	 public void testValue(){
		short[] font = Font.value(Font.CP437_FONT, (short)'A');
		Assert.assertArrayEquals(new short[]{0x7C, 0x7E, 0x13, 0x13, 0x7E, 0x7C, 0x00, 0x00}, font);
	 }
}
