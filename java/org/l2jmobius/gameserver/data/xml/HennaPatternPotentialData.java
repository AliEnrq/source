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
import java.util.Collection;
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
import org.l2jmobius.gameserver.model.item.henna.DyePotential;
import org.l2jmobius.gameserver.model.item.henna.DyePotentialFee;
import org.l2jmobius.gameserver.model.skill.Skill;

/**
 * @author Serenitty
 */
public class HennaPatternPotentialData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(HennaPatternPotentialData.class.getName());
	
	private final Map<Integer, Integer> _potenExpTable = new HashMap<>();
	private final Map<Integer, DyePotentialFee> _potenFees = new HashMap<>();
	private final Map<Integer, DyePotential> _potentials = new HashMap<>();
	
	private int MAX_POTEN_LEVEL = 0;
	
	protected HennaPatternPotentialData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_potenFees.clear();
		_potenExpTable.clear();
		_potentials.clear();
		parseDatapackFile("data/stats/hennaPatternPotential.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _potenFees.size() + " dye pattern fee data.");
		
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node m = doc.getFirstChild(); m != null; m = m.getNextSibling())
		{
			if ("list".equals(m.getNodeName()))
			{
				for (Node k = m.getFirstChild(); k != null; k = k.getNextSibling())
				{
					switch (k.getNodeName())
					{
						case "enchantFees":
						{
							for (Node n = k.getFirstChild(); n != null; n = n.getNextSibling())
							{
								if ("fee".equals(n.getNodeName()))
								{
									NamedNodeMap attrs = n.getAttributes();
									Node att;
									final StatSet set = new StatSet();
									for (int i = 0; i < attrs.getLength(); i++)
									{
										att = attrs.item(i);
										set.set(att.getNodeName(), att.getNodeValue());
									}
									
									final int step = parseInteger(attrs, "step");
									int itemId = 0;
									long itemCount = 0;
									int dailyCount = 0;
									final Map<Integer, Double> enchantExp = new HashMap<>();
									for (Node b = n.getFirstChild(); b != null; b = b.getNextSibling())
									{
										attrs = b.getAttributes();
										switch (b.getNodeName())
										{
											case "requiredItem":
											{
												itemId = parseInteger(attrs, "id");
												itemCount = parseLong(attrs, "count", 1L);
												break;
											}
											case "dailyCount":
											{
												dailyCount = Integer.parseInt(b.getTextContent());
												break;
											}
											case "enchantExp":
											{
												enchantExp.put(parseInteger(attrs, "count"), parseDouble(attrs, "chance"));
												break;
											}
										}
									}
									_potenFees.put(step, new DyePotentialFee(step, new ItemHolder(itemId, itemCount), dailyCount, enchantExp));
								}
							}
							break;
						}
						case "experiencePoints":
						{
							for (Node n = k.getFirstChild(); n != null; n = n.getNextSibling())
							{
								if ("hiddenPower".equals(n.getNodeName()))
								{
									NamedNodeMap attrs = n.getAttributes();
									Node att;
									final StatSet set = new StatSet();
									for (int i = 0; i < attrs.getLength(); i++)
									{
										att = attrs.item(i);
										set.set(att.getNodeName(), att.getNodeValue());
									}
									
									final int level = parseInteger(attrs, "level");
									final int exp = parseInteger(attrs, "exp");
									_potenExpTable.put(level, exp);
									if (MAX_POTEN_LEVEL < level)
									{
										MAX_POTEN_LEVEL = level;
									}
								}
							}
							break;
						}
						case "hiddenPotentials":
						{
							for (Node n = k.getFirstChild(); n != null; n = n.getNextSibling())
							{
								if ("poten".equals(n.getNodeName()))
								{
									NamedNodeMap attrs = n.getAttributes();
									Node att;
									final StatSet set = new StatSet();
									for (int i = 0; i < attrs.getLength(); i++)
									{
										att = attrs.item(i);
										set.set(att.getNodeName(), att.getNodeValue());
									}
									
									final int id = parseInteger(attrs, "id");
									final int slotId = parseInteger(attrs, "slotId");
									final int maxSkillLevel = parseInteger(attrs, "maxSkillLevel");
									final int skillId = parseInteger(attrs, "skillId");
									_potentials.put(id, new DyePotential(id, slotId, skillId, maxSkillLevel));
								}
							}
							break;
						}
					}
				}
			}
		}
	}
	
	public DyePotentialFee getFee(int step)
	{
		return _potenFees.get(step);
	}
	
	public int getMaxPotenEnchantStep()
	{
		return _potenFees.size();
	}
	
	public int getExpForLevel(int level)
	{
		return _potenExpTable.get(level);
	}
	
	public int getMaxPotenLevel()
	{
		return MAX_POTEN_LEVEL;
	}
	
	public DyePotential getPotential(int potenId)
	{
		return _potentials.get(potenId);
	}
	
	public Skill getPotentialSkill(int potenId, int slotId, int level)
	{
		final DyePotential potential = _potentials.get(potenId);
		if (potential == null)
		{
			return null;
		}
		if (potential.getSlotId() == slotId)
		{
			return potential.getSkill(level);
		}
		return null;
	}
	
	public Collection<Integer> getSkillIdsBySlotId(int slotId)
	{
		final List<Integer> skillIds = new ArrayList<>();
		for (DyePotential potential : _potentials.values())
		{
			if (potential.getSlotId() == slotId)
			{
				skillIds.add(potential.getSkillId());
			}
		}
		return skillIds;
	}
	
	public static HennaPatternPotentialData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final HennaPatternPotentialData INSTANCE = new HennaPatternPotentialData();
	}
}