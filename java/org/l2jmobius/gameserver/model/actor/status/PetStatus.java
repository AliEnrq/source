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
package org.l2jmobius.gameserver.model.actor.status;

import org.l2jmobius.gameserver.ai.CtrlEvent;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class PetStatus extends SummonStatus
{
	private int _currentFed = 0; // Current Fed of the Pet
	
	public PetStatus(Pet activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public void reduceHp(double value, Creature attacker)
	{
		reduceHp(value, attacker, true, false, false);
	}
	
	@Override
	public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHpConsumption)
	{
		if (getActiveChar().isDead())
		{
			return;
		}
		
		super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);
		
		if (attacker != null)
		{
			if (!isDOT && (getActiveChar().getOwner() != null))
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.C1_DEALS_S2_DAMAGE_TO_THE_PET);
				sm.addString(attacker.getName());
				sm.addInt((int) value);
				getActiveChar().sendPacket(sm);
			}
			getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker);
		}
	}
	
	public int getCurrentFed()
	{
		return _currentFed;
	}
	
	public void setCurrentFed(int value)
	{
		_currentFed = value;
	}
	
	@Override
	public Pet getActiveChar()
	{
		return (Pet) super.getActiveChar();
	}
}
