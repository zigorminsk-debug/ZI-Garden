package by.csl.gardener;

import java.util.ArrayList;
import java.util.List;

public class Plant {
    public static final int SIZE_MEDIUM = 1;
    public static final int SIZE_OLD = 2;
    public static final int SIZE_YOUNG = 0;
    public final String group;
    public final String icon;
    public final String id;
    public final String name;

    public final int iconRes;

    public Plant(String str, String str2, String str3, String str4, int iconRes) {
        this.iconRes = iconRes;
        this.id = str;
        this.name = str2;
        this.icon = str3;
        this.group = str4;
    }

    public static List<Plant> all() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new Plant("apple", "Яблоня", "🍎", "Плодовые деревья", 0));
        arrayList.add(new Plant("pear", "Груша", "🍐", "Плодовые деревья", 0));
        arrayList.add(new Plant("quince", "Айва", "🍋", "Плодовые деревья", R.drawable.pi_quince));
        arrayList.add(new Plant("rowan", "Рябина / арония", "🔴", "Плодовые деревья", R.drawable.pi_rowan));
        arrayList.add(new Plant("walnut", "Грецкий орех", "🌰", "Плодовые деревья", 0));
        arrayList.add(new Plant("sweet_cherry", "Черешня", "🍒", "Косточковые", 0));
        arrayList.add(new Plant("cherry", "Вишня", "🍒", "Косточковые", 0));
        arrayList.add(new Plant("plum", "Слива", "🟣", "Косточковые", R.drawable.pi_plum));
        arrayList.add(new Plant("cherry_plum", "Алыча", "🟡", "Косточковые", 0));
        arrayList.add(new Plant("apricot", "Абрикос", "🍑", "Косточковые", 0));
        arrayList.add(new Plant("peach", "Персик", "🍑", "Косточковые", 0));
        arrayList.add(new Plant("currant_black", "Смородина чёрная", "⚫", "Ягодные кустарники", R.drawable.pi_currant_black));
        arrayList.add(new Plant("currant_red", "Смородина красная/белая", "🔴", "Ягодные кустарники", R.drawable.pi_currant_red));
        arrayList.add(new Plant("gooseberry", "Крыжовник", "🟢", "Ягодные кустарники", R.drawable.pi_gooseberry));
        arrayList.add(new Plant("honeysuckle", "Жимолость", "🔵", "Ягодные кустарники", R.drawable.pi_honeysuckle));
        arrayList.add(new Plant("irga", "Ирга", "🟣", "Ягодные кустарники", R.drawable.pi_irga));
        arrayList.add(new Plant("chokeberry", "Арония черноплодная", "⚫", "Ягодные кустарники", R.drawable.pi_chokeberry));
        arrayList.add(new Plant("sea_buckthorn", "Облепиха", "🟠", "Ягодные кустарники", R.drawable.pi_seabuck));
        arrayList.add(new Plant("blueberry", "Голубика садовая", "🔵", "Ягодные кустарники", R.drawable.pi_blueberry));
        arrayList.add(new Plant("rosehip", "Шиповник", "🌺", "Ягодные кустарники", 0));
        arrayList.add(new Plant("raspberry", "Малина", "🍒", "Ягодники", R.drawable.pi_raspberry));
        arrayList.add(new Plant("blackberry", "Ежевика", "⚫", "Ягодники", R.drawable.pi_blackberry));
        arrayList.add(new Plant("strawberry", "Клубника (земляника)", "🍓", "Ягодники", 0));
        arrayList.add(new Plant("grape", "Виноград", "🍇", "Виноград и лианы", 0));
        arrayList.add(new Plant("clematis", "Клематис", "🌸", "Виноград и лианы", 0));
        arrayList.add(new Plant("actinidia", "Актинидия / лимонник", "🥝", "Виноград и лианы", 0));
        arrayList.add(new Plant("rose", "Роза", "🌹", "Декоративные", 0));
        arrayList.add(new Plant("conifer", "Хвойные (туя, можжевельник)", "🌲", "Декоративные", 0));
        arrayList.add(new Plant("hydrangea", "Гортензия", "💮", "Декоративные", 0));
        arrayList.add(new Plant("lawn", "Газон", "🌿", "Декоративные", 0));
        arrayList.add(new Plant("tomato", "Томат", "🍅", "Овощные культуры", 0));
        arrayList.add(new Plant("cucumber", "Огурец", "🥒", "Овощные культуры", 0));
        arrayList.add(new Plant("pepper", "Перец сладкий", "🌶️", "Овощные культуры", 0));
        arrayList.add(new Plant("eggplant", "Баклажан", "🍆", "Овощные культуры", 0));
        arrayList.add(new Plant("cabbage", "Капуста белокочанная", "🥬", "Овощные культуры", 0));
        arrayList.add(new Plant("carrot", "Морковь", "🥕", "Овощные культуры", 0));
        arrayList.add(new Plant("beet", "Свёкла столовая", "🟤", "Овощные культуры", R.drawable.pi_beet));
        arrayList.add(new Plant("onion", "Лук репчатый", "🧅", "Овощные культуры", 0));
        arrayList.add(new Plant("garlic", "Чеснок (озимый и яровой)", "🧄", "Овощные культуры", 0));
        arrayList.add(new Plant("potato", "Картофель", "🥔", "Овощные культуры", 0));
        arrayList.add(new Plant("zucchini", "Кабачок / патиссон", "🥒", "Овощные культуры", 0));
        arrayList.add(new Plant("pumpkin", "Тыква", "🎃", "Овощные культуры", 0));
        arrayList.add(new Plant("pea", "Горох / фасоль", "🟢", "Овощные культуры", R.drawable.pi_pea));
        arrayList.add(new Plant("radish", "Редис", "🔴", "Овощные культуры", R.drawable.pi_radish));
        arrayList.add(new Plant("greens", "Зелень и салаты", "🥗", "Овощные культуры", 0));
        arrayList.add(new Plant("greenhouse", "Теплица / парник", "🏡", "Теплица и постройки", 0));
        return arrayList;
    }

    public static Plant byId(String str) {
        for (Plant plant : all()) {
            if (plant.id.equals(str)) {
                return plant;
            }
        }
        return null;
    }

    public static boolean isPome(String str) {
        return "apple".equals(str) || "pear".equals(str) || "quince".equals(str) || "rowan".equals(str) || "irga".equals(str) || "chokeberry".equals(str);
    }

    public static boolean isStone(String str) {
        return "sweet_cherry".equals(str) || "cherry".equals(str) || "plum".equals(str) || "cherry_plum".equals(str) || "apricot".equals(str) || "peach".equals(str);
    }

    public static boolean isBerryBush(String str) {
        return "currant_black".equals(str) || "currant_red".equals(str) || "gooseberry".equals(str) || "honeysuckle".equals(str) || "irga".equals(str) || "chokeberry".equals(str) || "sea_buckthorn".equals(str) || "rosehip".equals(str);
    }

    public static boolean isTreeLike(String str) {
        return isPome(str) || isStone(str) || "walnut".equals(str);
    }

    public static boolean isVegetable(String str) {
        return "tomato".equals(str) || "cucumber".equals(str) || "pepper".equals(str) || "eggplant".equals(str) || "cabbage".equals(str) || "carrot".equals(str) || "beet".equals(str) || "onion".equals(str) || "garlic".equals(str) || "potato".equals(str) || "zucchini".equals(str) || "pumpkin".equals(str) || "pea".equals(str) || "radish".equals(str) || "greens".equals(str);
    }

    public static String groupName(String str) {
        Plant byId = byId(str);
        return byId == null ? "" : byId.group;
    }

    public static double sizeFactor(int i, String str) {
        double d = i != 0 ? i != 2 ? 1.0d : 1.6d : 0.5d;
        if (isTreeLike(str)) {
            return d;
        }
        if (isVegetable(str) || "greenhouse".equals(str)) {
            return 1.0d;
        }
        return d * ((isBerryBush(str) || "raspberry".equals(str) || "blackberry".equals(str) || "grape".equals(str) || "blueberry".equals(str)) ? 0.35d : 0.2d);
    }
}
