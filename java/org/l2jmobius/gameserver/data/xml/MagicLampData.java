/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.GreaterMagicLampHolder;
import org.l2jmobius.gameserver.model.holders.MagicLampDataHolder;
import org.l2jmobius.gameserver.model.stats.Stat;
import org.l2jmobius.gameserver.network.serverpackets.magiclamp.ExMagicLampExpInfoUI;

/**
 * @author L2CCCP
 */
public class MagicLampData implements IXmlReader
{
	private static final List<MagicLampDataHolder> LAMPS = new ArrayList<>();
	private static final List<GreaterMagicLampHolder> GREATER_LAMPS = new ArrayList<>();
	
	protected MagicLampData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		LAMPS.clear();
		GREATER_LAMPS.clear();
		parseDatapackFile("data/MagicLampData.xml");
		LOGGER.info("MagicLampData: Loaded " + (LAMPS.size() + GREATER_LAMPS.size()) + " magic lamps.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", listNode -> forEach(listNode, node ->
		{
			if (node.getNodeName().equalsIgnoreCase("greater_lamp_mode"))
			{
				GREATER_LAMPS.add(new GreaterMagicLampHolder(new StatSet(parseAttributes(node))));
			}
			else if (node.getNodeName().equalsIgnoreCase("lamp"))
			{
				LAMPS.add(new MagicLampDataHolder(new StatSet(parseAttributes(node))));
			}
		}));
	}
	
	public void addLampExp(Player player, double exp, boolean rateModifiers)
	{
		if (Config.ENABLE_MAGIC_LAMP && (player.getLampCount() < player.getMaxLampCount()))
		{
			final int lampExp = (int) (exp * (rateModifiers ? Config.MAGIC_LAMP_CHARGE_RATE * player.getStat().getMul(Stat.MAGIC_LAMP_EXP_RATE, 1) : 1));
			int calc = lampExp + player.getLampExp();
			if (calc > Config.MAGIC_LAMP_MAX_LEVEL_EXP)
			{
				calc %= Config.MAGIC_LAMP_MAX_LEVEL_EXP;
				player.setLampCount(player.getLampCount() + 1);
			}
			player.setLampExp(calc);
			player.sendPacket(new ExMagicLampExpInfoUI(player));
		}
	}
	
	public List<MagicLampDataHolder> getLamps()
	{
		return LAMPS;
	}
	
	public List<GreaterMagicLampHolder> getGreaterLamps()
	{
		return GREATER_LAMPS;
	}
	
	public static MagicLampData getInstance()
	{
		return Singleton.INSTANCE;
	}
	
	private static class Singleton
	{
		protected static final MagicLampData INSTANCE = new MagicLampData();
	}
}