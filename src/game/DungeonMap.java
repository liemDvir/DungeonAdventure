package game;

import model.exceptions.InvalidActionException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * מחלקה המייצגת את מפת המבוך.
 * משתמשת ב-HashMap למיפוי מזהי מיקום לאובייקטי GameLocation.
 */
public class DungeonMap {

    // HashMap ממזהה מיקום לאובייקט המיקום
    private HashMap<String, GameLocation> locations;
    private String currentLocationId;
    private String startLocationId;
    private String bossLocationId;

    public DungeonMap() {
        this.locations = new HashMap<>();
        this.currentLocationId = null;
        this.startLocationId = null;
        this.bossLocationId = null;
    }

    // ============================================================
    //  ניהול מפה
    // ============================================================

    /**
     * מוסיף מיקום חדש למפה.
     * אם זה המיקום הראשון, מגדיר אותו כנקודת ההתחלה.
     *
     * @param location המיקום להוספה
     */
    public void addLocation(GameLocation location) {
        locations.put(location.getId(), location);

        if (startLocationId == null) {
            startLocationId = location.getId();
            currentLocationId = startLocationId;
            location.markAsVisited();
        }
    }

    /**
     * מחבר שני מיקומים זה לזה (דו-כיווני).
     *
     * @param locationId1 מזהה מיקום ראשון
     * @param locationId2 מזהה מיקום שני
     * @throws InvalidActionException אם אחד המיקומים לא קיים
     */
    public void connectLocations(String locationId1, String locationId2)
            throws InvalidActionException {

        // 1. בדוק שני המיקומים קיימים
        GameLocation loc1 = locations.get(locationId1);
        GameLocation loc2 = locations.get(locationId2);
        if (loc1 == null || loc2 == null) {
            throw new InvalidActionException("connectLocations", "one or both of them is null");
        }
        // 2. הוסף חיבור דו-כיווני
        loc1.addConnection(locationId2);
        loc2.addConnection(locationId1);
    }

    /**
     * מחזיר מיקום לפי מזהה.
     *
     * @param locationId מזהה המיקום
     * @return המיקום, או null אם לא קיים
     */
    public GameLocation getLocation(String locationId) {
        return locations.get(locationId);
    }

    /**
     * מחזיר את המיקום הנוכחי.
     *
     * @return המיקום הנוכחי
     */
    public GameLocation getCurrentLocation() {
        if (currentLocationId == null) {
            return null;
        }
        return locations.get(currentLocationId);
    }

    /**
     * מזיז את השחקן למיקום אחר.
     * ניתן לזוז רק למיקום מחובר!
     *
     * @param locationId מזהה המיקום החדש
     * @throws InvalidActionException אם המיקום לא קיים או לא מחובר
     */
    public void moveTo(String locationId) throws InvalidActionException {
        GameLocation current = getCurrentLocation();
        GameLocation target = locations.get(locationId);
        // 1. בדוק שהמיקום קיים
        if (target == null) {
            throw new InvalidActionException("moveTo"," target doesnt exits");
        }
        // 2. בדוק שהמיקום הנוכחי מחובר למיקום החדש
        if (!current.getConnectedLocationIds().contains(locationId)) {
            throw new InvalidActionException("moveTo","the locations doesnt connected");
        }
        // 3. עדכן את currentLocationId
        currentLocationId = locationId;
        // 4. סמן את המיקום החדש כמבוקר
        target.markAsVisited();

    }

    /**
     * מחזיר רשימה של כל המיקומים שניתן להגיע אליהם מהמיקום הנוכחי.
     *
     * @return רשימת מיקומים נגישים
     */
    public ArrayList<GameLocation> getAccessibleLocations() {
        // עבור על כל ה-connectedLocationIds של המיקום הנוכחי
        // והחזר רשימה של האובייקטים המתאימים
        ArrayList<GameLocation> result = new ArrayList<>();
        GameLocation current = getCurrentLocation();
        if (current == null) {
            return result;
        }

        for (String id : current.getConnectedLocationIds()) {
            GameLocation loc = locations.get(id);
            if (loc != null) {
                result.add(loc);
            }
        }
        return result;
    }

    /**
     * מחזיר רשימה של כל המיקומים שכבר ביקרנו בהם.
     * @return רשימת מיקומים מבוקרים
     */
    public ArrayList<GameLocation> getVisitedLocations() {

        ArrayList<GameLocation> result = new ArrayList<>();
        for (GameLocation gameLocation: locations.values()) {
            if (gameLocation.isVisited()) {
                result.add(gameLocation);
            }
        }
        return result;
    }

    /**
     * מחזיר רשימה של כל המיקומים שעוד לא ביקרנו בהם.
     *
     * @return רשימת מיקומים לא מבוקרים
     */
    public ArrayList<GameLocation> getUnvisitedLocations() {

        ArrayList<GameLocation> result = new ArrayList<>();
        for (GameLocation gameLocation: locations.values()) {
            if (!gameLocation.isVisited()) {
                result.add(gameLocation);
            }
        }
        return result;
    }

    /**
     * מחזיר HashMap שממפה רמת סכנה לרשימת מיקומים.
     *
     * @return HashMap של (Integer -> ArrayList של GameLocation)
     */
    public HashMap<Integer, ArrayList<GameLocation>> getLocationsByDangerLevel() {

        HashMap<Integer, ArrayList<GameLocation>> map = new HashMap<>();
        for (GameLocation location : locations.values()) {
            int danger = location.getDangerLevel();

            map.putIfAbsent(danger, new ArrayList<>());
            map.get(danger).add(location);
        }

        return map;
    }

    /**
     * מחזיר את אחוז ההתקדמות בחקירת המפה.
     *
     * @return אחוז בין 0.0 ל-1.0
     */
    public double getExplorationProgress() {
        // מספר מיקומים מבוקרים / סך כל המיקומים
        if (locations.isEmpty()) {
            return 0.0;
        }
        int visited = 0;
        for (GameLocation location : locations.values()) {
            if (location.isVisited()) {
                visited++;
            }
        }
        return (double) visited / locations.size();

    }

    // Setters for special locations
    public void setStartLocation(String locationId) {
        this.startLocationId = locationId;
        this.currentLocationId = locationId;
        if (locations.containsKey(locationId)) {
            locations.get(locationId).markAsVisited();
        }
    }

    public void setBossLocation(String locationId) {
        this.bossLocationId = locationId;
        if (locations.containsKey(locationId)) {
            locations.get(locationId).setHasMaster(true);
        }
    }

    // Getters
    public String getCurrentLocationId() {
        return currentLocationId;
    }

    public String getStartLocationId() {
        return startLocationId;
    }

    public String getBossLocationId() {
        return bossLocationId;
    }

    public int getTotalLocations() {
        return locations.size();
    }

    public HashMap<String, GameLocation> getAllLocations() {
        return new HashMap<>(locations);
    }
}