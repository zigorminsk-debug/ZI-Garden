package by.csl.gardener;

public final class Operation {
    public static final String FEED_LEAF = "feed_leaf";
    public static final String FEED_ROOT = "feed_root";
    public static final String HARVEST = "harvest";
    public static final String MONITOR = "monitor";
    public static final String MOW = "mow";
    public static final String MULCH = "mulch";
    public static final String PLANT = "plant";
    public static final String PROTECT = "protect";
    public static final String PRUNE = "prune";
    public static final String REPAIR = "repair";
    public static final String SOIL = "soil";
    public static final String SPRAY = "spray";
    public static final String WATER = "water";
    public static final String WHITEN = "whiten";

    private Operation() {
    }

    public static String label(String str) {
        return SPRAY.equals(str) ? "Обработка (опрыскивание)" : PRUNE.equals(str) ? "Обрезка" : FEED_ROOT.equals(str) ? "Подкормка под корень" : FEED_LEAF.equals(str) ? "Подкормка по листу" : WHITEN.equals(str) ? "Побелка" : WATER.equals(str) ? "Полив" : MULCH.equals(str) ? "Мульчирование" : SOIL.equals(str) ? "Работа с почвой" : PROTECT.equals(str) ? "Защита / укрытие" : HARVEST.equals(str) ? "Сбор урожая" : MONITOR.equals(str) ? "Осмотр и мониторинг" : MOW.equals(str) ? "Стрижка" : PLANT.equals(str) ? "Посадка" : REPAIR.equals(str) ? "Лечение ран" : "Работа";
    }

    public static String icon(String str) {
        return SPRAY.equals(str) ? "💦" : PRUNE.equals(str) ? "✂️" : FEED_ROOT.equals(str) ? "🌱"
                : FEED_LEAF.equals(str) ? "💧" : WHITEN.equals(str) ? "🖌️" : WATER.equals(str) ? "🚿"
                : MULCH.equals(str) ? "🍂" : SOIL.equals(str) ? "⛏️" : PROTECT.equals(str) ? "🛡️"
                : HARVEST.equals(str) ? "🧺" : MONITOR.equals(str) ? "🔍" : MOW.equals(str) ? "🌾"
                : PLANT.equals(str) ? "🌷" : REPAIR.equals(str) ? "🔧" : "📌";
    }
}
