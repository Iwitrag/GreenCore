package cz.iwitrag.greencore.gameplay.playerskills;

import cz.iwitrag.greencore.playerbase.GPlayer;
import cz.iwitrag.greencore.playerbase.GPlayersManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Arrays;

public class SkillsListener implements Listener {

    @EventHandler
    public void onPlayerMining(BlockBreakEvent event) {

        // DELETE ME !!!
        if (!event.getPlayer().getName().equals("Iwitrag")) return;

        if (event.getPlayer().getInventory().getItemInMainHand().getType().toString().toLowerCase().contains("pickaxe") &&
                Arrays.asList(Material.STONE, Material.ANDESITE, Material.DIORITE, Material.GRANITE).contains(event.getBlock().getType())) {
            GPlayer gPlayer = GPlayersManager.getInstance().getGPlayer(event.getPlayer());
            double expBefore = gPlayer.getSkillStatus(Skill.MINING).getExperience();
            int currentLevel = gPlayer.getSkillStatus(Skill.MINING).getLevel();
            int messageInterval = 20 * currentLevel;
            double gainedExp = 1.00;
            gPlayer.addSkillExperience(Skill.MINING, gainedExp);
            if ((int)(expBefore / messageInterval) != (int)((expBefore + gainedExp) / messageInterval)) {
                event.getPlayer().sendMessage("§8Tvé zkušenosti v §7" + Skill.MINING + "§8 rostou! (§7" + (int)(expBefore + gainedExp) + "§8 / " +
                        (int) SkillsExpTable.getExpForNextLevel(currentLevel) + ")");
            }
        }
    }

}
