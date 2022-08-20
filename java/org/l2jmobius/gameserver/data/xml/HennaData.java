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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.henna.Henna;
import org.l2jmobius.gameserver.model.skill.Skill;

/**
 * This class holds the henna related information.<br>
 * Cost and required amount to add the henna to the player.<br>
 * Cost and retrieved amount for removing the henna from the player.<br>
 * Allowed classes to wear each henna.
 * @author Zoey76, Mobius
 */
public class HennaData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(HennaData.class.getName());
	
	private final Map<Integer, Henna> _hennaDyeIdList = new HashMap<>();
	private final Map<Integer, Henna> _hennaItemIdList = new HashMap<>();
	
	/**
	 * Instantiates a new henna data.
	 */
	protected HennaData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_hennaItemIdList.clear();
		_hennaDyeIdList.clear();
		parseDatapackFile("data/stats/hennaList.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _hennaDyeIdList.size() + " henna data.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equals(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("henna".equals(d.getNodeName()))
					{
						parseHenna(d);
					}
				}
			}
		}
	}
	
	/**
	 * Parses the henna.
	 * @param d the node
	 */
	private void parseHenna(Node d)
	{
		final StatSet set = new StatSet();
		final List<Integer> wearClassIds = new ArrayList<>();
		final List<Skill> skills = new ArrayList<>();
		NamedNodeMap attrs = d.getAttributes();
		Node attr;
		for (int i = 0; i < attrs.getLength(); i++)
		{
			attr = attrs.item(i);
			set.set(attr.getNodeName(), attr.getNodeValue());
		}
		
		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
		{
			final String name = c.getNodeName();
			attrs = c.getAttributes();
			switch (name)
			{
				case "stats":
				{
					for (int i = 0; i < attrs.getLength(); i++)
					{
						attr = attrs.item(i);
						set.set(attr.getNodeName(), attr.getNodeValue());
					}
					break;
				}
				case "wear":
				{
					attr = attrs.getNamedItem("count");
					set.set("wear_count", attr.getNodeValue());
					attr = attrs.getNamedItem("fee");
					set.set("wear_fee", attr.getNodeValue());
					break;
				}
				case "cancel":
				{
					attr = attrs.getNamedItem("count");
					set.set("cancel_count", attr.getNodeValue());
					attr = attrs.getNamedItem("fee");
					set.set("cancel_fee", attr.getNodeValue());
					break;
				}
				case "duration":
				{
					attr = attrs.getNamedItem("time"); // in minutes
					set.set("duration", attr.getNodeValue());
					break;
				}
				case "skill":
				{
					skills.add(SkillData.getInstance().getSkill(parseInteger(attrs, "id"), parseInteger(attrs, "level")));
					break;
				}
				case "classId":
				{
					for (String s : c.getTextContent().split(","))
					{
						wearClassIds.add(Integer.parseInt(s));
					}
					break;
				}
			}
		}
		
		final Henna henna = new Henna(set);
		henna.setSkills(skills);
		henna.setWearClassIds(wearClassIds);
		_hennaDyeIdList.put(henna.getDyeId(), henna);
		_hennaItemIdList.put(henna.getDyeItemId(), henna);
	}
	
	/**
	 * Gets the henna.
	 * @param id of the dye.
	 * @return the dye with that id.
	 */
	public Henna getHenna(int id)
	{
		return _hennaDyeIdList.get(id);
	}
	
	public Henna getHennaByDyeId(int id)
	{
		return _hennaDyeIdList.get(id);
	}
	
	public Henna getHennaByItemId(int id)
	{
		return _hennaItemIdList.get(id);
	}
	
	/**
	 * Gets the henna list.
	 * @param player the player's class Id.
	 * @return the list with all the allowed dyes.
	 */
	public List<Henna> getHennaList(Player player)
	{
		final List<Henna> list = new ArrayList<>();
		for (Henna henna : _hennaDyeIdList.values())
		{
			if (henna.isAllowedClass(player))
			{
				list.add(henna);
			}
		}
		return list;
	}
	
	/**
	 * Gets the single instance of HennaData.
	 * @return single instance of HennaData
	 */
	public static HennaData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final HennaData INSTANCE = new HennaData();
	}
}