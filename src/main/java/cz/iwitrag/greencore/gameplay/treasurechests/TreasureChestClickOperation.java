package cz.iwitrag.greencore.gameplay.treasurechests;

import cz.iwitrag.greencore.helpers.Percent;

public class TreasureChestClickOperation {

    private OperationType type;
    /** Used only with Operation.COPY and Operation.DELETE */
    private TreasureChest tChest;
    /** Used only with Operation.CREATE */
    private Percent chance;

    private TreasureChestClickOperation() {}

    public static TreasureChestClickOperation selectOperation() {
        TreasureChestClickOperation operation = new TreasureChestClickOperation();
        operation.type = OperationType.SELECT;
        return operation;
    }

    public static TreasureChestClickOperation createOperation(Percent chance) {
        TreasureChestClickOperation operation = new TreasureChestClickOperation();
        operation.type = OperationType.CREATE;
        operation.chance = chance.getNormalizedCopy();
        return operation;
    }

    public static TreasureChestClickOperation copyOperation(TreasureChest tChest) {
        TreasureChestClickOperation operation = new TreasureChestClickOperation();
        operation.type = OperationType.COPY;
        operation.tChest = tChest;
        return operation;
    }

    public static TreasureChestClickOperation deleteOperation(TreasureChest tChest) {
        TreasureChestClickOperation operation = new TreasureChestClickOperation();
        operation.type = OperationType.DELETE;
        operation.tChest = tChest;
        return operation;
    }

    public static TreasureChestClickOperation breakOperation(TreasureChest tChest) {
        TreasureChestClickOperation operation = new TreasureChestClickOperation();
        operation.type = OperationType.BREAK;
        operation.tChest = tChest;
        return operation;
    }

    public OperationType getType() {
        return type;
    }

    public TreasureChest getTChest() {
        return tChest;
    }

    public Percent getChance() {
        return chance;
    }

    public enum OperationType {
        /** Selecting tchest for future commands */
        SELECT,
        /** Creating new tchest */
        CREATE,
        /** Pasting copied tchest settings */
        COPY,
        /** Confirming deletion of tchest */
        DELETE,
        /** Confirming break-deletion of tchest */
        BREAK
    }

}
