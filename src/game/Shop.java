package game;

import model.characters.Character;
import model.exceptions.InvalidActionException;
import model.items.Item;
import model.items.Weapon;
import model.items.Armor;
import model.items.Potion;
import model.exceptions.InventoryFullException;
import model.exceptions.ItemNotFoundException;
import model.exceptions.InsufficientGoldException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * מחלקה המייצגת חנות במשחק.
 * מאפשרת קנייה ומכירה של פריטים.
 */
public class Shop {

    private String name;
    private ArrayList<Item> inventory;
    private HashMap<String, Integer> stock; // מיפוי שם פריט לכמות במלאי

    public Shop(String name) {
        this.name = name;
        this.inventory = new ArrayList<>();
        this.stock = new HashMap<>();
    }

    // ============================================================
    //  ניהול מלאי החנות
    // ============================================================

    /**
     * מוסיף פריט לחנות עם כמות מסוימת.
     *
     * @param item הפריט להוספה
     * @param quantity הכמות
     */
    public void addItemToShop(Item item, int quantity) {
        // 1. הוסף את הפריט ל-inventory (אם לא קיים)
        if (!inventory.contains(item))
        {
            inventory.add(item);
        }
        // 2. עדכן את הכמות ב-stock (הוסף לכמות הקיימת או צור חדש)
        if(!stock.containsKey(item.getName()))
        {
            stock.put(item.getName(),quantity);
        } else{
            stock.put((item.getName()), stock.get(item.getName()) + quantity);
        }
    }

    /**
     * מחזיר רשימה של כל הפריטים הזמינים (שיש מהם במלאי).
     *
     * @return רשימת פריטים זמינים
     */
    public ArrayList<Item> getAvailableItems() {
       ArrayList<Item> result = new ArrayList<>();

        for (Item item : inventory) {
            if (stock.get(item.getName()) > 0 )
            {
                result.add(item);
            }
        }
       return result;
    }

    /**
     * מחזיר פריטים לפי קטגוריה (Weapon, Armor, Potion).
     * השתמש ב-instanceof.
     *
     * @param category שם הקטגוריה ("weapon", "armor", "potion")
     * @return רשימת פריטים מהקטגוריה
     */
    public ArrayList<Item> getItemsByCategory(String category) {
        ArrayList<Item> result = new ArrayList<>();
        category = category.toLowerCase();
        for (Item item : inventory) {
            if (    category.equals("weapon") && item instanceof Weapon ||
                    category.equals("potion") && item instanceof Potion ||
                    category.equals("armor") && item instanceof Armor) {

                result.add(item);
            }
        }

        return result;
    }

    // ============================================================
    //  קנייה ומכירה
    // ============================================================

    /**
     *  מימוש buyItem
     * השחקן קונה פריט מהחנות.
     *
     * @param customer השחקן הקונה
     * @param itemName שם הפריט לקנייה
     * @return הפריט שנקנה
     * @throws ItemNotFoundException אם הפריט לא קיים בחנות
     * @throws InsufficientGoldException אם אין מספיק זהב
     * @throws InventoryFullException אם המלאי של השחקן מלא
     */
    public Item buyItem(Character customer, String itemName)
            throws ItemNotFoundException, InsufficientGoldException,
            InventoryFullException {
        Item itemToBuy = null;
        // 1. חפש את הפריט ב-inventory לפי שם
        for (Item item : inventory) {
            if(item.getName().equals(itemName))
            {
                itemToBuy = item;
                break;
            }
        }
        // 2. בדוק שיש מלאי (stock > 0)
        if (itemToBuy == null || getItemStock(itemName) <= 0) {
            throw new ItemNotFoundException(itemName);
        }
        // 3. בדוק שיש לשחקן מספיק זהב
        if (customer.getGold() < itemToBuy.getBuyPrice()) {
            throw new InsufficientGoldException(customer.getGold(),itemToBuy.getBuyPrice());
        }
        // 4. בדוק שיש מקום במלאי של השחקן
        if (customer.getInventory().size() >= customer.getMaxInventorySize())
        {
            throw new InventoryFullException(itemName, customer.getMaxInventorySize());
        }
        // 5. בצע את העסקה: הורד זהב, הוסף פריט לשחקן, הפחת מלאי
        customer.spendGold(itemToBuy.getBuyPrice());
        customer.addItem(itemToBuy);
        stock.put(itemName, stock.get(itemName) - 1);
        return itemToBuy;
    }

    /**
     * השחקן מוכר פריט לחנות.
     *
     * @param seller השחקן המוכר
     * @param itemName שם הפריט למכירה
     * @return כמות הזהב שהתקבלה
     * @throws ItemNotFoundException אם הפריט לא נמצא במלאי השחקן
     * @throws InvalidActionException אם הפריט לא ניתן למכירה
     */
    public int sellItem(Character seller, String itemName)
            throws ItemNotFoundException, InvalidActionException {
        Item itemToSell = null;
        // 1. חפש את הפריט במלאי השחקן
        for (Item item: seller.getInventory()) {
            if(item.equals(itemName))
            {
                itemToSell = item;
                break;
            }
        }
        if (itemToSell == null)
        {
            throw new ItemNotFoundException(itemName);
        }
        // 2. בדוק שהפריט ניתן למכירה (isSellable)
        if (!itemToSell.isSellable())
        {
            throw new InvalidActionException("sellItem" , "item isnt sellable");
        }
        // 3. הסר מהשחקן והוסף לחנות
        seller.removeItem(itemName);
        // 4. תן לשחקן את הזהב (getSellPrice)
        seller.addGold(itemToSell.getSellPrice());
        return itemToSell.getSellPrice();
    }

    /**
     * מחזיר את כמות המלאי של פריט מסוים.
     *
     * @param itemName שם הפריט
     * @return הכמות במלאי, או 0 אם לא קיים
     */
    public int getItemStock(String itemName) {
        if (!stock.containsKey(itemName)){
            return 0;
        }
        return stock.get(itemName);
    }

    /**
     * מחשב את הערך הכולל של כל הפריטים בחנות.
     *
     * @return הערך הכול
     */
    public int getTotalValue() {
        // סכום של (מחיר קנייה * כמות) לכל פריט
        int total = 0;
        for (Item item : inventory) {
            int quantity = getItemStock(item.getName());
            total += (item.getBuyPrice() * quantity);
        }
        return total;
    }

    // ============================================================
    //  דוחות (שימוש ב-HashMap)
    // ============================================================

    /**
     *  מימוש getInventoryReport
     * מחזיר HashMap עם דוח מלאי: שם פריט -> מידע (מחיר וכמות).
     *
     * @return HashMap של (String -> String) בפורמט "Price: X, Stock: Y"
     */
    public HashMap<String, String> getInventoryReport() {

        HashMap<String, String> report = new HashMap<>();

        for (Item item : inventory) {
            String name = item.getName();
            int quantity = getItemStock(item.getName());
            report.put(name, "Price: " + item.getBuyPrice() + ", Stock: " + quantity);
        }
        return report;
    }

    /**
     * משווה מחירים בין קנייה למכירה.
     *
     * @return HashMap של (String -> int[]) כאשר [0]=buyPrice, [1]=sellPrice
     */
    public HashMap<String, int[]> getPriceComparison() {
        HashMap<String, int[]> result = new HashMap<>();

        for (Item item : inventory) {
            result.put(item.getName(),
                    new int[]{item.getBuyPrice(), item.getSellPrice()});
        }
        return result;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getUniqueItemCount() {
        return inventory.size();
    }

    public int getTotalItemCount() {
        int total = 0;
        for (int count : stock.values()) {
            total += count;
        }
        return total;
    }

    @Override
    public String toString() {
        return String.format("Shop: %s | Items: %d unique, %d total",
                name, getUniqueItemCount(), getTotalItemCount());
    }
}