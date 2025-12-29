    package model.characters;

    /**
     * מחלקה המייצגת קשת במשחק.
     * יורשת מ-Character.
     * הקשת מתמחה בהתקפות מרחוק ובסיכוי לפגיעה קריטית.
     */
    public class Archer extends Character {

        private double criticalChance;
        private double criticalMultiplier;
        private int arrows;
        private static final int MAX_ARROWS = 30;
        private static final int MULTISHOT_ARROW_COST = 3;

        public Archer(String name) {
            // קשת: חיים בינוניים, מאנה בינונית, כוח בינוני, הגנה נמוכה
            super(name, 100, 80, 12, 5);
            this.criticalChance = 0.15; // 15% סיכוי לקריטי
            this.criticalMultiplier = 2.0; // נזק כפול בקריטי
            this.arrows = MAX_ARROWS;
        }

        // ============================================================
        //  מימוש מתודות אבסטרקטיות
        // ============================================================

        /**
         *  מימוש onLevelUp
         * כאשר קשת עולה רמה:
         * - maxHealth עולה ב-12
         * - maxMana עולה ב-10
         * - baseStrength עולה ב-2
         * - baseDefense עולה ב-1
         * - criticalChance עולה ב-0.02 (מקסימום 0.5)
         * - arrows מתמלאים למקסימום
         * - currentHealth ו-currentMana מתמלאים למקסימום
         */
        @Override
        protected void onLevelUp() {
            maxHealth += 12;
            maxMana += 10;
            baseStrength += 2;
            baseDefense += 1;
            criticalChance += 0.02;
            if(criticalChance > 0.5)
            {
                criticalChance = 0.5;
            }
            arrows = MAX_ARROWS;
            currentHealth = maxHealth;
            currentMana = maxMana;
        }

        /**
         * נזק קשת = baseStrength + נזק נשק (אם יש)
         * יש סיכוי של criticalChance לפגיעה קריטית (נזק * criticalMultiplier)
         * השתמש ב-Math.random() לקביעת קריטי
         *
         * @return נזק ההתקפה (עם או בלי קריטי)
         */
        @Override
        public int calculateAttackDamage() {
             int arrowDamage = baseStrength;
            if (equippedWeapon != null) {
                arrowDamage += equippedWeapon.calculateDamage();
            }
            if (Math.random() < criticalChance) {
                arrowDamage = (int) (arrowDamage * criticalMultiplier);
            }
            return arrowDamage;
        }

        /**
         * יכולת מיוחדת: ירי מרובה
         * - עולה MULTISHOT_ARROW_COST חיצים
         * - יורה 3 חיצים, כל אחד גורם 70% מנזק רגיל
         * - כל חץ יכול להיות קריטי בנפרד
         * - מחזיר true אם הצליח, false אם אין מספיק חיצים
         *
         * @param target היריב
         * @return true אם היכולת בוצעה
         */
        @Override
        public boolean useSpecialAbility(Character target) {
            if(arrows < MULTISHOT_ARROW_COST)
            {
                return false;
            }
            arrows -= MULTISHOT_ARROW_COST;

            for (int i = 0; i < 3; i++) {
                int damage = calculateAttackDamage();
                damage = (int) (damage * 0.7);
                target.takeDamage(damage);
            }

            return true;
        }

        // ============================================================
        //  מתודות ייחודיות לקשת
        // ============================================================

        /**
         *  מימוש shootArrow
         * יורה חץ בודד ביריב.
         * - עולה חץ אחד
         * - נזק רגיל עם סיכוי לקריטי
         *
         * @param target היריב
         * @return הנזק שנגרם, או -1 אם אין חיצים
         */
        public int shootArrow(Character target) {
            if (arrows <= 0) {
                return -1;
            }
            arrows--;
            int damage = calculateAttackDamage();
            target.takeDamage(damage);
            return damage;
        }

        /**
         * מימוש refillArrows
         * ממלא חיצים בחזרה למקסימום.
         * עולה 5 זהב לכל חץ שחסר.
         *
         * @return true אם המילוי הצליח, false אם אין מספיק זהב
         */
        public boolean refillArrows() {
            int cost = 5 * (MAX_ARROWS  - arrows);
            if(spendGold(cost))
            {
                arrows = MAX_ARROWS;
                return true;
            }
            return false;
        }

        /**
         * תמרון התחמקות - סיכוי להתחמק מהתקפה.
         * סיכוי ההתחמקות = criticalChance * 1.5
         * עולה 15 מאנה
         *
         * @return true אם ההתחמקות הצליחה
         */
        public boolean evasiveManeuver() {
            if (!useMana(15)) {
                return false;
            }
            double evadeChance = criticalChance * 1.5;
            return Math.random() < evadeChance;
        }

        // Getters
        public double getCriticalChance() {
            return criticalChance;
        }

        public double getCriticalMultiplier() {
            return criticalMultiplier;
        }

        public int getArrows() {
            return arrows;
        }

        public int getMaxArrows() {
            return MAX_ARROWS;
        }

        @Override
        public String toString() {
            return "Archer: " + super.toString() +
                    String.format(" | Arrows: %d/%d | Crit: %.0f%%",
                            arrows, MAX_ARROWS, criticalChance * 100);
        }
    }