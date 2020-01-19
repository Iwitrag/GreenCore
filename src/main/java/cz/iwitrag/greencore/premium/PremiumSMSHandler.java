package cz.iwitrag.greencore.premium;

import cz.iwitrag.greencore.Main;
import cz.iwitrag.greencore.premium.handlers.Handler;
import cz.iwitrag.greencore.premium.handlers.HandlerFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PremiumSMSHandler {

    public void handleSMS(int smsPrice, String smsVariantKeyword, List<String> smsParams) {

        // load yaml premium config
        Configuration premiumConfiguration;
        try {
            premiumConfiguration = Configuration.loadConfiguration("https://greenlandia.cz/premium.yml");
        } catch (Exception e) {
            e.printStackTrace();
            for (String player : smsParams) {
                notifyAboutProblem(player, "Interní chyba", "Internal error", true);
            }
            return;
        }

        // find service and variant from SMS
        PremiumService service = null;
        PremiumVariant variant = null;
        for (PremiumService loopedService : premiumConfiguration.getServices()) {
            variant = loopedService.getVariantByKeyword(smsVariantKeyword);
            if (variant != null) {
                service = loopedService;
                break;
            }
        }
        if (variant == null) {
            for (String player : smsParams) {
                notifyAboutProblem(player, "Tvar SMS se nepodařilo rozpoznat", "Unknown service", true);
            }
            return;
        }

        // map params
        Map<String, String> parameters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        List<String> variantParams = variant.getParams();
        for (int i = 0; i < variantParams.size(); i++) {
            parameters.put(variantParams.get(i), i < smsParams.size() ? smsParams.get(i) : null);
        }
        String playerName = parameters.get("NICK");


        // handle errors
        if (!service.isActive()) {
            notifyAboutProblem(playerName, "Objednaná služba není aktivní", "Service not active", true);
            return;
        }
        if (smsPrice < variant.getPrice()) {
            notifyAboutProblem(playerName, "Objednaná služba stojí více peněz", "Paid cash too low", true);
            return;
        }
        if (smsPrice > variant.getPrice()) {
            notifyAboutProblem(playerName, "Zaplatil(a) si více než kolik stojí služba", "Paid too much", false);
        }

        // some messages
        if (playerName != null) {
            Bukkit.broadcastMessage("§dHráč §5" + playerName + " §dpodpořil Greenlandii a získal §5" + variant.getFriendlyName() + "§d! Děkujeme!");
        }
        else
            Bukkit.broadcastMessage("§dJeden z hráčů podpořil Greenlandii a získal §5" + variant.getFriendlyName() + " §d! Děkujeme!");

        // pass to handlers
        Handler handler = HandlerFactory.getHandler(service.getInternalName());
        handler.handle(this, variant, parameters);
    }

    public void passHandleResult(Handler.HandleResult result) {
        if (result.getResult() == Handler.Result.SUCCESS) {
            String shortLogReason = result.getShortLogReason();
            String longCzechReason = result.getLongCzechReason();

            if (shortLogReason != null && !shortLogReason.trim().isEmpty())
            Main.getInstance().getLogger().info("Premium SMS processed successfully - " + shortLogReason);
            else
                Main.getInstance().getLogger().info("Premium SMS processed successfully.");

            String playerName = result.getPlayerName();
            if (playerName != null && !playerName.trim().isEmpty()) {
                Player player = Bukkit.getPlayer(result.getPlayerName());
                if (player != null && player.isOnline()) {
                    player.sendMessage("§aDěkujeme za podporu Greenlandia.cz!");
                    if (longCzechReason != null && !longCzechReason.trim().isEmpty()) {
                        for (String message : longCzechReason.split("\\n")) {
                            player.sendMessage("§a" + message);
                        }
                    }
                }
            }
        } else {
            notifyAboutProblem(result.getPlayerName(), result.getLongCzechReason(), result.getShortLogReason(), result.getResult() == Handler.Result.FAILURE);
        }
    }

    private void notifyAboutProblem(String playerName, String longCzechReason, String shortLogReason, boolean isFatal) {
        String loggerMessage = "";
        if (isFatal)
            loggerMessage += "SMS processing failed (";
        else
            loggerMessage += "SMS processing warning (";
        loggerMessage += shortLogReason + ")";

        if (playerName != null && !playerName.trim().isEmpty()) {
            Player player = Bukkit.getPlayer(playerName);
            if (player != null && player.isOnline()) {
                if (isFatal) {
                    player.sendMessage("§aDěkujeme za podporu Greenlandia.cz!");
                }
                player.sendMessage("§cTvá platba byla přijata, ale došlo k problému:");
                for (String message : longCzechReason.split("\\n")) {
                    player.sendMessage("§c" + message);
                }
                if (!isFatal)
                    player.sendMessage("§2To ale nevadí, objednanou službu jsme ti i přesto aktivovali :)");
                player.sendMessage("§cKontaktuj nás prosím na e-mailu §6platby@greenlandia.cz §ca napiš tam text SMS, číslo na které byla SMS odeslána a aktuální čas.");
                loggerMessage += ", player " + playerName + " was informed to write an e-mail.";
            } else {
                loggerMessage += ", no player was informed to write an e-mail.";
            }
        } else {
            loggerMessage += ", no player was informed to write an e-mail.";
        }
        Main.getInstance().getLogger().warning(loggerMessage);
    }

}
