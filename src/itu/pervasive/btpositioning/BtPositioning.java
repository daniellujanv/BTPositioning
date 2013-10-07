package itu.pervasive.btpositioning;

import android.app.Activity;

public class BtPositioning extends Activity{

	public BtPositioning(){
	}
	
	public static double getDistance(double rssi_reading){
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
		N = (dBmi + dBm0 - PLM);
		N = N / (-10*n);
		distance = Math.pow(A, N);
		distance = distance/1000;
		return distance;
	}

}
