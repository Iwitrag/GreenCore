package cz.iwitrag.greencore.premium.handlers;

import cz.iwitrag.greencore.Main;
import cz.iwitrag.greencore.helpers.LuckPermsHelper;
import cz.iwitrag.greencore.helpers.StringHelper;
import cz.iwitrag.greencore.helpers.TaskChainHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VipPlusHandler extends Handler {

    @Override
    public void handle() {
        String nick = parameters.get("NICK");
        if (nick == null) {
            solve(Result.FAILURE, "Do SMS nebyla zadána přezdívka", "Nick param missing");
            return;
        }

        final String keyword = "PLUSVIP";
        int months;
        if (variant.getKeyword().length() > keyword.length()) {
            try {
                months = Integer.parseInt(variant.getKeyword().substring(keyword.length()));
            } catch (NumberFormatException e) {
                solve(Result.FAILURE, "Nepodařilo se určit délku VIP+ z SMS", "VIP+ length not parsed");
                return;
            }
        } else {
            months = 1;
        }

        TaskChainHelper.newChain()
        .asyncFirst(() -> LuckPermsHelper.addPlayerToGroup(nick, "vipplus", (long)months*30*24*60*60))
        .syncLast((result) -> {
            if (result) {
                solve(Result.SUCCESS, "VIP+ úspěšně aktivováno!", "VIP+ added");
                TaskChainHelper.newChain()
                .asyncFirst(() -> LuckPermsHelper.getPlayerGroupDuration(nick, "vipplus"))
                .syncLast((duration) -> {
                    if (duration <= 0) {
                        Main.getInstance().getLogger().warning("VIP+ activated but failed to determine its duration?!?!");
                    } else {
                        Player player = Bukkit.getPlayer(nick);
                        if (player != null && player.isOnline()) {
                            player.sendMessage("§eTvůj zbývající čas VIP+: §6" + StringHelper.timeToLongString(duration));
                        }
                    }
                })
                .execute();
            } else {
                solve(Result.FAILURE, "VIP+ se nepodařilo aktivovat", "Failed to add VIP+");
            }
        }).execute();
    }


}
