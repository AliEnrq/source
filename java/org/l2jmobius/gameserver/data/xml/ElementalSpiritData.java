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

import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.model.holders.ElementalSpiritTemplateHolder;

/**
 * @author JoeAlisson
 */
public class ElementalSpiritData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ElementalSpiritData.class.getName());
	
	public static final float FRAGMENT_XP_CONSUME = 50000.0f;
	public static final int TALENT_INIT_FEE = 50000;
	public final static int[] EXTRACT_FEES =
	{
		100000,
		200000,
		300000,
		600000,
		1500000
	};
	
	private static final Map<Byte, Map<Byte, ElementalSpiritTemplateHolder>> SPIRIT_DATA = new HashMap<>(4);
	
	protected ElementalSpiritData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("data/ElementalSpiritData.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + SPIRIT_DATA.size() + " elemental spirit templates.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", list -> forEach(list, "spirit", this::parseSpirit));
	}
	
	private void parseSpirit(Node spiritNode)
	{
		final NamedNodeMap attributes = spiritNode.getAttributes();
		final byte type = parseByte(attributes, "type");
		final byte stage = parseByte(attributes, "stage");
		final int npcId = parseInteger(attributes, "npcId");
		final int extractItem = parseInteger(attributes, "extractItem");
		final int maxCharacteristics = parseInteger(attributes, "maxCharacteristics");
		final ElementalSpiritTemplateHolder template = new ElementalSpiritTemplateHolder(type, stage, npcId, extractItem, maxCharacteristics);
		SPIRIT_DATA.computeIfAbsent(type, HashMap::new).put(stage, template);
		
		forEach(spiritNode, "level", levelNode ->
		{
			final NamedNodeMap levelInfo = levelNode.getAttributes();
			final int level = parseInteger(levelInfo, "id");
			final int attack = parseInteger(levelInfo, "atk");
			final int defense = parseInteger(levelInfo, "def");
			final int criticalRate = parseInteger(levelInfo, "critRate");
			final int criticalDamage = parseInteger(levelInfo, "critDam");
			final long maxExperience = parseLong(levelInfo, "maxExp");
			template.addLevelInfo(level, attack, defense, criticalRate, criticalDamage, maxExperience);
		});
		
		forEach(spiritNode, "itemToEvolve", itemNode ->
		{
			final NamedNodeMap itemInfo = itemNode.getAttributes();
			final int itemId = parseInteger(itemInfo, "id");
			final int count = parseInteger(itemInfo, "count", 1);
			template.addItemToEvolve(itemId, count);
		});
		
		forEach(spiritNode, "absorbItem", absorbItemNode ->
		{
			final NamedNodeMap absorbInfo = absorbItemNode.getAttributes();
			final int itemId = parseInteger(absorbInfo, "id");
			final int experience = parseInteger(absorbInfo, "experience");
			template.addAbsorbItem(itemId, experience);
		});
	}
	
	public ElementalSpiritTemplateHolder getSpirit(byte type, byte stage)
	{
		if (SPIRIT_DATA.containsKey(type))
		{
			return SPIRIT_DATA.get(type).get(stage);
		}
		return null;
	}
	
	public static ElementalSpiritData getInstance()
	{
		return Singleton.INSTANCE;
	}
	
	private static class Singleton
	{
		static final ElementalSpiritData INSTANCE = new ElementalSpiritData();
	}
}
