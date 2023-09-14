/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genetic;

import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

public class FitnessFunction {

    public int dimension = GA.nCloudlets;
    private int minVal = 0;
    private int maxVal = GA.nTotalVms;
    
    public int[] minBounds;	// The minimum bounds for each dimension
    public int[] maxBounds;	// The maximum bounds for each dimension
    public int nbEvals = 0;

    // Constructor
    public FitnessFunction() {
        // Initialize boundaries
        minBounds = new int[dimension];
        maxBounds = new int[dimension];
        for (int i = 0; i < dimension; i++) {
            minBounds[i] = minVal;
            maxBounds[i] = maxVal;
        }
    }

    public double getFitnessValue(int[] results, List<Vm> vmList, List<Cloudlet> cloudletList) {
        
        int nTotalVms = vmList.size();
        int nCloudlets = cloudletList.size();

        int suminstructions[] = new int[nTotalVms];
        double relativeRatio[] = new double[nTotalVms];

        for (int i = 0; i < nCloudlets; i++) {
            suminstructions[results[i]] += cloudletList.get(i).getCloudletLength();
        }

        for (int i = 0; i < nTotalVms; i++) {
            relativeRatio[i] = suminstructions[i] / vmList.get(i).getMips();
        }

        double totalLength = 0;

        for (int i = 0; i < nTotalVms; i++) {
            totalLength += relativeRatio[i];
        }

        double mean = totalLength / GA.nTotalVms;

        double sum = 0;

        for (int i = 0; i < GA.nTotalVms; i++) {

            sum += Math.pow((relativeRatio[i] - mean), 2);

        }
        double std = sum / GA.nTotalVms;

        return std;
    }

}
