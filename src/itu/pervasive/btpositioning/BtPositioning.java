package itu.pervasive.btpositioning;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

public class BtPositioning extends Activity{

	public List<Double> readings;
	public List<Double> gaussianReadings;
	public List<Double> maReadings; // moving average 
	private double[] weights = new double[]{2, 2 ,2 , 3, 2, 2, 2};
	private List<Double> distances;
	
	public BtPositioning(){
		readings = new ArrayList<Double>();
		gaussianReadings = new ArrayList<Double>();
		distances = new ArrayList<Double>();
	}
	
	public double getDistance(double rssi_reading){
		double n = 4;
		double dBmi = rssi_reading;
		double dBm0 = -63;
		double distance;
		double PLM = 0; //4.3
		double A = 10,N; //variables of formula to solve for X in LOG enpressions LogA(distance)=N
		/**
		 * 
		 *	DBm(i) = DBm(0) + 10n*Log10(di/d0)
		 *	n = Pt/Pr (Power transmitted / Power received)
		 */
//		dBmi = dBm0 + 10*n*Math.log10(distance);
		/**
		 *     
		 *     dBmi - dBm0
		 *   [ ___________]= Log10(distance)
		 *        10*n
		 * 
		 *               N = LogA(distance)
		 *       
		 *    		dBmi - dBm0
		 *   N =  [ ___________] , A = 10 , distance
		 *        		10*n
		 * 
		 *  A^N = d
		 * 
		 */
		N = (dBmi - dBm0 - PLM);
		N = N / (-10*n);
		distance = Math.pow(A, N);
		return distance;
	}

	public double addReading(double reading){
		readings.add(reading);
        double distance = getDistance(reading);
        return distance;
	}
	
	public void calculateDistances(){
		GaussianFilter();
		
		for(int i=0; i< gaussianReadings.size();i++){
			distances.add(getDistance(gaussianReadings.get(i)));
		}
	}
	
 	private void GaussianFilter(){
//		float[] temp = new float[readings.size()];
		
		double[] digitsAround = new double[weights.length];
		
		int offset = (weights.length / 2);
		
		int denominator = 0;
		for(double r : weights){
			denominator += r;
		}
		
		for(int f = 0; f < readings.size(); f++){
//			double numberToFilter = (double)readings.get(f);
			//digitsAround = new float[weights.length];
//			int index = 0;
			
			for(int e = 0; e <  weights.length; e++){
//				int nextNumber = e;
				//|| (e+f)+offset > readings.length
				if((e+f) - offset < 0 || (e+f) - offset >= readings.size()){
					digitsAround[e] = (double)readings.get(f);
				}
				else{
					digitsAround[e] = (double)readings.get((e+f)-offset);
				
				}
//				index++;
			}
			
			float temporary = 0;
			
			for(int g = 0; g < digitsAround.length; g++){
				temporary += digitsAround[g] * weights[g];
			}
			gaussianReadings.add((double)(temporary / denominator));
			
		}
		
//		return temp;
	}

 	public String getCalculatedDistances(){
 		String response = null;
 		for(int i = 0; i < distances.size(); i++){
 			response += "\n init "+String.format("%.3f",readings.get(i))+", gauss_reading "
 					+String.format("%.3f",gaussianReadings.get(i))+" => "+ String.format("%.3f",distances.get(i));
 		}
 		return response;
 	}
}
