package cz.iwitrag.greencore.gameplay.lottery;

import cz.iwitrag.greencore.helpers.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TicketsStorage {

    private List<Ticket> tickets = new ArrayList<>();
    private Random random = new Random();

    public void addTickets(String playerName, int amount) {
        for (int i = 0; i < amount; i++) {
            tickets.add(new Ticket(playerName));
        }
    }

    public List<Ticket> pickRandomTickets(int amount) {
        List<Ticket> result = new ArrayList<>();
        if (tickets.size() <= amount) {
            result.addAll(tickets);
        }
        else {
            List<Ticket> ticketsCopy = new ArrayList<>(tickets);
            for (int i = 0; i < amount; i++) {
                Ticket pickedTicket = Utils.pickRandomElement(ticketsCopy);
                ticketsCopy.remove(pickedTicket);
                result.add(pickedTicket);
            }
        }
        return result;
    }

    public void clearTickets() {
        tickets.clear();
    }

}
