package by.csl.gardener;

import java.util.ArrayList;
import java.util.List;

public class Material {
    public final String id;
    public final String name;
    public final String note;
    public final String pack;
    public final double packQty;
    public final double price;
    public final String unit;

    public Material(String str, String str2, String str3, double d, String str4, double d2, String str5) {
        this.id = str;
        this.name = str2;
        this.pack = str3;
        this.packQty = d;
        this.unit = str4;
        this.price = d2;
        this.note = str5;
    }

    public static List<Material> all() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new Material("urea", "Карбамид (мочевина), 46% N", "1 кг", 1000.0d, "г", 3.2d, "Беларусь/Россия, «Гродно Азот», фермерская фасовка"));
        arrayList.add(new Material("ammonium_nitrate", "Аммиачная селитра, 34% N", "1 кг", 1000.0d, "г", 2.8d, "«Гродно Азот»"));
        arrayList.add(new Material("azofoska", "Азофоска (NPK 16:16:16)", "1 кг", 1000.0d, "г", 3.9d, "Универсальное весеннее удобрение"));
        arrayList.add(new Material("superphosphate", "Суперфосфат аммонизированный 9:30:9(S)", "1 кг", 1000.0d, "г", 4.5d, "Осеннее фосфорное удобрение"));
        arrayList.add(new Material("potassium_sulfate", "Сульфат калия (сернокислый калий)", "1 кг", 1000.0d, "г", 5.4d, "Осенняя подкормка, повышает зимостойкость"));
        arrayList.add(new Material("kalmag", "Калимагнезия", "1 кг", 1000.0d, "г", 3.6d, "Калий + магний, для песчаных почв"));
        arrayList.add(new Material("ammonium_sulfate", "Сульфат аммония", "1 кг", 1000.0d, "г", 3.1d, "Подкисляющее азотное удобрение (голубика, рододендрон)"));
        arrayList.add(new Material("dolomite", "Доломитовая мука (раскислитель)", "3 кг", 3000.0d, "г", 4.2d, "Норма 300–500 г/м²"));
        arrayList.add(new Material("ash", "Зола древесная", "1 кг", 1000.0d, "г", 1.2d, "Калий + микроэлементы, раскисляет почву"));
        arrayList.add(new Material("iron_sulfate", "Железный купорос (сульфат железа)", "500 г", 500.0d, "г", 3.0d, "Искореняющая обработка, лечение хлороза"));
        arrayList.add(new Material("copper_sulfate", "Медный купорос", "300 г", 300.0d, "г", 5.0d, "Искореняющая обработка, входит в бордоскую смесь"));
        arrayList.add(new Material("manure", "Перегной / компост", "10 л", 10.0d, "л", 4.5d, "Мульча и удобрение приствольных кругов"));
        arrayList.add(new Material("chicken_manure", "Куриный помёт гранулированный", "1 кг", 1000.0d, "г", 5.5d, "Настой 1:20, под корень"));
        arrayList.add(new Material("peat", "Торф верховой (кислый) для мульчи", "50 л", 50.0d, "л", 12.0d, "Для голубики, рододендронов"));
        arrayList.add(new Material("bark", "Кора сосновая, фракция 20–40 мм", "50 л", 50.0d, "л", 14.0d, "Мульча, слой 5–7 см"));
        arrayList.add(new Material("azofos", "Азофос, марка М (суспензия)", "580 г", 580.0d, "г", 10.32d, "Ранневесеннее искореняющее опрыскивание, «Гродно Азот»"));
        arrayList.add(new Material("bordeaux", "Бордоская смесь (медный купорос + известь)", "200 г", 200.0d, "г", 5.4d, "1% и 3% бордоская жидкость"));
        arrayList.add(new Material("bordeaux_liquid", "Бордоская жидкость готовая", "500 мл", 500.0d, "мл", 7.4d, "«Август», готовый раствор"));
        arrayList.add(new Material("horus", "Хорус, ВДГ (ципродинил)", "3 г", 3.0d, "г", 6.14d, "Монилиоз, парша, коккомикоз; работает от +3 °C"));
        arrayList.add(new Material("skor", "Скор, КЭ (дифеноконазол)", "2 мл", 2.0d, "мл", 6.9d, "Парша, мучнистая роса, клястероспориоз"));
        arrayList.add(new Material("rayok", "Раёк, КЭ (дифеноконазол)", "10 мл", 10.0d, "мл", 5.8d, "Аналог «Скор», «Август»"));
        arrayList.add(new Material("topaz", "Топаз, КЭ (пенконазол)", "4 мл", 4.0d, "мл", 6.15d, "Мучнистая роса, ржавчина, американская мучнистая роса крыжовника"));
        arrayList.add(new Material("fitosporin", "Фитоспорин-М, паста (био)", "200 г", 200.0d, "г", 5.15d, "Биопрепарат, можно в период вегетации и по плодам"));
        arrayList.add(new Material("phytoverm", "Фитоверм, КЭ (био)", "10 мл", 10.0d, "мл", 4.6d, "Клещи, тля, трипсы; ожидание 2–3 дня"));
        arrayList.add(new Material("bitoxibacillin", "Битоксибациллин (био)", "20 г", 20.0d, "г", 3.8d, "Гусеницы, листогрызущие; +18 °C и выше"));
        arrayList.add(new Material("lepidocide", "Лепидоцид (био)", "20 г", 20.0d, "г", 3.5d, "Плодожорка, листовертка, пяденица"));
        arrayList.add(new Material("aktara", "Актара, ВДГ (тиаметоксам)", "4 г", 4.0d, "г", 7.53d, "Системный инсектицид от тли, долгоносика, щитовки"));
        arrayList.add(new Material("alatar", "Алатар, КЭ", "25 мл", 25.0d, "мл", 6.68d, "Комплекс вредителей сада"));
        arrayList.add(new Material("aliot", "Алиот, КЭ", "10 мл", 10.0d, "мл", 7.2d, "Инсектоакарицид, яблонная плодожорка и клещи"));
        arrayList.add(new Material("biotlin", "Биотлин, ВРК", "3 мл", 3.0d, "мл", 2.1d, "Тля на плодовых и ягодных"));
        arrayList.add(new Material("sulfur", "Сера коллоидная (Тиовит Джет)", "30 г", 30.0d, "г", 3.3d, "Мучнистая роса, клещ; работать при +20…+35 °C"));
        arrayList.add(new Material("tornado", "Гербицид Торнадо, ВР", "100 мл", 100.0d, "мл", 12.16d, "Приствольные круги, дорожки; не попадать на листву"));
        arrayList.add(new Material("garden_tar", "Садовый вар Dr. Garden", "200 г", 200.0d, "г", 11.08d, "Замазка срезов и ран"));
        arrayList.add(new Material("zsp", "Замазка садовая противораковая ЗСП", "160 г", 160.0d, "г", 6.97d, "«МинскСортСемОвощ», лечение ран и морозобоин"));
        arrayList.add(new Material("whitewash", "Побелка садовая лечебная", "2 кг", 2.0d, "кг", 6.3d, "Защита штамбов от солнечных ожогов и морозобоин"));
        arrayList.add(new Material("glue_belt", "Клеевой ловчий пояс", "1 шт", 1.0d, "шт", 4.5d, "Ловля долгоносика, муравьёв, гусениц"));
        arrayList.add(new Material("pheromone", "Феромонная ловушка (яблонная плодожорка)", "1 шт", 1.0d, "шт", 6.0d, "Мониторинг лёта бабочек"));
        arrayList.add(new Material("spunbond", "Укрывной материал спанбонд-60", "3,2×5 м", 1.0d, "шт", 15.0d, "Зимнее укрытие роз, винограда, молодых саженцев"));
        arrayList.add(new Material("lapnik", "Лапник / сетка от грызунов", "1 шт", 1.0d, "шт", 5.0d, "Защита штамбов от мышей и зайцев"));
        arrayList.add(new Material("humate", "Гумат калия жидкий", "0,5 л", 500.0d, "мл", 6.5d, "Стимулятор корнеобразования, замачивание"));
        arrayList.add(new Material("epin", "Эпин-Экстра", "1 мл", 1.0d, "мл", 2.4d, "Антистресс после заморозков и пересадки"));
        arrayList.add(new Material("blueberry_mix", "Удобрение для голубики (кислая реакция)", "1 кг", 1000.0d, "г", 9.8d, "Комплекс для вересковых"));
        arrayList.add(new Material("rose_mix", "Удобрение для роз и цветов", "1 кг", 1000.0d, "г", 8.4d, "Комплекс NPK + Mg"));
        arrayList.add(new Material("lawn_spring", "Удобрение для газона весеннее (азотное)", "3 кг", 3.0d, "кг", 16.5d, "N 20–25%"));
        arrayList.add(new Material("lawn_autumn", "Удобрение для газона осеннее (PK)", "3 кг", 3.0d, "кг", 17.9d, "Повышает зимостойкость трав"));
        arrayList.add(new Material(Operation.WATER, "Вода для рабочего раствора", "10 л", 10.0d, "л", 0.0d, "Отстоянная, +15…+20 °C"));
        arrayList.add(new Material("seedling_soil", "Грунт питательный торфяной для рассады (pH 5,5–6,5)", "55 л", 55.0d, "л", 13.95d, "«Двина», ОАО «Торфопредприятие Глинка», Беларусь"));
        arrayList.add(new Material("peat_tablets", "Торфяные таблетки 41 мм", "10 шт", 10.0d, "шт", 9.95d, "Jiffy-7 и аналоги, 0,85–1,00 руб/шт"));
        arrayList.add(new Material("cassette", "Кассета для рассады с поддоном", "24 ячейки", 1.0d, "шт", 5.7d, "Многоразовая, для томата, перца, огурца"));
        arrayList.add(new Material("phytolamp", "Фитолампа светодиодная, E27", "15 Вт", 1.0d, "шт", 26.2d, "Досветка рассады 12–14 ч в сутки, «МинскСортСемОвощ»"));
        arrayList.add(new Material("agrofibre_17", "Агроволокно (спанбонд-17) для укрытия грядок", "3,2×10 м", 1.0d, "шт", 12.5d, "Защита от заморозков до −3 °C и крестоцветной блошки"));
        arrayList.add(new Material("agrofibre_40", "Агроволокно (спанбонд-40) для дуг", "3,2×10 м", 1.0d, "шт", 18.0d, "Укрытие теплолюбивых культур в открытом грунте"));
        arrayList.add(new Material("gh_film", "Плёнка полиэтиленовая для теплицы", "3×10 м, 120 мкм", 1.0d, "шт", 45.0d, "Светостабилизированная, 3 сезона"));
        arrayList.add(new Material("seeds_veg", "Семена овощных культур", "1 пакет", 1.0d, "шт", 1.5d, "Средняя цена пакета, «МинскСортСемОвощ», «Гавриш», «Поиск»"));
        arrayList.add(new Material("veg_fertilizer", "Удобрение для овощных культур (NPK + Mg)", "1 кг", 1000.0d, "г", 7.5d, "Комплекс для томата, огурца, капусты"));
        arrayList.add(new Material("potato_fertilizer", "Удобрение для картофеля", "2,5 кг", 2.5d, "кг", 12.0d, "Без хлора, с магнием"));
        arrayList.add(new Material("hom", "ХОМ (хлорокись меди)", "40 г", 40.0d, "г", 2.9d, "Фитофтороз, пероноспороз, бактериоз; 40 г на 10 л"));
        arrayList.add(new Material("previkur", "Превикур Энерджи (пропамокарб + фосэтил)", "25 мл", 25.0d, "мл", 14.5d, "Корневые гнили, пероноспороз; пролив рассады"));
        arrayList.add(new Material("confidor", "Конфидор Экстра (имидаклоприд)", "1 г", 1.0d, "г", 2.8d, "Колорадский жук, тля, тепличная белокрылка"));
        arrayList.add(new Material("actophyt", "Актофит (биоинсектицид)", "40 мл", 40.0d, "мл", 9.5d, "Клещ, тля, трипс, колорадский жук; ожидание 2 дня"));
        arrayList.add(new Material("fitosporin_tomato", "Фитоспорин-М «Томаты» (био)", "100 г", 100.0d, "г", 3.6d, "Профилактика фитофтороза, можно по плодам"));
        arrayList.add(new Material("lime", "Известь гашёная (пушонка)", "2 кг", 2.0d, "кг", 4.5d, "Побелка конструкций теплицы, раскисление почвы"));
        arrayList.add(new Material("potassium_permanganate", "Калия перманганат (марганцовка)", "10 г", 10.0d, "г", 1.8d, "Обеззараживание грунта, тары и семян"));
        arrayList.add(new Material("sulfur_check", "Шашка серная для теплиц и погребов", "300 г", 300.0d, "г", 8.5d, "1 шашка на 5–10 м³, окуривание после уборки"));
        arrayList.add(new Material("metaldehyde", "Гроза (метальдегид) от слизней", "30 г", 30.0d, "г", 4.2d, "3 г/м², гранулы по поверхности почвы"));
        arrayList.add(new Material("mustard_siderate", "Горчица белая (сидерат), семена", "500 г", 500.0d, "г", 6.5d, "Норма 150–200 г на сотку, посев после уборки"));
        arrayList.add(new Material("ash_husk", "Зола + луковая шелуха (народное средство)", "1 кг", 1000.0d, "г", 0.8d, "Опудривание от блошки и тли, настой для подкормки"));
        arrayList.add(new Material("trellis", "Шпагат / опора для подвязки растений", "100 м", 100.0d, "м", 6.0d, "Полипропиленовый шпагат для шпалеры в теплице"));
        return arrayList;
    }

    public static Material byId(String str) {
        for (Material material : all()) {
            if (material.id.equals(str)) {
                return material;
            }
        }
        return null;
    }
}
