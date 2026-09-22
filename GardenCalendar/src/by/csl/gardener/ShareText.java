package by.csl.gardener;

/** Текст совета по болезни/вредителю для отправки через мессенджер (Viber, Telegram, WhatsApp и др.). */
final class ShareText {
    private ShareText() {}

    /** Полный совет по записи справочника: распознавание, лечение по шагам, сезонная профилактика, препараты. */
    static String diseaseAdvice(Plant plant, Disease dz) {
        StringBuilder sb = new StringBuilder(900);
        sb.append("🩺 Совет садоводу");
        if (plant != null) {
            sb.append(": ").append(plant.name).append(" — ").append(dz.name);
        } else {
            sb.append(": ").append(dz.name);
        }
        sb.append('\n');
        if (dz.kind != null && dz.kind.length() > 0) {
            sb.append(dz.kind).append('\n');
        }
        sb.append('\n');

        if (dz.symptoms != null && dz.symptoms.length() > 0) {
            sb.append("🔎 Как распознать:\n").append(dz.symptoms).append("\n\n");
        }

        if (dz.cure != null && dz.cure.length > 0) {
            sb.append("🛠️ Лечение по шагам:\n");
            for (int i = 0; i < dz.cure.length; i++) {
                if (dz.cure[i] == null || dz.cure[i].length() == 0) continue;
                sb.append(i + 1).append(". ").append(dz.cure[i]).append('\n');
            }
            sb.append('\n');
        }

        boolean hasSeason = (dz.spring != null && dz.spring.length() > 0)
                || (dz.summer != null && dz.summer.length() > 0)
                || (dz.autumn != null && dz.autumn.length() > 0);
        if (hasSeason) {
            sb.append("📅 Профилактика по сезонам:\n");
            if (dz.spring != null && dz.spring.length() > 0) sb.append("🌱 Весна: ").append(dz.spring).append('\n');
            if (dz.summer != null && dz.summer.length() > 0) sb.append("☀️ Лето: ").append(dz.summer).append('\n');
            if (dz.autumn != null && dz.autumn.length() > 0) sb.append("🍂 Осень: ").append(dz.autumn).append('\n');
            sb.append('\n');
        }

        if (dz.mats != null && dz.mats.length > 0) {
            StringBuilder names = new StringBuilder();
            for (String id : dz.mats) {
                Material m = Material.byId(id);
                if (m == null) continue;
                if (names.length() > 0) names.append(", ");
                names.append(m.name);
            }
            if (names.length() > 0) {
                sb.append("💊 Препараты и материалы: ").append(names).append('\n');
            }
        }

        sb.append("\n— из справочника приложения ZI Garden");
        return sb.toString();
    }

    /** План работ на неделю текстом — для отправки семье в мессенджер (без выполненных). */
    static String weekPlan(java.util.List<Task> tasks, String fromLabel) {
        StringBuilder sb = new StringBuilder("🗓️ План работ на неделю");
        if (fromLabel != null && fromLabel.length() > 0) {
            sb.append(" (от ").append(fromLabel).append(')');
        }
        int shown = 0;
        int pending = 0;
        if (tasks != null) {
            for (Task t : tasks) {
                if (t == null || t.done) continue;
                pending++;
                if (sb.length() < 1400) {
                    sb.append('\n').append("• ").append(Dates.fmt(t.year, t.month, t.day))
                            .append(" (").append(Dates.weekday(t.year, t.month, t.day)).append(") ")
                            .append(Operation.icon(t.op)).append(' ')
                            .append(t.plantName).append(" — ").append(t.title);
                    shown++;
                }
            }
        }
        if (shown == 0) {
            sb.append("\nРабот на ближайшую неделю не запланировано 🌿");
        } else if (pending > shown) {
            sb.append("\n…ещё ").append(pending - shown).append(" — смотрите в приложении");
        }
        sb.append("\n\n— из приложения ZI Garden");
        return sb.toString();
    }

    /** Итоги сезона текстом: выполненные работы по культурам, бюджет закупок и последние записи журнала. */
    static String seasonSummary(java.util.List<String> journalRows, Planner.Budget budget, int year, String regionName) {
        return seasonSummary(journalRows, budget, year, regionName, null);
    }

    /** То же + урожай года: пары «культура — итог» (кг/шт/л); null — блока урожая не будет. */
    static String seasonSummary(java.util.List<String> journalRows, Planner.Budget budget, int year, String regionName,
            java.util.Map<String, String> harvest) {
        StringBuilder sb = new StringBuilder(900);
        sb.append("🌿 Итоги сезона ").append(year);
        if (regionName != null && regionName.length() > 0) {
            sb.append(" · ").append(regionName);
        }
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int done = 0;
        java.util.List<String[]> yearRows = new java.util.ArrayList<>();
        java.util.Map<String, Integer> byPlant = new java.util.HashMap<>();
        if (journalRows != null) {
            for (String raw : journalRows) {
                String[] f = Journal.decode(raw);
                if (f == null) continue;
                cal.setTimeInMillis(Journal.when(f));
                if (cal.get(java.util.Calendar.YEAR) != year) continue;
                done++;
                yearRows.add(f);
                String plant = f[Journal.F_PLANT].length() > 0 ? f[Journal.F_PLANT] : "Прочее";
                Integer c = byPlant.get(plant);
                byPlant.put(plant, c == null ? Integer.valueOf(1) : Integer.valueOf(c.intValue() + 1));
            }
        }
        sb.append("\n\n✅ Выполнено работ: ").append(done);
        if (!byPlant.isEmpty()) {
            java.util.List<java.util.Map.Entry<String, Integer>> plants =
                    new java.util.ArrayList<>(byPlant.entrySet());
            java.util.Collections.sort(plants, new java.util.Comparator<java.util.Map.Entry<String, Integer>>() {
                @Override
                public int compare(java.util.Map.Entry<String, Integer> a, java.util.Map.Entry<String, Integer> b) {
                    int c = b.getValue().intValue() - a.getValue().intValue();
                    return c != 0 ? c : a.getKey().compareTo(b.getKey());
                }
            });
            sb.append('\n');
            int shown = 0;
            int rest = 0;
            for (java.util.Map.Entry<String, Integer> e : plants) {
                if (shown < 6) {
                    if (shown > 0) sb.append(", ");
                    sb.append(e.getKey()).append(" — ").append(e.getValue());
                    shown++;
                } else {
                    rest += e.getValue().intValue();
                }
            }
            if (rest > 0) {
                sb.append(", ещё ").append(rest).append(" работ по другим культурам");
            }
        }
        if (budget != null) {
            sb.append("\n\n💰 Закупки сезона (справочные цены):\n")
                    .append("план: ").append(String.format(java.util.Locale.US, "%.2f %s", Double.valueOf(budget.planned), budget.currency))
                    .append(" · куплено: ").append(String.format(java.util.Locale.US, "%.2f %s", Double.valueOf(budget.boughtCost), budget.currency))
                    .append(" · осталось: ").append(String.format(java.util.Locale.US, "%.2f %s", Double.valueOf(Math.max(0.0d, budget.planned - budget.boughtCost)), budget.currency));
        }
        if (harvest != null && !harvest.isEmpty()) {
            sb.append("\n\n🌾 Урожай года:\n");
            int shownH = 0;
            int restH = 0;
            for (java.util.Map.Entry<String, String> e : harvest.entrySet()) {
                if (shownH < 8) {
                    if (shownH > 0) {
                        sb.append(", ");
                    }
                    sb.append(e.getKey()).append(" — ").append(e.getValue());
                    shownH++;
                } else {
                    restH++;
                }
            }
            if (restH > 0) {
                sb.append(", ещё ").append(restH).append(" культур");
            }
        }
        sb.append("\n\n📒 Последние работы:");
        if (yearRows.isEmpty()) {
            sb.append("\nпока ничего не отмечено выполненным");
        } else {
            int limit = Math.min(15, yearRows.size());
            for (int i = 0; i < limit; i++) {
                String[] f = yearRows.get(i);
                cal.setTimeInMillis(Journal.when(f));
                sb.append("\n• ").append(Dates.fmt(cal.get(java.util.Calendar.YEAR),
                        cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.DAY_OF_MONTH)))
                        .append(" · ").append(f[Journal.F_PLANT].length() > 0 ? f[Journal.F_PLANT] + " — " : "")
                        .append(f[Journal.F_TITLE]);
            }
            if (yearRows.size() > limit) {
                sb.append("\n…ещё ").append(yearRows.size() - limit).append(" — смотрите в приложении");
            }
        }
        sb.append("\n\n— из приложения ZI Garden");
        return sb.toString();
    }
}
