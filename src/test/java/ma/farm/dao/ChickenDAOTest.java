package ma.farm.dao;

import ma.farm.dao.ChickenDAO;
import ma.farm.model.Chicken;
import java.time.LocalDate;
import java.util.List;

public class ChickenDAOTest {
    public static void main(String[] args) {
        ChickenDAO chickenDAO = new ChickenDAO();

        System.out.println("=== Testing ChickenDAO ===\n");

        try {
            // Test 1: Get initial statistics
            System.out.println("1. Initial Chicken Statistics:");
            var initialStats = chickenDAO.getChickenStatistics();
            System.out.println("   " + initialStats);
            System.out.println();

            // Test 2: Get all chickens
            System.out.println("2. Getting all chickens:");
            List<Chicken> allChickens = chickenDAO.getAllChickens();
            System.out.println("   Total chicken batches in database: " + allChickens.size());
            if (!allChickens.isEmpty()) {
                System.out.println("   Sample batch: " + allChickens.get(0).getBatchNumber() +
                        " with " + allChickens.get(0).getQuantity() + " chickens");
            }
            System.out.println();

            // Test 3: Get chickens by house
            System.out.println("3. Testing house-based queries:");
            for (int houseId = 1; houseId <= 4; houseId++) {
                List<Chicken> houseChickens = chickenDAO.getChickensByHouse(houseId);
                int totalInHouse = chickenDAO.getTotalChickensInHouse(houseId);
                System.out.println("   House " + houseId + ": " + houseChickens.size() +
                        " batches, " + totalInHouse + " total chickens");
            }
            System.out.println();

            // Test 4: Create a new chicken batch
            System.out.println("4. Creating new chicken batch...");
            Chicken newChicken = createTestChicken();
            boolean created = chickenDAO.createChickenBatch(newChicken);
            if (created) {
                System.out.println("   New chicken batch created successfully!");
            } else {
                System.out.println("   Failed to create new chicken batch.");
            }
            System.out.println();

            // Test 5: Get chickens by batch number
            System.out.println("5. Getting chickens by batch number:");
            String testBatch = "TEST-BATCH-001";
            List<Chicken> batchChickens = chickenDAO.getChickensByBatch(testBatch);
            System.out.println("   Found " + batchChickens.size() + " batches with batch number: " + testBatch);
            if (!batchChickens.isEmpty()) {
                Chicken foundChicken = batchChickens.get(0);
                System.out.println("   Batch details:");
                System.out.println("     - Quantity: " + foundChicken.getQuantity());
                System.out.println("     - Gender: " + foundChicken.getGender());
                System.out.println("     - Health Status: " + foundChicken.getHealthStatus());
                System.out.println("     - Arrival Date: " + foundChicken.getArrivalDate());

                // Test 6: Update chicken batch
                System.out.println("6. Updating chicken batch...");
                foundChicken.setQuantity(350); // Increase quantity
                foundChicken.setHealthStatus("Excellent");
                foundChicken.setAverageWeight(1.2);

                boolean updated = chickenDAO.updateChickenBatch(foundChicken);
                if (updated) {
                    System.out.println("   Chicken batch updated successfully!");

                    // Verify update
                    Chicken updatedChicken = chickenDAO.getChickenById(foundChicken.getId());
                    if (updatedChicken != null) {
                        System.out.println("   Verified update - New quantity: " + updatedChicken.getQuantity() +
                                ", Health: " + updatedChicken.getHealthStatus());
                    }
                } else {
                    System.out.println("   Failed to update chicken batch.");
                }
            }
            System.out.println();

            // Test 7: Get chickens by health status
            System.out.println("7. Getting chickens by health status:");
            List<Chicken> healthyChickens = chickenDAO.getChickensByHealthStatus("Healthy");
            List<Chicken> excellentChickens = chickenDAO.getChickensByHealthStatus("Excellent");
            List<Chicken> growingChickens = chickenDAO.getChickensByHealthStatus("Growing");

            System.out.println("   Healthy chickens: " + healthyChickens.size() + " batches");
            System.out.println("   Excellent chickens: " + excellentChickens.size() + " batches");
            System.out.println("   Growing chickens: " + growingChickens.size() + " batches");
            System.out.println();

            // Test 8: Get chickens due for transfer
            System.out.println("8. Getting chickens due for transfer:");
            LocalDate futureDate = LocalDate.now().plusDays(60);
            List<Chicken> dueChickens = chickenDAO.getChickensDueForTransfer(futureDate);
            System.out.println("   Chickens due for transfer by " + futureDate + ": " + dueChickens.size());
            if (!dueChickens.isEmpty()) {
                System.out.println("   Upcoming transfers:");
                int count = 0;
                for (Chicken c : dueChickens) {
                    if (count++ >= 5) break;
                    System.out.println("     - Batch " + c.getBatchNumber() +
                            " transfers on " + c.getNextTransferDate());
                }
            }
            System.out.println();

            // Test 9: Clean up - Delete test batch
            System.out.println("9. Cleaning up test data...");
            if (!batchChickens.isEmpty()) {
                Chicken toDelete = batchChickens.get(0);
                boolean deleted = chickenDAO.deleteChickenBatch(toDelete.getId());
                if (deleted) {
                    System.out.println("   Test batch deleted successfully!");
                } else {
                    System.out.println("   Failed to delete test batch.");
                }
            }
            System.out.println();

            // Test 10: Final statistics
            System.out.println("10. Final Chicken Statistics:");
            var finalStats = chickenDAO.getChickenStatistics();
            System.out.println("   " + finalStats);
            System.out.println();

            // Test 11: Detailed batch analysis
            System.out.println("11. Detailed Batch Analysis:");
            analyzeBatches(chickenDAO);

        } catch (Exception e) {
            System.err.println("Error during testing: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== ChickenDAO Tests Complete ===");
    }

    private static Chicken createTestChicken() {
        Chicken chicken = new Chicken();
        chicken.setHouseId(2); // Assuming House 2 exists (Egg Layer)
        chicken.setBatchNumber("TEST-BATCH-001");
        chicken.setQuantity(300);
        chicken.setArrivalDate(LocalDate.now());
        chicken.setAgeInDays(1);
        chicken.setGender("Mixed");
        chicken.setHealthStatus("Healthy");
        chicken.setAverageWeight(0.05);
        chicken.setNextTransferDate(LocalDate.now().plusDays(45));
        return chicken;
    }

    private static void analyzeBatches(ChickenDAO chickenDAO) {
        List<Chicken> allChickens = chickenDAO.getAllChickens();

        if (allChickens.isEmpty()) {
            System.out.println("   No chicken batches found in database.");
            return;
        }

        System.out.println("   Total batches: " + allChickens.size());

        int totalChickens = 0;
        double totalWeight = 0;
        int weightCount = 0;

        for (Chicken c : allChickens) {
            totalChickens += c.getQuantity();
            if (c.getAverageWeight() != null && c.getAverageWeight() > 0) {
                totalWeight += c.getAverageWeight();
                weightCount++;
            }
        }

        System.out.println("   Total chickens across all batches: " + totalChickens);

        if (weightCount > 0) {
            double avgWeight = totalWeight / weightCount;
            System.out.println("   Average weight (from " + weightCount + " batches with weight data): " +
                    String.format("%.3f", avgWeight) + " kg");
        }

        // Age distribution
        int dayOld = 0, young = 0, mature = 0;
        for (Chicken c : allChickens) {
            if (c.getAgeInDays() <= 7) dayOld++;
            else if (c.getAgeInDays() <= 30) young++;
            else mature++;
        }

        System.out.println("   Age distribution:");
        System.out.println("     - Day-old (0-7 days): " + dayOld + " batches");
        System.out.println("     - Young (8-30 days): " + young + " batches");
        System.out.println("     - Mature (31+ days): " + mature + " batches");

        // Gender distribution
        int mixed = 0, female = 0, male = 0;
        for (Chicken c : allChickens) {
            String gender = c.getGender().toLowerCase();
            if (gender.contains("mixed")) mixed++;
            else if (gender.contains("female")) female++;
            else if (gender.contains("male")) male++;
        }

        System.out.println("   Gender distribution:");
        System.out.println("     - Mixed: " + mixed + " batches");
        System.out.println("     - Female: " + female + " batches");
        System.out.println("     - Male: " + male + " batches");
    }
}