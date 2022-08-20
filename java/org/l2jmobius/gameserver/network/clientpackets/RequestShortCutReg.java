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
package org.l2jmobius.gameserver.network.clientpackets;

import java.util.List;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.enums.ShortcutType;
import org.l2jmobius.gameserver.model.ShortCuts;
import org.l2jmobius.gameserver.model.Shortcut;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.ShortCutRegister;
import org.l2jmobius.gameserver.network.serverpackets.autoplay.ExActivateAutoShortcut;

public class RequestShortCutReg implements IClientIncomingPacket
{
	private ShortcutType _type;
	private int _id;
	private int _slot;
	private int _page;
	private int _level;
	private int _subLevel;
	private int _characterType; // 1 - player, 2 - pet
	private boolean _active;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		final int typeId = packet.readD();
		_type = ShortcutType.values()[(typeId < 1) || (typeId > 6) ? 0 : typeId];
		final int position = packet.readD();
		_slot = position % ShortCuts.MAX_SHORTCUTS_PER_BAR;
		_page = position / ShortCuts.MAX_SHORTCUTS_PER_BAR;
		_active = packet.readC() == 1; // 228
		_id = packet.readD();
		_level = packet.readH();
		_subLevel = packet.readH(); // Sublevel
		_characterType = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if ((_page > 23) || (_page < 0))
		{
			return;
		}
		
		final Shortcut sc = new Shortcut(_slot, _page, _type, _id, _level, _subLevel, _characterType);
		sc.setAutoUse(_active);
		player.registerShortCut(sc);
		player.sendPacket(new ShortCutRegister(sc));
		player.sendPacket(new ExActivateAutoShortcut(sc, _active));
		
		// When id is not auto used, deactivate auto shortcuts.
		if (!player.getAutoUseSettings().isAutoSkill(_id) && !player.getAutoUseSettings().getAutoSupplyItems().contains(_id))
		{
			final List<Integer> positions = player.getVariables().getIntegerList(PlayerVariables.AUTO_USE_SHORTCUTS);
			final Integer position = _slot + (_page * ShortCuts.MAX_SHORTCUTS_PER_BAR);
			if (!positions.contains(position))
			{
				return;
			}
			
			positions.remove(position);
			player.getVariables().setIntegerList(PlayerVariables.AUTO_USE_SHORTCUTS, positions);
			return;
		}
		
		// Activate if any other similar shortcut is activated.
		for (Shortcut shortcut : player.getAllShortCuts())
		{
			if (!shortcut.isAutoUse() || (shortcut.getId() != _id) || (shortcut.getType() != _type))
			{
				continue;
			}
			
			player.addAutoShortcut(_slot, _page);
			break;
		}
	}
}
