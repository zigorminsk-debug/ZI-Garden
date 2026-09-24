package by.csl.gardener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Тексты виджетов рабочего стола — чистая логика без android.*, проверяется LogicTest. */
final class WidgetTexts {
    private WidgetTexts() {}

    /** Названия месяцев в предложном падеже: «Активно в мае». */
    static final String[] MONTHS_GEN = {
            "январе", "феврале", "марте", "апреле", "мае", "июне",
            "июле", "августе", "сентябре", "октябре", "ноябре", "декабре"};

    /** Заголовок виджета погоды: «🌡️ Погода · Полоцк». */
    static String weatherTitle(Weather w) {
        if (w == null || w.days.isEmpty()) {
            return "🌡️ Погода для сада";
        }
        return "🌡️ Погода" + (w.cityName != null && w.cityName.length() > 0 ? " · " + w.cityName : "");
    }

    /** Текущее состояние: «⛅ +12° · переменная облачность». */
    static String weatherNow(Weather w) {
        if (w == null) {
            return "Нет данных — откройте приложение";
        }
        int code = w.currentCode;
        double temp = w.currentTemp;
        if (code < 0 && !w.days.isEmpty()) {
            code = w.days.get(0).code;
            temp = Double.isNaN(temp) ? w.days.get(0).tMax : temp;
        }
        if (code < 0) {
            return "Нет данных — откройте приложение";
        }
        String t = Double.isNaN(temp) ? "" : String.format(Locale.US, "%+.0f° ", temp);
        return Weather.iconFor(code) + " " + t + "· " + Weather.textFor(code);
    }

    /** Строка «Сегодня: 8…16 °C, дождь, осадки до 5 мм». */
    static String weatherToday(Weather w, int year, int month1, int day) {
        if (w == null || !w.hasForecast()) {
            return "";
        }
        Weather.Day d = w.dayFor(year, month1, day);
        if (d == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder("Сегодня: ").append(d.icon()).append(' ').append(d.summary());
        if (d.precipMm > 0.0d) {
            sb.append(String.format(Locale.US, ", осадки %.1f мм", d.precipMm));
            if (d.precipProb > 0.0d) {
                sb.append(String.format(Locale.US, " (%.0f%%)", d.precipProb));
            }
        }
        return sb.toString();
    }

    /** Совет дня по погоде — согласован с порогами Planner (дождь, жара, сухость 3 дня). */
    static String weatherAdvice(Weather w, int year, int month1, int day) {
        if (w == null || !w.hasForecast()) {
            return "Обновите прогноз в приложении";
        }
        Weather.Day today = w.dayFor(year, month1, day);
        if (today == null) {
            return "Обновите прогноз в приложении";
        }
        if (today.tMin < 0.0d) {
            return String.format(Locale.US,
                    "❄️ Ночью заморозок до %.0f° — укройте рассаду и цветущие!", today.tMin);
        }
        if (today.rainExpected()) {
            return "🌧️ Ожидается дождь — полив не нужен, обработки перенесите";
        }
        if (today.windMax >= 36.0d) {
            return String.format(Locale.US,
                    "🌬️ Ветер до %.0f км/ч — опрыскивание бесполезно", today.windMax);
        }
        double sum3 = today.precipMm;
        int taken = 1;
        for (Weather.Day d : w.days) {
            if (taken >= 3) break;
            if (d == today) continue;
            sum3 += d.precipMm;
            taken++;
        }
        if (sum3 < 2.0d && today.tMax >= 22.0d) {
            return String.format(Locale.US,
                    "💧 Засушливо (%.1f мм за 3 дня) — полейте утром или вечером", sum3);
        }
        if (today.tMax <= 5.0d) {
            return "🧤 Холодно для обработок — время уборки и плановых работ";
        }
        return "✅ Погода благоприятна для садовых работ";
    }

    /** Строки подсказок месяца для виджета (маркируются «• Культура — проблема»). */
    static List<String> seasonLines(int month, Set<String> plants, List<Disease> all, int max) {
        List<String> out = new ArrayList<>();
        List<Disease> hints = SeasonHints.forMonth(month, plants, all);
        for (int i = 0; i < hints.size() && out.size() < max; i++) {
            Disease dz = hints.get(i);
            Plant pl = Plant.byId(dz.plantId);
            out.add("• " + (pl == null ? dz.plantId : pl.name) + " — " + dz.name);
        }
        int more = hints.size() - out.size();
        if (more > 0) {
            out.add("…ещё " + more + " — в приложении");
        }
        return out;
    }

    /** Подпись виджета сезона, когда смотреть нечего (зима) или культуры не выбраны. */
    static String seasonQuiet(int month, Set<String> plants) {
        if (plants == null || plants.isEmpty()) {
            return "Отметьте культуры в разделе «Мои растения» — виджет покажет их сезонные угрозы";
        }
        return "🌙 Спокойный месяц: активных угроз нет — время планировать посадки";
    }

    /** Садовые памятки — одна в день, детерминированно по дню года. */
    static final String[] TIPS = {
            "🌱 Посевы лучше всходят, если перед посевом пролить бороздки тёплой водой.",
            "💧 Поливайте под корень утром — мокрая листва ночью провоцирует грибки.",
            "🍂 Мульча 5–7 см держит влагу как два полива.",
            "✂️ Санитарная обрезка — в любой сухой день: вырезайте сухое, больное, растущее внутрь.",
            "🐞 Божьи коровки съедают до 100 тлей в день — не трите золой укроп.",
            "🌸 Во время цветения опрыскивать нельзя — берегите пчёл.",
            "🧅 После лука и чеснока хорошо растут огурцы и капуста — держите севооборот.",
            "🍓 Клубнику пересаживайте раз в 3–4 года на новое место.",
            "🌿 Сидераты (горчица, фацелия) после уборки кормят почву и вытесняют сорняки.",
            "🍎 Падалицу убирайте своевременно — это рассадник плодожорки.",
            "🥬 Рассаду закаляйте 7–10 дней перед высадкой — сначала час, потом целый день.",
            "☀️ Обработки садовыми препаратами — только в безветренный вечер.",
            "🍇 Виноград лучше плодоносит на лозе прошлого года — проводите правильную обрезку.",
            "🥕 Подготовленные грядки осенью весной «работают» на две недели раньше.",
            "🌡️ Заморозок −2 °C по почве даже в мае губит цветущие бахчи.",
            "🫐 Голубике нужна кислая почва — мульшируйте хвоей и корой сосны.",
            "🥔 Глубокая посадка картофеля в холодную землю грозит ризоктонией.",
            "🌹 Розы окучивайте на зиму на 20–25 см — это защита шейки корня.",
            "🧄 Чеснок под зиму сажайте за 3–4 недели до морозов — успеет укорениться без роста.",
            "🍐 Побелка штамбов с осени спасает от солнечных ожогов в феврале–марте.",
            "🌳 Живая изгородь из туй просит формирующую стрижку дважды за лето.",
            "🫘 Бобовые сами обогащают грядку азотом — хорошие предшественники капусте.",
            "🍅 Пасынки помидоров выламывайте в росте до 5 см — раны заживают быстрее.",
            "🥒 Огурцы не любят сквозняков: ставьте гряду защищённой с севера.",
            "🌼 Бархатцы и календула на грядках отгоняют нематоду и тлю.",
            "🧅 Лук перо сушите на солнце — за зиму без просушки сгниёт на хранении.",
            "🍒 Приствольные круги держите под мульчей, а не под «чёрным паром».",
            "🍑 Косточковые не обрезайте осенью глубоко — гуммоз усилится.",
            "🫑 Перец высаживайте в грунт только при устойчивых +12 °C ночью.",
            "🌰 Ягодные кусты прореживайте сразу после сбора — свет даёт будущий урожай.",
            "🥦 Капусту после уборки убирайте с корнем — кила живёт в чешуе.",
            "🍋 Теплицу промораживайте зимой открытой — болезни вымерзают.",
            "🌾 Газон последний раз за сезон стригите чуть выше обычного — корни перезимуют лучше.",
            "🍆 Пережидайте стужу: баклажаны и перцы растут только тепло.",
            "🫛 Свежий навоз под корнеплоды — только перепревший: корневая гниль опасна.",
            "🍃 Компост кладут в тень: на солнце теряется азот до трети.",
            "🪴 Покупая саженцы, переверните горшок: белые нити в земле — корневая тля.",
            "🌦️ Перед заморозками полив почвы — не полив! — защищает корни лучше.",
            "🍄 Споры грибков летят с соседних участков — санитария важнее опрыскиваний.",
            "🐌 Мешковина ночью — ловушка для слизней: утром соберите под ней."
    };

    // ── Виджет «Лунный календарь» ──

    /** Фаза на дату: «🌒 Растущая луна». */
    static String moonPhase(int year, int month1, int day) {
        int idx = Moon.phaseIndex(year, month1, day);
        return Moon.EMOJI[idx] + " " + Moon.NAMES[idx];
    }

    /** Совет дня по лунным традициям, с заглавной буквы. */
    static String moonAdvice(int year, int month1, int day) {
        String advice = Moon.advice(Moon.phaseIndex(year, month1, day));
        return advice.substring(0, 1).toUpperCase(Locale.US) + advice.substring(1) + ".";
    }

    // ── Виджет «Сейчас в природе» ──

    /** Заголовок: «🌸 Сейчас в природе: весна». */
    static String phenologyTitle(int month1) {
        return "🌸 Сейчас в природе: " + PhenologyGuide.seasonFor(month1).toLowerCase();
    }

    /** Ориентиры текущего сезона: «💧 Сокодвижение у берёзы — конец марта … начало апреля». */
    static List<String> phenologyLines(int month1, int max) {
        List<String> out = new ArrayList<>();
        for (PhenologyGuide.Sign s : PhenologyGuide.bySeason(PhenologyGuide.seasonFor(month1))) {
            if (out.size() >= max) {
                break;
            }
            out.add(s.emoji + " " + s.title + " — " + s.period);
        }
        return out;
    }

    // ── Виджет «Минерал дня» ──

    /** Заголовок карточки: «Бор (B) — точка роста отмирает…». */
    static String deficitHead(int dayOfYear) {
        DeficiencyGuide.Item it = deficitFor(dayOfYear);
        return it.name + " (" + it.symbol + ") — " + firstSentence(it.signs);
    }

    /** Скорая помощь: «💊 Борная кислота 0,02–0,05%…». */
    static String deficitFix(int dayOfYear) {
        return "💊 " + firstSentence(deficitFor(dayOfYear).fix);
    }

    private static DeficiencyGuide.Item deficitFor(int dayOfYear) {
        List<DeficiencyGuide.Item> all = DeficiencyGuide.all();
        int idx = ((dayOfYear % all.size()) + all.size()) % all.size();
        return all.get(idx);
    }

    /** Первое предложение (до точки или точки с запятой), не длиннее 140 символов. */
    private static String firstSentence(String s) {
        if (s == null) {
            return "";
        }
        int dot = s.indexOf(". ");
        int semi = s.indexOf("; ");
        int cut = -1;
        if (dot > 0 && semi > 0) {
            cut = Math.min(dot, semi);
        } else if (dot > 0) {
            cut = dot;
        } else if (semi > 0) {
            cut = semi;
        }
        String head = cut > 0 ? s.substring(0, cut).trim() + "." : s;
        if (head.length() > 140) {
            head = head.substring(0, 140).trim() + "…";
        }
        return head;
    }

    static String tipOfDay(int dayOfYear) {
        if (dayOfYear < 1) {
            dayOfYear = 1;
        }
        return TIPS[(dayOfYear - 1) % TIPS.length];
    }
}
