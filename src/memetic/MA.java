package memetic;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cloudbus.cloudsim.util.WorkloadFileReader;

public class MA {

    public static int nServiceProvider = 1;    // number of service providers
    public static int nVms[] = {30, 20, 50, 30};    // number of VMs of each provider
    public static int nTotalVms = 30;
    public static int nCloudlets = 40315;

    public static int SPspec[][] = new int[][]{
        {512, 1000},
        {1000, 1000},
        {2000, 1000},
        {1000, 2000}

    };

    /**
     * The cloudlet list.
     */
    public static List<Cloudlet> cloudletList;

    /**
     * The vmList.
     */
    public static List<Vm> vmList;
    public static long fl = 0;

    //create Virtual machines
    private static List<Vm> createVM(int userId, int sp, int idShift) {

        LinkedList<Vm> list = new LinkedList<>();
        //VM Parameters
        long size = 10000;//image size (MB)
        long bw = 1000;
        int pesNumber = 1;//number of cpus
        String vmm = "Xen";//VMM name
        //create VMs
        Vm[] vm = new Vm[nVms[sp]];
        for (int i = 0; i < nVms[sp]; i++) {
            vm[i] = new Vm(idShift + i, userId, SPspec[sp][1], pesNumber, SPspec[sp][0], bw, size, vmm, new CloudletSchedulerTimeShared());
            list.add(vm[i]);
        }
        return list;
    }

    //create Cloudlets
    private static List<Cloudlet> createCloudlets(int userID) throws FileNotFoundException {

        //Read Clcudlets frcm workload file in the swf format 
        WorkloadFileReader workloadFileReader = new WorkloadFileReader("D:\\1\\data.swf", 1);

        //generate oIcudlets from worklcad file 
        List<Cloudlet> cloudlets = workloadFileReader.generateWorkload();

        for (int i = 0; i < cloudlets.size(); i++) {
            cloudlets.get(i).setUserId(userID);
        }

        return cloudlets;
    }

    public static void main(String[] args) {
        Log.printLine("Starting...");
        try {
            int num_user = 2;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            CloudSim.init(num_user, calendar, trace_flag);
            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            Datacenter datacenter1 = createDatacenter("Datacenter_1");
            Datacenter datacenter2 = createDatacenter("Datacenter_2");
            Datacenter datacenter3 = createDatacenter("Datacenter_3");
            Datacenter datacenter4 = createDatacenter("Datacenter_4");
            vmList = new LinkedList<>();

            DatacenterBroker broker = createBroker("Broker_0");
            int brokerId = broker.getId();
            for (int i = 0; i < nServiceProvider; i++) {
                List<Vm> tempVmList = new LinkedList<>();
                tempVmList = createVM(brokerId, i, i * 100);
                vmList.addAll(tempVmList);

            }

            cloudletList = createCloudlets(brokerId);
            FitnessFunction fitnessFunc = new FitnessFunction();
            // Run the simulation
            long t = 0;

            // Run the algorithm and get the Results
            MAAlgorithm ga = new MAAlgorithm();

            Results result = ga.GAlgorithm(vmList, cloudletList);

            int[] bestSol = result.getBestSolution();

            for (int j = 0; j < nCloudlets; j++) {
                cloudletList.get(j).setVmId(bestSol[j]);
            }

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);
            CloudSim.startSimulation();
            CloudSim.stopSimulation();

            double cost = 0;
            double ResponseTime = Double.POSITIVE_INFINITY;
            double ExecutionTime = 0;

            for (int i = 0; i < cloudletList.size(); i++) {

                if (cloudletList.get(i).getFinishTime() < ResponseTime) {
                    ResponseTime = cloudletList.get(i).getFinishTime();
                }
                if (cloudletList.get(i).getFinishTime() > ExecutionTime) {
                    ExecutionTime = cloudletList.get(i).getFinishTime();
                }

                cost += cloudletList.get(i).getProcessingCost();
            }
            System.out.println("Respons time: " + ResponseTime + "  ms ");
            System.out.println("Execution time: " + ExecutionTime + "  ms ");
            System.out.println("Cost: " + cost);
            Log.printLine(" finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static Datacenter createDatacenter(String name) {

        List<Host> hostList = new ArrayList<Host>();
        List<Pe> peList1 = new ArrayList<Pe>();
        int mips = 10000;
        peList1.add(new Pe(0, new PeProvisionerSimple(mips)));
        peList1.add(new Pe(1, new PeProvisionerSimple(mips)));
        peList1.add(new Pe(2, new PeProvisionerSimple(mips)));
        peList1.add(new Pe(3, new PeProvisionerSimple(mips)));
        List<Pe> peList2 = new ArrayList<Pe>();
        peList2.add(new Pe(0, new PeProvisionerSimple(mips)));
        peList2.add(new Pe(1, new PeProvisionerSimple(mips)));
        int hostId = 0;
        int ram = 16384;//host memory (MB)
        long storage = 1000000;//host storage
        int bw = 10000;
        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList1,
                        new VmSchedulerTimeShared(peList1)
                )
        );
        hostId++;
        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList2,
                        new VmSchedulerTimeShared(peList2)
                )
        );

        String arch = "x86";// system architecture
        String os = "Linux";// operating system
        String vmm = "Xen";
        double time_zone = 10.0;// time zone this resource located
        double cost = 3.0;// the cost of using processing in this resource
        double costPerMem = 0.05;// the cost of using memory in this resource
        double costPerStorage = 0.1;// the cost of using storage in this resource
        double costPerBw = 0.1;// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static DatacenterBroker createBroker(String name) {// create Broker

        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    public static class GlobalBroker extends SimEntity {

        private static final int CREATE_BROKER = 0;
        private List<Vm> vmList;
        private List<Cloudlet> cloudletList;
        private DatacenterBroker broker;

        public GlobalBroker(String name) {
            super(name);
        }

        @Override
        public void processEvent(SimEvent ev) {
            switch (ev.getTag()) {
                case CREATE_BROKER:
                    setBroker(createBroker(super.getName() + "_"));
                    setVmList(createVM(getBroker().getId(), 5, 100));
                     {
                        try {
                            setCloudletList(createCloudlets(getBroker().getId()));
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(MA.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    broker.submitVmList(getVmList());
                    broker.submitCloudletList(getCloudletList());
                    CloudSim.resumeSimulation();
                    break;

                default:

                    break;
            }
        }

        @Override
        public void startEntity() {

            schedule(getId(), 200, CREATE_BROKER);
        }

        @Override
        public void shutdownEntity() {
        }

        public List<Vm> getVmList() {
            return vmList;
        }

        protected void setVmList(List<Vm> vmList) {
            this.vmList = vmList;
        }

        public List<Cloudlet> getCloudletList() {
            return cloudletList;
        }

        protected void setCloudletList(List<Cloudlet> cloudletList) {
            this.cloudletList = cloudletList;
        }

        public DatacenterBroker getBroker() {
            return broker;
        }

        protected void setBroker(DatacenterBroker broker) {
            this.broker = broker;
        }

    }

    public static double Synchronization(double x, double y, double z) {
        double temhost = 18 * z;
        double tem = 50000;
        if (z > 6500) {

            tem = ((temhost / 100) - 1165) * 1000;

        }
        return tem;
    }

    /**
     * Returns the fitness of one country
     *
     * @param individual the solution to evaluate
     * @return the fitness
     */
}
