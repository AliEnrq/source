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
package org.l2jmobius.gameserver.model.options;

import java.util.logging.Logger;

/**
 * @author Pere, Mobius
 */
public class Variation
{
	private static final Logger LOGGER = Logger.getLogger(Variation.class.getSimpleName());
	
	private final int _mineralId;
	private final OptionDataGroup[] _effects = new OptionDataGroup[2];
	
	public Variation(int mineralId)
	{
		_mineralId = mineralId;
	}
	
	public int getMineralId()
	{
		return _mineralId;
	}
	
	public void setEffectGroup(int order, OptionDataGroup group)
	{
		_effects[order] = group;
	}
	
	public Options getRandomEffect(int order, int targetItemId)
	{
		if (_effects == null)
		{
			LOGGER.warning("Null effect: for mineral " + _mineralId + ", order " + order);
			return null;
		}
		if (_effects[order] == null)
		{
			return null;
		}
		return _effects[order].getRandomEffect(targetItemId);
	}
}