package utils;

import model.characters.Character;
import model.items.Item;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * מחלקת עזר עם פונקציות שימושיות למשחק.
 * כאן נתרגל שימוש במחלקות אנונימיות ו-Comparator.
 */
public class GameUtils {

    // ============================================================
    //  מיון פריטים (שימוש במחלקות אנונימיות)
    // ============================================================

    /**
     *  מימוש sortItemsByPrice
     * ממיין רשימת פריטים לפי מחיר (מהזול ליקר).
     * השתמש ב-Comparator כמחלקה אנונימית!
     *
     * @param items רשימת הפריטים למיון
     */
    public static void sortItemsByPrice(ArrayList<Item> items) {
        items.sort(new Comparator<Item>() {
            @Override
            public int compare(Item a, Item b) {
                return a.getBuyPrice() - b.getBuyPrice();
            }
        });
    }

    /**
     * ממיין רשימת פריטים לפי מחיר (מהיקר לזול).
     * השתמש ב-Comparator כמחלקה אנונימית!
     *
     * @param items רשימת הפריטים למיון
     */
    public static void sortItemsByPriceDescending(ArrayList<Item> items) {
        items.sort(new Comparator<Item>() {
            @Override
            public int compare(Item a, Item b) {
                return b.getBuyPrice() - a.getBuyPrice();
            }
        });
    }

    /**
     * ממיין רשימת פריטים לפי נדירות (מהנפוץ לנדיר ביותר).
     * סדר הנדירות: COMMON < UNCOMMON < RARE < EPIC < LEGENDARY
     * השתמש ב-ordinal() של ה-enum!
     *
     * @param items רשימת הפריטים למיון
     */
    public static void sortItemsByRarity(ArrayList<Item> items) {
        items.sort(new Comparator<Item>() {
            @Override
            public int compare(Item a, Item b) {
                return a.getRarity().ordinal() - b.getRarity().ordinal();
            }
        });
    }

    /**
     * ממיין רשימת פריטים לפי שם (אלפביתי).
     *
     * @param items רשימת הפריטים למיון
     */
    public static void sortItemsByName(ArrayList<Item> items) {

        items.sort(new Comparator<Item>() {
            @Override
            public int compare(Item a, Item b) {
                return a.getName().compareTo(b.getName());
            }
        });
    }

    /**
     * ממיין רשימת פריטים לפי משקל (מהקל לכבד).
     *
     * @param items רשימת הפריטים למיון
     */
    public static void sortItemsByWeight(ArrayList<Item> items) {
        items.sort(new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.getWeight() - o2.getWeight();
            }
        });
    }

    // ============================================================
    //  מיון דמויות
    // ============================================================

    /**
     * ממיין רשימת דמויות לפי בריאות נוכחית (מהנמוך לגבוה).
     *
     * @param characters רשימת הדמויות למיון
     */
    public static void sortCharactersByHealth(ArrayList<Character> characters) {

       characters.sort(new Comparator<Character>() {
           @Override
           public int compare(Character o1, Character o2) {
               return o1.getCurrentHealth() - o2.getCurrentHealth();
           }
       });
    }

    /**
     * ממיין רשימת דמויות לפי רמה (מהגבוה לנמוך).
     *
     * @param characters רשימת הדמויות למיון
     */
    public static void sortCharactersByLevel(ArrayList<Character> characters) {
       characters.sort(new Comparator<Character>() {
           @Override
           public int compare(Character o1, Character o2) {
               return o1.getLevel() - o2.getLevel();
           }
       });
    }

    // ============================================================
    //  סינון (Filtering)
    // ============================================================

    /**
     * ממשק פונקציונלי לסינון פריטים.
     */
    public interface ItemFilter {
        boolean accept(Item item);
    }

    /**
     * מסנן רשימת פריטים לפי תנאי מסוים.
     *
     * @param items רשימת הפריטים
     * @param filter הפילטר
     * @return רשימה חדשה עם הפריטים שעברו את הסינון
     */
    public static ArrayList<Item> filterItems(ArrayList<Item> items, ItemFilter filter) {
        ArrayList<Item> result = new ArrayList<>();

        for (Item item : items) {
            if (filter.accept(item)) {
                result.add(item);
            }
        }

        return  result;
    }

    /**
     * מסנן פריטים שהשחקן יכול לקנות.
     * השתמש ב-filterItems עם מחלקה אנונימית!
     *
     * @param items רשימת הפריטים
     * @param playerGold כמות הזהב של השחקן
     * @return רשימת פריטים שניתן לקנות
     */
    public static ArrayList<Item> filterAffordableItems(ArrayList<Item> items, int playerGold) {
        return filterItems(items, new ItemFilter() {
            @Override
            public boolean accept(Item item) {
                return item.getBuyPrice() <= playerGold;
            }
        });

    }

    /**
     * מסנן פריטים לפי נדירות מינימלית.
     *
     * @param items רשימת הפריטים
     * @param minRarity הנדירות המינימלית
     * @return רשימת פריטים בנדירות המבוקשת או יותר
     */
    public static ArrayList<Item> filterByRarity(ArrayList<Item> items,
                                                 Item.ItemRarity minRarity) {
        return filterItems(items, new ItemFilter() {
            @Override
            public boolean accept(Item item) {
                return item.getRarity().ordinal() >= minRarity.ordinal();
            }
        });
    }

    /**
     * מסנן פריטים קלים (עד משקל מסוים).
     *
     * @param items רשימת הפריטים
     * @param maxWeight המשקל המקסימלי
     * @return רשימת פריטים קלים
     */
    public static ArrayList<Item> filterLightItems(ArrayList<Item> items, int maxWeight) {
        return filterItems(items, new ItemFilter() {
            @Override
            public boolean accept(Item item) {
                return item.getWeight() <= maxWeight;
            }
        });
    }

    // ============================================================
    //  פונקציות עזר נוספות
    // ============================================================

    /**
     * מוצא את הפריט ה"טוב ביותר" לפי קריטריון מסוים.
     *
     * @param items רשימת הפריטים
     * @param comparator הקריטריון להשוואה
     * @return הפריט הטוב ביותר, או null אם הרשימה ריקה
     */
    public static Item findBestItem(ArrayList<Item> items, Comparator<Item> comparator) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        Item best = items.get(0);
        for (Item item : items) {
            if (comparator.compare(item, best) > 0) {
                best = item;
            }
        }
        return best;
    }

    /**
     * מחשב את המשקל הכולל של כל הפריטים ברשימה.
     *
     * @param items רשימת הפריטים
     * @return המשקל הכולל
     */
    public static int calculateTotalWeight(ArrayList<Item> items) {
       int calculatedWeight = 0;

        for (Item item : items) {
            calculatedWeight += item.getWeight();
        }
       return calculatedWeight;
    }

    /**
     * מחשב את הערך הכולל של כל הפריטים (לפי מחיר מכירה).
     *
     * @param items רשימת הפריטים
     * @return הערך הכולל
     */
    public static int calculateTotalValue(ArrayList<Item> items) {
       int totalCost = 0;

        for (Item item : items) {
            totalCost += item.getBuyPrice();
        }

       return totalCost;
    }
}