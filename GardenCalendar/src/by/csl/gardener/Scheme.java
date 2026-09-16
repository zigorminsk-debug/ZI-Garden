package by.csl.gardener;

public final class Scheme {
    public static String captionFor(int i) {
        return i == R.drawable.scheme_prune_pome ? "Схема: зелёным — ветви, которые оставляем; красным — вырезаем (внутрь кроны, волчки, острый угол); синим — срез на внешнюю почку под 45°" : i == R.drawable.scheme_prune_stone ? "Схема: чашевидная крона косточковых — 4–5 разведённых ветвей, центр открыт для света, проводник вырезан, срез замазан" : i == R.drawable.scheme_prune_currant ? "Схема: куст смородины — оставляем 15–20 разновозрастных побегов, старые (6+ лет), лежачие и сломанные вырезаем у самой земли без пенька" : i == R.drawable.scheme_prune_raspberry ? "Схема: малина — отплодоносившие побеги вырезаем у земли, оставляем 8–12 замещающих и подвязываем к шпалере, подмёрзшую верхушку укорачиваем до живой почки" : i == R.drawable.scheme_prune_grape ? "Схема: виноград — плодовая лоза на 8–12 глазков, срез с наклоном от глазка, невызревшую зелёную часть удаляем, оставляем сучок замещения" : i == R.drawable.scheme_prune_rose ? "Схема: роза — оставляем 3–5 сильных побегов с 3–5 почками, срез под 45° на 5 мм выше наружной почки, слабые и растущие внутрь вырезаем, основание окучиваем" : i == R.drawable.scheme_prune_clematis ? "Схема: клематис — группа 1: только санитарная обрезка; группа 2: укоротить до 1–1,5 м; группа 3: срезать на 2–3 узла (20–40 см от земли)" : i == R.drawable.scheme_prune_veg ? "Схема: пасынкование томата — ведём в один стебель, пасынки выламываем при длине 3–5 см, оставляя пенёк 5 мм; над верхней кистью оставляем 2 листа" : "";
    }

    private Scheme() {
    }

    public static int forTask(Task task) {
        return forRule(task.plantId, task.op, task.title);
    }

    public static int forRule(String str, String str2, String str3) {
        boolean equals = Operation.PRUNE.equals(str2);
        if (str3 == null) {
            str3 = "";
        }
        if ("tomato".equals(str) && (equals || str3.contains("Пасынкование") || str3.contains("пасынков"))) {
            return R.drawable.scheme_prune_veg;
        }
        if ("cucumber".equals(str) && equals) {
            return R.drawable.scheme_prune_veg;
        }
        if (equals) {
            if (Plant.isPome(str) || "walnut".equals(str)) {
                return R.drawable.scheme_prune_pome;
            }
            if (Plant.isStone(str)) {
                return R.drawable.scheme_prune_stone;
            }
            if (Plant.isBerryBush(str) || "blueberry".equals(str)) {
                return R.drawable.scheme_prune_currant;
            }
            if ("raspberry".equals(str) || "blackberry".equals(str)) {
                return R.drawable.scheme_prune_raspberry;
            }
            if ("grape".equals(str)) {
                return R.drawable.scheme_prune_grape;
            }
            if ("rose".equals(str)) {
                return R.drawable.scheme_prune_rose;
            }
            if ("clematis".equals(str) || "actinidia".equals(str)) {
                return R.drawable.scheme_prune_clematis;
            }
            if ("lawn".equals(str)) {
                return 0;
            }
        }
        if ("lawn".equals(str) && Operation.MOW.equals(str2)) {
            return 0;
        }
        if ("grape".equals(str) && str3.contains("сухая подвязка")) {
            return R.drawable.scheme_prune_grape;
        }
        if ("raspberry".equals(str) && str3.contains("шпалере")) {
            return R.drawable.scheme_prune_raspberry;
        }
        if ("rose".equals(str) && str3.contains("укрыти")) {
            return R.drawable.scheme_prune_rose;
        }
        if ("clematis".equals(str) && str3.contains("укрыти")) {
            return R.drawable.scheme_prune_clematis;
        }
        return 0;
    }
}
