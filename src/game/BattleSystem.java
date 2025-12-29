package game;

import model.characters.Character;
import model.exceptions.InventoryFullException;
import model.items.Armor;
import model.items.Item;
import model.items.Potion;
import model.exceptions.InvalidActionException;
import model.exceptions.ItemNotFoundException;
import model.items.Weapon;
import java.util.Scanner;

import java.util.*;

/**
 * מערכת הקרב של המשחק.
 * משתמשת ב-Queue לניהול תור הפעולות.
 */
public class BattleSystem {

    Scanner scanner = new Scanner(System.in);

    private Character player;
    private Character enemy;
    private Queue<BattleAction> actionQueue;
    private ArrayList<String> battleLog;
    private boolean battleEnded;
    private Character winner;

    public BattleSystem(Character player, Character enemy) {
        this.player = player;
        this.enemy = enemy;
        this.actionQueue = new LinkedList<>();
        this.battleLog = new ArrayList<>();
        this.battleEnded = false;
        this.winner = null;

        logMessage("Battle started: " + player.getName() + " vs " + enemy.getName());
    }

    // ============================================================
    // : ניהול תור פעולות (Queue Management)
    // ============================================================

    /**
     * מוסיף פעולה לתור הפעולות.
     *
     * @param action הפעולה להוספה
     * @throws InvalidActionException אם הקרב כבר הסתיים
     */
    public void queueAction(BattleAction action) throws InvalidActionException {
        // 1. בדוק שהקרב לא הסתיים
        if(battleEnded)
        {
            throw new InvalidActionException("queueAction", "the battle ended");
        }
        // 2. הוסף את הפעולה לתור
        actionQueue.add(action);
    }

    /**
     * יוצר ומוסיף פעולה של השחקן.
     *
     * @param actionType סוג הפעולה
     * @throws InvalidActionException אם הקרב הסתיים
     */
    public void queuePlayerAction(BattleAction.ActionType actionType)
            throws InvalidActionException {
        if (battleEnded)
        {
            throw new InvalidActionException("queuePlayerAction", "the battle ended");
        }
        BattleAction action = new BattleAction(
                player,
                enemy,
                actionType
        );
        queueAction(action);
    }

    /**
     * יוצר ומוסיף פעולה של שימוש בפריט.
     *
     * @param itemName שם הפריט
     * @throws InvalidActionException אם הקרב הסתיים
     */
    public void queuePlayerItemAction(String itemName) throws InvalidActionException {
        if (battleEnded)
        {
            throw new InvalidActionException("queuePlayerItemAction", "the battle ended");
        }
        BattleAction action = new BattleAction(
                player,
                player,
                BattleAction.ActionType.USE_ITEM,
                itemName
        );

    }

    /**
     * יוצר פעולה אקראית לאויב (AI פשוט).
     * - 60% התקפה רגילה
     * - 25% יכולת מיוחדת
     * - 15% הגנה
     *
     * @return פעולת האויב
     */
    public BattleAction generateEnemyAction() {
        int rand = (int)(Math.random() * 100);
        if (rand < 60) {
            return new BattleAction(player, enemy, BattleAction.ActionType.ATTACK);
        } else if ((rand < 85)) {
            return new BattleAction(player,enemy,BattleAction.ActionType.SPECIAL);
        }else {return new BattleAction(player,enemy,BattleAction.ActionType.DEFEND);
        }

    }

    /**
     * מבצע את הפעולה הבאה בתור.
     *
     * @return תיאור מה קרה, או null אם התור ריק
     */
    public String processNextAction() {
        // 1. בדוק שהתור לא ריק
            if(actionQueue.isEmpty()){
                return null;
            }
        // 2. הוצא פעולה מהתור
        BattleAction currentAction = actionQueue.poll();
        // 3. בצע את הפעולה לפי הסוג שלה
        String result = "";
        try {
            switch (currentAction.getActionType())
            {
                case FLEE -> {
                     boolean fled = executeFlee(currentAction.getActor());
                    result = fled ? "Successfully fled" : "Failed to flee";
                    break;
                }
                case ATTACK -> {
                    int damage = executeAttack(currentAction.getActor(),currentAction.getTarget());
                    result = currentAction.getActor().getName() +
                            " attacked for " + damage + " damage";
                    break;
                }
                case DEFEND -> {
                    executeDefend(currentAction.getActor());
                    result = currentAction.getActor().getName() + " is defending";
                    break;

                }
                case SPECIAL -> {
                   boolean success =  executeSpecialAbility(
                           currentAction.getActor(),
                           currentAction.getTarget()
                   );
                    result = currentAction.getActor().getName() +
                            (success ? " used special ability" : " failed special ability");
                    break;


                }
                case USE_ITEM -> {
                   boolean success = executeUseItem(currentAction.getActor(), currentAction.getItemName());
                    result = currentAction.getActor().getName() +
                            " used item " + currentAction.getItemName();
                    break;

                }
            }

        }catch (Exception e){
            result = e.getMessage();
        }
        // 4. בדוק אם הקרב הסתיים
        checkBattleEnd();
        // 5. רשום ללוג והחזר תיאור
        logMessage(result);
        return result;
    }

    /**
     * מבצע את כל הפעולות בתור עד שהוא מתרוקן או שהקרב נגמר.
     *
     * @return רשימה של כל התיאורים של מה שקרה
     */
    public ArrayList<String> processAllActions() throws InvalidActionException {

        ArrayList<String> results = new ArrayList<>();
        while(!actionQueue.isEmpty() && !battleEnded)
        {
            System.out.println("enter your next move: ");
            System.out.println("FLEE");
            System.out.println("ATTACK");
            System.out.println("DEFEND");
            System.out.println("SPECIAL");
            System.out.println("USE_ITEM");
            String nextMove = scanner.nextLine();
            queuePlayerItemAction(nextMove);
            String res = processNextAction();
            if (res != null) {
                results.add(res);
            }
            checkBattleEnd();
            generateEnemyAction();
        }
        return results;
    }

    // ============================================================
    // : ביצוע פעולות (Action Execution)
    // ============================================================

    /**
     * מבצע התקפה רגילה.
     *
     * @param attacker התוקף
     * @param defender המותקף
     * @return הנזק שנגרם
     */
    private int executeAttack(Character attacker, Character defender) {

        int damage = attacker.calculateAttackDamage();
        defender.takeDamage(damage);

        logMessage(attacker.getName() + " attacks " +
                defender.getName() + " for " + damage + " damage");

        return damage;
    }

    /**

     * מבצע יכולת מיוחדת.
     *
     * @param actor המבצע
     * @param target היעד
     * @return true אם הצליח
     */
    private boolean executeSpecialAbility(Character actor, Character target) {

        boolean success = actor.useSpecialAbility(target);
        if (success) {
            logMessage(actor.getName() + " used special ability on " +
                    target.getName());

        } else {
            logMessage(actor.getName() + " failed to use special ability");
        }

        return success;
    }

    /**
     *  מימוש executeUseItem
     * משתמש בפריט.
     *
     * @param actor המשתמש
     * @param itemName שם הפריט
     * @return true אם הצליח
     * @throws ItemNotFoundException אם הפריט לא נמצא
     */
    private boolean executeUseItem(Character actor, String itemName)
            throws ItemNotFoundException {
        
        // 1. חפש את הפריט במלאי
        Item currentItem;
        try {
            currentItem = actor.removeItem(itemName);
        }catch (Exception e)
        {
            throw new ItemNotFoundException(itemName);
        }
        // 2. אם זה Potion, השתמש בו

        if (currentItem instanceof Potion)
        {
            ((Potion) currentItem).use(actor);
            return true;
        }
        else if(currentItem instanceof Weapon){
            try {
                actor.equipWeapon((Weapon) currentItem);
            }catch (Exception e)
            {
                return false;
            }
        }else {
            try {
                actor.equipArmor((Armor) currentItem);
            }catch (Exception e)
            {
                return false;
            }

        }
        // 3. הוסף לסטאק של פריטים אחרונים
        actor.pushRecentlyUsed(currentItem);
        return true;
    }

    /**
     * מבצע פעולת הגנה - מפחית נזק בתור הבא ב-50%.
     *
     * @param defender המגן
     */
    private void executeDefend(Character defender) {
        // רשום ללוג שהדמות מגינה
        logMessage(defender.getName() + " is defending");
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     *  מימוש executeFlee
     * מנסה לברוח מהקרב.
     * סיכוי הצלחה = 30% + (רמת שחקן - רמת אויב) * 5%
     *
     * @param fleeing הבורח
     * @return true אם ההבריחה הצליחה
     */
    private boolean executeFlee(Character fleeing) {
        Character opponent = (fleeing == player) ? enemy : player;

        int chance = 30 + (fleeing.getLevel() - opponent.getLevel()) * 5;
        int roll = (int)(Math.random() * 100);

        if (roll < chance){
            battleEnded = true;
            winner = opponent;
            logMessage(fleeing.getName() + " successfully fled!");
            return true;
        }
        logMessage(fleeing.getName() + " failed to flee");
        return false;
    }

    // ============================================================
    //  בדיקת סיום קרב
    // ============================================================

    /**
     *  מימוש checkBattleEnd
     * בודק אם הקרב הסתיים וקובע מנצח.
     */
    private void checkBattleEnd() {
        if (!player.isAlive()) {
            battleEnded = true;
            winner = enemy;
            logMessage(enemy.getName() + " wins the battle!");
        }
        else if (!enemy.isAlive()) {
            battleEnded = true;
            winner = player;
            logMessage(player.getName() + " wins the battle!");
        }
    }

    // ============================================================
    //  מיון פעולות לפי עדיפות (שימוש במחלקה אנונימית)
    // ============================================================

    /**
     * ממיין רשימת פעולות לפי עדיפות (גבוה לנמוך).
     * השתמש ב-Comparator כמחלקה אנונימית!
     *
     * @param actions רשימת הפעולות למיון
     */
    public void sortActionsByPriority(ArrayList<BattleAction> actions) {

        // צור Comparator<BattleAction> כמחלקה אנונימית
        actions.sort(new Comparator<BattleAction>() {
            @Override
            public int compare(BattleAction o1, BattleAction o2) {
                return Integer.compare(
                        o2.getPriority(),
                        o1.getPriority()
                );
            }
        });
        // השתמש ב-actions.sort(comparator)
    }

    /**
     * מסנן פעולות לפי תנאי מסוים.
     * השתמש בממשק פונקציונלי!
     *
     * @param actions רשימת הפעולות
     * @param filter הפילטר (ממשק עם מתודת test)
     * @return רשימה מסוננת
     */
    public ArrayList<BattleAction> getActionsFilteredBy(
            ArrayList<BattleAction> actions, ActionFilter filter) {

        ArrayList<BattleAction> result = new ArrayList<>();

        for (BattleAction action : actions) {
            if (filter.test(action)) {
                result.add(action);
            }
        }
        return result;
    }

    /**
     * ממשק פונקציונלי לסינון פעולות.
     */
    public interface ActionFilter {
        boolean test(BattleAction action);
    }

    // ============================================================
    // Utility Methods
    // ============================================================

    private void logMessage(String message) {
        battleLog.add(message);
        System.out.println(message);
    }

    // Getters
    public Character getPlayer() {
        return player;
    }

    public Character getEnemy() {
        return enemy;
    }

    public boolean isBattleEnded() {
        return battleEnded;
    }

    public Character getWinner() {
        return winner;
    }

    public ArrayList<String> getBattleLog() {
        return new ArrayList<>(battleLog);
    }

    public int getQueueSize() {
        return actionQueue.size();
    }

    public boolean isQueueEmpty() {
        return actionQueue.isEmpty();
    }
}