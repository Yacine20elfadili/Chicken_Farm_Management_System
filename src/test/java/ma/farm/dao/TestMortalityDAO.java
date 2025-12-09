// TestMortalityDAO.java
package ma.farm.dao;

import ma.farm.dao.MortalityDAO;
import ma.farm.model.Mortality;
import java.time.LocalDate;

public class TestMortalityDAO {
    public static void main(String[] args) {
        MortalityDAO mortalityDAO = new MortalityDAO();

        System.out.println("=== Testing MortalityDAO ===\n");

        try {
            // 1. Get deaths recorded today
            System.out.println("1. Deaths recorded today:");
            var todayDeaths = mortalityDAO.getTodayDeaths();
            if (todayDeaths.isEmpty()) {
                System.out.println("   No deaths recorded today.");
            } else {
                System.out.println("   Found " + todayDeaths.size() + " mortality records for today.");
                for (Mortality m : todayDeaths) {
                    System.out.println("   - " + m.getCount() + " deaths in House " + m.getHouseId() + " due to " + m.getCause());
                }
            }
            System.out.println();

            // 2. Get total deaths this week
            System.out.println("2. Total deaths this week: " + mortalityDAO.getTotalDeathsThisWeek());

            // 3. Get total deaths this month
            System.out.println("3. Total deaths this month: " + mortalityDAO.getTotalDeathsThisMonth());
            System.out.println();

            // 4. Record a new death
            System.out.println("4. Recording new death...");
            Mortality newDeath = new Mortality();
            newDeath.setHouseId(1);
            newDeath.setDeathDate(LocalDate.now());
            newDeath.setCount(1);
            newDeath.setCause("Test Recording");
            newDeath.setSymptoms("None");
            newDeath.setIsOutbreak(false);
            newDeath.setRecordedBy("Test User");
            newDeath.setNotes("Test recording from Java application");

            int newId = mortalityDAO.recordMortality(newDeath);
            if (newId > 0) {
                System.out.println("   Death recorded with ID: " + newId);
            } else {
                System.out.println("   Failed to record death.");
            }
            System.out.println();

            // 5. Get mortality history for House 1
            System.out.println("5. Mortality history for House 1:");
            var house1Deaths = mortalityDAO.getMortalityByHouse(1);
            System.out.println("   Total deaths in House 1: " + mortalityDAO.getTotalDeathsInHouse(1));
            if (!house1Deaths.isEmpty()) {
                System.out.println("   Recent records:");
                int count = 0;
                for (Mortality m : house1Deaths) {
                    if (count++ >= 5) break; // Show only 5 recent records
                    System.out.println("   - " + m.getDeathDate() + ": " + m.getCount() + " deaths due to " + m.getCause());
                }
            }
            System.out.println();

            // 6. Get statistics
            System.out.println("6. Mortality Statistics:");
            var stats = mortalityDAO.getMortalityStatistics();
            System.out.println(stats);

        } catch (Exception e) {
            System.err.println("Error during testing: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== MortalityDAO Tests Complete ===");
    }
}