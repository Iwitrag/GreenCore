package cz.iwitrag.greencore.gameplay.lottery;

public abstract class Lottery {

    TicketsStorage tickets;

    public Lottery() {
        tickets = new TicketsStorage();
    }

}
