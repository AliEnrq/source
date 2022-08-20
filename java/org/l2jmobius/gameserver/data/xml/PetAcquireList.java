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
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.holders.PetSkillAcquireHolder;

/**
 * Written by Berezkin Nikolay, on 11.04.2021
 */
public class PetAcquireList implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(PetAcquireList.class.getName());
	
	private final Map<Integer, List<PetSkillAcquireHolder>> _skills = new HashMap<>();
	
	protected PetAcquireList()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_skills.clear();
		parseDatapackFile("data/PetAcquireList.xml");
		
		if (!_skills.isEmpty())
		{
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + _skills.size() + " pet skills.");
		}
		else
		{
			LOGGER.info(getClass().getSimpleName() + ": System is disabled.");
		}
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("pet".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						Node att;
						final StatSet set = new StatSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						
						final int type = parseInteger(attrs, "type");
						final List<PetSkillAcquireHolder> list = new ArrayList<>();
						for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
						{
							attrs = b.getAttributes();
							if ("skill".equalsIgnoreCase(b.getNodeName()))
							{
								list.add(new PetSkillAcquireHolder(parseInteger(attrs, "id"), parseInteger(attrs, "lvl"), parseInteger(attrs, "reqLvl"), parseInteger(attrs, "evolve"), parseInteger(attrs, "item") == null ? null : new ItemHolder(parseInteger(attrs, "item"), parseLong(attrs, "itemAmount"))));
							}
						}
						
						_skills.put(type, list);
					}
				}
			}
		}
	}
	
	public List<PetSkillAcquireHolder> getSkills(int type)
	{
		return _skills.get(type);
	}
	
	public Map<Integer, List<PetSkillAcquireHolder>> getAllSkills()
	{
		return _skills;
	}
	
	public int getSpecialSkillByType(int petType)
	{
		switch (petType)
		{
			case 15:
			{
				return 49001;
			}
			case 14:
			{
				return 49011;
			}
			case 12:
			{
				return 49021;
			}
			case 13:
			{
				return 49031;
			}
			case 17:
			{
				return 49041;
			}
			case 16:
			{
				return 49051;
			}
			default:
			{
				throw new IllegalStateException("Unexpected value: " + petType);
			}
		}
	}
	
	public static PetAcquireList getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final PetAcquireList INSTANCE = new PetAcquireList();
	}
}
