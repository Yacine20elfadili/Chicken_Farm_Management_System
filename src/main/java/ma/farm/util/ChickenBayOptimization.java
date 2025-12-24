package ma.farm.util;

import ma.farm.model.House;

import java.util.List;

public class ChickenBayOptimization {

    /**
     * Fonction objectif X
     */
    public int calculateX(List<House> houses) {
        int x = 0;
        for (House h : houses) {
            x += h.getCapacity();
        }
        return x;
    }

    /**
     * Validation des contraintes
     * E <= F
     * D <= E + M
     */
    public boolean validateConstraints(int E, int F, int D, int M) {
        return E <= F && D <= E + M;
    }

    /**
     * Hill Climbing (simplifié mais valide)
     */
    public void optimizeHillClimbing(List<House> houses) {

        if (houses == null || houses.size() < 4) {
            throw new IllegalArgumentException("Minimum 4 houses required");
        }

        int currentScore = calculateX(houses);
        boolean improved = true;

        while (improved) {
            improved = false;

            for (House h : houses) {
                int oldCapacity = h.getCapacity();
                h.setCapacity(oldCapacity + 1);

                int newScore = calculateX(houses);

                if (newScore > currentScore) {
                    currentScore = newScore;
                    improved = true;
                } else {
                    h.setCapacity(oldCapacity); // rollback
                }
            }
        }
    }
}
