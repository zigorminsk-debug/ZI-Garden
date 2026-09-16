package by.csl.gardener;

import java.util.ArrayList;
import java.util.List;

public class Task {
    public int day;
    public boolean done;
    public String id;
    public double maxTemp;
    public double minTemp;
    public int month;
    public String op;
    public String plantId;
    public String plantName;
    public int priority;
    public boolean rainBlocks;
    public double solutionL;
    public String text;
    public String title;
    public String window;
    public int year;
    public List<Item> items = new ArrayList();
    public int weatherState = 0;
    public String weatherNote = "";
    public int suggestDayOffset = -1;
    public String currency = "BYN";

    public static class Item {
        public boolean alternative;
        public String currency = "BYN";
        public String dose;
        public String name;
        public String need;
        public String pack;
        public int packs;
        public double price;
    }

    public double totalCost() {
        double d = 0.0d;
        for (Item item : this.items) {
            if (!item.alternative) {
                d += item.price * item.packs;
            }
        }
        return d;
    }
}
