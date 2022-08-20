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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.model.vip.VipInfo;

/**
 * @author Gabriel Costa Souza
 */
public class VipData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(VipData.class.getName());
	
	private final Map<Byte, VipInfo> _vipTiers = new HashMap<>();
	
	protected VipData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		if (!Config.VIP_SYSTEM_ENABLED)
		{
			return;
		}
		_vipTiers.clear();
		parseDatapackFile("data/Vip.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _vipTiers.size() + " vips.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				VIP_FILE: for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("vip".equalsIgnoreCase(d.getNodeName()))
					{
						final NamedNodeMap attrs = d.getAttributes();
						Node att;
						byte tier = -1;
						int required = -1;
						int lose = -1;
						
						att = attrs.getNamedItem("tier");
						if (att == null)
						{
							LOGGER.severe(getClass().getSimpleName() + ": Missing tier for vip, skipping");
							continue;
						}
						tier = Byte.parseByte(att.getNodeValue());
						
						att = attrs.getNamedItem("points-required");
						if (att == null)
						{
							LOGGER.severe(getClass().getSimpleName() + ": Missing points-required for vip: " + tier + ", skipping");
							continue;
						}
						required = Integer.parseInt(att.getNodeValue());
						
						att = attrs.getNamedItem("points-lose");
						if (att == null)
						{
							LOGGER.severe(getClass().getSimpleName() + ": Missing points-lose for vip: " + tier + ", skipping");
							continue;
						}
						lose = Integer.parseInt(att.getNodeValue());
						
						final VipInfo vipInfo = new VipInfo(tier, required, lose);
						for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
						{
							if ("bonus".equalsIgnoreCase(c.getNodeName()))
							{
								final int skill = Integer.parseInt(c.getAttributes().getNamedItem("skill").getNodeValue());
								try
								{
									vipInfo.setSkill(skill);
								}
								catch (Exception e)
								{
									LOGGER.severe(getClass().getSimpleName() + ": Error in bonus parameter for vip: " + tier + ", skipping");
									continue VIP_FILE;
								}
							}
						}
						_vipTiers.put(tier, vipInfo);
					}
				}
			}
		}
	}
	
	/**
	 * Gets the single instance of VipData.
	 * @return single instance of VipData
	 */
	public static VipData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * The Class SingletonHolder.
	 */
	private static class SingletonHolder
	{
		protected static final VipData INSTANCE = new VipData();
	}
	
	public int getSkillId(byte tier)
	{
		return _vipTiers.get(tier).getSkill();
	}
	
	public Map<Byte, VipInfo> getVipTiers()
	{
		return _vipTiers;
	}
}
