package hinadamari.bossrename;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * BossRename
 * @author hinadamari
 */
public class BossRename extends JavaPlugin implements Listener {

	private final String FILENAME = "name.txt";
	private static HashMap<String, String> costomname = new HashMap<String, String>();
	private final static Logger log = Logger.getLogger("Minecraft");

    /**
     * プラグインが有効になったときに呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    public void onEnable(){

    	this.loadFiles();
        getServer().getPluginManager().registerEvents(this, this);

        log.info("[BossRename] v" + getDescription().getVersion() + " is Enabled!");

    }

    /**
     * コマンド呼出時処理
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        if(cmd.getName().equalsIgnoreCase("BossRename"))
        {
            if(args.length > 0)
            {
                // コンフィグ再読込
                if(args[0].equalsIgnoreCase("Reload"))
                {
                    if(!sender.hasPermission("bossrename.reload"))
                    {
                        sender.sendMessage("You don't have bossrename.reload");
                        return true;
                    }
                    this.loadFiles();
                    sender.sendMessage(ChatColor.GREEN + "BossRename has been reloaded.");
                    return true;
                }
            }
        }
        return false;
    }

    /**
	 * 設定ファイルの読み込み
	 */
	private void loadFiles() {

		// フォルダやファイルがない場合は、作成したりする
		File dir = new File(getDataFolder().getAbsolutePath());
		if ( !dir.exists() ) {
			dir.mkdirs();
		}

		File file = new File(getDataFolder(), FILENAME);
		if ( !file.exists() ) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				log.info(e.getMessage());
			}
		}

		// 設定ファイルを読み込む
		this.loadConfig(getFile(), file, FILENAME, false);

		// ジエンドにいるエンダードラゴンの名前を変更
		this.renameEnderdragon();

	}

	/**
	 * コンフィグファイル読込処理
	 */
	private void loadConfig(File jarFile, File targetFile, String sourceFilePath, boolean isBinary) {

		try{
			File file = new File(getDataFolder(), FILENAME);
			InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
			BufferedReader br = new BufferedReader(isr);

			costomname.clear();

			// Stringとして一行ずつ読み取る。
			String line;
			String[] sp;
			while( (line = br.readLine()) != null) {
				sp = line.split(":");
				if (sp.length < 2) continue;
				costomname.put(sp[0], sp[1].trim());
			}
			isr.close();
			br.close();

			// 足りない設定情報を読み込む
			JarFile jar = new JarFile(jarFile);
			ZipEntry zipEntry = jar.getEntry(sourceFilePath);
			InputStream is = jar.getInputStream(zipEntry);
			FileOutputStream fos = new FileOutputStream(targetFile, true);
			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			OutputStreamWriter osw = new OutputStreamWriter(fos , "UTF-8");

			while ((line = br.readLine()) != null) {
				sp = line.split(":");
				if (!costomname.containsKey(sp[0])) {
					costomname.put(sp[0], sp[1]);
					osw.write(line + "\n");
				}
			}
			osw.flush();
			osw.close();
			br.close();
			fos.flush();
			fos.close();
			is.close();
			jar.close();

		}catch(FileNotFoundException e){
			log.info(e.getMessage());
		}catch(IOException e){
			log.info(e.getMessage());
		}

	}

	/**
	 * Enderdragonの名前を変更
	 */
	public void renameEnderdragon() {

		log.info("[BossRename] Renaming EnderDragons in THE_END");

		String name = costomname.get(EntityType.ENDER_DRAGON.getName().toLowerCase());

		for (World world : getServer().getWorlds()) {
			if (world.getEnvironment() == Environment.THE_END) {
				for (Entity entity : world.getEntities()) {
					if (entity.getType() == EntityType.ENDER_DRAGON) {
						((LivingEntity) entity).setCustomName(name);
					}
				}
			}
		}

	}

	/**
     * MOBがスポーンした時の処理
     * @param event
     */
    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {

    	switch (event.getEntityType()) {
    		case WITHER:
    		case ENDER_DRAGON:
    			event.getEntity().setCustomName(costomname.get(event.getEntityType().getName().toLowerCase()));
    			break;
    	}

    }


}
