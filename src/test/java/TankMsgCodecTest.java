import org.junit.Assert;
import org.junit.Test;

import com.feiyu.nettystudy.s13.TankMsg;
import com.feiyu.nettystudy.s13.TankMsgDecoder;
import com.feiyu.nettystudy.s13.TankMsgEncoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

public class TankMsgCodecTest {

	@Test
	public void testTankMsgEncoder() {
		TankMsg msg = new TankMsg(10, 10);
		EmbeddedChannel ch = new EmbeddedChannel(new TankMsgEncoder());//相当于有一个Channel虚拟连上网了
		ch.writeOutbound(msg);//通过Channel往外写数据
		
		ByteBuf buf = (ByteBuf)ch.readOutbound();
		int x = buf.readInt();
		int y = buf.readInt();
		
		Assert.assertTrue(x == 10 && y == 10);//true测试通过
		buf.release();
		
	}
	
	@Test
	public void testTankMsgEncoder2() {
		//TankMsg转成ByteBuf
		ByteBuf buf = Unpooled.buffer();
		TankMsg msg = new TankMsg(10, 10);
		buf.writeInt(msg.x);
		buf.writeInt(msg.y);
		
		
		EmbeddedChannel ch = new EmbeddedChannel(new TankMsgEncoder(), new TankMsgDecoder());
		ch.writeInbound(buf.duplicate());//相当于服务器往客户端写
		
		TankMsg tm = (TankMsg)ch.readInbound();
		
		
		Assert.assertTrue(tm.x == 10 && tm.y == 10);//true测试通过
		
	}

}
