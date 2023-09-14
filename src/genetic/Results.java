/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genetic;

/**
 *
 * @author Maziyar
 */
public class Results {
    
    private int[] bestSolution;
    private int[][] bestSolutions;
    
    public Results(int[] bestSolution, int[][] bestSolutions ){
        this.bestSolution = bestSolution;
        this.bestSolutions = bestSolutions;
        
    }

    public int[] getBestSolution() {
        return bestSolution;
    }

    public void setBestSolution(int[] bestSolution) {
        this.bestSolution = bestSolution;
    }

    public int[][] getBestSolutions() {
        return bestSolutions;
    }

    public void setBestSolutions(int[][] bestSolutions) {
        this.bestSolutions = bestSolutions;
    }


}
