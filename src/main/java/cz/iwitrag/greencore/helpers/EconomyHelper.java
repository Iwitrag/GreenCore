package cz.iwitrag.greencore.helpers;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;

import java.math.BigDecimal;

public class EconomyHelper {

    private EconomyHelper() {}

    public static boolean giveMoney(String playerName, double amount) {
        try {
            Economy.add(playerName, BigDecimal.valueOf(amount));
        } catch (UserDoesNotExistException | NoLoanPermittedException e) {
            return false;
        }

        return true;
    }

    public static boolean takeMoney(String playerName, double amount) {
        try {
            Economy.substract(playerName, BigDecimal.valueOf(amount));
        } catch (UserDoesNotExistException | NoLoanPermittedException e) {
            return false;
        }

        return true;
    }

}
