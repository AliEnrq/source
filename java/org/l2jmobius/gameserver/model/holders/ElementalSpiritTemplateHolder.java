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
package org.l2jmobius.gameserver.model.holders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JoeAlisson
 */
public class ElementalSpiritTemplateHolder
{
	private final byte _type;
	private final byte _stage;
	private final int _npcId;
	private final int _maxCharacteristics;
	private final int _extractItem;
	
	private final Map<Integer, SpiritLevel> _levels;
	private List<ItemHolder> _itemsToEvolve;
	private List<ElementalSpiritAbsorbItemHolder> _absorbItems;
	
	public ElementalSpiritTemplateHolder(byte type, byte stage, int npcId, int extractItem, int maxCharacteristics)
	{
		_type = type;
		_stage = stage;
		_npcId = npcId;
		_extractItem = extractItem;
		_maxCharacteristics = maxCharacteristics;
		_levels = new HashMap<>(10);
	}
	
	public void addLevelInfo(int level, int attack, int defense, int criticalRate, int criticalDamage, long maxExperience)
	{
		final SpiritLevel spiritLevel = new SpiritLevel();
		spiritLevel.attack = attack;
		spiritLevel.defense = defense;
		spiritLevel.criticalRate = criticalRate;
		spiritLevel.criticalDamage = criticalDamage;
		spiritLevel.maxExperience = maxExperience;
		_levels.put(level, spiritLevel);
	}
	
	public void addItemToEvolve(Integer itemId, Integer count)
	{
		if (_itemsToEvolve == null)
		{
			_itemsToEvolve = new ArrayList<>(2);
		}
		_itemsToEvolve.add(new ItemHolder(itemId, count));
	}
	
	public byte getType()
	{
		return _type;
	}
	
	public byte getStage()
	{
		return _stage;
	}
	
	public int getNpcId()
	{
		return _npcId;
	}
	
	public long getMaxExperienceAtLevel(int level)
	{
		final SpiritLevel spiritLevel = _levels.get(level);
		return spiritLevel == null ? 0 : spiritLevel.maxExperience;
	}
	
	public int getMaxLevel()
	{
		return _levels.size();
	}
	
	public int getAttackAtLevel(int level)
	{
		return _levels.get(level).attack;
	}
	
	public int getDefenseAtLevel(int level)
	{
		return _levels.get(level).defense;
	}
	
	public int getCriticalRateAtLevel(int level)
	{
		return _levels.get(level).criticalRate;
	}
	
	public int getCriticalDamageAtLevel(int level)
	{
		return _levels.get(level).criticalDamage;
	}
	
	public int getMaxCharacteristics()
	{
		return _maxCharacteristics;
	}
	
	public List<ItemHolder> getItemsToEvolve()
	{
		return _itemsToEvolve == null ? Collections.emptyList() : _itemsToEvolve;
	}
	
	public void addAbsorbItem(Integer itemId, Integer experience)
	{
		if (_absorbItems == null)
		{
			_absorbItems = new ArrayList<>();
		}
		_absorbItems.add(new ElementalSpiritAbsorbItemHolder(itemId, experience));
	}
	
	public List<ElementalSpiritAbsorbItemHolder> getAbsorbItems()
	{
		return _absorbItems == null ? Collections.emptyList() : _absorbItems;
	}
	
	public int getExtractItem()
	{
		return _extractItem;
	}
	
	private static class SpiritLevel
	{
		public SpiritLevel()
		{
		}
		
		long maxExperience;
		int criticalDamage;
		int criticalRate;
		int defense;
		int attack;
	}
}
