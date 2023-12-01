package carbonconfiglib.networking;

import carbonconfiglib.networking.carbon.ConfigAnswerPacket;
import carbonconfiglib.networking.carbon.ConfigRequestPacket;
import carbonconfiglib.networking.carbon.SaveConfigPacket;
import carbonconfiglib.networking.carbon.StateSyncPacket;
import carbonconfiglib.networking.minecraft.RequestGameRulesPacket;
import carbonconfiglib.networking.minecraft.SaveGameRulesPacket;
import carbonconfiglib.networking.snyc.BulkSyncPacket;
import carbonconfiglib.networking.snyc.SyncPacket;
import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketBuffer;

@Sharable
public class CarbonChannel extends FMLIndexedMessageToMessageCodec<ICarbonPacket>
{
	public CarbonChannel() {
		addDiscriminator(0, SyncPacket.class);
		addDiscriminator(1, BulkSyncPacket.class);
		addDiscriminator(2, ConfigRequestPacket.class);
		addDiscriminator(3, ConfigAnswerPacket.class);
		addDiscriminator(4, SaveConfigPacket.class);
		addDiscriminator(7, RequestGameRulesPacket.class);
		addDiscriminator(8, SaveGameRulesPacket.class);
		addDiscriminator(255, StateSyncPacket.class);
	}
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ICarbonPacket msg, ByteBuf target) throws Exception
	{
		try
		{
			msg.write(new PacketBuffer(target));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, ICarbonPacket msg)
	{
		try
		{
			msg.read(new PacketBuffer(source));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
