package cz.iwitrag.greencore.premium.handlers;

import cz.iwitrag.greencore.premium.PremiumSMSHandler;
import cz.iwitrag.greencore.premium.PremiumVariant;

import java.util.Map;

public abstract class Handler {

    public enum Result {SUCCESS, WARNING, FAILURE}

    private PremiumSMSHandler origin;
    PremiumVariant variant;
    Map<String, String> parameters;

    public void handle(PremiumSMSHandler origin, PremiumVariant variant, Map<String, String> parameters) {
        this.origin = origin;
        this.variant = variant;
        this.parameters = parameters;
        handle();
    }

    abstract void handle();

    final void solve(Result result, String longCzechReason, String shortLogReason) {
        origin.passHandleResult(new HandleResult(result, parameters.get("NICK"), longCzechReason, shortLogReason));
    }

    public class HandleResult {

        private Result result;
        private String playerName;
        private String longCzechReason;
        private String shortLogReason;

        private HandleResult(Result result, String playerName, String longCzechReason, String shortLogReason) {
            this.result = result;
            this.playerName = playerName;
            this.longCzechReason = longCzechReason;
            this.shortLogReason = shortLogReason;
        }

        public Result getResult() {
            return result;
        }

        public String getPlayerName() {
            return playerName;
        }

        public String getLongCzechReason() {
            return longCzechReason;
        }

        public String getShortLogReason() {
            return shortLogReason;
        }
    }
}
