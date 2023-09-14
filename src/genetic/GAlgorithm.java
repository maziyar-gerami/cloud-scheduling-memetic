/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genetic;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import org.cloudbus.cloudsim.lists.VmList;
import sun.security.util.Length;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

/**
 *
 * @author Maziyar
 */
public class GAlgorithm {

    private static int NUM_CHROMOSOMES = 50;
    private static int CHROMOSOMES_LENGTH = GA.nCloudlets;
    private static float MUTATE = (float) .2;
    private static double maxIt = 1000;
    private static List<Vm> vmList;
    private static List<Cloudlet> cloudletList;

    private static List<Chromosome> population;
    int numRuns = 0;

    public Results GAlgorithm(List<Vm> vmList, List<Cloudlet> clodletList) {
        this.vmList = vmList;
        this.cloudletList = clodletList;
        population = new LinkedList<>();
        generateRandomPopulation();
        Results result = start();
        return (result);
    }
    
    public Results GAlgorithm(List<Vm> vmList, List<Cloudlet> clodletList, int[][] bestSols) {
        this.vmList = vmList;
        this.cloudletList = clodletList;
        FitnessFunction fitfunc = new FitnessFunction();
        
        List<Chromosome> initialPop = new LinkedList<>();
        
        for (int i=0; i<NUM_CHROMOSOMES; i++){
            Chromosome temp = new Chromosome();
            temp.setPosition(bestSols[i]);
            temp.setCost(fitfunc.getFitnessValue(bestSols[i], vmList, cloudletList));
            initialPop.add(temp);
            
        }
        
        this.population = initialPop;
        FitnessFunction fitFunc = new FitnessFunction();

        Results result = start();
        return (result);
    }

    private static void generateRandomPopulation() {

        for (int i = 0; i < NUM_CHROMOSOMES; i++) {
            population.add(new Chromosome(CHROMOSOMES_LENGTH, vmList, cloudletList));

        }

    }

    private static Results start() {
        Collections.sort(population);
        Chromosome fitess = (Chromosome) population.get(0);

        for (int numRuns = 0; numRuns < maxIt; numRuns++) {

            generateNewPopulation();
            Collections.sort(population);

            List<Chromosome> temppopulation = population;
            population = new LinkedList<>();

            for (int i = 0; i < NUM_CHROMOSOMES; i++) {
                population.add(temppopulation.get(i));
            }

        }
        
        int[] bestSolution = fitess.getPosition();
        int[][] bestSolutions = new int [NUM_CHROMOSOMES][bestSolution.length];
        
        for (int i=0; i<NUM_CHROMOSOMES; i++){
                bestSolutions[i] = population.get(i).getPosition();
            }
            
        
        return (new Results (bestSolution, bestSolutions));

    }

    private static void generateNewPopulation() {

        LinkedList<Chromosome> temp = new LinkedList();

        for (int i = 0; i < population.size() / 2; ++i) {
            Chromosome p1 = selectParent();
            Chromosome p2 = selectParent();
            temp.addAll(cross(p1, p2));
        }

        population.addAll(temp);

    }

    private static Chromosome selectParent() {
        int delta = population.size();
        delta = NUM_CHROMOSOMES - NUM_CHROMOSOMES / 2;

        int num = (int) (Math.random() * 10 + 1);
        int index;

        if (num >= 4) {
            index = (int) (Math.random() * delta + NUM_CHROMOSOMES / 2);
        } else {
            index = (int) (Math.random() * delta);
        }

        return (Chromosome) population.get(index);
    }

    private static List<Chromosome> cross(Chromosome parent1, Chromosome parent2) {
        int[] p1 = parent1.getPosition();
        int[] p2 = parent2.getPosition();
        int length = p1.length;
        Random r = new Random();
        int rand = r.nextInt(length - 1);

        List<Chromosome> offsprings = new LinkedList<>();

        int[] newPosition1 = new int[length];
        int[] newPosition2 = new int[length];

        for (int i = 0; i < rand; i++) {

            newPosition1[i] = p1[i];
            newPosition2[i] = p2[i];
        }

        for (int i = rand + 1; i < length; i++) {

            newPosition1[i] = p2[i];
            newPosition2[i] = p1[i];
        }

        FitnessFunction fitFunc = new FitnessFunction();

        Chromosome c1 = new Chromosome();
        Chromosome c2 = new Chromosome();
        c1.setPosition(newPosition1);
        c2.setPosition(newPosition2);
        c1.setCost(fitFunc.getFitnessValue(newPosition1, vmList, cloudletList));
        c2.setCost(fitFunc.getFitnessValue(newPosition2, vmList, cloudletList));

        offsprings.add(c1);
        offsprings.add(c2);

        if (shouldMutate()) {
            Random ra = new Random();
            int randomNum = ra.nextInt(2);
            mutate(offsprings.get(randomNum));
        }

        return offsprings;
    }

    private static boolean shouldMutate() {
        double num = Math.random();
        int number = (int) (num * 100);
        num = (double) number / 100;
        return (num <= MUTATE);
    }

    private static void mutate(Chromosome offspring) {
        int[] position = offspring.getPosition();
        FitnessFunction fitfunc = new FitnessFunction();
        int num = position.length;
        Random rand = new Random();
        int index1 = rand.nextInt(num);
        int index2 = rand.nextInt(num);
        int[] newPosition = flip(position, index1, index2);
        offspring.setPosition(newPosition);
        offspring.setCost(fitfunc.getFitnessValue(newPosition, vmList, cloudletList));
    }

    private static int[] flip(int[] position, int index1, int index2) {
        int temp = position[index1];
        position[index1] = position[index2];
        position[index2] = temp;

        return position;
    }

}

class Chromosome implements Comparable {

    protected int[] position = new int[GA.nCloudlets];
    protected double cost;

    public Chromosome() {
    }

    public Chromosome(int length, List<Vm> vmList, List<Cloudlet> cloudletList) {
        for (int i = 0; i < length; i++) {
            position[i] = randomFill();
        }
        FitnessFunction fitFunc = new FitnessFunction();
        cost = fitFunc.getFitnessValue(position, vmList, cloudletList);

    }
    


    public int[] getPosition() {
        return position;
    }

    public double getCost() {
        return cost;
    }

    public void setPosition(int[] position) {
        this.position = position;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public int compareTo(Object o) {
        Chromosome c = (Chromosome) o;
        return ((int) (this.cost) - (int) (c.getCost()));
    }

    public int randomFill() {
        Random rand = new Random();
        int randomNum = rand.nextInt(GA.nTotalVms);
        return randomNum;
    }

}
