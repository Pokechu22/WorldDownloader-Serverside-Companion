package wdl;

import java.lang.reflect.Method;
import java.security.NoSuchProviderException;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Handles interfacing with Vault, even if Vault is not installed.
 */
public class VaultHandler {
	/**
	 * Permission-handling object.  May be an instance of
	 * {@link net.milkbowl.vault.permission.Permission} (vault), or
	 * it may be a custom object.  However, it is KNOWN to have a method
	 * with the signature <code>has(CommandSender, String)</code>.
	 */
	private final Object permissionHandler;
	/**
	 * {@link #permissionHandler}'s class, as returned by
	 * {@link Object#getClass()}.
	 */
	private final Class<?> permissionHandlerClass;
	/**
	 * {@link #permissionHandler}'s <code>has(CommandSender, String)</code> method.
	 */
	private final Method hasMethod;
	
	private final WDLCompanion plugin;
	
	/**
	 * Creates and sets up a new {@link VaultHandler}.
	 * @param plugin
	 */
	VaultHandler(WDLCompanion plugin) {
		plugin.getLogger().info("Attempting to link with Vault for permission support...");
		
		this.plugin = plugin;
		
		Object permissionHandler;
		
		try {
			Class<?> permissionClass = Class
					.forName("net.milkbowl.vault.permission.Permission");
			
			// Get the service provider for Permission.
			RegisteredServiceProvider<?> provider = plugin.getServer()
					.getServicesManager().getRegistration(permissionClass);
			
			if (provider == null) {
				//Technically NoSuchProviderException is for something else,
				//but its name works well enough here.
				throw new NoSuchProviderException(
						"RegisteredServiceProvider for " + permissionClass
								+ " is null!");
			}
			Object permission = provider.getProvider();
			if (permission == null) {
				throw new NoSuchProviderException(
						"RegisteredServiceProvider for " + permission
								+ " is not null (" + provider + ") but failed "
								+ "to provide anything!  "
								+ "(provider.getProvider() returned null)");
			}
			
			// Sanity check - there should be a method "has(Player, String)"
			// Will throw a NoSuchMethodException if the method is missing.
			Method method = permission.getClass().getMethod("has",
					CommandSender.class, String.class);
			
			if (!method.getReturnType().equals(boolean.class)) {
				throw new AssertionError("Permission (" + permission + ", "
						+ permission.getClass() + ") has a method named "
						+ "'has(CommandSender, String)', but that method doesn't "
						+ "return a boolean!  Actual return type: "
						+ method.getReturnType() + ", method signature: "
						+ method.toGenericString() + ")");
			}
			
			permissionHandler = permission;
			
			plugin.getLogger().info("Successfully linked with Vault: Using "
							+ permission + " (" + permission.getClass().getName()
							+ ") for permissions.");
		} catch (ClassNotFoundException | NoSuchMethodException | LinkageError
				| NoSuchProviderException e) {
			//Also includes ExceptionInInitializerError
			plugin.getLogger().log(Level.WARNING,
					"Failed to link with Vault: ", e);
			plugin.getLogger().warning("You may not have vault installed, in "
						+ "which case this is normal and you don't need to worry.  "
						+ "(Vault allows easier linking to different permission "
						+ "plugins; get it at http://dev.bukkit.org/server-mods/vault)");
			plugin.getLogger().warning("Using bukkit permission system instead...");
			
			permissionHandler = new Object() {
				// Accessed via reflection.
				@SuppressWarnings("unused")
				public boolean has(CommandSender player, String permission) {
					return player.hasPermission(permission);
				}
			};
		}
		
		this.permissionHandler = permissionHandler;
		this.permissionHandlerClass = permissionHandler.getClass();
		try {
			this.hasMethod = permissionHandlerClass.getMethod("has",
					CommandSender.class, String.class);
		} catch (Exception e) {
			//Shouldn't happen at this point.
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Check if the given player has the given permission.
	 * 
	 * @param player The player to check.
	 * @param perm The permission name.
	 * @return Whether the player has permission.
	 */
	public boolean hasPermission(CommandSender player, String perm) {
		try {
			return (boolean) hasMethod.invoke(permissionHandler, player, perm);
		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE,
					"Failed to perform permissions check - call to "
							+ hasMethod + " with player " + player + " and perm "
							+ perm + " threw an exception.  Treating as if " 
							+ "the player does not have the permission.", e);
			return false;
		}
	}
}
