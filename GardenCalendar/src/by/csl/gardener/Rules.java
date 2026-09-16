package by.csl.gardener;

import java.util.ArrayList;
import java.util.List;

public final class Rules {
    private static final int D1 = 1;
    private static final int D2 = 2;
    private static final int D3 = 4;
    private static final int DALL = 7;
    private static List<Rule> cache;

    private static int[] m(int... iArr) {
        return iArr;
    }

    private Rules() {
    }

    public static synchronized List<Rule> all() {
        synchronized (Rules.class) {
                    List<Rule> list = cache;
                    if (list != null) {
                        return list;
                    }
                    ArrayList arrayList = new ArrayList();
                    pomePlant(arrayList, "apple", "яблони", "Яблоня", true);
                    pomePlant(arrayList, "pear", "груши", "Груша", true);
                    pomePlant(arrayList, "quince", "айвы", "Айва", false);
                    pomePlant(arrayList, "rowan", "рябины", "Рябина", false);
                    walnut(arrayList);
                    stonePlant(arrayList, "sweet_cherry", "черешни", "Черешня", true);
                    stonePlant(arrayList, "cherry", "вишни", "Вишня", true);
                    stonePlant(arrayList, "plum", "сливы", "Слива", true);
                    stonePlant(arrayList, "cherry_plum", "алычи", "Алыча", false);
                    stonePlant(arrayList, "apricot", "абрикоса", "Абрикос", false);
                    stonePlant(arrayList, "peach", "персика", "Персик", true);
                    currantLike(arrayList, "currant_black", "чёрной смородины", "Смородина чёрная", true);
                    currantLike(arrayList, "currant_red", "красной смородины", "Смородина красная", false);
                    currantLike(arrayList, "gooseberry", "крыжовника", "Крыжовник", true);
                    simpleBush(arrayList, "honeysuckle", "жимолости", "Жимолость");
                    simpleBush(arrayList, "irga", "ирги", "Ирга");
                    simpleBush(arrayList, "chokeberry", "аронии", "Арония");
                    simpleBush(arrayList, "sea_buckthorn", "облепихи", "Облепиха");
                    simpleBush(arrayList, "rosehip", "шиповника", "Шиповник");
                    blueberry(arrayList);
                    raspberryLike(arrayList, "raspberry", "малины", "Малина", true);
                    raspberryLike(arrayList, "blackberry", "ежевики", "Ежевика", false);
                    strawberry(arrayList);
                    grape(arrayList);
                    clematis(arrayList);
                    actinidia(arrayList);
                    rose(arrayList);
                    conifer(arrayList);
                    hydrangea(arrayList);
                    lawn(arrayList);
                    tomato(arrayList);
                    cucumber(arrayList);
                    cabbage(arrayList);
                    potato(arrayList);
                    onion(arrayList);
                    garlic(arrayList);
                    veg(arrayList, "pepper", "Перец сладкий", "перца", 3, 6, new int[]{5}, 6, new int[]{DALL, 8, 9}, DALL, true);
                    veg(arrayList, "eggplant", "Баклажан", "баклажана", 2, D3, new int[]{5}, D3, new int[]{DALL, 8, 9}, DALL, true);
                    veg(arrayList, "carrot", "Морковь", "моркови", 0, 0, new int[]{D3, 5}, 6, new int[]{9, 10}, 6, false);
                    veg(arrayList, "beet", "Свёкла столовая", "свёклы", 0, 0, new int[]{5}, 3, new int[]{9, 10}, DALL, false);
                    veg(arrayList, "zucchini", "Кабачок", "кабачка", D3, 2, new int[]{5}, D3, new int[]{DALL, 8, 9}, DALL, true);
                    veg(arrayList, "pumpkin", "Тыква", "тыквы", D3, 2, new int[]{5}, D3, new int[]{9, 10}, DALL, true);
                    veg(arrayList, "pea", "Горох", "гороха", 0, 0, new int[]{D3}, 6, new int[]{6, DALL}, DALL, false);
                    veg(arrayList, "radish", "Редис", "редиса", 0, 0, new int[]{D3, 8}, DALL, new int[]{5, 9}, DALL, false);
                    veg(arrayList, "greens", "Зелень и салаты", "зелени", 0, 0, new int[]{D3, 5, 8}, DALL, new int[]{5, 6, DALL, 8, 9}, DALL, false);
                    greenhouseBlock(arrayList);
                    cache = arrayList;
                    markAlternatives();
                    return cache;
                }
    }

    public static void markAlternatives() {
        boolean z;
        for (Rule rule : all0()) {
            MatRef[] matRefArr = rule.mats;
            int length = matRefArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    z = false;
                    break;
                } else {
                    if (!matRefArr[i].alternative) {
                        z = true;
                        break;
                    }
                    i++;
                }
            }
            if (!z && rule.mats.length > 0) {
                rule.mats[0].alternative = false;
            }
        }
    }

    private static List<Rule> all0() {
        return cache;
    }

    private static MatRef pp(String str, double d) {
        return new MatRef(str, d, 0);
    }

    private static MatRef l10(String str, double d) {
        return new MatRef(str, d, 1);
    }

    private static MatRef m2(String str, double d) {
        return new MatRef(str, d, 2);
    }

    private static MatRef fx(String str, double d) {
        return new MatRef(str, d, 3);
    }

    private static void add(List<Rule> list, String str, String str2, String str3, int[] iArr, int i, double d, double d2, boolean z, int i2, String str4, double d3, boolean z2, MatRef... matRefArr) {
        list.add(new Rule(str, str2, str3, iArr, i, d, d2, z, i2, str4, d3, z2, matRefArr));
    }

    private static void pomePlant(List<Rule> list, String str, String str2, String str3, boolean z) {
        add(list, str, Operation.PRUNE, "Санитарная и формирующая обрезка " + str2, m(2, 3), DALL, -10.0d, 8.0d, false, 3, "Вырежьте сухие, сломанные и растущие внутрь кроны ветви до живой древесины. Формирующую обрезку проводите при температуре не ниже −8 °C. Срезы более 2 см замажьте садовым варом.", 0.0d, false, pp("garden_tar", 20.0d));
        add(list, str, Operation.WHITEN, "Побелка ствола и скелетных ветвей", m(3), 6, 2.0d, 15.0d, true, 2, "Побелите штамб и основания скелетных ветвей на высоту 1–1,2 м — защита от солнечных ожогов и морозобоин. Работайте в сухой день.", 0.0d, false, pp("whitewash", 0.15d));
        add(list, str, Operation.SPRAY, "Ранневесеннее искореняющее опрыскивание", m(3, D3), 5, 4.0d, 15.0d, true, 3, "До распускания почек обильно опрысните крону и приствольный круг 3% медным купоросом или бордоской жидкостью против зимующих стадий парши и лишайников. Расход — до полного смачивания.", 10.0d, true, l10("copper_sulfate", 300.0d), l10("bordeaux", 300.0d).alt());
        add(list, str, Operation.FEED_ROOT, "Весенняя подкормка азотом", m(D3), 3, 4.0d, 25.0d, false, 3, "Внесите азот по влажной почве в приствольный круг: карбамид 50–60 г на дерево (или аммиачную селитру), заделайте граблями и полейте.", 0.0d, true, pp("urea", 60.0d), pp("ammonium_nitrate", 50.0d).alt());
        add(list, str, Operation.SPRAY, "Обработка по зелёному конусу и бутонизации", m(D3), 6, 6.0d, 22.0d, true, 3, "В фазы зелёного конуса и выдвижения бутонов обработайте против парши и монилиоза Хорусом (работает от +3 °C) или Скором. Добавьте Биотлин от тли и медяницы.", 10.0d, true, l10("horus", 2.0d), l10("skor", 2.0d).alt(), l10("biotlin", 5.0d));
        add(list, str, Operation.MONITOR, "Развешивание ловушек и контроль плодожорки", m(5, 6), 3, 8.0d, 30.0d, false, 2, "Развесьте феромонные ловушки яблонной плодожорки (1 на дерево), клеевые пояса обновите. Больше 5 бабочек за сутки — нужна обработка инсектицидом.", 0.0d, false, fx("pheromone", 1.0d), fx("glue_belt", 1.0d));
        if (z) {
            add(list, str, Operation.SPRAY, "Обработка после цветения от парши и вредителей", m(5, 6), 6, 10.0d, 28.0d, true, 3, "Через 7–10 дней после цветения опрысните Скором (или Раёк) от парши и Алатаром от плодожорки и листогрызущих. Интервал между обработками — 14 дней.", 10.0d, true, l10("skor", 2.0d), l10("rayok", 2.0d).alt(), l10("alatar", 5.0d));
            add(list, str, Operation.FEED_ROOT, "Подкормка в период роста плодов", m(6), 3, 8.0d, 30.0d, false, 2, "Внесите полное минеральное удобрение: азофоска 80–100 г на дерево в кольцевую канавку, полейте. Органика: настой коровяка 1:10.", 0.0d, true, pp("azofoska", 80.0d));
            add(list, str, Operation.FEED_LEAF, "Внекорневая подкормка по листу", m(6, DALL), 6, 12.0d, 30.0d, true, 1, "Опрысните по листу раствором гумата калия (25 мл на 10 л) вечером или в пасмурную погоду — улучшает налив плодов и усвоение микроэлементов.", 10.0d, true, l10("humate", 25.0d));
            add(list, str, Operation.SPRAY, "Летняя защита от парши и плодовой гнили", m(DALL, 8), DALL, 12.0d, 32.0d, true, 2, "При появлении пятен парши обработайте Скором (ожидание 20 дней) или Раёк. Сильно поражённые плоды снимите и уничтожьте. Последняя обработка — не позже чем за 20 дней до съёма.", 10.0d, true, l10("skor", 2.0d), l10("rayok", 2.0d).alt(), l10("fitosporin", 15.0d).alt());
            add(list, str, Operation.WATER, "Влагозарядный полив при засухе", m(DALL, 8), DALL, 10.0d, 40.0d, false, 2, "В сухую погоду полейте 80–120 л под дерево в 2 приёма. Особенно важен полив в период налива плодов — иначе плоды мельчают и осыпаются.", 0.0d, false, pp(Operation.WATER, 100.0d));
            add(list, str, Operation.HARVEST, "Сбор урожая", m(8, 9), DALL, 5.0d, 30.0d, false, 3, "Снимайте плоды в сухую погоду, начиная с нижних ветвей. Повреждённые плоды не закладывайте на хранение. Падалицу ежедневно собирайте и утилизируйте.", 0.0d, false, new MatRef[0]);
            add(list, str, Operation.SPRAY, "Осенняя защита от парши (по листьям)", m(9), 3, 8.0d, 22.0d, true, 2, "После съёма урожая обработайте крону Раёк или Скором по оставшимся листьям — это снизит инфекционный фон парши на следующий сезон.", 10.0d, true, l10("rayok", 2.0d), l10("skor", 2.0d).alt());
        }
        add(list, str, Operation.FEED_ROOT, "Осенняя подкормка фосфором и калием", m(9), 6, 5.0d, 20.0d, false, 3, "Внесите под перекопку приствольного круга суперфосфат 60 г и сульфат калия 40 г на дерево (азот осенью не вносим!). Калий повышает зимостойкость.", 0.0d, true, pp("superphosphate", 60.0d), pp("potassium_sulfate", 40.0d), pp("kalmag", 50.0d).alt());
        add(list, str, Operation.SOIL, "Уборка и обеззараживание опавшей листвы", m(9, 10), DALL, 3.0d, 18.0d, false, 2, "Сгребите опавшие листья — в них зимует парша. Здоровые заложите в компост с переслойкой землёй, поражённые сожгите или вывезите.", 0.0d, false, new MatRef[0]);
        add(list, str, Operation.SPRAY, "Искореняющее осеннее опрыскивание", m(10, 11), DALL, 2.0d, 12.0d, true, 2, "После листопада опрысните деревья и почву 5% железным купоросом (500 г на 10 л) — против лишайников, мхов и зимующих спор. Железный купорос также профилактирует хлороз.", 10.0d, true, l10("iron_sulfate", 500.0d));
        add(list, str, Operation.REPAIR, "Лечение ран и морозобоин", m(10, 11), 3, 2.0d, 15.0d, true, 2, "Зачистите раны до здоровой древесины, продезинфицируйте 1% медным купоросом и замажьте садовой замазкой ЗСП или варом.", 0.0d, false, fx("zsp", 1.0d), pp("garden_tar", 30.0d));
        add(list, str, Operation.PROTECT, "Защита штамба от грызунов и морозобоин", m(11, 12), DALL, -15.0d, 8.0d, false, 3, "Обвяжите штамб лапником (иглами вниз), сеткой или побелите на зиму. В феврале обновите побелку — защита от солнечных ожогов важнее, чем кажется.", 0.0d, false, fx("lapnik", 1.0d), pp("whitewash", 0.15d));
        add(list, str, Operation.PLANT, "Посадка саженца (весна или осень)", m(D3, 9, 10), 3, 5.0d, 18.0d, false, 1, "Яма 70×70 см, смесь почвы с перегноем (2 ведра), суперфосфатом (150 г) и золой (300 г). Корневая шейка на 3–5 см выше уровня почвы. После посадки полейте 20–30 л и замульчируйте.", 0.0d, false, pp("manure", 20.0d), pp("superphosphate", 150.0d), pp("ash", 300.0d));
        if (z) {
            add(list, str, Operation.MONITOR, "Зимняя ревизия сада и снятие мумифицированных плодов", m(12, 1, 2), DALL, -25.0d, 5.0d, false, 1, "Снимите с ветвей оставшиеся мумифицированные плоды — источник монилиоза. Стряхните мокрый снег с ветвей, проверьте обвязку от грызунов.", 0.0d, false, new MatRef[0]);
        }
    }

    private static void walnut(List<Rule> list) {
        add(list, "walnut", Operation.PRUNE, "Санитарная обрезка грецкого ореха", m(3, 9), 3, -5.0d, 12.0d, false, 2, "Орех обрезают минимально: удалите сухие, перекрещивающиеся и загущающие ветви. Крупные спилы обязательно замажьте — орех сильно «плачет».", 0.0d, false, pp("garden_tar", 40.0d));
        add(list, "walnut", Operation.WHITEN, "Побелка штамба", m(3, 10), 6, 2.0d, 15.0d, true, 1, "Побелите штамб и основания скелетных ветвей садовой побелкой.", 0.0d, false, pp("whitewash", 0.2d));
        add(list, "walnut", Operation.FEED_ROOT, "Подкормка ореха", m(D3, 6), 3, 6.0d, 28.0d, false, 2, "Весной — азофоска 100 г под дерево, в июне — калимагнезия 60 г. Орех любит золу: 500 г под перекопку раз в 2 года.", 0.0d, true, pp("azofoska", 100.0d), pp("kalmag", 60.0d));
        add(list, "walnut", Operation.SPRAY, "Обработка от бактериоза и пятнистостей", m(5, 6), 6, 10.0d, 28.0d, true, 2, "При появлении тёмных пятен на листьях и плодах обработайте бордоской жидкостью 1% или медным купоросом. Повторите через 12–14 дней.", 10.0d, true, l10("bordeaux", 100.0d), l10("copper_sulfate", 100.0d).alt());
        add(list, "walnut", Operation.HARVEST, "Сбор орехов", m(9, 10), DALL, 5.0d, 22.0d, false, 2, "Собирайте орехи, когда лопается зелёная оболочка. Очистите от околоплодника и просушите 1–2 недели в сухом проветриваемом месте.", 0.0d, false, new MatRef[0]);
        add(list, "walnut", Operation.SOIL, "Содержание приствольного круга", m(5, 9), 2, 8.0d, 28.0d, false, 1, "Держите приствольный круг под мульчей или залужением — корни ореха не любят перекопки. Под орехом ничего не сажайте: корни выделяют юглон.", 0.0d, false, pp("bark", 50.0d));
    }

    private static void stonePlant(List<Rule> list, String str, String str2, String str3, boolean z) {
        add(list, str, Operation.PRUNE, "Обрезка " + str2, m(3), 3, -5.0d, 10.0d, false, 3, "Косточковые обрезайте ранней весной до набухания почек: удалите сухие, больные и загущающие ветви. У вишни не укорачивайте однолетние ветви — плоды образуются на их концах.", 0.0d, false, pp("garden_tar", 15.0d));
        add(list, str, Operation.SPRAY, "Ранневесеннее искореняющее опрыскивание", m(3), 6, 4.0d, 15.0d, true, 3, "До распускания почек обработайте 3% бордоской жидкостью или медным купоросом против коккомикоза, монилиоза и клястероспориоза.", 8.0d, true, l10("copper_sulfate", 300.0d), l10("bordeaux", 300.0d).alt());
        add(list, str, Operation.WHITEN, "Побелка ствола", m(3, 10), 6, 2.0d, 15.0d, true, 1, "Побелите штамб и основания скелетных ветвей — косточковые страдают от камедетечения после солнечных ожогов.", 0.0d, false, pp("whitewash", 0.12d));
        add(list, str, Operation.FEED_ROOT, "Весенняя подкормка", m(D3), 3, 5.0d, 25.0d, false, 3, "Аммиачная селитра 40–50 г или карбамид под дерево по влажной почве. Косточковые отзывчивы и на золу: 300 г под перекопку.", 0.0d, true, pp("ammonium_nitrate", 50.0d), pp("urea", 45.0d).alt(), pp("ash", 300.0d));
        add(list, str, Operation.SPRAY, "Защита в фазы бутонизации и цветения", m(D3), 6, 6.0d, 22.0d, true, 3, "Перед цветением и сразу после — Хорус от монилиоза и курчавости листьев персика (особенно важно при холодной влажной весне). При появлении тли добавьте Биотлин.", 8.0d, true, l10("horus", 2.0d), l10("skor", 2.0d).alt(), l10("biotlin", 5.0d));
        if (z) {
            add(list, str, Operation.SPRAY, "Летняя обработка от коккомикоза и пятнистостей", m(6, DALL), 3, 12.0d, 30.0d, true, 2, "После съёма ранних сортов обработайте Скором или Раёк против коккомикоза и клястероспориоза — сохраняем листья до осени.", 8.0d, true, l10("skor", 2.0d), l10("rayok", 2.0d).alt());
            add(list, str, Operation.HARVEST, "Сбор урожая", m(6, DALL, 8), DALL, 8.0d, 32.0d, false, 3, "Снимайте ягоды в сухую погоду утром. Черешню и вишню — с плодоножкой, сливу — в фазе технической спелости, дозаривается при хранении.", 0.0d, false, new MatRef[0]);
            add(list, str, Operation.MONITOR, "Контроль вишнёвой мухи и тли", m(5, 6), DALL, 10.0d, 30.0d, false, 2, "Жёлтые клеевые ловушки на вишнёвую муху с конца мая. При массовом лёте — обработка Алатаром до начала окрашивания плодов.", 0.0d, false, l10("alatar", 5.0d));
        }
        add(list, str, Operation.FEED_ROOT, "Осенняя подкормка фосфором и калием", m(9), 6, 5.0d, 20.0d, false, 3, "Суперфосфат 50 г + сульфат калия 30 г под дерево. Азот исключить — иначе побеги не вызреют и вымерзнут.", 0.0d, true, pp("superphosphate", 50.0d), pp("potassium_sulfate", 30.0d));
        add(list, str, Operation.SOIL, "Уборка опавшей листвы", m(9, 10), DALL, 3.0d, 18.0d, false, 2, "Листья косточковых — главный источник коккомикоза. Сгребите и сожгите или заложите в отдельный компост.", 0.0d, false, new MatRef[0]);
        add(list, str, Operation.SPRAY, "Искореняющее опрыскивание после листопада", m(10, 11), DALL, 2.0d, 12.0d, true, 2, "5% железный купорос по голым ветвям и приствольному кругу.", 8.0d, true, l10("iron_sulfate", 500.0d));
        add(list, str, Operation.PROTECT, "Защита от грызунов и выпревания", m(11, 12), DALL, -15.0d, 8.0d, false, 2, "Обвяжите штамбы лапником или сеткой. Абрикос и персик страдают от выпревания корневой шейки — не укрывайте их плёнкой, только дышащим материалом.", 0.0d, false, fx("lapnik", 1.0d));
        add(list, str, Operation.PLANT, "Посадка саженца", m(D3, 9, 10), 3, 5.0d, 18.0d, false, 1, "Косточковые сажают на возвышенных местах без застоя воды. Яма 60×60 см, перегной 1–2 ведра, зола 300 г. Не заглубляйте корневую шейку.", 0.0d, false, pp("manure", 15.0d), pp("ash", 300.0d));
        if (z) {
            add(list, str, Operation.MONITOR, "Зимний осмотр: камедь, повреждения, мумии", m(12, 1, 2), DALL, -25.0d, 5.0d, false, 1, "Зачистите натеки камеди и замажьте ЗСП. Снимите мумифицированные плоды. Проверьте защиту от мышей и зайцев.", 0.0d, false, fx("zsp", 1.0d));
        }
    }

    private static void currantLike(List<Rule> list, String str, String str2, String str3, boolean z) {
        int i;
        add(list, str, Operation.PRUNE, "Обрезка " + str2, m(2, 3), DALL, -10.0d, 8.0d, false, 3, "Вырежьте под корень ветви старше 5–6 лет (тёмные, с растрескавшейся корой), больные и лежащие на земле. Оставляйте 10–15 разновозрастных побегов.", 0.0d, false, new MatRef[0]);
        add(list, str, Operation.SPRAY, "Ранневесенняя обработка от зимующих вредителей", m(3), 6, 4.0d, 15.0d, true, 3, "До распускания почек опрысните кусты и почву под ними бордоской жидкостью 3% или медным купоросом против антракноза, септориоза и зимующих стадий тли и почкового клеща.", 3.0d, true, l10("bordeaux", 300.0d), l10("copper_sulfate", 300.0d).alt());
        add(list, str, Operation.FEED_ROOT, "Весенняя подкормка", m(D3), 3, 5.0d, 25.0d, false, 3, "Карбамид 30 г под куст (молодым) или азофоска 40 г (плодоносящим) в кольцевую бороздку, заделать и полить.", 0.0d, true, pp("urea", 30.0d), pp("azofoska", 40.0d).alt());
        if (z) {
            add(list, str, Operation.SPRAY, "Защита от тли и почкового клеща", m(D3, 5), 6, 8.0d, 25.0d, true, 3, "По распускающимся листьям — Биотлин от тли; верхушки с расселенцами тли обрежьте и уничтожьте. Против почкового клеща (вздутые почки) — сбор вздутых почек вручную и опрыскивание коллоидной серой до цветения; сильно поражённые ветви вырежьте и сожгите.", 3.0d, true, l10("biotlin", 5.0d), l10("sulfur", 30.0d).alt());
            add(list, str, Operation.SPRAY, "Обработка от антракноза и бокальчатой ржавчины", m(5, 6), DALL, 10.0d, 28.0d, true, 2, "При появлении пятен на листьях обработайте Скором или 1% бордоской жидкостью. Повторите через 12–14 дней, последняя обработка — за 20 дней до сбора.", 3.0d, true, l10("skor", 2.0d), l10("bordeaux", 100.0d).alt());
            i = 0;
            add(list, str, Operation.HARVEST, "Сбор ягод", m(6, DALL), DALL, 8.0d, 32.0d, false, 3, "Чёрную смородину собирайте кистями в сухую погоду. Крыжовник — по мере созревания, недозрелые ягоды дозариваются.", 0.0d, false, new MatRef[0]);
            add(list, str, Operation.SOIL, "Уход за прикустовым кругом и мульчирование", m(5, 9), 6, 8.0d, 28.0d, false, 1, "Прополка и мульчирование слоем 5–7 см (перегной, скошенная трава, кора). Мульча сохраняет влагу и сдерживает сорняки.", 0.0d, false, pp("manure", 5.0d), pp("bark", 30.0d).alt());
        } else {
            i = 0;
        }
        add(list, str, Operation.FEED_ROOT, "Осенняя подкормка и зола", m(9), 6, 5.0d, 20.0d, false, 3, "Суперфосфат 40 г + сульфат калия 25 г + зола 200 г под куст в бороздку по периметру кроны.", 0.0d, true, pp("superphosphate", 40.0d), pp("potassium_sulfate", 25.0d), pp("ash", 200.0d));
        add(list, str, Operation.SOIL, "Осенняя санитарная чистка куста", m(10), DALL, 2.0d, 15.0d, false, 2, "Уберите опавшие листья (источник антракноза), вырежьте сухие и больные ветви, слабые однолетние побеги.", 0.0d, false, new MatRef[i]);
        add(list, str, Operation.PLANT, "Посадка саженца", m(D3, 9, 10), 3, 5.0d, 18.0d, false, 1, "Яма 50×50 см, сажают наклонно на 5–7 см глубже, чем рос в питомнике — для образования новых побегов. Перегной ведро + зола 200 г.", 0.0d, false, pp("manure", 10.0d), pp("ash", 200.0d));
    }

    private static void simpleBush(List<Rule> list, String str, String str2, String str3) {
        add(list, str, Operation.PRUNE, "Обрезка и омоложение " + str2, m(3), 3, -8.0d, 10.0d, false, 2, "Удалите сухие, поломанные и загущающие ветви. Старые кусты омолаживайте постепенно, вырезая по 2–3 старые ветви в год.", 0.0d, false, new MatRef[0]);
        add(list, str, Operation.SPRAY, "Ранневесеннее опрыскивание", m(3, D3), 5, 4.0d, 15.0d, true, 2, "До распускания почек — бордоская жидкость 3% по кроне и почве.", 3.0d, true, l10("bordeaux", 300.0d));
        add(list, str, Operation.FEED_ROOT, "Подкормка", m(D3, 6), 3, 6.0d, 28.0d, false, 2, "Весной азофоска 40 г под куст, в июне — зола 200 г под рыхление.", 0.0d, true, pp("azofoska", 40.0d), pp("ash", 200.0d));
        add(list, str, Operation.HARVEST, "Сбор ягод", m(6, DALL, 8), DALL, 8.0d, 32.0d, false, 2, "Собирайте в сухую погоду. Облепиху удобнее срезать вместе с веточками или заморозить и обить.", 0.0d, false, new MatRef[0]);
        add(list, str, Operation.FEED_ROOT, "Осенняя подкормка", m(9), 6, 5.0d, 20.0d, false, 2, "Суперфосфат 40 г + сульфат калия 25 г под куст.", 0.0d, true, pp("superphosphate", 40.0d), pp("potassium_sulfate", 25.0d));
        add(list, str, Operation.PLANT, "Посадка саженца", m(D3, 9, 10), 3, 5.0d, 18.0d, false, 1, "Яма 50×50 см с перегноем и золой. Облепихе нужна пара: мужское и женское растение на участке.", 0.0d, false, pp("manure", 10.0d), pp("ash", 200.0d));
    }

    private static void blueberry(List<Rule> list) {
        add(list, "blueberry", Operation.PRUNE, "Обрезка голубики", m(3), 3, -5.0d, 10.0d, false, 2, "Удалите тонкие приросты у основания, ветви старше 5 лет и загущающие побеги. У молодых кустов удаляйте цветочные почки для формирования куста.", 0.0d, false, new MatRef[0]);
        add(list, "blueberry", Operation.SOIL, "Подкисление почвы", m(D3, 8), 3, 5.0d, 28.0d, false, 3, "Голубике нужен pH 3,5–5,0. Подкисляйте сульфатом аммония (физиологически кислое удобрение, 20–30 г/м² за 2 приёма) или элементарной серой при осенней перекопке. Золу и навоз под голубику не вносят никогда!", 3.0d, true, pp("ammonium_sulfate", 30.0d));
        add(list, "blueberry", Operation.FEED_ROOT, "Подкормка специальным удобрением", m(D3, 5, 6), 3, 6.0d, 28.0d, false, 3, "Только кислые минеральные удобрения для голубики: 40–50 г под куст в 2–3 приёма за сезон (апрель, май, июнь). Никакой органики и золы.", 0.0d, true, pp("blueberry_mix", 50.0d));
        add(list, "blueberry", Operation.MULCH, "Мульчирование сосновой корой", m(D3, 9), 2, 5.0d, 25.0d, false, 2, "Слой коры 7–10 см — сохраняет влагу, кислотность и защищает корни от перегрева.", 0.0d, false, pp("bark", 50.0d));
        add(list, "blueberry", Operation.SPRAY, "Профилактика грибных болезней", m(5, 6), 6, 10.0d, 28.0d, true, 2, "При признаках серой гнили и пятнистостей обработайте Скором (за 20 дней до сбора) или Фитоспорином как биопрепаратом.", 3.0d, true, l10("skor", 2.0d), l10("fitosporin", 15.0d).alt());
        add(list, "blueberry", Operation.HARVEST, "Сбор ягод", m(DALL, 8), DALL, 10.0d, 32.0d, false, 3, "Ягоды готовы, когда полностью посинеют и легко отрываются. Собирайте в 2–3 приёма.", 0.0d, false, new MatRef[0]);
        add(list, "blueberry", Operation.WATER, "Регулярный полив в засуху", m(6, DALL), DALL, 10.0d, 40.0d, false, 3, "Корни голубики поверхностные: 10–15 л под куст 2 раза в неделю в засуху. Пересыхание в период налива — потеря урожая следующего года.", 0.0d, false, pp(Operation.WATER, 15.0d));
        add(list, "blueberry", Operation.PROTECT, "Укрытие на зиму молодых кустов", m(11), DALL, -5.0d, 8.0d, false, 2, "Молодые кусты обвяжите спанбондом в 1–2 слоя. Ветки пригните к земле, зимой набросайте снега.", 0.0d, false, fx("spunbond", 1.0d));
    }

    private static void raspberryLike(List<Rule> list, String str, String str2, String str3, boolean z) {
        add(list, str, Operation.PRUNE, "Весенняя вырезка и нормировка побегов " + str2, m(D3), 3, 2.0d, 15.0d, false, 3, "Вырежьте под корень отплодоносившие, сухие и слабые побеги. Оставьте 8–10 сильных побегов на погонный метр, верхушки укоротите до первой здоровой почки.", 0.0d, false, new MatRef[0]);
        add(list, str, Operation.PROTECT, "Подвязка к шпалере", m(D3), 6, 5.0d, 20.0d, false, 3, "Подвяжите побеги к шпалере веером или лентой на высоте 0,5 и 1,5 м — ягоды чище, куст проветривается, меньше болезней.", 0.0d, false, fx("trellis", 1.0d));
        add(list, str, Operation.FEED_ROOT, "Весенняя подкормка азотом", m(D3, 5), 3, 5.0d, 25.0d, false, 3, "Карбамид 20–30 г/м² или настой коровяка 1:10. " + str3 + " очень требовательна к азоту весной.", 0.0d, false, pp("urea", 25.0d));
        if (z) {
            add(list, str, Operation.SPRAY, "Защита от тли, клеща и стеблевой мухи", m(5, 6), 3, 10.0d, 28.0d, true, 2, "До цветения — Биотлин или Фитоверм от тли и малинного клеща. Увядающие верхушки (стеблевая муха) срежьте на 15 см ниже увядания.", 3.0d, true, l10("biotlin", 5.0d), l10("phytoverm", 20.0d).alt());
            add(list, str, Operation.SPRAY, "Обработка от пурпуровой пятнистости и ржавчины", m(6, DALL), 6, 12.0d, 30.0d, true, 2, "При пятнах на стеблях и листьях — Фитоспорин каждые 10–14 дней или Скор (не позже чем за 20 дней до сбора ягод).", 3.0d, true, l10("fitosporin", 15.0d), l10("skor", 2.0d).alt());
        }
        add(list, str, Operation.HARVEST, "Сбор ягод", m(DALL, 8), DALL, 10.0d, 32.0d, false, 3, "Собирайте каждые 1–2 дня в сухую погоду. Ремонтантные сорта плодоносят до заморозков на однолетних побегах.", 0.0d, false, new MatRef[0]);
        add(list, str, Operation.FEED_ROOT, "Осенняя подкормка", m(9), 6, 5.0d, 20.0d, false, 3, "Суперфосфат 40 г/м² + сульфат калия 20 г/м² в бороздки. Зола 200 г/м².", 0.0d, false, pp("superphosphate", 40.0d), pp("potassium_sulfate", 20.0d), pp("ash", 200.0d));
        add(list, str, Operation.PRUNE, "Осенняя вырезка отплодоносивших побегов", m(9, 10), DALL, 3.0d, 18.0d, false, 3, "Вырежьте под корень все двухлетние (отплодоносившие) побеги, не оставляя пеньков. Ремонтантную можно скосить полностью — весной отрастёт заново.", 0.0d, false, new MatRef[0]);
        add(list, str, Operation.MULCH, "Мульчирование рядов", m(10, 11), DALL, 0.0d, 15.0d, false, 2, "Замульчируйте ряды перегноем или соломой 7–10 см — защита корней от мороза и весенний запас питания.", 0.0d, false, pp("manure", 5.0d));
        if (z) {
            add(list, str, Operation.PROTECT, "Пригибание и укрытие на зиму", m(11), DALL, -5.0d, 8.0d, false, 2, "Свяжите побеги в пучки и пригните к земле до устойчивых морозов, зимой набросайте снега. Побеги не должны выпирать из-под снега.", 0.0d, false, new MatRef[0]);
        }
        add(list, str, Operation.PLANT, "Посадка новых побегов", m(D3, 9, 10), 3, 5.0d, 18.0d, false, 1, "Саженцы в траншею 40×40 см с перегноем, через 0,5–0,7 м. Корневую шейку не заглублять. Ограничьте ряд шифером — " + str3 + " сильно расползается.", 0.0d, false, pp("manure", 10.0d));
    }

    private static void strawberry(List<Rule> list) {
        add(list, "strawberry", Operation.SOIL, "Ранневесенняя чистка плантации", m(3, D3), 6, 2.0d, 15.0d, false, 3, "Уберите старые сухие листья и мульчу, взрыхлите междурядья, подсыпьте землю к оголившимся рожкам. Пролейте тёплой водой для старта роста.", 0.0d, false, new MatRef[0]);
        add(list, "strawberry", Operation.FEED_ROOT, "Весенняя подкормка", m(D3), 6, 5.0d, 22.0d, false, 3, "Аммиачная селитра 15–20 г/м² или настой коровяка 1:10 по влажной почве.", 0.0d, false, pp("ammonium_nitrate", 20.0d));
        add(list, "strawberry", Operation.SPRAY, "Обработка перед цветением", m(D3, 5), 6, 8.0d, 22.0d, true, 3, "По молодым листьям — Фитоспорин от серой гнили и пятнистостей, Биотлин от тли. Во время цветения обработки не проводят!", 2.0d, true, l10("fitosporin", 15.0d), l10("biotlin", 5.0d));
        add(list, "strawberry", Operation.MONITOR, "Защита ягод от слизней и гнили", m(5, 6), DALL, 8.0d, 28.0d, false, 3, "Замульчируйте междурядья соломой или разложите Грозу от слизней (3 г/м²). Ягоды не должны лежать на земле.", 0.0d, false, m2("metaldehyde", 3.0d));
        add(list, "strawberry", Operation.HARVEST, "Сбор ягод", m(6, DALL), DALL, 10.0d, 32.0d, false, 3, "Собирайте утром в сухую погоду с чашелистиками и плодоножкой, каждые 1–2 дня.", 0.0d, false, new MatRef[0]);
        add(list, "strawberry", Operation.PRUNE, "Обрезка листьев после плодоношения", m(DALL, 8), 6, 10.0d, 30.0d, false, 3, "Скосите или срежьте старые листья через 2–3 недели после сбора (не позже начала августа!), не повредив сердечко. Листья уберите — в них зимуют клещ и пятнистости.", 0.0d, false, new MatRef[0]);
        add(list, "strawberry", Operation.FEED_ROOT, "Подкормка после обрезки листьев", m(8), 6, 10.0d, 28.0d, false, 3, "Азофоска 30 г/м² + зола 200 г/м² — закладываем урожай следующего года. Полейте и замульчируйте.", 0.0d, false, pp("azofoska", 30.0d), pp("ash", 200.0d));
        add(list, "strawberry", Operation.PLANT, "Посадка новых кустов и укоренение усов", m(8, 9), DALL, 8.0d, 25.0d, false, 2, "Укорените первые розетки от маточных кустов или посадите рассаду: сердечко на уровне почвы, 30×50 см, полить и притенить спанбондом.", 0.0d, false, fx("agrofibre_17", 1.0d));
        add(list, "strawberry", Operation.FEED_ROOT, "Осенняя подкормка калием", m(9), 6, 5.0d, 20.0d, false, 3, "Сульфат калия 20 г/м² или зола 300 г/м² — без азота. Повышает зимостойкость цветочных почек.", 0.0d, false, pp("potassium_sulfate", 20.0d), pp("ash", 300.0d).alt());
        add(list, "strawberry", Operation.PROTECT, "Укрытие на зиму", m(11), DALL, -5.0d, 5.0d, false, 2, "После первых заморозков укройте лапником или спанбондом-60 по дугам. Солому не используйте — мыши.", 0.0d, false, fx("spunbond", 1.0d), fx("lapnik", 1.0d).alt());
    }

    private static void grape(List<Rule> list) {
        add(list, "grape", Operation.PROTECT, "Открытие кустов после зимы", m(D3), 3, 3.0d, 15.0d, false, 3, "Откройте кусты, когда минует угроза сильных морозов (обычно первая половина апреля). Поднимите лозу, просушите, подвяжите. При угрозе заморозков укройте спанбондом по дугам.", 0.0d, false, fx("spunbond", 1.0d));
        add(list, "grape", Operation.PRUNE, "Сухая подвязка лозы", m(D3), 6, 5.0d, 18.0d, false, 3, "Подвяжите прошлогодние лозы горизонтально к нижней проволоке шпалеры — побеги пойдут равномерно. Смотрите схему обрезки винограда в приложении.", 0.0d, false, fx("trellis", 1.0d));
        add(list, "grape", Operation.SPRAY, "Первая обработка по распускающимся почкам", m(D3, 5), 3, 8.0d, 22.0d, true, 3, "По зелёному конусу — бордоская жидкость 1% или ХОМ против милдью и антракноза. Профилактика обязательна даже без признаков болезни.", 5.0d, true, l10("bordeaux", 100.0d), l10("hom", 40.0d).alt());
        add(list, "grape", Operation.PRUNE, "Зелёные операции: обломка, пасынкование, чеканка", m(5, 6, DALL), DALL, 10.0d, 32.0d, false, 2, "Обломайте лишние и двойные побеги, выщипните пасынки (оставляя 1–2 листа), в конце июля проведите чеканку. Лоза должна проветриваться.", 0.0d, false, new MatRef[0]);
        add(list, "grape", Operation.SPRAY, "Защита от милдью и оидиума", m(6, DALL), 6, 12.0d, 32.0d, true, 3, "До цветения и после — Топаз от оидиума + бордоская жидкость от милдью. Последняя химобработка — за 30 дней до сбора. В сырое лето обработки каждые 10–14 дней.", 5.0d, true, l10("topaz", 2.0d), l10("bordeaux", 100.0d), l10("sulfur", 30.0d).alt());
        add(list, "grape", Operation.FEED_ROOT, "Подкормка перед цветением и наливом ягод", m(5, 6), 2, 8.0d, 30.0d, false, 2, "Азофоска 40 г на куст до цветения; во время налива — сульфат калия 30 г + суперфосфат 40 г. Азот во второй половине лета исключите.", 0.0d, true, pp("azofoska", 40.0d), pp("potassium_sulfate", 30.0d), pp("superphosphate", 40.0d));
        add(list, "grape", Operation.HARVEST, "Сбор урожая", m(8, 9), DALL, 8.0d, 28.0d, false, 3, "Срезайте кисти секатором в сухую погоду. Контролируйте сахаристость по вкусу и окраске семян.", 0.0d, false, new MatRef[0]);
        add(list, "grape", Operation.PRUNE, "Осенняя обрезка лозы", m(10), DALL, 0.0d, 12.0d, false, 3, "После листопада обрежьте лозу: на сучок замещения 3–4 почки, на плодовую стрелку 6–10 почек. Невызревшую (зелёную) часть удалите. Смотрите схему обрезки винограда.", 0.0d, false, new MatRef[0]);
        add(list, "grape", Operation.PROTECT, "Укрытие лозы на зиму", m(10, 11), 6, -5.0d, 10.0d, false, 3, "Снимите лозу со шпалеры, обработайте 5% железным купоросом, свяжите в пучки, уложите на землю и укройте: земля 10–15 см, сверху лапник или щиты. Плёнкой не укрывать — выпревание глазков.", 5.0d, true, l10("iron_sulfate", 500.0d), fx("lapnik", 1.0d));
    }

    private static void clematis(List<Rule> list) {
        add(list, "clematis", Operation.PROTECT, "Открытие после зимовки", m(D3), 3, 3.0d, 15.0d, false, 3, "Снимите укрытие постепенно, притените от яркого солнца на 1–2 недели. Проверьте основания побегов — не заплесневели ли.", 0.0d, false, new MatRef[0]);
        add(list, "clematis", Operation.PRUNE, "Обрезка по группе", m(3, 10), 3, -5.0d, 12.0d, false, 3, "1-я группа — только санитарная; 2-я — укоротить до 1–1,5 м; 3-я группа — обрезка «на пень» 20–30 см. Смотрите схему обрезки клематиса.", 0.0d, false, new MatRef[0]);
        add(list, "clematis", Operation.FEED_ROOT, "Подкормки в сезон", m(D3, 5, 6), 2, 6.0d, 28.0d, false, 2, "Весной — азофоска 30 г, в бутонизации — сульфат калия 20 г. Клематис любит известкование: горсть доломитовой муки раз в 2 года.", 0.0d, true, pp("azofoska", 30.0d), pp("potassium_sulfate", 20.0d), pp("dolomite", 100.0d));
        add(list, "clematis", Operation.SPRAY, "Профилактика увядания (вилта)", m(5, 6), 3, 10.0d, 28.0d, true, 2, "Пролейте основание куста Фитоспорином или раствором марганцовки (розовый). Увядший побег вырежьте до здоровой ткани немедленно.", 2.0d, true, l10("fitosporin", 15.0d), l10("potassium_permanganate", 3.0d).alt());
        add(list, "clematis", Operation.WATER, "Полив в засуху", m(6, DALL, 8), DALL, 10.0d, 38.0d, false, 2, "15–20 л под куст раз в неделю, не по листьям. Корни должны быть в тени — мульча или низкие растения у основания.", 0.0d, false, pp(Operation.WATER, 20.0d));
        add(list, "clematis", Operation.PROTECT, "Укрытие на зиму", m(10, 11), 3, -5.0d, 8.0d, false, 3, "Окучьте основание на 15–20 см смесью земли и перегноя, обработайте центр куста Фундазолом/Фитоспорином, укройте лапником. Мыши — главная опасность зимой.", 0.0d, false, fx("lapnik", 1.0d), pp("manure", 5.0d));
    }

    private static void actinidia(List<Rule> list) {
        add(list, "actinidia", Operation.PRUNE, "Обрезка и подвязка лианы", m(3), 3, -5.0d, 10.0d, false, 2, "Обрезайте до сокодвижения: удалите сухие и загущающие побеги, подвяжите к опоре. Летом прищипните побеги после 10-го листа.", 0.0d, false, fx("trellis", 1.0d));
        add(list, "actinidia", Operation.FEED_ROOT, "Подкормка", m(D3, 6), 3, 6.0d, 28.0d, false, 2, "Азофоска 30 г весной, сульфат калия 20 г в июне. Известь и хлорсодержащие удобрения актинидия не переносит.", 0.0d, true, pp("azofoska", 30.0d), pp("potassium_sulfate", 20.0d));
        add(list, "actinidia", Operation.HARVEST, "Сбор плодов", m(8, 9), DALL, 8.0d, 26.0d, false, 2, "Плоды снимают твёрдыми, дозаривают в помещении с яблоками. Кошек на время сокодвижения не подпускайте — грызут почки.", 0.0d, false, new MatRef[0]);
        add(list, "actinidia", Operation.PROTECT, "Укрытие молодой лианы на зиму", m(11), DALL, -5.0d, 8.0d, false, 2, "Молодые лианы снимите с опоры, сверните и укройте лапником. Взрослая коломикта зимует без укрытия.", 0.0d, false, fx("lapnik", 1.0d));
    }

    private static void rose(List<Rule> list) {
        add(list, "rose", Operation.PROTECT, "Открытие роз после зимы", m(D3), 1, 2.0d, 12.0d, false, 3, "Снимайте укрытие в пасмурный день, постепенно. Обрежьте подопревшие побеги, пролейте Фитоспорином. Первые 2 недели притеняйте от солнца.", 0.0d, false, new MatRef[0]);
        add(list, "rose", Operation.PRUNE, "Весенняя обрезка роз", m(D3), 3, 4.0d, 15.0d, false, 3, "Чайно-гибридные — на 4–6 почек, флорибунда — на 5–7, плетистые — только санитарная. Срез над наружной почкой под 45°. Смотрите схему обрезки роз.", 0.0d, false, new MatRef[0]);
        add(list, "rose", Operation.FEED_ROOT, "Подкормки в течение сезона", m(D3, 5, 6, DALL), 3, 6.0d, 28.0d, false, 2, "Удобрение для роз 40–50 г: при отрастании, в бутонизацию и после первой волны цветения. С августа — только фосфор и калий.", 0.0d, true, pp("rose_mix", 45.0d));
        add(list, "rose", Operation.SPRAY, "Защита от тли", m(5, 6), DALL, 10.0d, 28.0d, true, 2, "При появлении колоний тли на бутонах — Биотлин (или био-Фитоверм). Повторите через 7–10 дней.", 3.0d, true, l10("biotlin", 5.0d), l10("phytoverm", 20.0d).alt());
        add(list, "rose", Operation.SPRAY, "Профилактика чёрной пятнистости и мучнистой росы", m(6, DALL, 8), 3, 12.0d, 30.0d, true, 2, "Скор от чёрной пятнистости, Топаз от мучнистой росы — каждые 12–14 дней в сырую погоду. Поражённые листья убирайте.", 3.0d, true, l10("skor", 2.0d), l10("topaz", 2.0d).alt());
        add(list, "rose", Operation.PRUNE, "Осенняя обрезка и окучивание", m(10), 6, 2.0d, 12.0d, false, 3, "Невызревшие побеги вырежьте, остальные укоротите до 30–40 см, листья оборвите. Окучьте основание на 15–20 см сухой землёй с песком.", 0.0d, false, new MatRef[0]);
        add(list, "rose", Operation.PROTECT, "Укрытие роз на зиму", m(11), 3, -5.0d, 3.0d, false, 3, "Укрывайте в сухую погоду при −3…−5 °C: каркас + спанбонд-60 в 2 слоя. Между укрытием и кустом — воздушная прослойка. Торцы закройте после устойчивых морозов.", 0.0d, false, fx("spunbond", 1.0d));
    }

    private static void conifer(List<Rule> list) {
        add(list, "conifer", Operation.PROTECT, "Защита от солнечных ожогов", m(2, 3), DALL, -10.0d, 10.0d, false, 3, "В феврале–марте хвоя горит на солнце при мёрзлых корнях. Затените туи и можжевельники сеткой или спанбондом с южной стороны.", 0.0d, false, fx("spunbond", 1.0d));
        add(list, "conifer", Operation.FEED_ROOT, "Весенняя подкормка хвойных", m(D3), 3, 4.0d, 20.0d, false, 2, "Только специальное удобрение для хвойных 40–60 г или азофоска 30 г. Навоз и зола хвойным противопоказаны.", 0.0d, true, pp("azofoska", 30.0d));
        add(list, "conifer", Operation.PRUNE, "Стрижка и санитарная обрезка", m(6), 3, 10.0d, 28.0d, false, 2, "Стригите после окончания роста побегов, срезая не более 1/3 прироста. Сухие и побуревшие ветви вырежьте.", 0.0d, false, new MatRef[0]);
        add(list, "conifer", Operation.WATER, "Осенний влагозарядный полив", m(10), 6, 3.0d, 15.0d, false, 3, "30–50 л под растение до замерзания почвы — защита хвои от зимнего иссушения.", 0.0d, false, pp(Operation.WATER, 40.0d));
    }

    private static void hydrangea(List<Rule> list) {
        add(list, "hydrangea", Operation.PRUNE, "Обрезка гортензии", m(3, D3), 3, 2.0d, 15.0d, false, 3, "Метельчатая и древовидная — коротко на 2–4 пары почек; крупнолистная — только санитарная (цветёт на прошлогодних побегах).", 0.0d, false, new MatRef[0]);
        add(list, "hydrangea", Operation.FEED_ROOT, "Подкормки и подкисление", m(D3, 5, 6), 3, 6.0d, 28.0d, false, 2, "Удобрение для гортензий/рододендронов 40 г в 3 приёма. Для голубого цвета — подкисление сульфатом алюминия или серой.", 0.0d, true, pp("rose_mix", 40.0d), l10("sulfur", 10.0d));
        add(list, "hydrangea", Operation.WATER, "Обильный полив в засуху", m(6, DALL, 8), DALL, 10.0d, 38.0d, false, 3, "20–30 л мягкой воды под куст 1–2 раза в неделю. Гортензия не переносит пересыхания в цветение.", 0.0d, false, pp(Operation.WATER, 25.0d));
        add(list, "hydrangea", Operation.PROTECT, "Укрытие крупнолистной гортензии", m(10, 11), 3, -5.0d, 8.0d, false, 3, "Побеги пригните, укройте спанбондом-60 в 2 слоя по каркасу. Метельчатая гортензия зимует без укрытия.", 0.0d, false, fx("spunbond", 1.0d));
    }

    private static void lawn(List<Rule> list) {
        add(list, "lawn", Operation.MOW, "Первая стрижка газона", m(D3), 6, 8.0d, 22.0d, false, 3, "Первый покос при высоте травы 8–10 см, срежьте не более 1/3. Острые ножи — обязательно, иначе кончики желтеют.", 0.0d, false, new MatRef[0]);
        add(list, "lawn", Operation.FEED_ROOT, "Весенняя подкормка газона", m(D3), 3, 5.0d, 22.0d, false, 3, "Азотное удобрение для газона 30–40 г/м² по влажной траве, затем полейте.", 0.0d, false, m2("lawn_spring", 0.035d));
        add(list, "lawn", Operation.MOW, "Регулярная стрижка", m(5, 6, DALL, 8, 9), DALL, 8.0d, 32.0d, false, 2, "Косите раз в 5–7 дней на высоту 4–5 см. В жару поднимите нож до 6–7 см — трава меньше страдает и хуже растут сорняки.", 0.0d, false, new MatRef[0]);
        add(list, "lawn", Operation.FEED_ROOT, "Осенняя подкормка газона (PK)", m(9), 3, 6.0d, 20.0d, false, 3, "Осеннее удобрение без азота 30–40 г/м² — укрепляет дернину перед зимой.", 0.0d, false, m2("lawn_autumn", 0.035d));
        add(list, "lawn", Operation.SOIL, "Аэрация и удаление войлока", m(D3, 9), 2, 5.0d, 25.0d, false, 1, "Проколите дёрн вилами или аэратором, вычешите граблями войлок и мох. После — подсев проплешин и полив.", 0.0d, false, fx("seeds_veg", 1.0d));
    }

    private static void tomato(List<Rule> list) {
        add(list, "tomato", Operation.MONITOR, "Закупка семян и материалов на рассаду", m(1, 2), DALL, 0.0d, 99.0d, false, 3, "Проверьте запасы семян — всхожесть и срок годности: семена старше 2 лет могут не взойти. Закупите заранее: семена томата (ранние, средние и поздние сорта), грунт для рассады или торф с перегноем, кассеты 3–4 см и стаканчики 0,3–0,5 л, поддоны, фитолампу при северных окнах. В феврале ассортимент шире, а цены ниже — в марте нужные сорта разбирают.", 0.0d, false, fx("seeds_veg", 1.0d), fx("seedling_soil", 1.0d), fx("cassette", 1.0d), fx("peat_tablets", 1.0d).alt(), fx("phytolamp", 1.0d).alt());
        add(list, "tomato", Operation.SOIL, "Подготовка грунта и тары для рассады", m(2), DALL, 0.0d, 99.0d, false, 3, "Смешайте: 1 часть дерновой земли + 2 части торфа + 1 часть перегноя (pH 5,5–6,5) или возьмите готовый рассадный субстрат. За 1–2 дня до посева пролейте Фитоспорином (5 г на 10 л) или пропарьте грунт 30 минут — профилактика чёрной ножки. Свежий навоз не вносить! В таре сделайте дренажные отверстия; культуры, не любящие пересадку, сейте сразу в стаканчики 0,3–0,5 л.", 0.0d, false, fx("seedling_soil", 1.0d), l10("fitosporin", 5.0d));
        add(list, "tomato", Operation.PLANT, "Посев томата на рассаду", m(2, 3), 5, 0.0d, 99.0d, false, 3, "Сейте за 55–60 дней до высадки: ранние и тепличные — конец февраля–начало марта, для открытого грунта — середина марта. Грунт для рассады, таблетки или кассеты, глубина 1 см, +22…+25 °C до всходов. Досветка фитолампой 12–14 часов.", 0.0d, false, fx("seedling_soil", 1.0d), fx("seeds_veg", 1.0d), fx("cassette", 1.0d), fx("peat_tablets", 1.0d).alt(), fx("phytolamp", 1.0d));
        add(list, "tomato", Operation.FEED_ROOT, "Уход за рассадой: пикировка, подкормка, закалка", m(3, D3), DALL, 10.0d, 30.0d, false, 3, "Пикируйте в фазе 2 настоящих листьев. Подкормка гуматом (25 мл/10 л) каждые 10 дней. За 10–14 дней до высадки закаляйте: выносите на балкон, сократите полив. При вытягивании — Эпин и больше света.", 2.0d, false, l10("humate", 25.0d), l10("epin", 1.0d));
        add(list, "tomato", Operation.PLANT, "Высадка рассады в теплицу и открытый грунт", m(5), DALL, 10.0d, 25.0d, false, 3, "В теплицу — в начале мая, в грунт под агроволокно — середина-конец мая, когда почва прогреется до +12…+14 °C. Заглубляйте до семядолей вытянувшуюся рассаду. В лунку — горсть перегноя и золы, пролить тёплой водой.", 0.0d, false, pp("manure", 2.0d), pp("ash", 50.0d), fx("agrofibre_17", 1.0d));
        add(list, "tomato", Operation.FEED_ROOT, "Подкормки в период роста и налива", m(6, DALL, 8), 3, 10.0d, 32.0d, false, 2, "Удобрение для овощей 30–40 г/м² раз в 2 недели: до цветения — с азотом, во время налива — калийные (зола 200 г/м² или сульфат калия). Избыток азота в налив — жирующие кусты без плодов.", 0.0d, false, m2("veg_fertilizer", 35.0d), m2("ash", 200.0d).alt());
        add(list, "tomato", Operation.PRUNE, "Формировка и пасынкование томата", m(6, DALL), DALL, 10.0d, 35.0d, false, 3, "Индетерминантные — в 1–2 стебля: пасынки выламывайте утром, не оставляя пенька более 0,5 см, когда они 3–5 см. Нижние листья до первой кисти удалите. Подвязывайте к шпалере. Смотрите схему формирования томата.", 0.0d, false, fx("trellis", 1.0d));
        add(list, "tomato", Operation.SPRAY, "Профилактика фитофторы", m(DALL, 8), DALL, 10.0d, 30.0d, true, 3, "С середины июля в сырую погоду — Фитоспорин-М «Томаты» каждые 7–10 дней (биопрепарат, без ожидания) или ХОМ (ожидание 20 дней). Проветривайте теплицу, поливайте только под корень утром.", 3.0d, false, l10("fitosporin_tomato", 10.0d), l10("hom", 40.0d).alt());
        add(list, "tomato", Operation.WATER, "Правильный полив", m(6, DALL, 8), DALL, 12.0d, 38.0d, false, 2, "5–7 л/м² раз в 3–5 дней строго под корень, утром. Нерегулярный полив — вершинная гниль и растрескивание плодов.", 0.0d, false, m2(Operation.WATER, 5.0d));
        add(list, "tomato", Operation.SPRAY, "Защита от тли и белокрылки в теплице", m(6, DALL), 6, 12.0d, 35.0d, false, 2, "Актофит (био) — ожидание 2–3 дня, можно почти до сбора; при сильном заселении — Конфидор под корень (ожидание 20 дней, только до начала сбора). Жёлтые клеевые ловушки на белокрылку.", 3.0d, false, l10("actophyt", 20.0d), l10("confidor", 1.0d).alt());
        add(list, "tomato", Operation.HARVEST, "Сбор плодов и уборка растительных остатков", m(8, 9), DALL, 8.0d, 28.0d, false, 3, "Снимайте бурые плоды на дозаривание при угрозе фитофторы и ночных холодов (ниже +8 °C). После окончания сезона кусты уберите в компост только если они здоровы; поражённые фитофторой — сжечь или в мусор.", 0.0d, false, new MatRef[0]);
    }

    private static void cucumber(List<Rule> list) {
        add(list, "cucumber", Operation.MONITOR, "Закупка семян и материалов на рассаду", m(1, 2), DALL, 0.0d, 99.0d, false, 3, "Проверьте запасы семян — всхожесть и срок годности: семена старше 2 лет могут не взойти. Закупите заранее: семена огурца (ранние, средние и поздние сорта), грунт для рассады или торф с перегноем, кассеты 3–4 см и стаканчики 0,3–0,5 л, поддоны, фитолампу при северных окнах. В феврале ассортимент шире, а цены ниже — в марте нужные сорта разбирают.", 0.0d, false, fx("seeds_veg", 1.0d), fx("seedling_soil", 1.0d), fx("cassette", 1.0d), fx("peat_tablets", 1.0d).alt(), fx("phytolamp", 1.0d).alt());
        add(list, "cucumber", Operation.SOIL, "Подготовка грунта и тары для рассады", m(2), DALL, 0.0d, 99.0d, false, 3, "Смешайте: 1 часть дерновой земли + 2 части торфа + 1 часть перегноя (pH 5,5–6,5) или возьмите готовый рассадный субстрат. За 1–2 дня до посева пролейте Фитоспорином (5 г на 10 л) или пропарьте грунт 30 минут — профилактика чёрной ножки. Свежий навоз не вносить! В таре сделайте дренажные отверстия; культуры, не любящие пересадку, сейте сразу в стаканчики 0,3–0,5 л.", 0.0d, false, fx("seedling_soil", 1.0d), l10("fitosporin", 5.0d));
        add(list, "cucumber", Operation.FEED_ROOT, "Подкормки рассады до высадки", m(3, 4), 3, 10.0d, 30.0d, false, 3, "Первая подкормка через 10–14 дней после пикировки: нитроаммофоска 2–3 г на 1 л или гумат 25 мл на 10 л. Вторая — в фазе бутонизации: суперфосфат 5 г + сульфат калия 3 г на 1 л. За 2–3 дня до высадки — Эпин-Экстра по листу (1 мл на 5 л) и начало закаливания. Удобрения вносите только по влажному грунту — не по сухим корням.", 0.0d, false, l10("humate", 25.0d), l10("epin", 1.0d), pp("azofoska", 3.0d).alt());
        add(list, "cucumber", Operation.PLANT, "Посев огурца на рассаду", m(D3), 3, 0.0d, 99.0d, false, 3, "Сейте за 25–30 дней до высадки (конец апреля) в стаканчики или таблетки — огурец не любит пересадку. +25…+27 °C до всходов, досветка.", 0.0d, false, fx("seedling_soil", 1.0d), fx("seeds_veg", 1.0d), fx("peat_tablets", 1.0d), fx("phytolamp", 1.0d).alt());
        add(list, "cucumber", Operation.PLANT, "Высадка в теплицу и грунт", m(5), 3, 12.0d, 28.0d, false, 3, "Теплица — начало мая, грунт под дуги с агроволокном — середина-конец мая. Почва +15 °C, в лунку перегной и зола, полив тёплой водой.", 0.0d, false, pp("manure", 2.0d), pp("ash", 50.0d), fx("agrofibre_17", 1.0d));
        add(list, "cucumber", Operation.PRUNE, "Подвязка и формировка", m(5, 6), 6, 12.0d, 32.0d, false, 2, "Подвяжите к шпалере на 5–7-м листе. У партенокарпиков ослепите нижние 4–5 узлов, побеги прищипывайте по схеме. Смотрите схему формировки огурца.", 0.0d, false, fx("trellis", 1.0d));
        add(list, "cucumber", Operation.WATER, "Полив тёплой водой", m(6, DALL, 8), DALL, 12.0d, 38.0d, false, 3, "Огурец — самая влаголюбивая культура: 5–8 л/м² через 1–2 дня, только тёплой (+20…+25 °C) водой под корень или дождеванием утром. Холодная вода — корневые гнили и горечь плодов.", 0.0d, false, m2(Operation.WATER, 6.0d));
        add(list, "cucumber", Operation.FEED_ROOT, "Подкормки в плодоношение", m(6, DALL, 8), 3, 12.0d, 35.0d, false, 2, "Каждые 10–14 дней: удобрение для овощей 20–30 г/м² или настой золы/травы. Огурец любит калий — зола 200 г/м² раз в 2 недели.", 0.0d, false, m2("veg_fertilizer", 25.0d), m2("ash", 200.0d).alt());
        add(list, "cucumber", Operation.SPRAY, "Профилактика пероноспороза (ложной мучнистой росы)", m(6, DALL, 8), 6, 12.0d, 32.0d, true, 3, "При первых жёлтых маслянистых пятнах — ХОМ (ожидание 20 дней) или Фитоспорин каждые 7 дней как профилактика. Удаляйте поражённые листья, проветривайте, не поливайте вечером.", 3.0d, false, l10("hom", 40.0d), l10("fitosporin_tomato", 10.0d).alt());
        add(list, "cucumber", Operation.HARVEST, "Регулярный сбор зеленцов", m(6, DALL, 8, 9), DALL, 10.0d, 32.0d, false, 3, "Собирайте каждые 1–2 дня: переросшие плоды тормозят налив новых. Срезайте или аккуратно отламывайте, не переворачивая плети.", 0.0d, false, new MatRef[0]);
    }

    private static void cabbage(List<Rule> list) {
        add(list, "cabbage", Operation.MONITOR, "Закупка семян и материалов на рассаду", m(1, 2), DALL, 0.0d, 99.0d, false, 3, "Проверьте запасы семян — всхожесть и срок годности: семена старше 2 лет могут не взойти. Закупите заранее: семена капусты (ранние, средние и поздние сорта), грунт для рассады или торф с перегноем, кассеты 3–4 см и стаканчики 0,3–0,5 л, поддоны, фитолампу при северных окнах. В феврале ассортимент шире, а цены ниже — в марте нужные сорта разбирают.", 0.0d, false, fx("seeds_veg", 1.0d), fx("seedling_soil", 1.0d), fx("cassette", 1.0d), fx("peat_tablets", 1.0d).alt(), fx("phytolamp", 1.0d).alt());
        add(list, "cabbage", Operation.SOIL, "Подготовка грунта и тары для рассады", m(2), DALL, 0.0d, 99.0d, false, 3, "Смешайте: 1 часть дерновой земли + 2 части торфа + 1 часть перегноя (pH 5,5–6,5) или возьмите готовый рассадный субстрат. За 1–2 дня до посева пролейте Фитоспорином (5 г на 10 л) или пропарьте грунт 30 минут — профилактика чёрной ножки. Свежий навоз не вносить! В таре сделайте дренажные отверстия; культуры, не любящие пересадку, сейте сразу в стаканчики 0,3–0,5 л.", 0.0d, false, fx("seedling_soil", 1.0d), l10("fitosporin", 5.0d));
        add(list, "cabbage", Operation.FEED_ROOT, "Подкормки рассады до высадки", m(3, 4), 3, 10.0d, 30.0d, false, 3, "Первая подкормка через 10–14 дней после пикировки: нитроаммофоска 2–3 г на 1 л или гумат 25 мл на 10 л. Вторая — в фазе бутонизации: суперфосфат 5 г + сульфат калия 3 г на 1 л. За 2–3 дня до высадки — Эпин-Экстра по листу (1 мл на 5 л) и начало закаливания. Удобрения вносите только по влажному грунту — не по сухим корням.", 0.0d, false, l10("humate", 25.0d), l10("epin", 1.0d), pp("azofoska", 3.0d).alt());
        add(list, "cabbage", Operation.PLANT, "Посев капусты на рассаду", m(3), 6, 0.0d, 99.0d, false, 3, "Ранняя — середина марта, средняя и поздняя — конец марта–начало апреля. +18 °C до всходов, затем +8…+12 °C (иначе вытянется!), досветка, полив марганцовкой для профилактики чёрной ножки.", 0.0d, false, fx("seedling_soil", 1.0d), fx("seeds_veg", 1.0d), fx("cassette", 1.0d), l10("potassium_permanganate", 1.0d));
        add(list, "cabbage", Operation.PLANT, "Высадка рассады в грунт", m(D3, 5), 6, 5.0d, 20.0d, false, 3, "Ранняя — конец апреля под агроволокно, поздняя — май. Заглубляйте до первых настоящих листьев, 50×60 см. В лунку — зола и перегной.", 0.0d, false, pp("manure", 2.0d), pp("ash", 50.0d), fx("agrofibre_17", 1.0d));
        add(list, "cabbage", Operation.SOIL, "Известкование почвы под капусту", m(9, 10), DALL, 3.0d, 18.0d, false, 2, "Капуста не растёт на кислой почве (кила!). Осенью под перекопку внесите гашёную известь 300–400 г/м² или доломитовую муку 500 г/м². Не совмещайте с навозом.", 0.0d, false, m2("lime", 0.35d), m2("dolomite", 500.0d).alt());
        add(list, "cabbage", Operation.SPRAY, "Защита от гусениц и блошки", m(5, 6, DALL), DALL, 10.0d, 30.0d, false, 3, "От крестоцветной блошки — зола с табачной пылью или Актара. От гусениц — Лепидоцид или Битоксибациллин (био, ожидание 5 дней) каждые 7–8 дней; при массовом нашествии — Алиот. Лучшая профилактика — сетка/агроволокно сразу после высадки.", 0.0d, false, m2("lepidocide", 3.0d), m2("bitoxibacillin", 5.0d).alt(), m2("aliot", 1.0d).alt(), m2("ash_husk", 50.0d).alt());
        add(list, "cabbage", Operation.MONITOR, "Защита от слизней", m(6, DALL, 8), DALL, 8.0d, 28.0d, false, 2, "Гроза в междурядья 3 г/м², ловушки из досок и пива, острый мульчирующий материал вокруг растений.", 0.0d, false, m2("metaldehyde", 3.0d));
        add(list, "cabbage", Operation.FEED_ROOT, "Подкормки для налива кочана", m(6, DALL), 3, 10.0d, 30.0d, false, 2, "Удобрение для овощей 40 г/м² или настой коровяка 1:10 каждые 2 недели. Во время налива — упор на калий (зола 300 г/м²).", 0.0d, false, m2("veg_fertilizer", 40.0d), m2("ash", 300.0d).alt());
        add(list, "cabbage", Operation.WATER, "Обильный полив в налив кочана", m(6, DALL, 8), DALL, 12.0d, 35.0d, false, 3, "10–15 л/м² в неделю, вечером или утром. Недостаток влаги в налив — рыхлые кочаны и растрескивание.", 0.0d, false, m2(Operation.WATER, 12.0d));
        add(list, "cabbage", Operation.HARVEST, "Уборка и закладка на хранение", m(9, 10), DALL, 2.0d, 18.0d, false, 3, "Срезайте в сухую погоду при +2…+8 °C (после лёгкого заморозка вкуснее). Оставьте 2–3 кроющих листа и кочерыгу 3–4 см. Хранение при 0…+1 °C.", 0.0d, false, new MatRef[0]);
    }

    private static void potato(List<Rule> list) {
        add(list, "potato", Operation.SOIL, "Проращивание и подготовка клубней", m(3, D3), DALL, 5.0d, 20.0d, false, 3, "За 3–4 недели до посадки разложите клубни на свету при +12…+15 °C для озеленения и проращивания. Перед посадкой опудрите золой или обработайте Фитоспорином.", 0.0d, false, m2("ash", 50.0d), m2("fitosporin", 10.0d));
        add(list, "potato", Operation.PLANT, "Посадка картофеля", m(D3, 5), 3, 8.0d, 20.0d, false, 3, "Сажайте, когда почва на глубине 10 см прогреется до +8 °C (зацвела берёза). Гребни через 70 см, глубина 8–10 см, в лунку — удобрение для картофеля 20 г и горсть золы.", 0.0d, false, m2("potato_fertilizer", 0.02d), m2("ash", 30.0d));
        add(list, "potato", Operation.SOIL, "Окучивание", m(5, 6), 6, 10.0d, 28.0d, false, 3, "Первое окучивание при высоте ботвы 15 см, второе — перед цветением. Окучивайте по влажной почве (после дождя или полива) — защищает клубни от зелени и проволочника.", 0.0d, false, new MatRef[0]);
        add(list, "potato", Operation.SPRAY, "Обработка от колорадского жука", m(6, DALL), DALL, 12.0d, 30.0d, false, 3, "По молодым личинкам — Алатар или Актара (ожидание 20–30 дней, однократно за сезон); био-вариант — Фитоверм (ожидание 3 дня, каждые 7–10 дней). Кладки яиц собирайте вручную.", 3.0d, false, l10("alatar", 5.0d), l10("aktara", 1.4d).alt(), l10("phytoverm", 20.0d).alt());
        add(list, "potato", Operation.SPRAY, "Профилактика фитофтороза", m(DALL, 8), 3, 12.0d, 30.0d, true, 3, "Смыкание ботвы и сырая погода — старт обработок: ХОМ каждые 10–14 дней (ожидание 20 дней) или бордоская жидкость 1%. Фитофтора на картофеле — главная причина гнили клубней при хранении.", 3.0d, false, l10("hom", 40.0d), l10("bordeaux", 100.0d).alt());
        add(list, "potato", Operation.HARVEST, "Уборка урожая", m(8, 9), DALL, 8.0d, 25.0d, false, 3, "Копайте после усыхания ботвы в сухую погоду. Клубни просушите в тени 2–3 часа, 2 недели лечебный период в прохладном месте, затем сортировка и закладка при +2…+4 °C.", 0.0d, false, new MatRef[0]);
        add(list, "potato", Operation.SOIL, "Сидераты после уборки", m(8, 9), 3, 8.0d, 22.0d, false, 2, "Сразу после копки посейте белую горчицу 150–200 г на сотку — оздоравливает почву от фитофторы и проволочника, к зиме заделайте или оставьте.", 0.0d, false, m2("mustard_siderate", 15.0d));
    }

    private static void onion(List<Rule> list) {
        add(list, "onion", Operation.PLANT, "Посадка лука-севка", m(D3, 5), 3, 6.0d, 20.0d, false, 3, "Севок прогрейте 2–3 дня при +30…+35 °C (против стрелкования и пероноспороза), замочите 15 минут в розовом растворе марганцовки. Грядки 20×30 см, глубина 3–4 см.", 0.0d, false, l10("potassium_permanganate", 3.0d));
        add(list, "onion", Operation.SOIL, "Прополка и рыхление", m(5, 6), DALL, 8.0d, 28.0d, false, 2, "Лук не терпит сорняков и корки: рыхлите после каждого дождя, не засыпая отрастающую луковицу.", 0.0d, false, new MatRef[0]);
        add(list, "onion", Operation.SPRAY, "Защита от луковой мухи и пероноспороза", m(5, 6), 6, 10.0d, 28.0d, true, 2, "От луковой мухи — Актара под корень (ожидание 21 день) или зола с табачной пылью по грядке каждые 10 дней. От пероноспороза (серый налёт на пере) — ХОМ (ожидание 20 дней).", 2.0d, false, l10("aktara", 1.4d), m2("ash_husk", 50.0d).alt(), l10("hom", 40.0d).alt());
        add(list, "onion", Operation.FEED_ROOT, "Подкормка в рост пера", m(5, 6), 3, 8.0d, 28.0d, false, 2, "Аммиачная селитра 15 г/м² в фазе 3–4 пера, затем — зола или сульфат калия 20 г/м² для налива луковицы. Свежий навоз луку нельзя.", 0.0d, false, m2("ammonium_nitrate", 15.0d), m2("potassium_sulfate", 20.0d));
        add(list, "onion", Operation.WATER, "Полив до середины июля", m(6, DALL), 1, 10.0d, 32.0d, false, 2, "Поливайте до середины июля, затем полив прекратите — луковица должна вызреть и не тронуться в рост. Полегание пера — сигнал к уборке.", 0.0d, false, m2(Operation.WATER, 5.0d));
        add(list, "onion", Operation.HARVEST, "Уборка и сушка лука", m(8), 3, 8.0d, 26.0d, false, 3, "Убирайте в сухую погоду при полегании 60–70% пера. Сушите 10–14 дней на солнце или в проветриваемом месте, затем обрежьте шейку до 3–4 см. Недозревший лук — на дозаривание.", 0.0d, false, new MatRef[0]);
        add(list, "onion", Operation.PLANT, "Посадка озимого лука", m(10), 3, 3.0d, 12.0d, false, 2, "Озимые сорта сажают за 3–4 недели до устойчивых морозов (октябрь): севок 1–1,5 см, глубина 4–5 см, 10×20 см. Грядка сухая, с золой.", 0.0d, false, m2("ash", 100.0d));
    }

    private static void garlic(List<Rule> list) {
        add(list, "garlic", Operation.PLANT, "Посадка озимого чеснока", m(10), 3, 3.0d, 12.0d, false, 3, "За 2–3 недели до устойчивых морозов (первая половина октября), чтобы зубок укоренился, но не пророс. Зубки на глубину 5–7 см через 10×25 см, на дно — песок и зола. Грядка после огурцов/кабачков, не после лука.", 0.0d, false, m2("ash", 100.0d));
        add(list, "garlic", Operation.PROTECT, "Мульчирование посадок на зиму", m(11), DALL, -5.0d, 8.0d, false, 2, "Замульчируйте торфом или перегноем 3–5 см после первых заморозков.", 0.0d, false, m2("manure", 3.0d));
        add(list, "garlic", Operation.FEED_ROOT, "Весенние подкормки", m(D3, 5), 3, 5.0d, 22.0d, false, 3, "По талой почве — аммиачная селитра 15 г/м², в мае — зола 200 г/м² или сульфат калия 15 г/м².", 0.0d, false, m2("ammonium_nitrate", 15.0d), m2("potassium_sulfate", 15.0d));
        add(list, "garlic", Operation.PRUNE, "Выломка стрелок", m(6), 3, 10.0d, 30.0d, false, 3, "Выломайте стрелки, когда они сделали петлю, оставив пенёк 2 см — прибавка к урожаю до 30%. Часть стрелок оставьте как индикатор созревания (лопнувший чехлик).", 0.0d, false, new MatRef[0]);
        add(list, "garlic", Operation.PLANT, "Посадка ярового чеснока", m(D3), 3, 4.0d, 15.0d, false, 2, "Яровой чеснок сажают как можно раньше (холодостоек): зубки на глубину 3 см, хранить перед посадкой при +3…+5 °C.", 0.0d, false, new MatRef[0]);
        add(list, "garlic", Operation.HARVEST, "Уборка чеснока", m(DALL, 8), 3, 10.0d, 28.0d, false, 3, "Озимый — при пожелтении нижних листьев и выпрямлении стрелок (конец июля). Не передерживайте: распадается на зубки и не хранится. Сушите 2 недели, заплетите в косы или обрежьте.", 0.0d, false, new MatRef[0]);
    }

    private static void veg(List<Rule> list, String str, String str2, String str3, int i, int i2, int[] iArr, int i3, int[] iArr2, int i4, boolean z) {
        if (z) {
            add(list, str, Operation.PLANT, "Посев " + str3 + " на рассаду", m(i), i2, 0.0d, 99.0d, false, 3, "Посейте " + str3.toLowerCase() + " в питательный грунт или торфяные таблетки за 40–55 дней до высадки. До всходов +24…+26 °C под плёнкой, затем снижайте до +18…+20 °C и досвечивайте 12–14 часов.", 0.0d, false, fx("seedling_soil", 1.0d), fx("seeds_veg", 1.0d), fx("cassette", 1.0d), fx("peat_tablets", 1.0d).alt());
            add(list, str, Operation.PLANT, "Высадка рассады " + str3 + " в грунт", iArr, i3, 10.0d, 26.0d, false, 3, "Высаживайте закалённую рассаду, когда почва прогреется до +12…+15 °C и минует угроза заморозков (под агроволокно — на 2 недели раньше). В лунку — перегной и зола, обильный полив тёплой водой.", 0.0d, false, pp("manure", 2.0d), pp("ash", 40.0d), fx("agrofibre_17", 1.0d));
        add(list, str, Operation.MONITOR, "Закупка семян и материалов на рассаду", m(1, 2), DALL, 0.0d, 99.0d, false, 3, "Проверьте запасы семян — всхожесть и срок годности: семена старше 2 лет могут не взойти. Закупите заранее: семена " + str3.toLowerCase() + " (ранние, средние и поздние сорта), грунт для рассады или торф с перегноем, кассеты 3–4 см и стаканчики 0,3–0,5 л, поддоны, фитолампу при северных окнах. В феврале ассортимент шире, а цены ниже — в марте нужные сорта разбирают.", 0.0d, false, fx("seeds_veg", 1.0d), fx("seedling_soil", 1.0d), fx("cassette", 1.0d), fx("peat_tablets", 1.0d).alt(), fx("phytolamp", 1.0d).alt());
        add(list, str, Operation.SOIL, "Подготовка грунта и тары для рассады", m(2), DALL, 0.0d, 99.0d, false, 3, "Смешайте: 1 часть дерновой земли + 2 части торфа + 1 часть перегноя (pH 5,5–6,5) или возьмите готовый рассадный субстрат. За 1–2 дня до посева пролейте Фитоспорином (5 г на 10 л) или пропарьте грунт 30 минут — профилактика чёрной ножки. Свежий навоз не вносить! В таре сделайте дренажные отверстия; культуры, не любящие пересадку, сейте сразу в стаканчики 0,3–0,5 л.", 0.0d, false, fx("seedling_soil", 1.0d), l10("fitosporin", 5.0d));
        add(list, str, Operation.FEED_ROOT, "Подкормки рассады до высадки", m(3, 4), 3, 10.0d, 30.0d, false, 3, "Первая подкормка через 10–14 дней после пикировки: нитроаммофоска 2–3 г на 1 л или гумат 25 мл на 10 л. Вторая — в фазе бутонизации: суперфосфат 5 г + сульфат калия 3 г на 1 л. За 2–3 дня до высадки — Эпин-Экстра по листу (1 мл на 5 л) и начало закаливания. Удобрения вносите только по влажному грунту — не по сухим корням.", 0.0d, false, l10("humate", 25.0d), l10("epin", 1.0d), pp("azofoska", 3.0d).alt());
        } else {
            add(list, str, Operation.PLANT, "Посев " + str3 + " в грунт", iArr, i3, 6.0d, 22.0d, false, 3, "Сейте в прогретую до +8…+10 °C почву по схеме для культуры. Грядку заранее заправьте удобрением для овощей и золой, после посева полейте и накройте агроволокном до всходов.", 0.0d, false, m2("veg_fertilizer", 40.0d), m2("ash", 100.0d), fx("agrofibre_17", 1.0d));
        }
        add(list, str, Operation.FEED_ROOT, "Подкормка " + str3, m(6, DALL), 3, 8.0d, 32.0d, false, 2, "Удобрение для овощей 25–30 г/м² раз в 2 недели или настой золы/травы. В начале роста — азот, с цветением — калий.", 0.0d, false, m2("veg_fertilizer", 30.0d), m2("ash", 150.0d).alt());
        add(list, str, Operation.SPRAY, "Профилактическая обработка " + str3 + " от вредителей и болезней", m(6, DALL, 8), 6, 10.0d, 30.0d, true, 2, "Фитоспорин от болезней каждые 10–14 дней (без ожидания до сбора); от вредителей — Фитоверм (ожидание 2–3 дня). Регулярно осматривайте растения.", 3.0d, false, l10("fitosporin", 10.0d), l10("phytoverm", 20.0d).alt());
        add(list, str, Operation.WATER, "Полив в засуху", m(6, DALL, 8), DALL, 12.0d, 38.0d, false, 2, "Поливайте под корень утром или вечером, 4–8 л/м² по погоде. Мульча сокращает поливы вдвое.", 0.0d, false, m2(Operation.WATER, 5.0d));
        add(list, str, Operation.HARVEST, "Сбор урожая " + str3, iArr2, i4, 5.0d, 30.0d, false, 3, "Убирайте в сухую погоду по мере созревания — регулярный сбор продлевает плодоношение и улучшает качество.", 0.0d, false, new MatRef[0]);
        add(list, str, Operation.SOIL, "Уборка растительных остатков " + str3, m(9, 10), 6, 3.0d, 18.0d, false, 2, "Уберите ботву и корни с грядки (здоровые — в компост), перекопайте или посейте сидераты. Не оставляйте остатки на зиму — в них зимуют болезни и вредители.", 0.0d, false, m2("mustard_siderate", 10.0d).alt());
    }

    private static void greenhouseBlock(List<Rule> list) {
        add(list, "greenhouse", Operation.REPAIR, "Ревизия теплицы и подготовка к сезону", m(2, 3), 6, 0.0d, 15.0d, false, 3, "Проверьте каркас и покрытие: замените порванную плёнку (3×10 м на стандартную теплицу), подтяните крепления, смажьте петли форточек. Промойте покрытие изнутри и снаружи — грязь забирает до 15% света.", 0.0d, false, fx("gh_film", 1.0d));
        add(list, "greenhouse", Operation.SOIL, "Весеннее обеззараживание почвы", m(3, D3), 3, 3.0d, 18.0d, false, 3, "Пролейте грунт горячим розовым раствором марганцовки (3–5 г на 10 л на м²) или Фитоспорином. Сильно заражённую почву замените верхним слоем 10–15 см или внесите свежий торфяной грунт.", 0.0d, false, m2("potassium_permanganate", 5.0d), m2("fitosporin", 10.0d).alt());
        add(list, "greenhouse", Operation.SOIL, "Заправка грунта и посев ранних культур", m(D3), 3, 5.0d, 20.0d, false, 2, "Внесите удобрение для овощей 40–50 г/м² и золу, перекопайте. С начала апреля в прогретую теплицу — редис, салаты, укроп.", 0.0d, false, m2("veg_fertilizer", 45.0d), fx("seeds_veg", 1.0d));
        add(list, "greenhouse", Operation.PROTECT, "Проветривание и защита от перегрева летом", m(6, DALL, 8), DALL, 15.0d, 40.0d, false, 2, "Держите двери и форточки открытыми днём — выше +32 °C пыльца становится стерильной и завязи осыпаются. В жару притеняйте покрытие меловым раствором или белой сеткой.", 0.0d, false, new MatRef[0]);
        add(list, "greenhouse", Operation.WATER, "Полив теплицы по погоде и влажности", m(5, 6, DALL, 8), DALL, 10.0d, 45.0d, false, 3, "Поливайте утром тёплой водой (+20…+25 °C) под корень: томаты — 2–3 л на куст 1–2 раза в неделю, огурцы — 4–6 л/м² почти ежедневно; идеально — капельный полив. Не мочите листья: влага на них провоцирует фитофтору и кладоспориоз. Влажность воздуха: огурцам 75–85%, томатам — ниже 70%; в жару поливайте дорожки и проветривайте. Мульча удерживает влагу в почве.", 0.0d, false, m2(Operation.WATER, 5.0d));
        add(list, "greenhouse", Operation.SPRAY, "Осенняя уборка и обеззараживание теплицы", m(9, 10), DALL, 3.0d, 18.0d, false, 3, "Уберите все растительные остатки и шпагат. Промойте каркас и покрытие. Обработайте серной шашкой (300 г на 5–10 м³, сутки плотно закрытой) против паутинного клеща, белокрылки и грибков — работать в респираторе, затем проветрить 2–3 дня. Почву пролейте марганцовкой или Фитоспорином.", 0.0d, false, fx("sulfur_check", 1.0d), m2("potassium_permanganate", 5.0d), m2("fitosporin", 10.0d).alt());
        add(list, "greenhouse", Operation.SOIL, "Восстановление плодородия почвы теплицы", m(10, 11), DALL, 0.0d, 15.0d, false, 2, "Посейте горчицу как сидерат и заделайте перед морозами, или внесите перегной 1 ведро/м² под перекопку. Раз в 3–4 года меняйте верхний слой грунта.", 0.0d, false, m2("mustard_siderate", 15.0d), m2("manure", 10.0d).alt());
        add(list, "greenhouse", Operation.PROTECT, "Зимнее обслуживание теплицы", m(12, 1, 2), DALL, -25.0d, 5.0d, false, 2, "Сбрасывайте мокрый снег с покрытия сразу, иначе дуги сложатся. Набрасывайте снег внутрь — весной это влага и защита почвы от промерзания. Проверьте целостность плёнки после метелей.", 0.0d, false, new MatRef[0]);
    }
}
