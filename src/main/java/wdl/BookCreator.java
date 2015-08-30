package wdl;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;

public class BookCreator {
	/**
	 * The owning plugin.
	 */
	private final Plugin plugin;
	
	private class Displayer implements Runnable {
		/**
		 * The player.
		 */
		private final Player player;
		/**
		 * ItemStack to display -- the book.
		 */
		private final ItemStack display;
		/**
		 * ItemStack that was previously held.
		 */
		private final ItemStack held;
		
		/**
		 * Current phase.
		 * 
		 * 0: Set held item to {@link #display}.
		 * 1: Send <code>MC|BOpen</code>.
		 * 2: Set held item to {@link #held}.
		 */
		private int phase;
		
		public Displayer(Player player, ItemStack display, ItemStack held) {
			this.player = player;
			this.display = display;
			this.held = held;
			
			phase = 0;
		}
		
		@Override
		public void run() {
			switch (phase) {
			case 0: {
				player.setItemInHand(display);
				break;
			}
			case 1: {
				player.sendPluginMessage(plugin, BOOK_CHANNEL, new byte[0]);
				break;
			}
			case 2: {
				player.setItemInHand(held);
				break;
			}
			}
			
			phase++;
			if (phase < 3) {
				Bukkit.getScheduler().runTaskLater(plugin, this, 1);
			}
		}
	}
	
	/**
	 * Vanilla minecraft's plugin channel to open the held book.
	 */
	private static final String BOOK_CHANNEL = "MC|BOpen";
	
	public BookCreator(Plugin plugin) {
		if (plugin == null) {
			throw new IllegalArgumentException("plugin must not be null!");
		}
		
		this.plugin = plugin;
	}
	
	/**
	 * 
	 * @param player
	 * @param pages
	 */
	public void openBook(Player player, String title, String... pages) {
		try {
			registerChannelIfNeeded(player);
			
			ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
			
			BookMeta meta = (BookMeta)book.getItemMeta();
			meta.setAuthor(plugin.getName() + " plugin");
			meta.setTitle(title);
			meta.setPages(pages);
			book.setItemMeta(meta);
			
			Bukkit.getScheduler().runTask(plugin,
					new Displayer(player, book, player.getItemInHand()));
		} catch (Exception e) {
			player.sendMessage("§c[" + plugin.getName()
					+ "]: Failed to open a book for you!");
		}
	}
	
	/**
	 * Ensures that the given channel is registered to the player and the
	 * plugin.
	 * 
	 * If it isn't, it tricks the server into thinking it is. <br/>
	 * <code>{@value #BOOK_CHANNEL}</code> is used by vanilla minecraft, but
	 * never <code>REGISTER</code>ed. This works around that.
	 * 
	 * @param player
	 *            The player to ensure the channel is registered to.
	 * @throws Exception
	 *             when the process fails. No attempt to undo any changes is
	 *             undertooken.
	 */
	public void registerChannelIfNeeded(Player player) throws Exception {
		if (!Bukkit.getMessenger().isOutgoingChannelRegistered(plugin,
				BOOK_CHANNEL)) {
			Bukkit.getMessenger().registerOutgoingPluginChannel(plugin,
					BOOK_CHANNEL);
		}
		
		if (!player.getListeningPluginChannels().contains(BOOK_CHANNEL)) {
			// CraftPlayer uses this method.
			
			// public void addChannel(String channel)
			Method addChannel = player.getClass().getMethod("addChannel",
					String.class);
			
			addChannel.invoke(player, BOOK_CHANNEL);
		}
	}
}
