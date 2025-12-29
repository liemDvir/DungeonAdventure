
package game;

import model.characters.*;
import model.characters.Character;
import model.items.*;
import model.exceptions.*;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * המחלקה הראשית של המשחק.
 * מנהלת את זרימת המשחק והאינטראקציה עם השחקן.
 */
public class Game {

    private Character player;
    private DungeonMap map;
    private Shop shop;
    private Scanner scanner;
    private boolean gameRunning;

    public Game() {
        this.scanner = new Scanner(System.in);
        this.gameRunning = false;
    }

    /**
     * מתחיל את המשחק.
     */
    public void start() {
        System.out.println("=================================");
        System.out.println("  Welcome to Dungeon Adventure!");
        System.out.println("=================================\n");

        createCharacter();
        initializeMap();
        initializeShop();

        gameRunning = true;
        gameLoop();
    }

    /**
     * מאפשר לשחקן לבחור סוג דמות וליצור אותה.
     */
    private void createCharacter() {

        // 1. בקש שם מהשחקן
        System.out.print("Enter character name: ");
        String name = scanner.nextLine();
        // 2. הצג אפשרויות: 1. Warrior, 2. Mage, 3. Archer
        System.out.println("Choose class:");
        System.out.println("1. Warrior");
        System.out.println("2. Mage");
        System.out.println("3. Archer");
        System.out.print("Choice: ");

        // 3. צור את הדמות המתאימה
        int choice = getPlayerChoice();

        switch (choice) {
            case 2:
                player = new Mage(name);
                break;
            case 3:
                player = new Archer(name);
                break;
            case 1:
            default:
                player = new Warrior(name);
        }
        System.out.println("Character created: " + player.getName());

    }

    /**
     * יוצר את מפת המבוך עם כמה מיקומים.
     */
    private void initializeMap() {
        // 1. צור DungeonMap חדש
        map = new DungeonMap();
        // 2. הוסף לפחות 5 מיקומים
        GameLocation entrance = new GameLocation(
                "entrance",
                "Dungeon Entrance",
                "The dark entrance of the dungeon.",
                1
        );
        GameLocation hall = new GameLocation(
                "hall",
                "Main Hall",
                "A large hall with old torches on the walls.",
                2
        );
        GameLocation armory = new GameLocation(
                "armory",
                "Abandoned Armory",
                "Rusty weapons are scattered everywhere.",
                3
        );
        GameLocation dungeon = new GameLocation(
                "dungeon",
                "Deep Dungeon",
                "You hear terrifying sounds from the darkness.",
                4
        );
        GameLocation bossRoom = new GameLocation(
                "boss",
                "Boss Chamber",
                "The lair of the dungeon master.",
                5
        );
        map.addLocation(entrance);
        map.addLocation(hall);
        map.addLocation(armory);
        map.addLocation(dungeon);
        map.addLocation(bossRoom);
        // 3. חבר ביניהם
        entrance.addConnection("hall");

        hall.addConnection("entrance");
        hall.addConnection("armory");
        hall.addConnection("dungeon");

        armory.addConnection("hall");

        dungeon.addConnection("hall");
        dungeon.addConnection("boss");

        bossRoom.addConnection("dungeon");

        // 4. הגדר נקודת התחלה ומיקום הבוס
        bossRoom.setHasMaster(true);
        map.setStartLocation("entrance");
        map.setBossLocation("boss");
    }

    /**

     * יוצר את החנות עם פריטים התחלתיים.
     */
    private void initializeShop() {
        // 1. צור Shop חדש
        shop = new Shop("Village Shop");
        // 2. הוסף כמה נשקים, שריונים ושיקויים
        shop.addItemToShop(new Weapon("Sword",
                "sharp",
                5,
                10,
                Item.ItemRarity.COMMON,
                2,5,
                Weapon.WeaponType.SWORD),
                3);
        shop.addItemToShop(new Weapon("Sword",
                        "sharp",
                        5,
                        10,
                        Item.ItemRarity.COMMON,
                        2,5,
                        Weapon.WeaponType.SWORD),
                3);
        shop.addItemToShop(new Weapon("axe",
                        "sharp",
                        3,
                        9,
                        Item.ItemRarity.RARE,
                        1,6,
                        Weapon.WeaponType.AXE),
                3);
        shop.addItemToShop(new Weapon("bow",
                        "long distance",
                        3,
                        15,
                        Item.ItemRarity.EPIC,
                        4,7,
                        Weapon.WeaponType.BOW),
                3);
        shop.addItemToShop(new Weapon("poison",
                        "magic item",
                        3,
                        12,
                        Item.ItemRarity.LEGENDARY,
                        6,7,
                        Weapon.WeaponType.STAFF),
                1);

    }

    /**
     * לולאת המשחק הראשית.
     */
    private void gameLoop() {
        while (gameRunning) {
            displayMenu();
            int choice = getPlayerChoice();
            handleChoice(choice);
        }

        System.out.println("\nThanks for playing Dungeon Adventure!");
    }

    /**
     * מציג את התפריט הראשי.
     */
    private void displayMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. View Character");
        System.out.println("2. View Inventory");
        System.out.println("3. Move to Location");
        System.out.println("4. Visit Shop");
        System.out.println("5. Battle");
        System.out.println("6. Save & Quit");
        System.out.print("Choose: ");
    }

    /**
     * קורא בחירה מהשחקן.
     */
    private int getPlayerChoice() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * מטפל בבחירת השחקן.
     */
    private void handleChoice(int choice) {

        // switch על choice וקרא למתודות המתאימות
        switch (choice) {
            case 1:
                viewCharacter();
                break;
            case 2:
                viewInventory();
                break;
            case 3:
                moveToLocation();
                break;
            case 4:
                visitShop();
                break;
            case 5:
                startBattle();
                break;
            case 6:
                gameRunning = false;
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }

    /**
     * מציג מידע על הדמות.
     */
    private void viewCharacter() {
        System.out.println("\n" + player.toString());
    }

    /**
     * מציג את המלאי של השחקן.
     */
    private void viewInventory() {
        // הצג את כל הפריטים במלאי
        ArrayList<Item> items = player.getInventory();

        if (items.isEmpty()) {
            System.out.println("Inventory is empty.");
            return;
        }

        System.out.println("--- Inventory ---");
        for (Item item : items) {
            System.out.println(item);
        }
    }

    /**
     * מאפשר לשחקן לזוז למיקום אחר.
     */
    private void moveToLocation() {
        GameLocation current = map.getCurrentLocation();
        ArrayList<String> connections = current.getConnectedLocationIds();
        // 1. הצג מיקומים נגישים
        System.out.println("\nAvailable locations:");
        for (int i = 0; i < connections.size(); i++) {
            GameLocation loc = map.getLocation(connections.get(i));
            System.out.println((i + 1) + ". " + loc);
        }
        // 2. בקש בחירה מהשחקן
        System.out.print("Choose destination: ");
        int choice = getPlayerChoice() - 1;

        if (choice < 0 || choice >= connections.size()) {
            System.out.println("Invalid location.");
            return;
        }
        // 3. הזז את השחקן
        map.setStartLocation(connections.get(choice));
        map.getCurrentLocation().markAsVisited();

        System.out.println("You moved to: " + map.getCurrentLocation().getName());
    }

    /**
     * מאפשר לשחקן לקנות ולמכור בחנות.
     */
    private void visitShop() {

        System.out.println("\n--- Shop ---");
        System.out.println(shop);

        for (Item item : shop.getAvailableItems()) {
            System.out.println(item.getName() +
                    " | Price: " + item.getBuyPrice() +
                    " | Stock: " + shop.getItemStock(item.getName()));
        }
    }

    /**
     * מתחיל קרב עם אויב.
     */
    private void startBattle() {
        GameLocation location = map.getCurrentLocation();

        if (location.getDangerLevel() == 0) {
            System.out.println("No enemies here.");
            return;
        }
        // 1. צור אויב (דמות אקראית)
        Character enemy = new Warrior("lv" + location.getDangerLevel() + "");
        // 2. צור BattleSystem
        BattleSystem battle = new BattleSystem(player, enemy);
        // 3. הרץ את הקרב
        try{
            battle.processAllActions();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (battle.getWinner() == player) {
            System.out.println("You won the battle!");
        } else {
            System.out.println("You were defeated...");
            gameRunning = false;
        }
    }

    /**
     * נקודת הכניסה למשחק.
     */
    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}
