package model.characters;

import model.items.Weapon;
import model.items.Armor;
import model.items.Item;
import model.exceptions.InventoryFullException;
import model.exceptions.ItemNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * מחלקה אבסטרקטית המייצגת דמות במשחק.
 * מממשת את ממשק Attackable.
 * כל סוגי הדמויות (Warrior, Mage, Archer) יורשים ממחלקה זו.
 */
public abstract class Character implements Attackable {

    // Basic stats
    protected String name;
    protected int level;
    protected int experience;
    protected int gold;

    // Health & Mana
    protected int currentHealth;
    protected int maxHealth;
    protected int currentMana;
    protected int maxMana;

    // Combat stats
    protected int baseStrength;
    protected int baseDefense;

    // Equipment - HashMap מ-slot לשריון
    protected HashMap<Armor.ArmorSlot, Armor> equippedArmor;
    protected Weapon equippedWeapon;
    // Inventory - ArrayList של פריטים + Stack לפריטים אחרונים שהשתמשנו בהם
    protected ArrayList<Item> inventory;
    protected Stack<Item> recentlyUsedItems;
    protected final int maxInventorySize;

    // Constants
    protected static final int EXPERIENCE_PER_LEVEL = 100;
    protected static final int DEFAULT_INVENTORY_SIZE = 20;

    public Character(String name, int maxHealth, int maxMana,
                     int baseStrength, int baseDefense) {
        this.name = name;
        this.level = 1;
        this.experience = 0;
        this.gold = 0;

        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.maxMana = maxMana;
        this.currentMana = maxMana;

        this.baseStrength = baseStrength;
        this.baseDefense = baseDefense;

        this.equippedArmor = new HashMap<>();
        this.equippedWeapon = null;

        this.inventory = new ArrayList<>();
        this.recentlyUsedItems = new Stack<>();
        this.maxInventorySize = DEFAULT_INVENTORY_SIZE;
    }

    // ============================================================
    // מימוש ממשק Attackable
    // ============================================================

    /**
     * מקבל נזק והופך אותו לנזק בפועל אחרי הפחתת שריון.
     * - חשב את סך הפחתת הנזק מכל חלקי השריון המצוידים
     * - הפחת את הנזק המופחת מ-currentHealth
     * - currentHealth לא יכול לרדת מתחת ל-0
     */
    @Override
    public void takeDamage(int damage) {

        int reduceDamage = damage;
        for (Armor armor : equippedArmor.values()) {
            reduceDamage = armor.reduceDamage(reduceDamage);

            //start: 100
            //אחרי Chest: 100 * 0.7 = 70
            //אחרי Boots: 70 * 0.9 = 63
        }
        currentHealth = currentHealth - damage;
        if(currentHealth <= 0){
            currentHealth = 0;
        }

    }

    /**
     * @return true אם currentHealth > 0
     */
    @Override
    public boolean isAlive() {
        return currentHealth > 0;
    }

    @Override
    public int getCurrentHealth() {
        return currentHealth;
    }

    @Override
    public int getMaxHealth() {
        return maxHealth;
    }

    // ============================================================
    // ניהול מלאי (Inventory Management)
    // ============================================================

    /**
     * מוסיף פריט למלאי.
     *
     * @param item הפריט להוספה
     * @throws InventoryFullException אם המלאי מלא
     */
    public void addItem(Item item) throws InventoryFullException {
        // אם המלאי מלא, זרוק InventoryFullException
        // אחרת, הוסף את הפריט ל-inventory

        if (inventory.size() >= maxInventorySize) {
            throw new InventoryFullException(item.getName(),maxInventorySize);
        }
        inventory.add(item);

    }

    /**
     * מסיר פריט מהמלאי לפי שם.
     *
     * @param itemName שם הפריט להסרה
     * @return הפריט שהוסר
     * @throws ItemNotFoundException אם הפריט לא נמצא
     */
    public Item removeItem(String itemName) throws ItemNotFoundException {
        // חפש את הפריט לפי שם
        // אם לא נמצא, זרוק ItemNotFoundException
        // אם נמצא, הסר מהמלאי והחזר אותו
        for (Item  item : inventory) {
            if(item.getName() == itemName)
            {
                inventory.remove(item);
                return item;
            }
        }
        throw new ItemNotFoundException(itemName);
    }

    /**
     * מחזיר רשימה של כל הפריטים מסוג מסוים במלאי.
     * השתמש ב-instanceof לבדיקת הסוג.
     *
     * @param itemClass המחלקה לחיפוש (לדוגמה: Weapon.class)
     * @return רשימה של פריטים מהסוג המבוקש
     */
    public <T extends Item> ArrayList<T> findItemsByType(Class<T> itemClass) {
        // עבור על כל הפריטים במלאי
        // אם הפריט הוא מהסוג המבוקש, הוסף אותו לרשימת התוצאות
        ArrayList<T> result = new ArrayList<>();
        for (Item item: inventory) {
            if(itemClass.isInstance(item))
            {
                result.add(itemClass.cast(item));
            }
        }
        return result;
    }

    /**
     * מחזיר HashMap שממפה רמת נדירות לרשימת פריטים.
     *
     * @return HashMap של (ItemRarity -> ArrayList של Items)
     */
    public HashMap<Item.ItemRarity, ArrayList<Item>> getItemsByRarity() {
        // צור HashMap חדש
        // עבור על כל הפריטים במלאי
        // הוסף כל פריט לרשימה המתאימה לפי הנדירות שלו
        HashMap<Item.ItemRarity,ArrayList<Item>> map = new HashMap<>();
        for (Item item: inventory) {
            if (!map.containsKey(item.getRarity())) {
                map.put(item.getRarity(), new ArrayList<>());
            }
            map.get(item.getRarity()).add(item);
        }
        return map;
    }

    // ============================================================
    // ציוד (Equipment)
    // ============================================================

    /**
     * מצייד נשק. אם כבר יש נשק מצויד, מחזיר אותו למלאי.
     * @param weapon הנשק לציוד
     * @throws ItemNotFoundException אם הנשק לא נמצא במלאי
     * @throws InventoryFullException אם אי אפשר להחזיר את הנשק הישן למלאי
     */
    public void equipWeapon(Weapon weapon) throws ItemNotFoundException, InventoryFullException {
        // 1. בדוק שהנשק קיים במלאי
        if (!inventory.contains(weapon)) {
            throw new ItemNotFoundException(weapon.getName());
        }
        // 2. אם יש נשק מצויד, החזר אותו למלאי
        if (equippedWeapon != null) {
            addItem(equippedWeapon);
        }
        // 3. הסר את הנשק החדש מהמלאי
        inventory.remove(weapon);
        // 4. ציית את הנשק החדש
        equippedWeapon = weapon;
    }

    /**
     * מציית שריון. אם כבר יש שריון באותו slot, מחזיר אותו למלאי.
     *
     * @param armor השריון לציוד
     * @throws ItemNotFoundException אם השריון לא נמצא במלאי
     * @throws InventoryFullException אם אי אפשר להחזיר את השריון הישן למלאי
     */
    public void equipArmor(Armor armor) throws ItemNotFoundException, InventoryFullException {
        // 1. בדוק שהשריון קיים במלאי
        if(!inventory.contains(armor)){
            throw new ItemNotFoundException(armor.getName());
        }

        // 2. אם יש שריון ב-slot הזה, החזר אותו למלאי
        if(equippedArmor.containsKey(armor.getSlot())){
            addItem(equippedArmor.get(armor.getSlot()));
        }
        // 3. הסר את השריון החדש מהמלאי
        inventory.remove(armor);
        // 4. ציית את השריון ב-HashMap לפי ה-slot שלו
        equippedArmor.put(armor.getSlot(),armor);

    }

    /**
     * מחשב את סך ההגנה מכל חלקי השריון.
     *
     * @return סך ההגנה
     */
    public int getTotalDefense() {
        // חבר את ה-defense מכל השריונים ב-equippedArmor
        // הוסף את baseDefense
        int total = baseDefense;
        for (Armor armor: equippedArmor.values()) {
            total += armor.getDefense();
        }
        return total;
    }

    // ============================================================
    //  ריפוי ומאנה
    // ============================================================

    /**
     * מרפא את הדמות בכמות מסוימת.
     * currentHealth לא יכול לעלות מעל maxHealth.
     *
     * @param amount כמות הריפוי
     */
    public void heal(int amount) {
        currentHealth += amount;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
    }

    /**
     * משחזר מאנה לדמות.
     * currentMana לא יכול לעלות מעל maxMana.
     *
     * @param amount כמות המאנה לשחזור
     */
    public void restoreMana(int amount) {
        currentMana += amount;
        if (currentMana > maxMana) {
            currentMana = maxMana;
        }
    }

    /**
     * משתמש במאנה לכישוף.
     * @param amount כמות המאנה לשימוש
     * @return true אם היה מספיק מאנה והשימוש הצליח
     */
    public boolean useMana(int amount) {
        if (currentMana >= amount) {
            currentMana -= amount;
            return true;
        }
        return false;
    }

    // ============================================================
    // ניסיון ורמות (Experience & Leveling)
    // ============================================================

    /**
     * מוסיף ניסיון לדמות ומעלה רמה אם צריך.
     * כל EXPERIENCE_PER_LEVEL נקודות ניסיון מעלות רמה אחת.
     * ניתן לעלות כמה רמות בבת אחת.
     *
     * @param amount כמות הניסיון
     */
    public void gainExperience(int amount) {
        // הוסף את הניסיון
        // כל עוד יש מספיק ניסיון לרמה הבאה, העלה רמה וקרא ל-onLevelUp
        experience += amount;
        while (experience >= EXPERIENCE_PER_LEVEL) {
            experience -= EXPERIENCE_PER_LEVEL;
            level++;
            onLevelUp();
        }
    }

    /**
     * מתודה אבסטרקטית שנקראת כאשר הדמות עולה רמה.
     * כל סוג דמות מגדיר מה קורה כשעולים רמה.
     */
    protected abstract void onLevelUp();

    /**
     * מתודה אבסטרקטית לחישוב נזק התקפה.
     * כל סוג דמות מחשב נזק בצורה שונה.
     *
     * @return נזק ההתקפה
     */
    public abstract int calculateAttackDamage();

    /**
     * מתודה אבסטרקטית לביצוע יכולת מיוחדת.
     * כל סוג דמות יש לו יכולת מיוחדת.
     *
     * @param target היעד של היכולת
     * @return true אם היכולת בוצעה בהצלחה
     */
    public abstract boolean useSpecialAbility(Character target);

    // ============================================================
    // Recently Used Items Stack
    // ============================================================

    /**
     * מוסיף פריט לסטאק הפריטים האחרונים.
     * @param item הפריט שנעשה בו שימוש
     */
    public void pushRecentlyUsed(Item item) {
        recentlyUsedItems.push(item);
    }

    /**
     * מחזיר את הפריט האחרון שנעשה בו שימוש.
     * @return הפריט האחרון, או null אם הסטאק ריק
     */
    public Item popRecentlyUsed() {
        if (recentlyUsedItems.isEmpty()) {
            return null;
        }
        return recentlyUsedItems.pop();
    }

    /**
     * מציץ לפריט האחרון בלי להסיר אותו.
     * @return הפריט האחרון, או null אם הסטאק ריק
     */
    public Item peekRecentlyUsed() {
        if (recentlyUsedItems.isEmpty()) {
            return null;
        }
        return recentlyUsedItems.peek();
    }

    // ============================================================
    // Getters & Setters
    // ============================================================

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public int getGold() {
        return gold;
    }

    public void addGold(int amount) {
        this.gold += amount;
    }

    public boolean spendGold(int amount) {
        if (gold >= amount) {
            gold -= amount;
            return true;
        }
        return false;
    }

    public int getCurrentMana() {
        return currentMana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public int getBaseStrength() {
        return baseStrength;
    }

    public int getBaseDefense() {
        return baseDefense;
    }

    public Weapon getEquippedWeapon() {
        return equippedWeapon;
    }

    public HashMap<Armor.ArmorSlot, Armor> getEquippedArmor() {
        return new HashMap<>(equippedArmor);
    }

    public ArrayList<Item> getInventory() {
        return new ArrayList<>(inventory);
    }

    public int getInventorySize() {
        return inventory.size();
    }

    public int getMaxInventorySize() {
        return maxInventorySize;
    }

    @Override
    public String toString() {
        return String.format("%s (Level %d) - HP: %d/%d, Mana: %d/%d, Gold: %d",
                name, level, currentHealth, maxHealth, currentMana, maxMana, gold);
    }
}