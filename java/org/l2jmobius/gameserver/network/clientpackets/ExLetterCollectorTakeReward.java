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

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemChanceHolder;
import org.l2jmobius.gameserver.model.itemcontainer.PlayerInventory;
import org.l2jmobius.gameserver.network.GameClient;

/**
 * @author Index, Mobius
 */
public class ExLetterCollectorTakeReward implements IClientIncomingPacket
{
	// Items
	private static final int A = 3875;
	private static final int E = 3877;
	private static final int G = 3879;
	private static final int I = 3881;
	private static final int L = 3882;
	private static final int N = 3883;
	private static final int R = 3885;
	private static final int M = 34956;
	private static final int S = 3886;
	private static final int T = 3886;
	private static final int H = 3880;
	private static final int II = 3888;
	private static final int D = 92021;
	private static final int K = 93412;
	private static final int U = 93413;
	
	// Rewards
	private static final ItemChanceHolder[] LINEAGE_II_REWARDS =
	{
		new ItemChanceHolder(49683, 5, 1), // Talisman of Baium
		new ItemChanceHolder(91119, 5, 1), // Ignis' Necklace
		new ItemChanceHolder(91117, 5, 1), // Nebula's Necklace
		new ItemChanceHolder(91121, 5, 1), // Procella's Necklace
		new ItemChanceHolder(91123, 5, 1), // Petram's Necklace
		new ItemChanceHolder(91952, 5, 1), // Ring of Insolance
		new ItemChanceHolder(91953, 5, 1), // Dragon Valley's Earring
		new ItemChanceHolder(91035, 15, 1), // Water Spirit Ore
		new ItemChanceHolder(91036, 15, 1), // Fire Spirit Ore
		new ItemChanceHolder(91037, 15, 1), // Wind Spirit Ore
		new ItemChanceHolder(91038, 15, 1), // Earth Spirit Ore
		new ItemChanceHolder(91641, 30, 1), // Sayha's Blessing
		new ItemChanceHolder(49674, 30, 1), // XP Growth Scroll
		new ItemChanceHolder(90907, 30, 5), // Soulshot Ticket
		new ItemChanceHolder(91757, 30, 1), // Magic Lamp Charging Potion
		new ItemChanceHolder(91974, 30, 10), // HP Recovery Potion
		new ItemChanceHolder(3031, 30, 10), // Spirit Ore
	};
	private static final ItemChanceHolder[] DEATH_REWARDS =
	{
		new ItemChanceHolder(91012, 5, 1), // Top-grade A-grade Weapon Pack
		new ItemChanceHolder(93459, 5, 1), // A-grade Armor Pack - Majestic Equipment
		new ItemChanceHolder(93460, 5, 1), // A-grade Armor Pack - Equipment of Nightmare
		new ItemChanceHolder(93461, 5, 1), // A-grade Armor Pack - Tallum Equipment
		new ItemChanceHolder(93462, 5, 1), // A-grade Armor Pack - Dark Crystal Equipment
		new ItemChanceHolder(90015, 15, 1), // Top-grade Life Stone - Weapon
		new ItemChanceHolder(93100, 15, 1), // Mid-grade Life Stone Shield / Sigil
		new ItemChanceHolder(91938, 15, 1), // Primeval Isle's Time Stone
		new ItemChanceHolder(93699, 15, 1), // Charging Stone of Random Crafting - 1 charge
		new ItemChanceHolder(91641, 30, 1), // Sayha's Blessing
		new ItemChanceHolder(49674, 30, 1), // XP Growth Scroll
		new ItemChanceHolder(90907, 30, 5), // Soulshot Ticket
		new ItemChanceHolder(91757, 30, 1), // Magic Lamp Charging Potion
		new ItemChanceHolder(91974, 30, 10), // HP Recovery Potion
		new ItemChanceHolder(3031, 30, 10), // Spirit Ore
	};
	private static final ItemChanceHolder[] KNIGHT_REWARDS =
	{
		new ItemChanceHolder(93103, 5, 1), // Spellbook: Divine Beam
		new ItemChanceHolder(92401, 5, 1), // Spellbook: White Guardian
		new ItemChanceHolder(91945, 5, 1), // Book of Shadows
		new ItemChanceHolder(91944, 5, 1), // Book of Light
		new ItemChanceHolder(91943, 15, 1), // Crystal of Shadows
		new ItemChanceHolder(91942, 15, 1), // Crystal of Light
		new ItemChanceHolder(8619, 15, 1), // Buff Expansion Book Lv. 2
		new ItemChanceHolder(8620, 15, 1), // Buff Expansion Book Lv. 3
		new ItemChanceHolder(90045, 15, 1), // Magical Tablet
		new ItemChanceHolder(91641, 30, 1), // Sayha's Blessing
		new ItemChanceHolder(49674, 30, 1), // XP Growth Scroll
		new ItemChanceHolder(90907, 30, 5), // Soulshot Ticket
		new ItemChanceHolder(91757, 30, 1), // Magic Lamp Charging Potion
		new ItemChanceHolder(91974, 30, 10), // HP Recovery Potion
		new ItemChanceHolder(3031, 30, 10), // Spirit Ore
	};
	private static final ItemChanceHolder[] SUMMER_REWARDS =
	{
		new ItemChanceHolder(93976, 5, 1), // Blessed Scroll: Enchant A-grade Weapon
		new ItemChanceHolder(93977, 5, 1), // Blessed Scroll: Enchant A-grade Armor
		new ItemChanceHolder(729, 5, 1), // Scroll: Enchant A-grade Weapon
		new ItemChanceHolder(730, 5, 1), // Scroll: Enchant A-grade Armor
		new ItemChanceHolder(947, 15, 1), // Scroll: Enchant B-grade Weapon
		new ItemChanceHolder(948, 15, 1), // Scroll: Enchant B-grade Armor
		new ItemChanceHolder(91967, 15, 1), // Scroll: Enchant Dragon Valley's Earring
		new ItemChanceHolder(91966, 15, 1), // Scroll: Enchant Ring of Insolance
		new ItemChanceHolder(91641, 30, 1), // Sayha's Blessing
		new ItemChanceHolder(91780, 30, 1), // Battle Scroll
		new ItemChanceHolder(93486, 30, 1), // Combat Scroll
		new ItemChanceHolder(49674, 30, 1), // XP Growth Scroll
		new ItemChanceHolder(90907, 30, 5), // Soulshot Ticket
		new ItemChanceHolder(1538, 30, 1), // Improved Scroll of Escape
		new ItemChanceHolder(91757, 30, 1), // Magic Lamp Charging Potion
		new ItemChanceHolder(91974, 30, 10), // HP Recovery Potion
		new ItemChanceHolder(3031, 30, 10), // Spirit Ore
	};
	
	private int _wordId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_wordId = packet.readD();
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
		
		final PlayerInventory inventory = player.getInventory();
		if (inventory == null)
		{
			return;
		}
		
		switch (_wordId)
		{
			case 0:
			{
				if ((inventory.getInventoryItemCount(L, -1) < 1) || //
					(inventory.getInventoryItemCount(I, -1) < 1) || //
					(inventory.getInventoryItemCount(N, -1) < 1) || //
					(inventory.getInventoryItemCount(E, -1) < 2) || //
					(inventory.getInventoryItemCount(A, -1) < 1) || //
					(inventory.getInventoryItemCount(G, -1) < 1) || //
					(inventory.getInventoryItemCount(II, -1) < 1))
				{
					return;
				}
				
				player.destroyItemByItemId("LetterCollector", L, 1, player, true);
				player.destroyItemByItemId("LetterCollector", I, 1, player, true);
				player.destroyItemByItemId("LetterCollector", N, 1, player, true);
				player.destroyItemByItemId("LetterCollector", E, 1, player, true);
				player.destroyItemByItemId("LetterCollector", A, 1, player, true);
				player.destroyItemByItemId("LetterCollector", G, 1, player, true);
				player.destroyItemByItemId("LetterCollector", E, 1, player, true);
				player.destroyItemByItemId("LetterCollector", II, 1, player, true);
				
				player.addItem("LetterCollector", getRandomReward(LINEAGE_II_REWARDS), player, true);
				break;
			}
			case 1:
			{
				if ((inventory.getInventoryItemCount(D, -1) < 1) || //
					(inventory.getInventoryItemCount(E, -1) < 1) || //
					(inventory.getInventoryItemCount(A, -1) < 1) || //
					(inventory.getInventoryItemCount(T, -1) < 1) || //
					(inventory.getInventoryItemCount(H, -1) < 1))
				{
					return;
				}
				
				player.destroyItemByItemId("LetterCollector", D, 1, player, true);
				player.destroyItemByItemId("LetterCollector", E, 1, player, true);
				player.destroyItemByItemId("LetterCollector", A, 1, player, true);
				player.destroyItemByItemId("LetterCollector", T, 1, player, true);
				player.destroyItemByItemId("LetterCollector", H, 1, player, true);
				
				player.addItem("LetterCollector", getRandomReward(DEATH_REWARDS), player, true);
				break;
			}
			case 2:
			{
				if ((inventory.getInventoryItemCount(K, -1) < 1) || //
					(inventory.getInventoryItemCount(N, -1) < 1) || //
					(inventory.getInventoryItemCount(I, -1) < 1) || //
					(inventory.getInventoryItemCount(G, -1) < 1) || //
					(inventory.getInventoryItemCount(H, -1) < 1) || //
					(inventory.getInventoryItemCount(T, -1) < 1))
				{
					return;
				}
				
				player.destroyItemByItemId("LetterCollector", K, 1, player, true);
				player.destroyItemByItemId("LetterCollector", N, 1, player, true);
				player.destroyItemByItemId("LetterCollector", I, 1, player, true);
				player.destroyItemByItemId("LetterCollector", G, 1, player, true);
				player.destroyItemByItemId("LetterCollector", H, 1, player, true);
				player.destroyItemByItemId("LetterCollector", T, 1, player, true);
				
				player.addItem("LetterCollector", getRandomReward(KNIGHT_REWARDS), player, true);
				break;
			}
			case 3:
			{
				if ((inventory.getInventoryItemCount(S, -1) < 1) || //
					(inventory.getInventoryItemCount(U, -1) < 1) || //
					(inventory.getInventoryItemCount(M, -1) < 2) || //
					(inventory.getInventoryItemCount(E, -1) < 1) || //
					(inventory.getInventoryItemCount(R, -1) < 1))
				{
					return;
				}
				
				player.destroyItemByItemId("LetterCollector", S, 1, player, true);
				player.destroyItemByItemId("LetterCollector", U, 1, player, true);
				player.destroyItemByItemId("LetterCollector", M, 1, player, true);
				player.destroyItemByItemId("LetterCollector", M, 1, player, true);
				player.destroyItemByItemId("LetterCollector", E, 1, player, true);
				player.destroyItemByItemId("LetterCollector", R, 1, player, true);
				
				player.addItem("LetterCollector", getRandomReward(SUMMER_REWARDS), player, true);
				break;
			}
		}
	}
	
	private ItemChanceHolder getRandomReward(ItemChanceHolder[] rewards)
	{
		ItemChanceHolder reward = null;
		while (reward == null)
		{
			final ItemChanceHolder random = rewards[Rnd.get(rewards.length)];
			if ((Rnd.get(100) < random.getChance()) && (ItemTable.getInstance().getTemplate(random.getId()) != null))
			{
				reward = random;
			}
		}
		return reward;
	}
}