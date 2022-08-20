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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.holders.TimedHuntingZoneHolder;

/**
 * @author Mobius
 */
public class TimedHuntingZoneData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(TimedHuntingZoneData.class.getName());
	
	private final Map<Integer, TimedHuntingZoneHolder> _timedHuntingZoneData = new HashMap<>();
	
	protected TimedHuntingZoneData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_timedHuntingZoneData.clear();
		parseDatapackFile("data/TimedHuntingZoneData.xml");
		
		if (!_timedHuntingZoneData.isEmpty())
		{
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + _timedHuntingZoneData.size() + " timed hunting zones.");
		}
		else
		{
			LOGGER.info(getClass().getSimpleName() + ": System is disabled.");
		}
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node xmlNode = doc.getFirstChild(); xmlNode != null; xmlNode = xmlNode.getNextSibling())
		{
			if ("list".equalsIgnoreCase(xmlNode.getNodeName()))
			{
				final NamedNodeMap listAttributes = xmlNode.getAttributes();
				final Node attribute = listAttributes.getNamedItem("enabled");
				if ((attribute != null) && Boolean.parseBoolean(attribute.getNodeValue()))
				{
					for (Node listNode = xmlNode.getFirstChild(); listNode != null; listNode = listNode.getNextSibling())
					{
						if ("zone".equalsIgnoreCase(listNode.getNodeName()))
						{
							final NamedNodeMap zoneAttributes = listNode.getAttributes();
							int id = parseInteger(zoneAttributes, "id");
							String name = parseString(zoneAttributes, "name", "");
							int initialTime = 0;
							int maxAddedTime = 0;
							int resetDelay = 0;
							int entryItemId = 57;
							int entryFee = 10000;
							int minLevel = 1;
							int maxLevel = 999;
							int remainRefillTime = 3600;
							int refillTimeMax = 3600;
							int instanceId = 0;
							boolean soloInstance = true;
							boolean weekly = false;
							boolean useWorldPrefix = false;
							Location enterLocation = null;
							Location exitLocation = null;
							for (Node zoneNode = listNode.getFirstChild(); zoneNode != null; zoneNode = zoneNode.getNextSibling())
							{
								switch (zoneNode.getNodeName())
								{
									case "enterLocation":
									{
										final String[] coordinates = zoneNode.getTextContent().split(",");
										enterLocation = new Location(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[2]));
										break;
									}
									case "exitLocation":
									{
										final String[] coordinates = zoneNode.getTextContent().split(",");
										exitLocation = new Location(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[2]));
										break;
									}
									case "initialTime":
									{
										initialTime = Integer.parseInt(zoneNode.getTextContent()) * 1000;
										break;
									}
									case "maxAddedTime":
									{
										maxAddedTime = Integer.parseInt(zoneNode.getTextContent()) * 1000;
										break;
									}
									case "resetDelay":
									{
										resetDelay = Integer.parseInt(zoneNode.getTextContent()) * 1000;
										break;
									}
									case "entryItemId":
									{
										entryItemId = Integer.parseInt(zoneNode.getTextContent());
										break;
									}
									case "entryFee":
									{
										entryFee = Integer.parseInt(zoneNode.getTextContent());
										break;
									}
									case "minLevel":
									{
										minLevel = Integer.parseInt(zoneNode.getTextContent());
										break;
									}
									case "maxLevel":
									{
										maxLevel = Integer.parseInt(zoneNode.getTextContent());
										break;
									}
									case "remainRefillTime":
									{
										remainRefillTime = Integer.parseInt(zoneNode.getTextContent());
										break;
									}
									case "refillTimeMax":
									{
										refillTimeMax = Integer.parseInt(zoneNode.getTextContent());
										break;
									}
									case "instanceId":
									{
										instanceId = Integer.parseInt(zoneNode.getTextContent());
										break;
									}
									case "soloInstance":
									{
										soloInstance = Boolean.parseBoolean(zoneNode.getTextContent());
										break;
									}
									case "weekly":
									{
										weekly = Boolean.parseBoolean(zoneNode.getTextContent());
										break;
									}
									case "useWorldPrefix":
									{
										useWorldPrefix = Boolean.parseBoolean(zoneNode.getTextContent());
										break;
									}
								}
							}
							_timedHuntingZoneData.put(id, new TimedHuntingZoneHolder(id, name, initialTime, maxAddedTime, resetDelay, entryItemId, entryFee, minLevel, maxLevel, remainRefillTime, refillTimeMax, instanceId, soloInstance, weekly, useWorldPrefix, enterLocation, exitLocation));
						}
					}
				}
			}
		}
	}
	
	public TimedHuntingZoneHolder getHuntingZone(int zoneId)
	{
		return _timedHuntingZoneData.get(zoneId);
	}
	
	public Collection<TimedHuntingZoneHolder> getAllHuntingZones()
	{
		return _timedHuntingZoneData.values();
	}
	
	public int getSize()
	{
		return _timedHuntingZoneData.size();
	}
	
	public static TimedHuntingZoneData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final TimedHuntingZoneData INSTANCE = new TimedHuntingZoneData();
	}
}
