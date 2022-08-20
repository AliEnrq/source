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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.holders.TeleportListHolder;

/**
 * @author Gustavo Fonseca
 */
public class RaidTeleportListData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(RaidTeleportListData.class.getName());
	
	private final Map<Integer, TeleportListHolder> _teleports = new HashMap<>();
	
	protected RaidTeleportListData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_teleports.clear();
		parseDatapackFile("data/RaidTeleportListData.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _teleports.size() + " teleports.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "teleport", teleportNode ->
		{
			final StatSet set = new StatSet(parseAttributes(teleportNode));
			final int tpId = set.getInt("id");
			final int x = set.getInt("x");
			final int y = set.getInt("y");
			final int z = set.getInt("z");
			final int tpPrice = set.getInt("price");
			_teleports.put(tpId, new TeleportListHolder(tpId, x, y, z, tpPrice, false));
		}));
	}
	
	public TeleportListHolder getTeleport(int teleportId)
	{
		return _teleports.get(teleportId);
	}
	
	public static RaidTeleportListData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final RaidTeleportListData INSTANCE = new RaidTeleportListData();
	}
}